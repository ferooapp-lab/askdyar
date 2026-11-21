package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "broadcast_notifications")
data class BroadcastNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val category: String = "Duyuru",
    val emoji: String = "📢",
    val sentBy: String = "Yönetici",
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
