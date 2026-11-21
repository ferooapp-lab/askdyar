package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "romantic_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val author: String = "Ben",
    val tags: String = "", // Comma-separated: "Sevgilime, Yıldönümü, Şiir"
    val voiceAudioPath: String? = null,
    val voiceDurationSeconds: Int = 0,
    val isFavorite: Boolean = false,
    val backgroundTheme: String = "rose_velvet",
    val filterStyle: String = "normal",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSyncedWithCloud: Boolean = true
)
