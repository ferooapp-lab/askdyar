package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "engagement_notifications")
data class EngagementNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "LIKE", "COMMENT", "APPROVED", "MODERATION"
    val quoteId: Long = 0,
    val quotePreview: String,
    val actorName: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
