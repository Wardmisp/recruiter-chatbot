package io.github.wardmisp.recruiterchat

import android.app.Application
import androidx.room.Room
import io.github.wardmisp.recruiterchat.data.ChatDatabase
import io.github.wardmisp.recruiterchat.data.ChatHistoryStore
import io.github.wardmisp.recruiterchat.data.RoomChatHistoryStore

/** Porte la base Room : une seule instance pour toute l'application, même quand l'activité est recréée. */
class RecruiterChatApplication : Application() {

    val historyStore: ChatHistoryStore by lazy {
        val database = Room.databaseBuilder(this, ChatDatabase::class.java, "chat.db").build()
        RoomChatHistoryStore(database.messageDao())
    }
}
