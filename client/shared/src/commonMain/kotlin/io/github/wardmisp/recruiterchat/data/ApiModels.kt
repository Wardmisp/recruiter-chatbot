package io.github.wardmisp.recruiterchat.data

import kotlinx.serialization.Serializable

/** Message de l'historique envoyé à POST /chat (role : "user" ou "assistant"). */
@Serializable
data class ApiMessage(val role: String, val content: String)

@Serializable
data class ChatRequest(val messages: List<ApiMessage>)

/** Données de l'événement SSE "token". */
@Serializable
internal data class TokenData(val text: String)

/** Données de l'événement SSE "error", et corps JSON des réponses d'erreur HTTP. */
@Serializable
internal data class ErrorData(val code: String? = null, val error: String? = null)
