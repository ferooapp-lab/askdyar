package com.example.data.firebase

import androidx.annotation.Keep
import com.example.data.local.AdminMessageEntity
import com.example.data.local.AdminQuoteEntity
import com.example.data.local.BroadcastNotificationEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CommunityCommentEntity
import com.example.data.local.CommunityQuoteEntity
import com.example.data.local.CustomMusicTrackEntity
import com.example.data.local.EngagementNotificationEntity
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.SavedFavoriteQuoteEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.AdminSettings
import com.example.data.model.EmotionalJournalEntry
import com.example.data.model.EntitlementState
import com.example.data.model.JournalDateHelper
import com.example.data.model.JournalMood
import com.example.data.model.LoveCounterInfo
import com.example.data.model.LoveTimeCapsule
import com.example.data.model.PurchaseRecord
import com.example.data.model.SpecialCountdownEvent
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import kotlin.math.abs

/**
 * Generates a deterministic positive 63-bit Long ID from any Firestore String document ID
 * so existing Compose LazyColumn keys and UI callbacks keep stable unique Long identifiers.
 */
fun stableLongIdFromDocId(docId: String): Long {
    if (docId.isBlank()) return 1L
    var h1 = -3750763034362895579L // FNV-1a 64-bit offset basis
    for (ch in docId) {
        h1 = h1 xor ch.code.toLong()
        h1 *= 1099511628211L
    }
    val positive = abs(h1 and Long.MAX_VALUE)
    return if (positive == 0L) 1L else positive
}

private fun DocumentSnapshot.tsMillis(field: String, fallback: Long = System.currentTimeMillis()): Long {
    return getTimestamp(field, ServerTimestampBehavior.ESTIMATE)?.toDate()?.time ?: fallback
}

private fun DocumentSnapshot.nullableTsMillis(field: String): Long? {
    return getTimestamp(field, ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
}

private fun DocumentSnapshot.stringList(field: String): List<String> {
    val raw = get(field) as? List<*> ?: return emptyList()
    return raw.mapNotNull { it as? String }
}

@Keep
data class FsPublicProfile(
    val uid: String = "",
    val displayName: String = "Kullanıcı",
    val partnerName: String = "Sevgilim",
    val role: String = "USER",
    val isPremium: Boolean = false,
    val vipPlanName: String = "",
    val isBlocked: Boolean = false,
    val isMutedInChat: Boolean = false,
    val sharedQuotesCount: Int = 0,
    val receivedHeartsCount: Int = 0,
    val updatedAt: Long = 0L
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): FsPublicProfile {
            return FsPublicProfile(
                uid = doc.getString("uid") ?: doc.id,
                displayName = doc.getString("displayName") ?: "Kullanıcı",
                partnerName = doc.getString("partnerName") ?: "Sevgilim",
                role = doc.getString("role") ?: "USER",
                isPremium = doc.getBoolean("isPremium") ?: false,
                vipPlanName = doc.getString("vipPlanName") ?: "",
                isBlocked = doc.getBoolean("isBlocked") ?: false,
                isMutedInChat = doc.getBoolean("isMutedInChat") ?: false,
                sharedQuotesCount = (doc.getLong("sharedQuotesCount") ?: 0L).toInt(),
                receivedHeartsCount = (doc.getLong("receivedHeartsCount") ?: 0L).toInt(),
                updatedAt = doc.tsMillis("updatedAt", 0L)
            )
        }
    }
}

