package io.github.wardmisp.recruiterchat.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction

/** Un message de la conversation enregistrée ; [role] contient le nom de l'enum Role. */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long,
    val role: String,
    val text: String,
    val sentAt: Long,
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY id")
    suspend fun getAll(): List<MessageEntity>

    @Insert
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    /** La conversation est réécrite en entier (50 messages au plus) : simple et toujours cohérent. */
    @Transaction
    suspend fun replaceAll(messages: List<MessageEntity>) {
        deleteAll()
        insertAll(messages)
    }
}

@Database(entities = [MessageEntity::class], version = 1)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
