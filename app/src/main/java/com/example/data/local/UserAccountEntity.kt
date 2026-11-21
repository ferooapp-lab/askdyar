package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val partnerName: String = "Sevgilim",
    val isBlocked: Boolean = false, // Banlı (Kalıcı veya Yönetici tarafından yasaklı)
    val isRestricted: Boolean = false, // Kısıtlı (Söz/yorum/müzik kısıtlaması)
    val canSubmitQuotes: Boolean = true,
    val canComment: Boolean = true,
    val canUploadMusic: Boolean = true,
    val canChat: Boolean = true,
    val restrictionReason: String = "",
    val restrictedUntil: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
