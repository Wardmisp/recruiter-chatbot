package io.github.wardmisp.recruiterchat

import io.github.wardmisp.recruiterchat.data.ApiMessage
import io.github.wardmisp.recruiterchat.data.ChatError
import io.github.wardmisp.recruiterchat.data.ChatEvent
import io.github.wardmisp.recruiterchat.data.ChatHistoryJson
import io.github.wardmisp.recruiterchat.data.ChatHistoryStore
import io.github.wardmisp.recruiterchat.data.ChatService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeChatService(
    var available: Boolean = true,
    var events: List<ChatEvent> = emptyList(),
) : ChatService {
    val requests = mutableListOf<List<ApiMessage>>()

    override suspend fun isAvailable() = available

    override fun ask(history: List<ApiMessage>): Flow<ChatEvent> {
        requests += history
        return events.asFlow()
    }
}

private class FakeHistoryStore(var saved: List<ChatMessage> = emptyList()) : ChatHistoryStore {
    var cleared = false

    override suspend fun load() = saved

    override suspend fun save(messages: List<ChatMessage>) {
        saved = messages
    }

    override suspend fun clear() {
        saved = emptyList()
        cleared = true
    }
}

private const val NOW = 1_000_000_000_000L

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun ChatUiState.texts() = messages.map { it.role to it.text }

    @Test
    fun reportsUnavailableServer() {
        val viewModel = ChatViewModel(FakeChatService(available = false))
        assertEquals(ServerStatus.Unavailable, viewModel.state.value.server)
        assertTrue(!viewModel.state.value.canSend)
    }

    @Test
    fun streamsTheAnswerIntoASingleMessage() {
        val service = FakeChatService(
            events = listOf(ChatEvent.Thinking, ChatEvent.Token("Bon"), ChatEvent.Token("jour"), ChatEvent.Done),
        )
        val viewModel = ChatViewModel(service)

        viewModel.send("  Salut  ")

        val state = viewModel.state.value
        assertEquals(listOf(Role.User to "Salut", Role.Assistant to "Bonjour"), state.texts())
        assertEquals(AnswerPhase.Idle, state.phase)
        assertNull(state.error)
        assertEquals(listOf(ApiMessage("user", "Salut")), service.requests.single())
    }

    @Test
    fun failureRemovesTheEmptyAnswerAndShowsTheError() {
        val service = FakeChatService(events = listOf(ChatEvent.Thinking, ChatEvent.Failure(ChatError.Busy)))
        val viewModel = ChatViewModel(service)

        viewModel.send("Question")

        val state = viewModel.state.value
        assertEquals(listOf(Role.User to "Question"), state.texts())
        assertEquals(ChatError.Busy, state.error)
        assertEquals(AnswerPhase.Idle, state.phase)
    }

    @Test
    fun streamEndingWithoutDoneDoesNotBlockTheChat() {
        val viewModel = ChatViewModel(FakeChatService(events = listOf(ChatEvent.Token("Partiel"))))

        viewModel.send("Question")

        assertEquals(AnswerPhase.Idle, viewModel.state.value.phase)
        assertTrue(viewModel.state.value.canSend)
    }

    @Test
    fun historyIsLimitedAndSkipsFailedAnswers() {
        val service = FakeChatService(events = listOf(ChatEvent.Failure(ChatError.Unavailable)))
        val viewModel = ChatViewModel(service)
        repeat(Config.MAX_HISTORY_MESSAGES + 3) { viewModel.send("Q$it") }

        val lastRequest = service.requests.last()
        assertEquals(Config.MAX_HISTORY_MESSAGES, lastRequest.size)
        assertTrue(lastRequest.all { it.role == "user" })
        assertEquals("Q${Config.MAX_HISTORY_MESSAGES + 2}", lastRequest.last().content)
    }

    @Test
    fun ignoresBlankAndOverlongInput() {
        val service = FakeChatService(events = listOf(ChatEvent.Done))
        val viewModel = ChatViewModel(service)

        viewModel.send("   ")
        viewModel.send("x".repeat(Config.MAX_MESSAGE_CHARS + 50))

        assertEquals(1, service.requests.size)
        assertEquals(Config.MAX_MESSAGE_CHARS, service.requests.single().single().content.length)
    }

    private fun viewModelWith(
        history: FakeHistoryStore,
        events: List<ChatEvent> = listOf(ChatEvent.Token("Réponse"), ChatEvent.Done),
        service: FakeChatService = FakeChatService(events = events),
    ) = ChatViewModel(service, history, now = { NOW })

    @Test
    fun restoresTheSavedConversationAndContinuesIds() {
        val history = FakeHistoryStore(
            listOf(
                ChatMessage(4, Role.User, "Ancienne question", NOW - 1000),
                ChatMessage(5, Role.Assistant, "Ancienne réponse", NOW - 900),
            ),
        )
        val service = FakeChatService(events = listOf(ChatEvent.Token("Nouvelle"), ChatEvent.Done))
        val viewModel = viewModelWith(history, service = service)

        assertEquals(
            listOf(Role.User to "Ancienne question", Role.Assistant to "Ancienne réponse"),
            viewModel.state.value.texts(),
        )

        viewModel.send("Suite")

        val messages = viewModel.state.value.messages
        assertEquals(listOf(4L, 5L, 6L, 7L), messages.map { it.id })
        // Le contexte restauré est bien renvoyé au modèle.
        assertEquals("Ancienne question", service.requests.single().first().content)
    }

    @Test
    fun savesTheConversationOnceTheAnswerIsComplete() {
        val history = FakeHistoryStore()
        val viewModel = viewModelWith(history)

        viewModel.send("Question")

        assertEquals(
            listOf(Role.User to "Question", Role.Assistant to "Réponse"),
            history.saved.map { it.role to it.text },
        )
        assertTrue(history.saved.all { it.sentAt == NOW })
    }

    @Test
    fun failedAnswerIsNotSaved() {
        val history = FakeHistoryStore()
        val viewModel = viewModelWith(history, events = listOf(ChatEvent.Failure(ChatError.Busy)))

        viewModel.send("Question")

        assertEquals(listOf(Role.User to "Question"), history.saved.map { it.role to it.text })
    }

    @Test
    fun expiredConversationIsDiscarded() {
        val old = NOW - Config.SAVED_HISTORY_MAX_AGE_MILLIS - 1
        val history = FakeHistoryStore(listOf(ChatMessage(0, Role.User, "Trop vieux", old)))

        val viewModel = viewModelWith(history)

        assertTrue(viewModel.state.value.messages.isEmpty())
        assertTrue(history.cleared)
    }

    @Test
    fun keepsOnlyTheLastSavedMessages() {
        val history = FakeHistoryStore()
        val viewModel = viewModelWith(history)

        repeat(Config.SAVED_MESSAGES) { viewModel.send("Q$it") }

        assertEquals(Config.SAVED_MESSAGES, history.saved.size)
        assertEquals("Réponse", history.saved.last().text)
        assertEquals("Q${Config.SAVED_MESSAGES / 2}", history.saved.first().text)
    }

    @Test
    fun newConversationClearsScreenAndStorage() {
        val history = FakeHistoryStore()
        val viewModel = viewModelWith(history)
        assertFalse(viewModel.state.value.canStartNewConversation)

        viewModel.send("Question")
        assertTrue(viewModel.state.value.canStartNewConversation)
        viewModel.newConversation()

        assertTrue(viewModel.state.value.messages.isEmpty())
        assertTrue(history.saved.isEmpty())
        assertTrue(history.cleared)
    }

    @Test
    fun unreadableJsonGivesAnEmptyConversation() {
        assertTrue(ChatHistoryJson.decode("pas du json").isEmpty())
        assertTrue(ChatHistoryJson.decode(null).isEmpty())
        val messages = listOf(ChatMessage(1, Role.Assistant, "Salut", NOW))
        assertEquals(messages, ChatHistoryJson.decode(ChatHistoryJson.encode(messages)))
    }
}
