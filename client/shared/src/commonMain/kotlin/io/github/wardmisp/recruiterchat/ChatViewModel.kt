package io.github.wardmisp.recruiterchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wardmisp.recruiterchat.data.ApiMessage
import io.github.wardmisp.recruiterchat.data.ChatApi
import io.github.wardmisp.recruiterchat.data.ChatError
import io.github.wardmisp.recruiterchat.data.ChatEvent
import io.github.wardmisp.recruiterchat.data.ChatService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Role(val apiName: String) { User("user"), Assistant("assistant") }

data class ChatMessage(val id: Long, val role: Role, val text: String)

enum class ServerStatus { Checking, Available, Unavailable }

/** Où en est la réponse en cours. */
enum class AnswerPhase { Idle, Waiting, Thinking, Writing }

data class ChatUiState(
    val server: ServerStatus = ServerStatus.Checking,
    val messages: List<ChatMessage> = emptyList(),
    val phase: AnswerPhase = AnswerPhase.Idle,
    val error: ChatError? = null,
) {
    val isAnswering: Boolean get() = phase != AnswerPhase.Idle
    val canSend: Boolean get() = server == ServerStatus.Available && !isAnswering
}

class ChatViewModel(private val service: ChatService = ChatApi()) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var nextId = 0L

    init {
        checkServer()
    }

    fun checkServer() {
        viewModelScope.launch {
            _state.update { it.copy(server = ServerStatus.Checking) }
            val available = service.isAvailable()
            _state.update {
                it.copy(server = if (available) ServerStatus.Available else ServerStatus.Unavailable)
            }
        }
    }

    fun send(text: String) {
        val question = text.trim().take(Config.MAX_MESSAGE_CHARS)
        if (question.isEmpty() || !_state.value.canSend) return

        val userMessage = ChatMessage(nextId++, Role.User, question)
        val answerId = nextId++
        // Les réponses vides (échecs) ne sont pas renvoyées au modèle.
        val history = (_state.value.messages + userMessage)
            .filter { it.text.isNotBlank() }
            .takeLast(Config.MAX_HISTORY_MESSAGES)
            .map { ApiMessage(it.role.apiName, it.text) }

        _state.update {
            it.copy(
                messages = it.messages + userMessage + ChatMessage(answerId, Role.Assistant, ""),
                phase = AnswerPhase.Waiting,
                error = null,
            )
        }

        viewModelScope.launch {
            service.ask(history).collect { event -> onEvent(answerId, event) }
            // Flux terminé sans "done" (connexion coupée) : on ne reste pas bloqué en attente.
            finishAnswer(answerId)
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun onEvent(answerId: Long, event: ChatEvent) {
        when (event) {
            ChatEvent.Thinking -> _state.update { it.copy(phase = AnswerPhase.Thinking) }
            is ChatEvent.Token -> _state.update { state ->
                state.copy(
                    phase = AnswerPhase.Writing,
                    messages = state.messages.map {
                        if (it.id == answerId) it.copy(text = it.text + event.text) else it
                    },
                )
            }
            ChatEvent.Done -> finishAnswer(answerId)
            is ChatEvent.Failure -> {
                _state.update { it.copy(error = event.error) }
                finishAnswer(answerId)
            }
        }
    }

    private fun finishAnswer(answerId: Long) {
        _state.update { state ->
            state.copy(
                phase = AnswerPhase.Idle,
                messages = state.messages.filterNot { it.id == answerId && it.text.isBlank() },
            )
        }
    }
}
