package com.example.data.model

enum class BillingProductKind {
    SUBSCRIPTION,
    NON_CONSUMABLE,
    CONSUMABLE_COINS
}

data class StoreProduct(
    val skuId: String,
    val title: String,
    val subtitle: String,
    val priceDisplay: String,
    val priceTry: Double,
    val badge: String?,
    val kind: BillingProductKind,
    val coinGrant: Int = 0,
    val grantsVip: Boolean = false,
    val grantsAdFree: Boolean = false,
    val grantsAllFrames: Boolean = false,
    val grantsUnlimitedBackup: Boolean = false,
    val grantsDesignPack: Boolean = false,
    val grantsVipBadgeAndAi: Boolean = false
)

data class PurchaseRecord(
    val id: String,
    val title: String,
    val amountDisplay: String,
    val timestamp: Long,
    val source: String // "Play Store", "Ödüllü Reklam", "Harcama"
)

data class EntitlementState(
    val isVip: Boolean = false,
    val vipPlanName: String = "",
    val isAdFree: Boolean = false,
    val hasUnlimitedBackup: Boolean = false,
    val hasDesignPack: Boolean = false,
    val hasVipBadgeAndAi: Boolean = false,
    val loveCoins: Int = 30,
    val unlockedItemIds: Set<String> = emptySet(),
    val adsEnabledByAdmin: Boolean = true,
    val rewardCoinsPerAd: Int = 15,
    val totalCoinPurchases: Int = 0,
    val totalVipOrPackPurchases: Int = 0,
    val estimatedRevenueTry: Double = 0.0,
    val purchaseHistory: List<PurchaseRecord> = emptyList()
) {
    val shouldShowAds: Boolean
        get() = adsEnabledByAdmin && !isAdFree && !isVip

    val hasUnlimitedCloudBackup: Boolean
        get() = isVip || hasUnlimitedBackup || unlockedItemIds.contains("pack_unlimited_backup")

    val hasSpecialDesignPack: Boolean
        get() = isVip || hasDesignPack || unlockedItemIds.contains("pack_all_frames") || unlockedItemIds.contains("pack_design_pro")

    val hasUnlimitedAiAccess: Boolean
        get() = isVip || hasVipBadgeAndAi || unlockedItemIds.contains("pack_vip_badge_ai")

    fun hasAccessToFrame(frame: StoryTemplateFrame): Boolean {
        if (!frame.isPremium) return true
        return isVip || hasDesignPack || unlockedItemIds.contains("pack_all_frames") || unlockedItemIds.contains(frame.id)
    }

    fun hasAccessToStudioTheme(preset: ThemePreset): Boolean {
        if (!preset.isPremium) return true
        return isVip || hasDesignPack || unlockedItemIds.contains("pack_all_frames") || unlockedItemIds.contains(preset.id)
    }

    fun hasAccessToItem(itemId: String, isPremiumItem: Boolean): Boolean {
        if (!isPremiumItem) return true
        return isVip || unlockedItemIds.contains(itemId)
    }
}

data class PremiumFeatureComparison(
    val title: String,
    val freeText: String,
    val premiumText: String,
    val emoji: String
)

object MonetizationCatalog {
    const val FREE_CLOUD_BACKUP_LIMIT = 10

    const val SKU_VIP_MONTHLY = "ask_vip_monthly"
    const val SKU_VIP_YEARLY = "ask_vip_yearly"
    const val SKU_REMOVE_ADS = "ask_remove_ads"
    const val SKU_STORY_PRO_PACK = "ask_story_pro_pack"
    const val SKU_UNLIMITED_BACKUP = "ask_unlimited_backup"
    const val SKU_COUPLE_BADGE_AI_PACK = "ask_couple_badge_pack"
    const val SKU_COINS_100 = "ask_coins_100"
    const val SKU_COINS_350 = "ask_coins_350"
    const val SKU_COINS_1000 = "ask_coins_1000"

    val vipSubscriptions = listOf(
        StoreProduct(
            skuId = SKU_VIP_YEARLY,
            title = "Yıllık Premium Üyelik",
            subtitle = "7 gün ücretsiz dene • Reklamsız + Tüm Tasarım Paketleri + Sınırsız Bulut Yedek + Sınırsız AI Koçu + 250 🪙",
            priceDisplay = "349,99 ₺ / yıl",
            priceTry = 349.99,
            badge = "%42 TASARRUF",
            kind = BillingProductKind.SUBSCRIPTION,
            coinGrant = 250,
            grantsVip = true,
            grantsAdFree = true,
            grantsAllFrames = true,
            grantsUnlimitedBackup = true,
            grantsDesignPack = true,
            grantsVipBadgeAndAi = true
        ),
        StoreProduct(
            skuId = SKU_VIP_MONTHLY,
            title = "Aylık Premium Üyelik",
            subtitle = "Reklamsız deneyim, tüm tasarım paketleri, sınırsız yedekleme & her ay +50 Aşk Parası",
            priceDisplay = "49,99 ₺ / ay",
            priceTry = 49.99,
            badge = "POPÜLER",
            kind = BillingProductKind.SUBSCRIPTION,
            coinGrant = 50,
            grantsVip = true,
            grantsAdFree = true,
            grantsAllFrames = true,
            grantsUnlimitedBackup = true,
            grantsDesignPack = true,
            grantsVipBadgeAndAi = true
        )
    )

