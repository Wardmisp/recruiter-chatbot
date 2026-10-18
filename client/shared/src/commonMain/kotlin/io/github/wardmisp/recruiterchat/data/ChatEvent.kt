package io.github.wardmisp.recruiterchat.data

/** Ce que l'API renvoie au fil d'une réponse. */
sealed interface ChatEvent {
    /** Le modèle réfléchit avant d'écrire (envoyé au plus une fois). */
    data object Thinking : ChatEvent

    /** Un morceau de la réponse. */
    data class Token(val text: String) : ChatEvent

    /** La réponse est complète. */
    data object Done : ChatEvent

    /** La réponse a échoué ou a été refusée. */
    data class Failure(val error: ChatError) : ChatEvent
}

enum class ChatError {
    Unavailable,
    Timeout,
    Busy,
    RateLimited,
    MessageTooLong;

    companion object {
        /** Codes renvoyés par l'API (événement SSE "error" ou corps d'une réponse HTTP d'erreur). */
        fun fromCode(code: String?): ChatError = when (code) {
            "timeout" -> Timeout
            "busy" -> Busy
            "message_too_long" -> MessageTooLong
            else -> Unavailable
        }

        fun fromHttpStatus(status: Int, code: String?): ChatError = when (status) {
            413 -> MessageTooLong
            429 -> RateLimited
            503 -> if (code == "busy") Busy else Unavailable
            504 -> Timeout
            else -> fromCode(code)
        }
    }
}
