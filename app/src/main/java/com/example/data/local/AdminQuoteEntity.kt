package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_quotes")
data class AdminQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val sectionId: String = "ask", // "ask", "romantik", "gunaydin", "gece"
    val subCategory: String = "Editör Seçimi",
    val tagsCsv: String = "Özel, Romantik",
    val isFeaturedDaily: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