@Keep
data class FsUserDoc(
    val uid: String = "",
    val fullName: String = "Kullanıcı",
    val email: String = "",
    val partnerName: String = "Sevgilim",
    val isBlocked: Boolean = false,
    val isRestricted: Boolean = false,
    val canSubmitQuotes: Boolean = true,
    val canComment: Boolean = true,
    val canUploadMusic: Boolean = true,
    val canChat: Boolean = true,
    val restrictionReason: String = "",
    val restrictedUntil: Long? = null,
    val isVip: Boolean = false,
    val vipPlanName: String = "",
    val isAdFree: Boolean = false,
    val hasUnlimitedBackup: Boolean = false,
    val hasDesignPack: Boolean = false,
    val hasVipBadgeAndAi: Boolean = false,
    val loveCoins: Int = 30,
    val unlockedItemIds: List<String> = emptyList(),
    val totalCoinPurchases: Int = 0,
    val totalVipOrPackPurchases: Int = 0,
    val estimatedRevenueTry: Double = 0.0,
    val themeMode: String = "system",
    val designTheme: String = "ruby_velvet",
    val dailyNotifEnabled: Boolean = true,
    val dailyNotifHour: Int = 8,
    val dailyNotifMinute: Int = 30,
    val dailyNotifTone: String = "MIXED",
    val loveCounterMyName: String = "Ben",
    val loveCounterPartnerName: String = "Sevgilim",
    val loveCounterStartDate: Long = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 120),
    val loveCounterTitle: String = "İlk Günümüzden Beri",
    val redeemedCouponIds: List<String> = emptyList(),
    val doneBucketIds: List<String> = emptyList(),
    val readBroadcastIds: List<String> = emptyList(),
    val ourSongTitle: String = "",
    val ourSongArtist: String = "",
    val loveStreak: Int = 1,
    val streakLastDay: String = "",
    val todayMoodLabel: String = "",
    val todayMoodEmoji: String = "",
    val todayMoodScore: Int = 0,
    val todayMoodDate: String = "",
    val moodCheckInHistoryJson: String = "{}",
    val loveLanguageName: String = "",
    val missYouTotalCount: Int = 0,
    val missYouTodayCount: Int = 0,
    val missYouDay: String = "",
    val truthDareCompletedCount: Int = 0,
    val lastSyncAt: Long = 0L,
    val syncedNotesCount: Int = 0,
    val syncedFavsCount: Int = 0,
    val syncedRemindersCount: Int = 0,
    val lastBackupJson: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toUserAccountEntity(legacyId: Long): UserAccountEntity = UserAccountEntity(
        id = legacyId,
        fullName = fullName,
        email = email,
        passwordHash = "google_auth",
        partnerName = partnerName,
        isBlocked = isBlocked,
        isRestricted = isRestricted,
        canSubmitQuotes = canSubmitQuotes,
        canComment = canComment,
        canUploadMusic = canUploadMusic,
        canChat = canChat,
        restrictionReason = restrictionReason,
        restrictedUntil = restrictedUntil,
        createdAt = createdAt,
        lastLoginAt = updatedAt
    )

    fun toEntitlementState(
        adminSettings: AdminSettings,
        purchases: List<PurchaseRecord>
    ): EntitlementState = EntitlementState(
        isVip = isVip,
        vipPlanName = vipPlanName,
        isAdFree = isAdFree,
        hasUnlimitedBackup = hasUnlimitedBackup,
        hasDesignPack = hasDesignPack,
        hasVipBadgeAndAi = hasVipBadgeAndAi,
        loveCoins = loveCoins,
        unlockedItemIds = unlockedItemIds.toSet(),
        adsEnabledByAdmin = adminSettings.adsEnabled,
        rewardCoinsPerAd = adminSettings.rewardCoinsPerAd,
        totalCoinPurchases = totalCoinPurchases,
        totalVipOrPackPurchases = totalVipOrPackPurchases,
        estimatedRevenueTry = estimatedRevenueTry,
        purchaseHistory = purchases
    )

    fun toLoveCounterInfo(): LoveCounterInfo = LoveCounterInfo(
        myName = loveCounterMyName.ifBlank { fullName },
        partnerName = loveCounterPartnerName.ifBlank { partnerName },
        startTimestamp = loveCounterStartDate,
        celebrationTitle = loveCounterTitle.ifBlank { "İlk Günümüzden Beri" }
    )

    companion object {
        fun initialCreateMap(
            uid: String,
            fullName: String,
            email: String,
            todayKey: String,
            defaultCounterStart: Long
        ): Map<String, Any?> {
            val safeName = fullName.trim().ifBlank { "Aşk Yolcusu" }.take(100)
            val safeEmail = email.trim().take(160)
            return hashMapOf(
                "uid" to uid,
                "fullName" to safeName,
                "email" to safeEmail,
                "partnerName" to "Sevgilim",
                "isBlocked" to false,
                "isRestricted" to false,
                "canSubmitQuotes" to true,
                "canComment" to true,
                "canUploadMusic" to true,
                "canChat" to true,
                "restrictionReason" to "",
                "restrictedUntil" to null,
                "isVip" to false,
                "vipPlanName" to "",
                "isAdFree" to false,
                "hasUnlimitedBackup" to false,
                "hasDesignPack" to false,
                "hasVipBadgeAndAi" to false,
                "loveCoins" to 30,
                "unlockedItemIds" to emptyList<String>(),
                "totalCoinPurchases" to 0,
                "totalVipOrPackPurchases" to 0,
                "estimatedRevenueTry" to 0,
                "themeMode" to "system",
                "designTheme" to "ruby_velvet",
                "dailyNotifEnabled" to true,
                "dailyNotifHour" to 8,
                "dailyNotifMinute" to 30,
                "dailyNotifTone" to "MIXED",
                "loveCounterMyName" to safeName.take(80),
                "loveCounterPartnerName" to "Sevgilim",
                "loveCounterStartDate" to defaultCounterStart,
                "loveCounterTitle" to "İlk Günümüzden Beri",
                "redeemedCouponIds" to emptyList<String>(),
                "doneBucketIds" to emptyList<String>(),
                "readBroadcastIds" to emptyList<String>(),
                "ourSongTitle" to "",
                "ourSongArtist" to "",
                "loveStreak" to 1,
                "streakLastDay" to todayKey,
                "todayMoodLabel" to "",
                "todayMoodEmoji" to "",
                "todayMoodScore" to 0,
                "todayMoodDate" to "",
                "moodCheckInHistoryJson" to "{}",
                "loveLanguageName" to "",
                "missYouTotalCount" to 0,
                "missYouTodayCount" to 0,
                "missYouDay" to todayKey,
                "truthDareCompletedCount" to 0,
                "lastSyncAt" to 0L,
                "syncedNotesCount" to 0,
                "syncedFavsCount" to 0,
                "syncedRemindersCount" to 0,
                "lastBackupJson" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }

        fun fromSnapshot(doc: DocumentSnapshot): FsUserDoc {
            val defaultCounter = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 120)
            return FsUserDoc(
                uid = doc.getString("uid") ?: doc.id,
                fullName = doc.getString("fullName") ?: "Kullanıcı",
                email = doc.getString("email") ?: "",
                partnerName = doc.getString("partnerName") ?: "Sevgilim",
                isBlocked = doc.getBoolean("isBlocked") ?: false,
                isRestricted = doc.getBoolean("isRestricted") ?: false,
                canSubmitQuotes = doc.getBoolean("canSubmitQuotes") ?: true,
                canComment = doc.getBoolean("canComment") ?: true,
                canUploadMusic = doc.getBoolean("canUploadMusic") ?: true,
                canChat = doc.getBoolean("canChat") ?: true,
                restrictionReason = doc.getString("restrictionReason") ?: "",
                restrictedUntil = doc.getLong("restrictedUntil"),
                isVip = doc.getBoolean("isVip") ?: false,
                vipPlanName = doc.getString("vipPlanName") ?: "",
                isAdFree = doc.getBoolean("isAdFree") ?: false,
                hasUnlimitedBackup = doc.getBoolean("hasUnlimitedBackup") ?: false,
                hasDesignPack = doc.getBoolean("hasDesignPack") ?: false,
                hasVipBadgeAndAi = doc.getBoolean("hasVipBadgeAndAi") ?: false,
                loveCoins = (doc.getLong("loveCoins") ?: 30L).toInt(),
                unlockedItemIds = doc.stringList("unlockedItemIds"),
                totalCoinPurchases = (doc.getLong("totalCoinPurchases") ?: 0L).toInt(),
                totalVipOrPackPurchases = (doc.getLong("totalVipOrPackPurchases") ?: 0L).toInt(),
                estimatedRevenueTry = (doc.get("estimatedRevenueTry") as? Number)?.toDouble() ?: 0.0,
                themeMode = doc.getString("themeMode") ?: "system",
                designTheme = doc.getString("designTheme") ?: "ruby_velvet",
                dailyNotifEnabled = doc.getBoolean("dailyNotifEnabled") ?: true,
                dailyNotifHour = (doc.getLong("dailyNotifHour") ?: 8L).toInt(),
                dailyNotifMinute = (doc.getLong("dailyNotifMinute") ?: 30L).toInt(),
                dailyNotifTone = doc.getString("dailyNotifTone") ?: "MIXED",
                loveCounterMyName = doc.getString("loveCounterMyName") ?: "Ben",
                loveCounterPartnerName = doc.getString("loveCounterPartnerName") ?: "Sevgilim",
                loveCounterStartDate = doc.getLong("loveCounterStartDate") ?: defaultCounter,
                loveCounterTitle = doc.getString("loveCounterTitle") ?: "İlk Günümüzden Beri",
                redeemedCouponIds = doc.stringList("redeemedCouponIds"),
                doneBucketIds = doc.stringList("doneBucketIds"),
                readBroadcastIds = doc.stringList("readBroadcastIds"),
                ourSongTitle = doc.getString("ourSongTitle") ?: "",
                ourSongArtist = doc.getString("ourSongArtist") ?: "",
                loveStreak = (doc.getLong("loveStreak") ?: 1L).toInt(),
                streakLastDay = doc.getString("streakLastDay") ?: "",
                todayMoodLabel = doc.getString("todayMoodLabel") ?: "",
                todayMoodEmoji = doc.getString("todayMoodEmoji") ?: "",
                todayMoodScore = (doc.getLong("todayMoodScore") ?: 0L).toInt(),
                todayMoodDate = doc.getString("todayMoodDate") ?: "",
                moodCheckInHistoryJson = doc.getString("moodCheckInHistoryJson") ?: "{}",
                loveLanguageName = doc.getString("loveLanguageName") ?: "",
                missYouTotalCount = (doc.getLong("missYouTotalCount") ?: 0L).toInt(),
                missYouTodayCount = (doc.getLong("missYouTodayCount") ?: 0L).toInt(),
                missYouDay = doc.getString("missYouDay") ?: "",
                truthDareCompletedCount = (doc.getLong("truthDareCompletedCount") ?: 0L).toInt(),
                lastSyncAt = doc.getLong("lastSyncAt") ?: 0L,
                syncedNotesCount = (doc.getLong("syncedNotesCount") ?: 0L).toInt(),
                syncedFavsCount = (doc.getLong("syncedFavsCount") ?: 0L).toInt(),
                syncedRemindersCount = (doc.getLong("syncedRemindersCount") ?: 0L).toInt(),
                lastBackupJson = doc.getString("lastBackupJson") ?: "",
                createdAt = doc.tsMillis("createdAt"),
                updatedAt = doc.tsMillis("updatedAt")
            )
        }
    }
}

