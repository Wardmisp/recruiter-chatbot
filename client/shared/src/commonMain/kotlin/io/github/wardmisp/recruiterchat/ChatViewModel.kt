package io.github.wardmisp.recruiterchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wardmisp.recruiterchat.data.ApiMessage
import io.github.wardmisp.recruiterchat.data.ChatApi
import io.github.wardmisp.recruiterchat.data.ChatError
import io.github.wardmisp.recruiterchat.data.ChatEvent
import io.github.wardmisp.recruiterchat.data.ChatHistoryStore
import io.github.wardmisp.recruiterchat.data.ChatService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
enum class Role(val apiName: String) { User("user"), Assistant("assistant") }

/** [sentAt] : date d'envoi en millisecondes (epoch), pour faire expirer la conversation enregistrée. */
@Serializable
data class ChatMessage(val id: Long, val role: Role, val text: String, val sentAt: Long = 0)

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
    val canStartNewConversation: Boolean get() = messages.isNotEmpty() && !isAnswering
}

@OptIn(ExperimentalTime::class)
class ChatViewModel(
    private val service: ChatService = ChatApi(),
    private val history: ChatHistoryStore = ChatHistoryStore.None,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var nextId = 0L

    /** Les sauvegardes passent l'une après l'autre : la dernière écrit toujours l'état le plus récent. */
    private val saveLock = Mutex()

    init {
        viewModelScope.launch {
            // Avant la vérification du serveur : l'envoi reste impossible tant que l'historique n'est pas chargé.
            restoreHistory()
            refreshServerStatus()
        }
    }

    fun checkServer() {
        viewModelScope.launch { refreshServerStatus() }
    }

    fun send(text: String) {
        val question = text.trim().take(Config.MAX_MESSAGE_CHARS)
        if (question.isEmpty() || !_state.value.canSend) return

        val userMessage = ChatMessage(nextId++, Role.User, question, now())
        val answerId = nextId++
        // Les réponses vides (échecs) ne sont pas renvoyées au modèle.
        val apiHistory = (_state.value.messages + userMessage)
            .filter { it.text.isNotBlank() }
            .takeLast(Config.MAX_HISTORY_MESSAGES)
            .map { ApiMessage(it.role.apiName, it.text) }

        _state.update {
            it.copy(
                messages = it.messages + userMessage + ChatMessage(answerId, Role.Assistant, "", now()),
                phase = AnswerPhase.Waiting,
                error = null,
            )
        }
        saveHistory()

        viewModelScope.launch {
            service.ask(apiHistory).collect { event -> onEvent(answerId, event) }
            // Flux terminé sans "done" (connexion coupée) : on ne reste pas bloqué en attente.
            finishAnswer(answerId)
        }
    }

    fun newConversation() {
        if (!_state.value.canStartNewConversation) return
        _state.update { it.copy(messages = emptyList(), error = null) }
        viewModelScope.launch { saveLock.withLock { history.clear() } }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private suspend fun refreshServerStatus() {
        _state.update { it.copy(server = ServerStatus.Checking) }
        val available = service.isAvailable()
        _state.update {
            it.copy(server = if (available) ServerStatus.Available else ServerStatus.Unavailable)
        }
    }

    private suspend fun restoreHistory() {
        // Un stockage illisible ne doit pas empêcher d'utiliser le chat.
        val saved = runCatching { history.load() }.getOrDefault(emptyList())
        if (saved.isEmpty()) return
        if (now() - saved.last().sentAt > Config.SAVED_HISTORY_MAX_AGE_MILLIS) {
            runCatching { history.clear() }
            return
        }
        val messages = saved.takeLast(Config.SAVED_MESSAGES)
        nextId = messages.maxOf { it.id } + 1
        _state.update { it.copy(messages = messages) }
    }

    private fun saveHistory() {
        viewModelScope.launch {
            saveLock.withLock {
                // Réponse en cours (encore vide) exclue : elle sera enregistrée une fois terminée.
                val messages = _state.value.messages
                    .filter { it.text.isNotBlank() }
                    .takeLast(Config.SAVED_MESSAGES)
                runCatching { history.save(messages) }
            }
        }
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
        // Appelé deux fois quand le flux se termine après "done" : une seule sauvegarde suffit.
        if (_state.value.phase == AnswerPhase.Idle) return
        _state.update { state ->
            state.copy(
                phase = AnswerPhase.Idle,
                messages = state.messages.filterNot { it.id == answerId && it.text.isBlank() },
            )
        }
        saveHistory()
    }
}
