package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.R

data class ThemePreset(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val brush: Brush,
    val isPremium: Boolean = false,
    val coinCost: Int = 0
)

data class BackgroundImagePreset(
    val id: String,
    val name: String,
    @DrawableRes val drawableRes: Int,
    val textColor: Color,
    val accentColor: Color,
    val scrimColor: Color
)

enum class StudioTypographyPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    @FontRes val fontResId: Int,
    val defaultItalic: Boolean = false
) {
    PLAYFAIR(
        id = "playfair",
        title = "Playfair Zarif",
        subtitle = "Klasik Editoryal",
        fontResId = R.font.playfair_display,
        defaultItalic = true
    ),
    LORA(
        id = "lora",
        title = "Lora Şiirsel",
        subtitle = "Edebi Mektup",
        fontResId = R.font.lora,
        defaultItalic = false
    ),
    PLUS_JAKARTA(
        id = "plus_jakarta",
        title = "Modern Sans",
        subtitle = "Temiz & Güncel",
        fontResId = R.font.plus_jakarta_sans,
        defaultItalic = false
    ),
    CORMORANT(
        id = "cormorant",
        title = "Cormorant İnci",
        subtitle = "Romantik Kaligrafi",
        fontResId = R.font.cormorant_garamond,
        defaultItalic = true
    );

    fun toFontFamily(): FontFamily = FontFamily(Font(fontResId, FontWeight.Normal))

    fun preferredFontStyle(): FontStyle = if (defaultItalic) FontStyle.Italic else FontStyle.Normal
}

enum class FilterPreset(val id: String, val title: String, val description: String) {
    NORMAL("normal", "Doğal", "Orijinal net renk tonları"),
    WARM_GLOW("warm_glow", "Sıcak Parıltı", "Romantik altın gün batımı ışığı"),
    ROSE_BLOOM("rose_bloom", "Pembe Rüya", "Yumuşak gül kurusu rüyası"),
    VINTAGE_SEPIA("vintage_sepia", "Nostalji", "Eski aşk mektubu tonu"),
    NOIR("noir", "Zarif Noir", "Siyah-beyaz edebi ağırlık"),
    MIDNIGHT_VELVET("midnight_velvet", "Gece Yıldızı", "Karanlık ve gizemli pırıltı")
}

enum class StoryTemplateFrame(
    val id: String,
    val title: String,
    val badge: String,
    val description: String,
    val isPremium: Boolean = false,
    val coinCost: Int = 0
) {
    NONE(
        id = "none",
        title = "Çerçevesiz",
        badge = "Sade",
        description = "Tam ekran temiz görünüm",
        isPremium = false,
        coinCost = 0
    ),
    INSTA_STORY_BORDER(
        id = "ig_story",
        title = "IG Hikaye Şeridi",
        badge = "Instagram",
        description = "Neon çift çerçeve & hikaye rozeti",
        isPremium = false,
        coinCost = 0
    ),
    WHATSAPP_STATUS(
        id = "wp_status",
        title = "WP Durum Kartı",
        badge = "WhatsApp",
        description = "Üst sevgi şeridi & zarif durum kenarlığı",
        isPremium = false,
        coinCost = 0
    ),
    POLAROID_MEMORY(
        id = "polaroid",
        title = "Polaroid Anı",
        badge = "Anı",
        description = "Klasik kalın beyaz çerçeve & alt imza alanı",
        isPremium = false,
        coinCost = 0
    ),
    GOLD_ROYAL_FRAME(
        id = "gold_royal",
        title = "Altın Varak VIP",
        badge = "VIP 30🪙",
        description = "Çift altın çizgili köşe süslemeli lüks çerçeve",
        isPremium = true,
        coinCost = 30
    ),
    EDITORIAL_MAGAZINE(
        id = "editorial_mag",
        title = "Dergi Kapağı VIP",
        badge = "VIP 40🪙",
        description = "Editoryal manşet & lüks kapak çerçevesi",
        isPremium = true,
        coinCost = 40
    );

    companion object {
        fun fromId(id: String): StoryTemplateFrame = values().find { it.id == id } ?: NONE
    }
}

