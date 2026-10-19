package io.github.wardmisp.recruiterchat

import io.github.wardmisp.recruiterchat.data.ChatHistoryJson
import io.github.wardmisp.recruiterchat.data.ChatHistoryStore

/** Sous-ensemble de l'API Web Storage utilisé ici, commun aux cibles js et wasmJs. */
private external interface Storage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

private external val localStorage: Storage

/**
 * Conversation enregistrée dans le localStorage du navigateur (propre à l'origine
 * wardmisp.github.io). Navigation privée ou stockage bloqué : le chat fonctionne sans historique.
 */
class LocalStorageChatHistoryStore : ChatHistoryStore {

    override suspend fun load(): List<ChatMessage> =
        ChatHistoryJson.decode(runCatching { localStorage.getItem(KEY) }.getOrNull())

    override suspend fun save(messages: List<ChatMessage>) {
        runCatching { localStorage.setItem(KEY, ChatHistoryJson.encode(messages)) }
    }

    override suspend fun clear() {
        runCatching { localStorage.removeItem(KEY) }
    }

    private companion object {
        const val KEY = "recruiterchat.history"
    }
}
