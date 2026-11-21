package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

enum class AppDesignTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryPreview: Color,
    val accentPreview: Color,
    val bgPreview: Color
) {
    RUBY_VELVET(
        id = "ruby_velvet",
        title = "Romantik Kırmızı",
        subtitle = "Tutkulu kırmızı, yakut kadife ve şampanya ışıltısı",
        primaryPreview = Color(0xFFB71C1C),
        accentPreview = Color(0xFFFFD54F),
        bgPreview = Color(0xFFFFF8F9)
    ),
    MIDNIGHT_INDIGO(
        id = "midnight_indigo",
        title = "Gece Yarısı Mavisi",
        subtitle = "Derin gece mavisi ve yıldızlı ay ışığı ferahlığı",
        primaryPreview = Color(0xFF283593),
        accentPreview = Color(0xFF80DEEA),
        bgPreview = Color(0xFFF4F6FC)
    ),
    BLUSH_PEONY(
        id = "blush_peony",
        title = "Pastel Pembe",
        subtitle = "Yumuşak pastel pembe, gül kurusu ve pudra dokusu",
        primaryPreview = Color(0xFFEC407A),
        accentPreview = Color(0xFFFCE4EC),
        bgPreview = Color(0xFFFFF5F8)
    ),
    LAVENDER_TWILIGHT(
        id = "lavender_twilight",
        title = "Lavanta Rüyası",
        subtitle = "Büyülü eflatun, zarif mor ve romantik akşam tonu",
        primaryPreview = Color(0xFF6A1B9A),
        accentPreview = Color(0xFFF48FB1),
        bgPreview = Color(0xFFFAF5FF)
    ),
    EMERALD_ROMANCE(
        id = "emerald_romance",
        title = "Zümrüt & Kehribar",
        subtitle = "Asil zümrüt yeşili ve sıcak amber parıltısı",
        primaryPreview = Color(0xFF00695C),
        accentPreview = Color(0xFFFFD54F),
        bgPreview = Color(0xFFF2FBF9)
    ),
    ESPRESSO_CREAM(
        id = "espresso_cream",
        title = "Fildişi & Espresso",
        subtitle = "Sıcak fildişi ve karamel esintili zarif kahve tonu",
        primaryPreview = Color(0xFF4E342E),
        accentPreview = Color(0xFFFFE082),
        bgPreview = Color(0xFFFCFBF8)
    );

    companion object {
        fun fromId(id: String): AppDesignTheme {
            return values().find { it.id == id } ?: RUBY_VELVET
        }
    }
}

// 1. Romantik Kırmızı (Ruby Velvet)
private val RubyLightScheme = lightColorScheme(
    primary = RubyPrimaryLight,
    onPrimary = RubyOnPrimaryLight,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = RubySecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE4EC),
    onSecondaryContainer = Color(0xFF560027),
    tertiary = RubyTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3CD),
    onTertiaryContainer = Color(0xFF3D2700),
    background = RubyBackgroundLight,
    surface = RubySurfaceLight,
    surfaceVariant = RubySurfaceVariantLight,
    onBackground = Color(0xFF261019),
    onSurface = Color(0xFF261019),
    onSurfaceVariant = Color(0xFF654E57)
)

private val RubyDarkScheme = darkColorScheme(
    primary = RubyPrimaryDark,
    onPrimary = RubyOnPrimaryDark,
    primaryContainer = Color(0xFF6A0B3B),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = RubySecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4F132C),
    onSecondaryContainer = Color(0xFFFFD9E6),
    tertiary = RubyTertiaryDark,
    onTertiary = Color(0xFF3D2800),
    tertiaryContainer = Color(0xFF5A3E00),
    onTertiaryContainer = Color(0xFFFFE082),
    background = RubyBackgroundDark,
    surface = RubySurfaceDark,
    surfaceVariant = RubySurfaceVariantDark,
    onBackground = Color(0xFFFCE4EC),
    onSurface = Color(0xFFFCE4EC),
    onSurfaceVariant = Color(0xFFD2B2BE)
)

// 2. Gece Yarısı Mavisi (Midnight Indigo)
private val MidnightLightScheme = lightColorScheme(
    primary = MidnightPrimaryLight,
    onPrimary = MidnightOnPrimaryLight,
    primaryContainer = Color(0xFFDCE1FF),
    onPrimaryContainer = Color(0xFF0A145A),
    secondary = MidnightSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EAF6),
    onSecondaryContainer = Color(0xFF1A237E),
    tertiary = MidnightTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD0F0F5),
    onTertiaryContainer = Color(0xFF00363D),
    background = MidnightBackgroundLight,
    surface = MidnightSurfaceLight,
    surfaceVariant = MidnightSurfaceVariantLight,
    onBackground = Color(0xFF131728),
    onSurface = Color(0xFF131728),
    onSurfaceVariant = Color(0xFF4E5676)
)

