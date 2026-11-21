package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "romantic_reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String = "",
    val targetTimestamp: Long,
    val reminderType: String = "Özel Gün", // "Özel Gün", "Görev / Sürpriz", "Mesaj Hatırlatıcı", "Günün Aşk Sözü"
    val isCompleted: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
