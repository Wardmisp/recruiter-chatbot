package io.github.wardmisp.recruiterchat.data

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Lit un flux Server-Sent Events ligne par ligne.
 *
 * Un événement se termine par une ligne vide :
 * ```
 * event: token
 * data: {"text": "Bonjour"}
 *
 * ```
 * Les événements inconnus ou mal formés sont ignorés plutôt que d'interrompre la réponse.
 */
class SseParser {
    private var eventName: String? = null
    private val data = StringBuilder()

    /** Renvoie l'événement terminé par cette ligne, s'il y en a un. */
    fun feed(line: String): ChatEvent? {
        when {
            line.isEmpty() -> return dispatch()
            line.startsWith(":") -> Unit // commentaire SSE (keep-alive)
            line.startsWith("event:") -> eventName = line.removePrefix("event:").trim()
            line.startsWith("data:") -> {
                if (data.isNotEmpty()) data.append('\n')
                data.append(line.removePrefix("data:").removePrefix(" "))
            }
        }
        return null
    }

    /** À appeler en fin de flux : un dernier événement peut ne pas être suivi d'une ligne vide. */
    fun finish(): ChatEvent? = dispatch()

    private fun dispatch(): ChatEvent? {
        val name = eventName
        val payload = data.toString()
        eventName = null
        data.clear()
        return try {
            when (name) {
                "thinking" -> ChatEvent.Thinking
                "token" -> ChatEvent.Token(json.decodeFromString<TokenData>(payload).text)
                "done" -> ChatEvent.Done
                "error" -> ChatEvent.Failure(
                    ChatError.fromCode(json.decodeFromString<ErrorData>(payload).code),
                )
                else -> null
            }
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
