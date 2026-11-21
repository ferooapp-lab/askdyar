package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: String = "genel",
    val senderUserId: Long = 0L,
    val senderName: String,
    val senderRole: String = "USER", // "USER", "ADMIN", "GUEST"
    val isVipSender: Boolean = false,
    val message: String,
    val replyToMessageId: Long? = null,
    val replyToSenderName: String? = null,
    val replyToSnippet: String? = null,
    val status: String = "PUBLISHED", // "PUBLISHED", "PENDING", "HIDDEN"
    val isPinned: Boolean = false,
    val likesCount: Int = 0,
    val moderationNote: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
