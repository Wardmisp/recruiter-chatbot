package io.github.wardmisp.recruiterchat.data

import io.github.wardmisp.recruiterchat.ChatMessage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Conversation conservée sur l'appareil du visiteur, pour la retrouver à la réouverture.
 * Rien n'est stocké côté serveur. Implémentations : Room (Android), localStorage (web).
 */
interface ChatHistoryStore {
    suspend fun load(): List<ChatMessage>

    /** Remplace la conversation enregistrée par [messages]. */
    suspend fun save(messages: List<ChatMessage>)

    suspend fun clear()

    /** Sans persistance : la conversation disparaît à la fermeture. */
    object None : ChatHistoryStore {
        override suspend fun load() = emptyList<ChatMessage>()
        override suspend fun save(messages: List<ChatMessage>) = Unit
        override suspend fun clear() = Unit
    }
}

/** Format JSON des stores qui enregistrent la conversation sous forme de texte. */
object ChatHistoryJson {
    private val serializer = ListSerializer(ChatMessage.serializer())
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(messages: List<ChatMessage>): String = json.encodeToString(serializer, messages)

    /** Liste vide si le texte est absent ou illisible (ancien format, données corrompues). */
    fun decode(text: String?): List<ChatMessage> =
        text?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }.orEmpty()
}
