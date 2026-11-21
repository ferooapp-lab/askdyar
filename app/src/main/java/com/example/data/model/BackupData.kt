package com.example.data.model

import com.example.data.local.FavoriteEntity
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val notes: List<BackupNoteItem> = emptyList(),
    val favoriteQuoteIds: List<String> = emptyList(),
    val reminders: List<BackupReminderItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BackupNoteItem(
    val id: Long,
    val title: String,
    val content: String,
    val author: String,
    val tags: String,
    val isFavorite: Boolean,
    val backgroundTheme: String,
    val filterStyle: String,
    val createdAt: Long,
    val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class BackupReminderItem(
    val id: Long,
    val title: String,
    val note: String,
    val targetTimestamp: Long,
    val reminderType: String,
    val isCompleted: Boolean
)

enum class CloudSyncState(val label: String) {
    SYNCED("Bulutla Güncel"),
    PENDING_CHANGES("Yedek Bekleniyor"),
    NEVER_BACKED_UP("Henüz Yedeklenmedi")
}

data class CloudBackupStatus(
    val lastBackupTimestamp: Long = 0L,
    val syncedNotesCount: Int = 0,
    val syncedFavoritesCount: Int = 0,
    val syncedRemindersCount: Int = 0,
    val unsyncedNotesCount: Int = 0,
    val hasSnapshot: Boolean = false,
    val totalLocalItemsCount: Int = 0,
    val backupLimit: Int = 10,
    val isUnlimited: Boolean = false
) {
    val isOverFreeLimit: Boolean
        get() = !isUnlimited && totalLocalItemsCount > backupLimit

    val state: CloudSyncState
        get() = when {
            lastBackupTimestamp <= 0L || !hasSnapshot -> CloudSyncState.NEVER_BACKED_UP
            unsyncedNotesCount > 0 -> CloudSyncState.PENDING_CHANGES
            else -> CloudSyncState.SYNCED
        }
}

