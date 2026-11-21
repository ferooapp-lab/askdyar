package com.example.data.model

data class AdminSettings(
    val submissionsEnabled: Boolean = true,
    val dailyLimitPerUser: Int = 3,
    val minCharCount: Int = 10,
    val maxCharCount: Int = 280,
    val autoApprove: Boolean = false,
    val adminPin: String = "1234",
    val maintenanceMode: Boolean = false,
    val maintenanceTitle: String = "Kısa Bir Aşk Molası 🛠️",
    val maintenanceMessage: String = "Sizlere daha romantik ve kusursuz bir deneyim sunmak için kısa bir bakım çalışması yapıyoruz. Çok yakında tekrar buradayız!",
    val maintenanceEstimatedTime: String = "Yaklaşık 15 dakika",
    val allowUserRegistration: Boolean = true,
    val allowCustomMusic: Boolean = true,
    val allowCommunityComments: Boolean = true,
    val announcementBannerText: String = "",
    val adsEnabled: Boolean = true,
    val rewardCoinsPerAd: Int = 15,
    val interstitialCooldownSeconds: Int = 180,
    val adBlockerGuardEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val chatClosedMessage: String = "Sohbet odaları şu anda yönetici tarafından geçici olarak kapatıldı.",
    val chatAllowGuests: Boolean = true,
    val chatRequireApproval: Boolean = false,
    val chatSlowModeSeconds: Int = 0,
    val chatMaxCharCount: Int = 300,
    val chatBlockedWordsCsv: String = "küfür,hakaret,spam,reklam"
)
