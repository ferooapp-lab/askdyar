package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_comments")
data class CommunityCommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quoteId: Long,
    val authorName: String,
    val commentText: String,
    val createdAt: Long = System.currentTimeMillis()
)