object FsMappers {
    fun adminSettingsFromSnapshot(doc: DocumentSnapshot): AdminSettings {
        if (!doc.exists()) return AdminSettings()
        return AdminSettings(
            submissionsEnabled = doc.getBoolean("submissionsEnabled") ?: true,
            dailyLimitPerUser = (doc.getLong("dailyLimitPerUser") ?: 3L).toInt(),
            minCharCount = (doc.getLong("minCharCount") ?: 10L).toInt(),
            maxCharCount = (doc.getLong("maxCharCount") ?: 280L).toInt(),
            autoApprove = doc.getBoolean("autoApprove") ?: false,
            adminPin = "", // Not stored in Firestore under Firebase Auth zero-trust rules
            maintenanceMode = doc.getBoolean("maintenanceMode") ?: false,
            maintenanceTitle = doc.getString("maintenanceTitle") ?: "Kısa Bir Aşk Molası 🛠️",
            maintenanceMessage = doc.getString("maintenanceMessage")
                ?: "Sizlere daha romantik ve kusursuz bir deneyim sunmak için kısa bir bakım çalışması yapıyoruz. Çok yakında tekrar buradayız!",
            maintenanceEstimatedTime = doc.getString("maintenanceEstimatedTime") ?: "Yaklaşık 15 dakika",
            allowUserRegistration = doc.getBoolean("allowUserRegistration") ?: true,
            allowCustomMusic = doc.getBoolean("allowCustomMusic") ?: true,
            allowCommunityComments = doc.getBoolean("allowCommunityComments") ?: true,
            announcementBannerText = doc.getString("announcementBannerText") ?: "",
            adsEnabled = doc.getBoolean("adsEnabled") ?: true,
            rewardCoinsPerAd = (doc.getLong("rewardCoinsPerAd") ?: 15L).toInt(),
            interstitialCooldownSeconds = (doc.getLong("interstitialCooldownSeconds") ?: 180L).toInt(),
            adBlockerGuardEnabled = doc.getBoolean("adBlockerGuardEnabled") ?: true,
            chatEnabled = doc.getBoolean("chatEnabled") ?: true,
            chatClosedMessage = doc.getString("chatClosedMessage")
                ?: "Sohbet odaları şu anda yönetici tarafından geçici olarak kapatıldı.",
            chatAllowGuests = doc.getBoolean("chatAllowGuests") ?: true,
            chatRequireApproval = doc.getBoolean("chatRequireApproval") ?: false,
            chatSlowModeSeconds = (doc.getLong("chatSlowModeSeconds") ?: 0L).toInt(),
            chatMaxCharCount = (doc.getLong("chatMaxCharCount") ?: 300L).toInt(),
            chatBlockedWordsCsv = doc.getString("chatBlockedWordsCsv") ?: "küfür,hakaret,spam,reklam"
        )
    }

