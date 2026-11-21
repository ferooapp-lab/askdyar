package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BroadcastNotificationDao {
    @Query("SELECT * FROM broadcast_notifications ORDER BY sentAt DESC")
    fun getAllBroadcasts(): Flow<List<BroadcastNotificationEntity>>

    @Query("SELECT COUNT(*) FROM broadcast_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM broadcast_notifications")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroadcast(broadcast: BroadcastNotificationEntity): Long

    @Query("UPDATE broadcast_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE broadcast_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM broadcast_notifications WHERE id = :id")
    suspend fun deleteBroadcastById(id: Long)
}
