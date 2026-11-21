package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_messages")
data class AdminMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderEmail: String = "",
    val topic: String,
    val message: String,
    val adminReply: String? = null,
    val status: String = "PENDING", // PENDING, REPLIED
    val isReadByAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val repliedAt: Long? = null
)
