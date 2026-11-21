package com.example.data.model

import java.util.Locale

enum class ChatRoom(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String
) {
    GENEL(
        id = "genel",
        title = "Genel Sohbet",
        emoji = "💬",
        description = "Tüm aşıkların buluştuğu samimi sohbet odası"
    ),
    SIIR_SOZ(
        id = "siir_soz",
        title = "Şiir & Sözler",
        emoji = "✍️",
        description = "Kendi dizelerini ve romantik sözlerini paylaş"
    ),
    ILISKI_TAVSIYE(
        id = "tavsiye",
        title = "İlişki Tavsiyeleri",
        emoji = "💞",
        description = "Deneyimler, hediye fikirleri ve sohbet"
    ),
    GECE_KOSESI(
        id = "gece",
        title = "İyi Geceler Köşesi",
        emoji = "🌙",
        description = "Geceye özel huzurlu ve duygusal sohbetler"
    );

    companion object {
        fun fromId(id: String): ChatRoom {
            return values().find { it.id == id } ?: GENEL
        }
    }
}

object ChatModerationHelper {
    private val turkishLocale = Locale("tr", "TR")

    fun parseBlockedWords(csv: String): List<String> {
        return csv.split(",", ";", "\n")
            .map { it.trim().lowercase(turkishLocale) }
            .filter { it.length >= 2 }
            .distinct()
    }

    fun containsBlockedWord(text: String, blockedWordsCsv: String): String? {
        val blocked = parseBlockedWords(blockedWordsCsv)
        if (blocked.isEmpty()) return null
        val normalized = text.lowercase(turkishLocale)
        return blocked.firstOrNull { word ->
            val escaped = Regex.escape(word)
            Regex("(^|[^\\p{L}\\p{N}])$escaped([^\\p{L}\\p{N}]|$)").containsMatchIn(normalized)
        }
    }

    fun checkSlowModeRemainingSeconds(
        lastSentAtMillis: Long,
        slowModeSeconds: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        if (slowModeSeconds <= 0 || lastSentAtMillis <= 0L) return 0
        val elapsedMillis = nowMillis - lastSentAtMillis
        if (elapsedMillis < 0L) return 0
        val requiredMillis = slowModeSeconds * 1000L
        return if (elapsedMillis < requiredMillis) {
            ((requiredMillis - elapsedMillis + 999L) / 1000L).toInt().coerceAtLeast(1)
        } else {
            0
        }
    }

    fun validateMessageContent(text: String, maxChars: Int): String? {
        val clean = text.trim()
        if (clean.length < 2) {
            return "Mesajınız en az 2 karakter olmalıdır."
        }
        if (clean.length > maxChars) {
            return "Mesaj çok uzun! En fazla $maxChars karakter gönderebilirsiniz."
        }
        return null
    }
}

object ChatHistoryMerger {
    private val turkishLocale = Locale("tr", "TR")

    /**
     * Merges cursor-paginated historical messages with live Firestore snapshot updates.
     * Live items override older cached versions of the same message ID (e.g., live like count / pin status),
     * and the combined list is ordered chronologically ascending.
     */
    fun mergePagedAndLive(
        loadedHistory: List<com.example.data.local.ChatMessageEntity>,
        liveMessages: List<com.example.data.local.ChatMessageEntity>,
        roomId: String,
        includeAllStatuses: Boolean = false
    ): List<com.example.data.local.ChatMessageEntity> {
        val merged = LinkedHashMap<Long, com.example.data.local.ChatMessageEntity>()
        loadedHistory.forEach { msg ->
            if (msg.roomId == roomId && (includeAllStatuses || msg.status == "PUBLISHED")) {
                merged[msg.id] = msg
            }
        }
        liveMessages.forEach { msg ->
            if (msg.roomId == roomId && (includeAllStatuses || msg.status == "PUBLISHED")) {
                merged[msg.id] = msg
            }
        }
        return merged.values.sortedWith(
            compareBy<com.example.data.local.ChatMessageEntity> { it.createdAt }.thenBy { it.id }
        )
    }

    fun filterByQuery(
        messages: List<com.example.data.local.ChatMessageEntity>,
        query: String
    ): List<com.example.data.local.ChatMessageEntity> {
        val clean = query.trim().lowercase(turkishLocale)
        if (clean.isBlank()) return messages
        return messages.filter {
            it.message.lowercase(turkishLocale).contains(clean) ||
                it.senderName.lowercase(turkishLocale).contains(clean)
        }
    }
}