private val MidnightDarkScheme = darkColorScheme(
    primary = MidnightPrimaryDark,
    onPrimary = MidnightOnPrimaryDark,
    primaryContainer = Color(0xFF1E2970),
    onPrimaryContainer = Color(0xFFDCE1FF),
    secondary = MidnightSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF232F60),
    onSecondaryContainer = Color(0xFFE8EAF6),
    tertiary = MidnightTertiaryDark,
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F58),
    onTertiaryContainer = Color(0xFFB2EBF2),
    background = MidnightBackgroundDark,
    surface = MidnightSurfaceDark,
    surfaceVariant = MidnightSurfaceVariantDark,
    onBackground = Color(0xFFE8EAF6),
    onSurface = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFFB0B9D6)
)

// 3. Pastel Pembe (Blush Peony)
private val PeonyLightScheme = lightColorScheme(
    primary = Color(0xFFD81B60),
    onPrimary = PeonyOnPrimaryLight,
    primaryContainer = Color(0xFFFFE0EB),
    onPrimaryContainer = Color(0xFF4A0020),
    secondary = Color(0xFFF06292),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE4EC),
    onSecondaryContainer = Color(0xFF5E1132),
    tertiary = PeonyTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBEE),
    onTertiaryContainer = Color(0xFF4B0C20),
    background = Color(0xFFFFF6F9),
    surface = PeonySurfaceLight,
    surfaceVariant = Color(0xFFFCE8F0),
    onBackground = Color(0xFF2B101D),
    onSurface = Color(0xFF2B101D),
    onSurfaceVariant = Color(0xFF724F60)
)

private val PeonyDarkScheme = darkColorScheme(
    primary = PeonyPrimaryDark,
    onPrimary = PeonyOnPrimaryDark,
    primaryContainer = Color(0xFF7A1845),
    onPrimaryContainer = Color(0xFFFFE0EB),
    secondary = PeonySecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4D1833),
    onSecondaryContainer = Color(0xFFFDE8ED),
    tertiary = PeonyTertiaryDark,
    onTertiary = Color(0xFF4B0C20),
    tertiaryContainer = Color(0xFF631E3A),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = PeonyBackgroundDark,
    surface = PeonySurfaceDark,
    surfaceVariant = PeonySurfaceVariantDark,
    onBackground = Color(0xFFFDE8ED),
    onSurface = Color(0xFFFDE8ED),
    onSurfaceVariant = Color(0xFFDCB7C7)
)

// 4. Lavanta Rüyası (Lavender Twilight)
private val LavenderLightScheme = lightColorScheme(
    primary = LavenderPrimaryLight,
    onPrimary = LavenderOnPrimaryLight,
    primaryContainer = Color(0xFFF0D9FF),
    onPrimaryContainer = Color(0xFF330052),
    secondary = LavenderSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = Color(0xFF4A148C),
    tertiary = LavenderTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE4EC),
    onTertiaryContainer = Color(0xFF560027),
    background = LavenderBackgroundLight,
    surface = LavenderSurfaceLight,
    surfaceVariant = LavenderSurfaceVariantLight,
    onBackground = Color(0xFF241133),
    onSurface = Color(0xFF241133),
    onSurfaceVariant = Color(0xFF624A75)
)

private val LavenderDarkScheme = darkColorScheme(
    primary = LavenderPrimaryDark,
    onPrimary = LavenderOnPrimaryDark,
    primaryContainer = Color(0xFF4E1278),
    onPrimaryContainer = Color(0xFFF0D9FF),
    secondary = LavenderSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3A1A5A),
    onSecondaryContainer = Color(0xFFF3E5F5),
    tertiary = LavenderTertiaryDark,
    onTertiary = Color(0xFF490024),
    tertiaryContainer = Color(0xFF611942),
    onTertiaryContainer = Color(0xFFFFD9E6),
    background = LavenderBackgroundDark,
    surface = LavenderSurfaceDark,
    surfaceVariant = LavenderSurfaceVariantDark,
    onBackground = Color(0xFFF3E5F5),
    onSurface = Color(0xFFF3E5F5),
    onSurfaceVariant = Color(0xFFCBB1DC)
)

// 5. Zümrüt & Kehribar (Emerald Romance)
private val EmeraldLightScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = Color(0xFFC8EFE8),
    onPrimaryContainer = Color(0xFF002923),
    secondary = EmeraldSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF00332C),
    tertiary = EmeraldTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3D6),
    onTertiaryContainer = Color(0xFF422700),
    background = EmeraldBackgroundLight,
    surface = EmeraldSurfaceLight,
    surfaceVariant = EmeraldSurfaceVariantLight,
    onBackground = Color(0xFF0F2622),
    onSurface = Color(0xFF0F2622),
    onSurfaceVariant = Color(0xFF43655F)
)

