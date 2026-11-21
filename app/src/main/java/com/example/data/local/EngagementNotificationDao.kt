package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EngagementNotificationDao {
    @Query("SELECT * FROM engagement_notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<EngagementNotificationEntity>>

    @Query("SELECT COUNT(*) FROM engagement_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM engagement_notifications")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(item: EngagementNotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EngagementNotificationEntity>)

    @Query("UPDATE engagement_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE engagement_notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("DELETE FROM engagement_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM engagement_notifications")
    suspend fun clearAll()
}