object CardStyleOptions {
    val backgroundImages = listOf(
        BackgroundImagePreset(
            id = "bg_rose_petals",
            name = "Gül Yaprakları",
            drawableRes = R.drawable.img_bg_rose_petals_1794933886890,
            textColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFFFFE4EC),
            scrimColor = Color(0x704A0E24)
        ),
        BackgroundImagePreset(
            id = "bg_starry_night",
            name = "Yıldızlı Gece",
            drawableRes = R.drawable.img_bg_starry_night_1794933889236,
            textColor = Color(0xFFF8FAFC),
            accentColor = Color(0xFF80DEEA),
            scrimColor = Color(0x4D090E1C)
        ),
        BackgroundImagePreset(
            id = "bg_golden_sunset",
            name = "Altın Günbatımı",
            drawableRes = R.drawable.img_bg_golden_sunset_1794933888613,
            textColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFFFFF3B0),
            scrimColor = Color(0x6B431422)
        ),
        BackgroundImagePreset(
            id = "bg_vintage_paper",
            name = "Mektup Kağıdı",
            drawableRes = R.drawable.img_bg_vintage_paper_1794933888391,
            textColor = Color(0xFF2D1B14),
            accentColor = Color(0xFF9E2A48),
            scrimColor = Color(0x26FFFDF7)
        )
    )

    val themePresets = listOf(
        ThemePreset(
            id = "ruby_velvet",
            name = "Yakut Kadife",
            primaryColor = Color(0xFF880E4F),
            secondaryColor = Color(0xFF4A0023),
            textColor = Color(0xFFFFF0F5),
            accentColor = Color(0xFFFFD54F),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF880E4F), Color(0xFF560027), Color(0xFF2E0014))
            )
        ),
        ThemePreset(
            id = "rose_sunset",
            name = "Gül & Günbatımı",
            primaryColor = Color(0xFFE91E63),
            secondaryColor = Color(0xFFFF8A80),
            textColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFFFFF9C4),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE91E63), Color(0xFFFF5252), Color(0xFFFF7043))
            )
        ),
        ThemePreset(
            id = "midnight_dusk",
            name = "Gece Mavisi & Ay",
            primaryColor = Color(0xFF1A237E),
            secondaryColor = Color(0xFF311B92),
            textColor = Color(0xFFE8EAF6),
            accentColor = Color(0xFF80DEEA),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81))
            )
        ),
        ThemePreset(
            id = "lavender_mist",
            name = "Lavanta Fısıltısı",
            primaryColor = Color(0xFF7E57C2),
            secondaryColor = Color(0xFFBA68C8),
            textColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFFF8BBD0),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF5E35B1), Color(0xFF7E57C2), Color(0xFFAB47BC))
            )
        ),
        ThemePreset(
            id = "minimal_cream",
            name = "Fildişi & Espresso",
            primaryColor = Color(0xFFFFF8E7),
            secondaryColor = Color(0xFFF5EBE0),
            textColor = Color(0xFF2C1810),
            accentColor = Color(0xFF8D6E63),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFFBF2), Color(0xFFF8EED9), Color(0xFFE8DAC3))
            )
        ),
        ThemePreset(
            id = "emerald_romance",
            name = "Zümrüt Yeşili",
            primaryColor = Color(0xFF004D40),
            secondaryColor = Color(0xFF00695C),
            textColor = Color(0xFFE0F2F1),
            accentColor = Color(0xFFFFD700),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF004D40), Color(0xFF00332C), Color(0xFF021B16))
            )
        ),
        ThemePreset(
            id = "pure_dark",
            name = "Mat Siyah & Altın",
            primaryColor = Color(0xFF121212),
            secondaryColor = Color(0xFF1E1E1E),
            textColor = Color(0xFFEDEDED),
            accentColor = Color(0xFFFFC107),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212), Color(0xFF000000))
            )
        ),
        ThemePreset(
            id = "royal_gold_silk",
            name = "Kraliyet Altını VIP",
            primaryColor = Color(0xFF2B1B12),
            secondaryColor = Color(0xFF5D3A1A),
            textColor = Color(0xFFFFF8E7),
            accentColor = Color(0xFFFFD54F),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF3E2723), Color(0xFF6D4C2F), Color(0xFF21130D))
            ),
            isPremium = true,
            coinCost = 35
        ),
        ThemePreset(
            id = "aurora_pearl",
            name = "İnci Aurora VIP",
            primaryColor = Color(0xFF311B47),
            secondaryColor = Color(0xFF880E4F),
            textColor = Color(0xFFFFF5FA),
            accentColor = Color(0xFFFFD1DC),
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFFAF7B))
            ),
            isPremium = true,
            coinCost = 35
        )
    )
}