private val EmeraldDarkScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = Color(0xFF004F45),
    onPrimaryContainer = Color(0xFFC8EFE8),
    secondary = EmeraldSecondaryDark,
    onSecondary = Color(0xFF002923),
    secondaryContainer = Color(0xFF133A34),
    onSecondaryContainer = Color(0xFFE0F2F1),
    tertiary = EmeraldTertiaryDark,
    onTertiary = Color(0xFF3E2400),
    tertiaryContainer = Color(0xFF563700),
    onTertiaryContainer = Color(0xFFFFE082),
    background = EmeraldBackgroundDark,
    surface = EmeraldSurfaceDark,
    surfaceVariant = EmeraldSurfaceVariantDark,
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1),
    onSurfaceVariant = Color(0xFFA8CFC9)
)

// 6. Fildişi & Espresso (Espresso Cream)
private val EspressoLightScheme = lightColorScheme(
    primary = EspressoPrimaryLight,
    onPrimary = EspressoOnPrimaryLight,
    primaryContainer = Color(0xFFEDDFD9),
    onPrimaryContainer = Color(0xFF2B1B15),
    secondary = EspressoSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFEBE9),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = EspressoTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEFC2),
    onTertiaryContainer = Color(0xFF3B2100),
    background = EspressoBackgroundLight,
    surface = EspressoSurfaceLight,
    surfaceVariant = EspressoSurfaceVariantLight,
    onBackground = Color(0xFF261D19),
    onSurface = Color(0xFF261D19),
    onSurfaceVariant = Color(0xFF655650)
)

private val EspressoDarkScheme = darkColorScheme(
    primary = EspressoPrimaryDark,
    onPrimary = EspressoOnPrimaryDark,
    primaryContainer = Color(0xFF4A352E),
    onPrimaryContainer = Color(0xFFEDDFD9),
    secondary = EspressoSecondaryDark,
    onSecondary = Color(0xFF271B17),
    secondaryContainer = Color(0xFF342722),
    onSecondaryContainer = Color(0xFFEFEBE9),
    tertiary = EspressoTertiaryDark,
    onTertiary = Color(0xFF3A2400),
    tertiaryContainer = Color(0xFF543800),
    onTertiaryContainer = Color(0xFFFFE082),
    background = EspressoBackgroundDark,
    surface = EspressoSurfaceDark,
    surfaceVariant = EspressoSurfaceVariantDark,
    onBackground = Color(0xFFEFEBE9),
    onSurface = Color(0xFFEFEBE9),
    onSurfaceVariant = Color(0xFFD2C4BD)
)

@Composable
private fun animateSchemeColor(target: Color): Color {
    val animated by animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 360),
        label = "theme_color_transition"
    )
    return animated
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    designTheme: AppDesignTheme = AppDesignTheme.RUBY_VELVET,
    content: @Composable () -> Unit
) {
    val targetScheme: ColorScheme = when (designTheme) {
        AppDesignTheme.RUBY_VELVET -> if (darkTheme) RubyDarkScheme else RubyLightScheme
        AppDesignTheme.MIDNIGHT_INDIGO -> if (darkTheme) MidnightDarkScheme else MidnightLightScheme
        AppDesignTheme.BLUSH_PEONY -> if (darkTheme) PeonyDarkScheme else PeonyLightScheme
        AppDesignTheme.LAVENDER_TWILIGHT -> if (darkTheme) LavenderDarkScheme else LavenderLightScheme
        AppDesignTheme.EMERALD_ROMANCE -> if (darkTheme) EmeraldDarkScheme else EmeraldLightScheme
        AppDesignTheme.ESPRESSO_CREAM -> if (darkTheme) EspressoDarkScheme else EspressoLightScheme
    }

    val animatedColorScheme = targetScheme.copy(
        primary = animateSchemeColor(targetScheme.primary),
        onPrimary = animateSchemeColor(targetScheme.onPrimary),
        primaryContainer = animateSchemeColor(targetScheme.primaryContainer),
        onPrimaryContainer = animateSchemeColor(targetScheme.onPrimaryContainer),
        secondary = animateSchemeColor(targetScheme.secondary),
        onSecondary = animateSchemeColor(targetScheme.onSecondary),
        secondaryContainer = animateSchemeColor(targetScheme.secondaryContainer),
        onSecondaryContainer = animateSchemeColor(targetScheme.onSecondaryContainer),
        tertiary = animateSchemeColor(targetScheme.tertiary),
        onTertiary = animateSchemeColor(targetScheme.onTertiary),
        tertiaryContainer = animateSchemeColor(targetScheme.tertiaryContainer),
        onTertiaryContainer = animateSchemeColor(targetScheme.onTertiaryContainer),
        background = animateSchemeColor(targetScheme.background),
        onBackground = animateSchemeColor(targetScheme.onBackground),
        surface = animateSchemeColor(targetScheme.surface),
        onSurface = animateSchemeColor(targetScheme.onSurface),
        surfaceVariant = animateSchemeColor(targetScheme.surfaceVariant),
        onSurfaceVariant = animateSchemeColor(targetScheme.onSurfaceVariant)
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