    val oneTimePacks = listOf(
        StoreProduct(
            skuId = SKU_REMOVE_ADS,
            title = "Ömür Boyu Reklamsız Paket",
            subtitle = "Banner ve geçiş reklamlarını tek seferlik ödemeyle kalıcı olarak kaldır",
            priceDisplay = "79,99 ₺",
            priceTry = 79.99,
            badge = "TEK SEFERLİK",
            kind = BillingProductKind.NON_CONSUMABLE,
            grantsAdFree = true
        ),
        StoreProduct(
            skuId = SKU_STORY_PRO_PACK,
            title = "Özel Tasarım & Çerçeve Koleksiyonu",
            subtitle = "Altın Varak, Dergi Kapağı ve lüks Stüdyo arka plan gradyanlarını kalıcı aç",
            priceDisplay = "39,99 ₺",
            priceTry = 39.99,
            badge = "TASARIM PAKETİ",
            kind = BillingProductKind.NON_CONSUMABLE,
            grantsAllFrames = true,
            grantsDesignPack = true
        ),
        StoreProduct(
            skuId = SKU_UNLIMITED_BACKUP,
            title = "Sınırsız Bulut Yedekleme Paketi",
            subtitle = "Ücretsiz 10 kayıt sınırını kaldır, tüm aşk notlarını ve favorilerini sınırsız yedekle",
            priceDisplay = "29,99 ₺",
            priceTry = 29.99,
            badge = "BULUT ∞",
            kind = BillingProductKind.NON_CONSUMABLE,
            grantsUnlimitedBackup = true
        ),
        StoreProduct(
            skuId = SKU_COUPLE_BADGE_AI_PACK,
            title = "VIP Çift Rozeti & Özel AI Mektup",
            subtitle = "Profilinde altın Premium rozeti, sınırsız AI aşk mektubu & romantik öngörü",
            priceDisplay = "44,99 ₺",
            priceTry = 44.99,
            badge = "YENİ",
            kind = BillingProductKind.NON_CONSUMABLE,
            grantsVipBadgeAndAi = true
        )
    )

    val coinPacks = listOf(
        StoreProduct(
            skuId = SKU_COINS_100,
            title = "100 Aşk Parası 🪙",
            subtitle = "Özel tasarım paketleri ve çerçeveler için başlangıç paketi",
            priceDisplay = "29,99 ₺",
            priceTry = 29.99,
            badge = null,
            kind = BillingProductKind.CONSUMABLE_COINS,
            coinGrant = 100
        ),
        StoreProduct(
            skuId = SKU_COINS_350,
            title = "350 Aşk Parası 🪙",
            subtitle = "+50 Bonus Aşk Parası hediyesiyle en çok tercih edilen paket",
            priceDisplay = "79,99 ₺",
            priceTry = 79.99,
            badge = "+50 BONUS",
            kind = BillingProductKind.CONSUMABLE_COINS,
            coinGrant = 350
        ),
        StoreProduct(
            skuId = SKU_COINS_1000,
            title = "1000 Aşk Parası 🪙",
            subtitle = "Tüm VIP temaları ve hikaye çerçevelerini açan mega kasa",
            priceDisplay = "179,99 ₺",
            priceTry = 179.99,
            badge = "EN AVANTAJLI",
            kind = BillingProductKind.CONSUMABLE_COINS,
            coinGrant = 1000
        )
    )

    val featureComparison = listOf(
        PremiumFeatureComparison("Reklam Deneyimi", "Banner & Geçiş Reklamı", "Tamamen Reklamsız", "🚫"),
        PremiumFeatureComparison("Bulut Yedekleme", "En fazla 10 kayıt", "Sınırsız Kapasite (∞)", "☁️"),
        PremiumFeatureComparison("Tasarım Paketleri", "Temel temalar", "Tüm VIP Çerçeve & Temalar", "🎨"),
        PremiumFeatureComparison("AI Aşk Koçu & Rozet", "Standart erişim", "Özel AI + VIP Rozet", "👑")
    )

    val allProducts: List<StoreProduct> = vipSubscriptions + oneTimePacks + coinPacks
}
