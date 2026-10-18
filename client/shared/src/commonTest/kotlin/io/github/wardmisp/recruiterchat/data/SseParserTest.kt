package io.github.wardmisp.recruiterchat.data

import kotlin.test.Test
import kotlin.test.assertEquals

class SseParserTest {

    private fun parse(stream: String): List<ChatEvent> {
        val parser = SseParser()
        return stream.lines().mapNotNull(parser::feed) + listOfNotNull(parser.finish())
    }

    @Test
    fun parsesAFullAnswer() {
        val events = parse(
            """
            event: thinking
            data: {}

            event: token
            data: {"text": "Bon"}

            event: token
            data: {"text": "jour \"Rémi\""}

            event: done
            data: {}

            """.trimIndent(),
        )
        assertEquals(
            listOf(ChatEvent.Thinking, ChatEvent.Token("Bon"), ChatEvent.Token("jour \"Rémi\""), ChatEvent.Done),
            events,
        )
    }

    @Test
    fun parsesErrorCodes() {
        assertEquals(
            listOf(ChatEvent.Failure(ChatError.Timeout)),
            parse("event: error\ndata: {\"code\": \"timeout\"}\n"),
        )
    }

    @Test
    fun dispatchesLastEventWithoutTrailingBlankLine() {
        assertEquals(listOf(ChatEvent.Done), parse("event: done\ndata: {}"))
    }

    @Test
    fun ignoresCommentsUnknownEventsAndMalformedData() {
        val events = parse(
            """
            : keep-alive

            event: unknown
            data: {}

            event: token
            data: not json

            event: token
            data: {"text": "ok"}

            """.trimIndent(),
        )
        assertEquals(listOf(ChatEvent.Token("ok")), events)
    }

    @Test
    fun mapsHttpStatuses() {
        assertEquals(ChatError.RateLimited, ChatError.fromHttpStatus(429, null))
        assertEquals(ChatError.MessageTooLong, ChatError.fromHttpStatus(413, "message_too_long"))
        assertEquals(ChatError.Busy, ChatError.fromHttpStatus(503, "busy"))
        assertEquals(ChatError.Unavailable, ChatError.fromHttpStatus(503, null))
    }
}
