package io.github.wardmisp.recruiterchat

import io.github.wardmisp.recruiterchat.data.ApiMessage
import io.github.wardmisp.recruiterchat.data.ChatError
import io.github.wardmisp.recruiterchat.data.ChatEvent
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
}
