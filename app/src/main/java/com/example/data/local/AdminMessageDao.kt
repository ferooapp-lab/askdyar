package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminMessageDao {
    @Query("SELECT * FROM admin_messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<AdminMessageEntity>>

    @Query("SELECT COUNT(*) FROM admin_messages WHERE status = 'PENDING'")
    fun getUnreadOrPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AdminMessageEntity): Long

    @Query("UPDATE admin_messages SET adminReply = :reply, status = 'REPLIED', isReadByAdmin = 1, repliedAt = :repliedAt WHERE id = :id")
    suspend fun replyToMessage(id: Long, reply: String, repliedAt: Long = System.currentTimeMillis())

    @Query("UPDATE admin_messages SET isReadByAdmin = 1 WHERE id = :id")
    suspend fun markReadByAdmin(id: Long)

    @Query("DELETE FROM admin_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)
}
