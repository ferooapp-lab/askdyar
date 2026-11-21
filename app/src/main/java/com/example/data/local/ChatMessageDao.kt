package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY createdAt DESC")
    fun getAllMessagesDesc(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE status = 'PUBLISHED' ORDER BY createdAt ASC")
    fun getPublishedMessagesAsc(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE status = 'PUBLISHED'")
    fun getPublishedCount(): Flow<Int>

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): ChatMessageEntity?

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET status = :status, moderationNote = :note WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, note: String? = null)

    @Query("UPDATE chat_messages SET isPinned = 0 WHERE roomId = :roomId")
    suspend fun clearRoomPins(roomId: String)

    @Query("UPDATE chat_messages SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("UPDATE chat_messages SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementLike(id: Long)

    @Query("UPDATE chat_messages SET likesCount = MAX(0, likesCount - 1) WHERE id = :id")
    suspend fun decrementLike(id: Long)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun deleteByRoom(roomId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
