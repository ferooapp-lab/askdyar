package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_favorites")
data class FavoriteEntity(
    @PrimaryKey
    val quoteId: String,
    val savedAt: Long = System.currentTimeMillis()
)
