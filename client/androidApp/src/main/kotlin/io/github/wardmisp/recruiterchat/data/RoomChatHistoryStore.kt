package io.github.wardmisp.recruiterchat.data

import io.github.wardmisp.recruiterchat.ChatMessage
import io.github.wardmisp.recruiterchat.Role

/** Conversation enregistrée dans la base Room de l'application. */
class RoomChatHistoryStore(private val dao: MessageDao) : ChatHistoryStore {

    override suspend fun load(): List<ChatMessage> = dao.getAll().mapNotNull { it.toMessage() }

    override suspend fun save(messages: List<ChatMessage>) = dao.replaceAll(messages.map { it.toEntity() })

    override suspend fun clear() = dao.deleteAll()
}

private fun ChatMessage.toEntity() = MessageEntity(id, role.name, text, sentAt)

/** Rôle inconnu (base écrite par une autre version) : message ignoré plutôt qu'un plantage. */
private fun MessageEntity.toMessage(): ChatMessage? =
    Role.entries.find { it.name == role }?.let { ChatMessage(id, it, text, sentAt) }