    fun adminSettingsToMap(settings: AdminSettings): Map<String, Any?> = hashMapOf(
        "submissionsEnabled" to settings.submissionsEnabled,
        "dailyLimitPerUser" to settings.dailyLimitPerUser.coerceIn(1, 50),
        "minCharCount" to settings.minCharCount.coerceIn(1, 200),
        "maxCharCount" to settings.maxCharCount.coerceIn(20, 2000),
        "autoApprove" to settings.autoApprove,
        "maintenanceMode" to settings.maintenanceMode,
        "maintenanceTitle" to settings.maintenanceTitle.take(120),
        "maintenanceMessage" to settings.maintenanceMessage.take(600),
        "maintenanceEstimatedTime" to settings.maintenanceEstimatedTime.take(80),
        "allowUserRegistration" to settings.allowUserRegistration,
        "allowCustomMusic" to settings.allowCustomMusic,
        "allowCommunityComments" to settings.allowCommunityComments,
        "announcementBannerText" to settings.announcementBannerText.take(300),
        "adsEnabled" to settings.adsEnabled,
        "rewardCoinsPerAd" to settings.rewardCoinsPerAd.coerceIn(0, 500),
        "interstitialCooldownSeconds" to settings.interstitialCooldownSeconds.coerceIn(0, 3600),
        "adBlockerGuardEnabled" to settings.adBlockerGuardEnabled,
        "chatEnabled" to settings.chatEnabled,
        "chatClosedMessage" to settings.chatClosedMessage.take(300),
        "chatAllowGuests" to settings.chatAllowGuests,
        "chatRequireApproval" to settings.chatRequireApproval,
        "chatSlowModeSeconds" to settings.chatSlowModeSeconds.coerceIn(0, 600),
        "chatMaxCharCount" to settings.chatMaxCharCount.coerceIn(20, 1000),
        "chatBlockedWordsCsv" to settings.chatBlockedWordsCsv.take(2000),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun noteFromSnapshot(doc: DocumentSnapshot): NoteEntity {
        return NoteEntity(
            id = stableLongIdFromDocId(doc.id),
            title = doc.getString("title") ?: "Romantik Not",
            content = doc.getString("content") ?: "",
            author = doc.getString("author") ?: "Ben",
            tags = doc.getString("tags") ?: "",
            voiceAudioPath = doc.getString("voiceAudioPath"),
            voiceDurationSeconds = (doc.getLong("voiceDurationSeconds") ?: 0L).toInt(),
            isFavorite = doc.getBoolean("isFavorite") ?: false,
            backgroundTheme = doc.getString("backgroundTheme") ?: "ruby_velvet",
            filterStyle = doc.getString("filterStyle") ?: "normal",
            createdAt = doc.tsMillis("createdAt"),
            updatedAt = doc.tsMillis("updatedAt"),
            isSyncedWithCloud = doc.getBoolean("isSyncedWithCloud") ?: true
        )
    }

    fun savedFavoriteFromSnapshot(doc: DocumentSnapshot): SavedFavoriteQuoteEntity {
        return SavedFavoriteQuoteEntity(
            id = stableLongIdFromDocId(doc.id),
            title = doc.getString("title") ?: "Favori Sözüm",
            text = doc.getString("text") ?: "",
            author = doc.getString("author") ?: "Anonim",
            category = doc.getString("category") ?: "Romantik",
            sourceLabel = doc.getString("sourceLabel") ?: "Kişisel Favori",
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun reminderFromSnapshot(doc: DocumentSnapshot): ReminderEntity {
        return ReminderEntity(
            id = stableLongIdFromDocId(doc.id),
            title = doc.getString("title") ?: "Hatırlatıcı",
            note = doc.getString("note") ?: "",
            targetTimestamp = doc.getLong("targetTimestamp") ?: System.currentTimeMillis(),
            reminderType = doc.getString("reminderType") ?: "Özel Gün",
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            isNotificationEnabled = doc.getBoolean("isNotificationEnabled") ?: true,
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun musicTrackFromSnapshot(doc: DocumentSnapshot): CustomMusicTrackEntity {
        return CustomMusicTrackEntity(
            id = stableLongIdFromDocId(doc.id),
            title = doc.getString("title") ?: "Romantik Fon Müziği",
            artist = doc.getString("artist") ?: "Kişisel Kütüphane",
            filePath = doc.getString("filePath") ?: "",
            presetKey = doc.getString("presetKey"),
            addedBy = doc.getString("addedBy") ?: "Kullanıcı",
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun engagementNotifFromSnapshot(doc: DocumentSnapshot): EngagementNotificationEntity {
        return EngagementNotificationEntity(
            id = stableLongIdFromDocId(doc.id),
            type = doc.getString("type") ?: "LIKE",
            quoteId = stableLongIdFromDocId(doc.getString("quoteDocId") ?: ""),
            quotePreview = doc.getString("quotePreview") ?: "",
            actorName = doc.getString("actorName") ?: "Kullanıcı",
            message = doc.getString("message") ?: "",
            isRead = doc.getBoolean("isRead") ?: false,
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun timeCapsuleFromSnapshot(doc: DocumentSnapshot): LoveTimeCapsule {
        return LoveTimeCapsule(
            id = doc.id,
            title = doc.getString("title") ?: "Geleceğe Mektup",
            message = doc.getString("message") ?: "",
            author = doc.getString("author") ?: "Ben",
            createdAt = doc.tsMillis("createdAt"),
            unlockAt = doc.getLong("unlockAt") ?: System.currentTimeMillis()
        )
    }

    fun countdownFromSnapshot(doc: DocumentSnapshot): SpecialCountdownEvent {
        return SpecialCountdownEvent(
            id = doc.id,
            title = doc.getString("title") ?: "Özel Gün",
            emoji = doc.getString("emoji") ?: "💖",
            targetTimestamp = doc.getLong("targetTimestamp") ?: System.currentTimeMillis()
        )
    }

    fun purchaseFromSnapshot(doc: DocumentSnapshot): PurchaseRecord {
        return PurchaseRecord(
            id = doc.id,
            title = doc.getString("title") ?: "İşlem",
            amountDisplay = doc.getString("amountDisplay") ?: "",
            timestamp = doc.tsMillis("createdAt"),
            source = doc.getString("source") ?: "Play Store"
        )
    }

    fun adminQuoteFromSnapshot(doc: DocumentSnapshot): AdminQuoteEntity {
        return AdminQuoteEntity(
            id = stableLongIdFromDocId(doc.id),
            text = doc.getString("text") ?: "",
            author = doc.getString("author") ?: "Aşk Sözleri Editörü",
            sectionId = doc.getString("sectionId") ?: "ask",
            subCategory = doc.getString("subCategory") ?: "Editör Seçimi",
            tagsCsv = doc.getString("tagsCsv") ?: "Özel, Romantik",
            isFeaturedDaily = doc.getBoolean("isFeaturedDaily") ?: false,
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun broadcastFromSnapshot(doc: DocumentSnapshot, readIds: Set<String>): BroadcastNotificationEntity {
        return BroadcastNotificationEntity(
            id = stableLongIdFromDocId(doc.id),
            title = doc.getString("title") ?: "Duyuru",
            message = doc.getString("message") ?: "",
            category = doc.getString("category") ?: "Duyuru",
            emoji = doc.getString("emoji") ?: "📢",
            sentBy = doc.getString("sentBy") ?: "Yönetici",
            sentAt = doc.tsMillis("sentAt"),
            isRead = readIds.contains(doc.id)
        )
    }

    fun supportMessageFromSnapshot(doc: DocumentSnapshot): AdminMessageEntity {
        return AdminMessageEntity(
            id = stableLongIdFromDocId(doc.id),
            senderName = doc.getString("senderName") ?: "Kullanıcı",
            senderEmail = doc.getString("senderEmail") ?: "",
            topic = doc.getString("topic") ?: "Genel Mesaj",
            message = doc.getString("message") ?: "",
            adminReply = doc.getString("adminReply"),
            status = doc.getString("status") ?: "PENDING",
            isReadByAdmin = doc.getBoolean("isReadByAdmin") ?: false,
            createdAt = doc.tsMillis("createdAt"),
            repliedAt = doc.nullableTsMillis("repliedAt")
        )
    }

    fun communityQuoteFromSnapshot(doc: DocumentSnapshot): CommunityQuoteEntity {
        return CommunityQuoteEntity(
            id = stableLongIdFromDocId(doc.id),
            text = doc.getString("text") ?: "",
            author = doc.getString("author") ?: "Anonim",
            category = doc.getString("category") ?: "Aşk Sözleri",
            submitterName = doc.getString("submitterName") ?: "Âşık Bir Yürek",
            status = doc.getString("status") ?: "PENDING",
            likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
            createdAt = doc.tsMillis("createdAt"),
            approvedAt = doc.nullableTsMillis("approvedAt"),
            moderationNote = doc.getString("moderationNote")
        )
    }

    fun communityCommentFromSnapshot(
        quoteDocId: String,
        doc: DocumentSnapshot
    ): CommunityCommentEntity {
        return CommunityCommentEntity(
            id = stableLongIdFromDocId(doc.id),
            quoteId = stableLongIdFromDocId(quoteDocId),
            authorName = doc.getString("authorName") ?: "Okur",
            commentText = doc.getString("commentText") ?: "",
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun chatMessageFromSnapshot(doc: DocumentSnapshot): ChatMessageEntity {
        val senderUid = doc.getString("senderUid") ?: ""
        val replyDocId = doc.getString("replyToMessageId")
        return ChatMessageEntity(
            id = stableLongIdFromDocId(doc.id),
            roomId = doc.getString("roomId") ?: "genel",
            senderUserId = if (senderUid.isNotBlank()) stableLongIdFromDocId(senderUid) else 0L,
            senderName = doc.getString("senderName") ?: "Kullanıcı",
            senderRole = doc.getString("senderRole") ?: "USER",
            isVipSender = doc.getBoolean("isPremiumSender") ?: false,
            message = doc.getString("message") ?: "",
            replyToMessageId = replyDocId?.takeIf { it.isNotBlank() }?.let { stableLongIdFromDocId(it) },
            replyToSenderName = doc.getString("replyToSenderName"),
            replyToSnippet = doc.getString("replyToSnippet"),
            status = doc.getString("status") ?: "PUBLISHED",
            isPinned = doc.getBoolean("isPinned") ?: false,
            likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
            moderationNote = doc.getString("moderationNote"),
            createdAt = doc.tsMillis("createdAt")
        )
    }

    fun journalEntryFromSnapshot(doc: DocumentSnapshot): EmotionalJournalEntry {
        val memoryTs = (doc.getLong("memoryTimestamp") ?: doc.tsMillis("createdAt")).coerceAtLeast(1L)
        val rawDateKey = doc.getString("dateKey")?.takeIf { it.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) }
            ?: JournalDateHelper.toDateKey(memoryTs)
        return EmotionalJournalEntry(
            id = stableLongIdFromDocId(doc.id),
            docId = doc.id,
            title = doc.getString("title") ?: "Aşk Anısı",
            content = doc.getString("content") ?: "",
            moodId = JournalMood.fromId(doc.getString("moodId")).id,
            dateKey = rawDateKey,
            memoryTimestamp = memoryTs,
            locationOrMoment = doc.getString("locationOrMoment") ?: "",
            isFavorite = doc.getBoolean("isFavorite") ?: false,
            isPrivateLocked = doc.getBoolean("isPrivateLocked") ?: false,
            createdAt = doc.tsMillis("createdAt"),
            updatedAt = doc.tsMillis("updatedAt")
        )
    }
}
