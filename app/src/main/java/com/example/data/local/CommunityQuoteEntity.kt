package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_quotes")
data class CommunityQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val category: String = "Aşk Sözleri", // "Aşk Sözleri", "Romantik Sözler", "Günaydın", "Gece", "Özel"
    val submitterName: String = "Âşık Bir Yürek",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val moderationNote: String? = null
)
