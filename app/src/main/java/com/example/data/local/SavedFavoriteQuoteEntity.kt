package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_favorite_quotes")
data class SavedFavoriteQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    val author: String,
    val category: String,
    val sourceLabel: String = "Kişisel Favori",
    val createdAt: Long = System.currentTimeMillis()
)
