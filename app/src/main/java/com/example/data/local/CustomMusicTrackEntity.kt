package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_music_tracks")
data class CustomMusicTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "Özel Fon Müziği",
    val filePath: String,
    val presetKey: String? = null,
    val addedBy: String = "Kullanıcı",
    val createdAt: Long = System.currentTimeMillis()
)
