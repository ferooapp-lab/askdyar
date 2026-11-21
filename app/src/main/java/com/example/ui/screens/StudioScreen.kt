package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardStyleOptions
import com.example.data.model.FilterPreset
import com.example.data.model.StoryTemplateFrame
import com.example.data.model.StudioTypographyPreset
import com.example.data.model.ThemePreset
import com.example.ui.LoveQuotesViewModel
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.util.SharePlatform
import com.example.util.SocialCardFormat
import com.example.util.SocialCardPalette
import com.example.util.SocialImageShareHelper

@Composable
fun StudioScreen(
    viewModel: LoveQuotesViewModel,
    onOpenStore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val studioState by viewModel.studioState.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()
    val curatedQuotes by viewModel.quotesWithFavorites.collectAsState()
    val savedFavorites by viewModel.savedFavoriteQuotes.collectAsState()
    val communityQuotes by viewModel.approvedCommunityQuotes.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()

    var showEditTextDialog by remember { mutableStateOf(false) }
    var showQuotePickerSheet by remember { mutableStateOf(false) }
    var lockedFramePrompt by remember { mutableStateOf<StoryTemplateFrame?>(null) }
    var lockedThemePrompt by remember { mutableStateOf<ThemePreset?>(null) }
    var bgTabIndex by remember(studioState.backgroundImagePreset) {
        mutableIntStateOf(if (studioState.backgroundImagePreset != null) 1 else 0)
    }

    val stickers = listOf("💖", "🌹", "💍", "✨", "🌙", "💌", "🕊️", "🥂")

    val activeBgImage = studioState.backgroundImagePreset
    val activeTextColor = activeBgImage?.textColor ?: studioState.themePreset.textColor
    val activeAccentColor = activeBgImage?.accentColor ?: studioState.themePreset.accentColor
    val activeStyleTitle = activeBgImage?.name ?: studioState.themePreset.name

    fun renderCurrentPostBitmap(): Bitmap {
        val palette = SocialCardPalette(
            id = activeBgImage?.id ?: studioState.themePreset.id,
            name = activeStyleTitle,
            startColor = studioState.themePreset.primaryColor.toArgb(),
            endColor = studioState.themePreset.secondaryColor.toArgb(),
            accentColor = activeAccentColor.toArgb(),
            textColor = activeTextColor.toArgb()
        )
        val dedication = if (studioState.dedicationName.isNotBlank()) {
            "💌 ${studioState.dedicationName}"
        } else {
            ""
        }
        return SocialImageShareHelper.renderQuoteCardBitmap(
            quoteText = studioState.quoteText,
            author = studioState.author,
            categoryLabel = activeStyleTitle,
            submitterOrDedication = dedication,
            sticker = studioState.sticker,
            palette = palette,
            format = SocialCardFormat.fromId(studioState.cardFormat),
            filterPreset = studioState.filterPreset,
            context = context,
            backgroundImageResId = activeBgImage?.drawableRes,
            imageScrimColor = activeBgImage?.scrimColor?.toArgb(),
            fontResId = studioState.typographyPreset.fontResId,
            isItalicQuote = studioState.typographyPreset.defaultItalic,
            storyFrame = studioState.storyFrame
        )
    }

    fun sharePostTo(platform: SharePlatform) {
        val bitmap = renderCurrentPostBitmap()
        val caption = "✨ \"${studioState.quoteText}\"\n\n— ${studioState.author}\n\n[$activeStyleTitle • Aşka Dair]"
        val res = SocialImageShareHelper.shareBitmapToPlatform(
            context = context,
            bitmap = bitmap,
            shareCaption = caption,
            platform = platform
        )
        res.onSuccess {
            viewModel.notifyVisualCardExported(activity)
        }.onFailure {
            Toast.makeText(
                context,
                "Görsel paylaşımı başlatılamadı: ${it.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
            .testTag("studio_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Görsel Gönderi Oluştur",
                    fontFamily = PlayfairDisplayFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Gradyan veya hazır görsel seç, özel tipografiyle Instagram ve WhatsApp için tasarla",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Format Switcher Bar (Story / Post / Square)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = (studioState.cardFormat == "card"),
                onClick = { viewModel.updateStudioFormat("card") },
                label = { Text("Kart (4:5)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = (studioState.cardFormat == "square"),
                onClick = { viewModel.updateStudioFormat("square") },
                label = { Text("Kare (1:1)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = (studioState.cardFormat == "story"),
                onClick = { viewModel.updateStudioFormat("story") },
                label = { Text("Hikaye (9:16)") },
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Story Templates Row (IG & WhatsApp 9:16 instant shortcuts)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (studioState.cardFormat == "story" && studioState.storyFrame == StoryTemplateFrame.INSTA_STORY_BORDER)
                    Color(0xFFDD2A7B).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color(0xFFDD2A7B).copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.applyStoryTemplatePreset(StoryTemplateFrame.INSTA_STORY_BORDER) }
                    .testTag("studio_preset_ig_story")
            ) {
                Text(
                    text = "📸 IG Hikaye (9:16)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC13584),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (studioState.cardFormat == "story" && studioState.storyFrame == StoryTemplateFrame.WHATSAPP_STATUS)
                    Color(0xFF25D366).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.45f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.applyStoryTemplatePreset(StoryTemplateFrame.WHATSAPP_STATUS) }
                    .testTag("studio_preset_wp_status")
            ) {
                Text(
                    text = "💚 WP Durum (9:16)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF128C7E),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Visual Card Canvas (Preview)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val cardModifier = when (studioState.cardFormat) {
                "story" -> Modifier
                    .fillMaxWidth(0.84f)
                    .aspectRatio(9f / 16f)
                "square" -> Modifier
                    .fillMaxWidth(0.95f)
                    .aspectRatio(1f)
                else -> Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
            }

            ElevatedCard(
                modifier = cardModifier
                    .testTag("visual_card_canvas")
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = studioState.themePreset.primaryColor
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (activeBgImage != null) {
                        Image(
                            painter = painterResource(id = activeBgImage.drawableRes),
                            contentDescription = activeBgImage.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(activeBgImage.scrimColor)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(studioState.themePreset.brush)
                        )
                    }

                    // Filter Overlay Layer
                    FilterEffectOverlay(filterPreset = studioState.filterPreset)

                    // Live Story Template Frame Preview Overlay
                    StoryFramePreviewOverlay(
                        storyFrame = studioState.storyFrame,
                        accentColor = activeAccentColor
                    )

                    val isPolaroid = studioState.storyFrame == StoryTemplateFrame.POLAROID_MEMORY

                    // Inner Frame & Quote Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = if (studioState.storyFrame != StoryTemplateFrame.NONE) 36.dp else 24.dp,
                                bottom = if (isPolaroid) 46.dp else 24.dp
                            ),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = if (studioState.isCentered) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (studioState.dedicationName.isNotBlank()) {
                                Text(
                                    text = "💌 ${studioState.dedicationName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeAccentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (studioState.showQuotesDecoration) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = activeAccentColor.copy(alpha = 0.85f),
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(1.dp))
                            }

                            studioState.sticker?.let { stk ->
                                Text(stk, fontSize = 24.sp)
                            }
                        }

                        Column(
                            horizontalAlignment = if (studioState.isCentered) Alignment.CenterHorizontally else Alignment.Start,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = studioState.quoteText,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = studioState.fontSizeSp.sp,
                                    lineHeight = (studioState.fontSizeSp * 1.38f).sp,
                                    fontFamily = studioState.typographyPreset.toFontFamily(),
                                    fontStyle = studioState.typographyPreset.preferredFontStyle()
                                ),
                                color = activeTextColor,
                                textAlign = if (studioState.isCentered) TextAlign.Center else TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (studioState.isCentered) Alignment.CenterHorizontally else Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(1.dp)
                                        .background(activeAccentColor)
                                )
                                Text(
                                    text = studioState.author,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = activeAccentColor,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aşka Dair",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeTextColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Pick Quote / Edit Text / Alignment Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showQuotePickerSheet = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("studio_pick_quote_button")
            ) {
                Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Söz Seç", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            OutlinedButton(
                onClick = { showEditTextDialog = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Düzenle", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            OutlinedButton(
                onClick = { viewModel.toggleStudioCentered() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatAlignLeft,
                    contentDescription = "Hizala",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Mode Selector: Preset Gradients vs Preset Images
        Text(
            text = "Arka Plan Koleksiyonu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = bgTabIndex == 0,
                onClick = {
                    bgTabIndex = 0
                    viewModel.updateStudioBackgroundImage(null)
                },
                label = { Text("🎨 Gradyanlar") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("studio_bg_tab_gradients")
            )
            FilterChip(
                selected = bgTabIndex == 1,
                onClick = {
                    bgTabIndex = 1
                    if (studioState.backgroundImagePreset == null) {
                        viewModel.updateStudioBackgroundImage(CardStyleOptions.backgroundImages.first())
                    }
                },
                label = { Text("🖼️ Hazır Görseller") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("studio_bg_tab_images")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (bgTabIndex == 0) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(CardStyleOptions.themePresets) { preset ->
                    val isSelected = (studioState.backgroundImagePreset == null && studioState.themePreset.id == preset.id)
                    val hasThemeAccess = entitlementState.hasAccessToStudioTheme(preset)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (hasThemeAccess) {
                                    viewModel.updateStudioTheme(preset)
                                } else {
                                    lockedThemePrompt = preset
                                }
                            }
                            .padding(4.dp)
                            .testTag("studio_theme_${preset.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(preset.brush)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!hasThemeAccess) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.38f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "VIP Tasarım Paketi",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = preset.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!hasThemeAccess) {
                            Text(
                                text = "VIP ${preset.coinCost}🪙",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(CardStyleOptions.backgroundImages) { imgPreset ->
                    val isSelected = studioState.backgroundImagePreset?.id == imgPreset.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.updateStudioBackgroundImage(imgPreset) }
                            .padding(4.dp)
                            .testTag("studio_bg_image_${imgPreset.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 74.dp, height = 92.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = imgPreset.drawableRes),
                                contentDescription = imgPreset.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seçili",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = imgPreset.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hikaye & Durum Çerçeve Şablonları (IG / WP / Polaroid / VIP)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hikaye & Durum Çerçeve Şablonları",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "🪙 ${entitlementState.loveCoins}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenStore)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.testTag("studio_story_frames_row")
        ) {
            items(StoryTemplateFrame.values()) { frame ->
                val isSelected = studioState.storyFrame == frame
                val hasAccess = entitlementState.hasAccessToFrame(frame)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (hasAccess) {
                                viewModel.updateStudioStoryFrame(frame)
                            } else {
                                lockedFramePrompt = frame
                            }
                        }
                        .testTag("studio_frame_${frame.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!hasAccess) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kilitli VIP Çerçeve",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(13.dp)
                                )
                            } else if (frame.isPremium) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "VIP",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = frame.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (!hasAccess) frame.badge else if (frame.isPremium) "Açık ✨" else frame.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!hasAccess) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Typography Selector
        Text(
            text = "Özel Tipografi Stili",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(StudioTypographyPreset.values()) { typo ->
                val isSelected = studioState.typographyPreset == typo
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.updateStudioTypography(typo) }
                        .testTag("studio_font_${typo.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aa Aşk",
                            fontFamily = typo.toFontFamily(),
                            fontStyle = typo.preferredFontStyle(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = typo.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Romantic Stickers Row
        Text(
            text = "Romantik Damga & Çıkartma",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stickers) { stk ->
                val isSelected = (studioState.sticker == stk)
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            viewModel.updateStudioSticker(if (isSelected) null else stk)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stk, fontSize = 22.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Estetik Filtreler
        Text(
            text = "Estetik Filtre Seçenekleri",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FilterPreset.values()) { filter ->
                val isSelected = (studioState.filterPreset == filter)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateStudioFilter(filter) },
                    label = { Text(filter.title) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterVintage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Font Boyutu
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Yazı Boyutu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = "${studioState.fontSizeSp.toInt()} sp",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = studioState.fontSizeSp,
                onValueChange = { viewModel.updateStudioFontSize(it) },
                valueRange = 16f..32f,
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Instagram Görsel / WhatsApp Görsel / Genel Paylaşım / Kopyala & Kaydet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { sharePostTo(SharePlatform.INSTAGRAM) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("studio_instagram_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC13584))
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Instagram", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { sharePostTo(SharePlatform.WHATSAPP) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("studio_whatsapp_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { sharePostTo(SharePlatform.GENERAL) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("studio_share_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Görsel Gönderiyi Paylaş", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(
                            ClipData.newPlainText(
                                "Aşk Kartı",
                                "\"${studioState.quoteText}\"\n— ${studioState.author}"
                            )
                        )
                        Toast.makeText(context, "Kart metni panoya kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kopyala")
                }

                Button(
                    onClick = {
                        viewModel.saveNote(
                            title = "Özel Kart: ${studioState.author}",
                            content = studioState.quoteText,
                            author = studioState.author,
                            tags = "TasarımKart, $activeStyleTitle",
                            backgroundTheme = studioState.themePreset.id,
                            filterStyle = studioState.filterPreset.id
                        )
                        Toast.makeText(context, "Notlarıma kaydedildi! 💌", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Notlara Kaydet")
                }
            }
        }
    }

    if (showQuotePickerSheet) {
        StudioQuotePickerSheet(
            curatedQuotes = curatedQuotes,
            savedFavorites = savedFavorites,
            communityQuotes = communityQuotes,
            notes = allNotes,
            onSelectQuote = { text, author ->
                viewModel.updateStudioQuote(text, author)
            },
            onDismiss = { showQuotePickerSheet = false }
        )
    }

    if (showEditTextDialog) {
        var editQuote by remember { mutableStateOf(studioState.quoteText) }
        var editAuthor by remember { mutableStateOf(studioState.author) }
        var editDedication by remember { mutableStateOf(studioState.dedicationName) }

        AlertDialog(
            onDismissRequest = { showEditTextDialog = false },
            title = { Text("Kart Metnini & İthafı Düzenle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editQuote,
                        onValueChange = { editQuote = it },
                        label = { Text("Aşk Sözü") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = editAuthor,
                        onValueChange = { editAuthor = it },
                        label = { Text("Yazar / İmza") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDedication,
                        onValueChange = { editDedication = it },
                        label = { Text("Kime İthafen? (İsteğe Bağlı)") },
                        placeholder = { Text("Örn: Canım Ayşe'ye...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStudioQuote(editQuote, editAuthor)
                        viewModel.updateStudioDedication(editDedication)
                        showEditTextDialog = false
                    }
                ) {
                    Text("Uygula")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTextDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    lockedFramePrompt?.let { lockedFrame ->
        AlertDialog(
            onDismissRequest = { lockedFramePrompt = null },
            icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFD97706)) },
            title = { Text("${lockedFrame.title} Şablonu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = lockedFrame.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Bakiyen: 🪙 ${entitlementState.loveCoins} Aşk Parası • Gereken: 🪙 ${lockedFrame.coinCost}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unlockStoryFrameWithCoins(lockedFrame) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                lockedFramePrompt = null
                            }
                        }
                    }
                ) {
                    Text("${lockedFrame.coinCost} 🪙 ile Aç")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            lockedFramePrompt = null
                            onOpenStore()
                        }
                    ) {
                        Text("Coin / VIP Al")
                    }
                    TextButton(onClick = { lockedFramePrompt = null }) {
                        Text("Kapat")
                    }
                }
            }
        )
    }

    lockedThemePrompt?.let { lockedTheme ->
        AlertDialog(
            onDismissRequest = { lockedThemePrompt = null },
            icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFD97706)) },
            title = { Text("${lockedTheme.name} Tasarım Paketi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bu lüks stüdyo renk teması Özel Tasarım Paketi veya Premium Üyelik ile sınırsız açılır; dilersen Aşk Parası ile de kalıcı açabilirsin.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Bakiyen: 🪙 ${entitlementState.loveCoins} • Gereken: 🪙 ${lockedTheme.coinCost}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unlockStudioThemeWithCoins(lockedTheme) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                lockedThemePrompt = null
                            }
                        }
                    }
                ) {
                    Text("${lockedTheme.coinCost} 🪙 ile Aç")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            lockedThemePrompt = null
                            onOpenStore()
                        }
                    ) {
                        Text("Premium / Paket Al")
                    }
                    TextButton(onClick = { lockedThemePrompt = null }) {
                        Text("Kapat")
                    }
                }
            }
        )
    }
}

@Composable
fun StoryFramePreviewOverlay(
    storyFrame: StoryTemplateFrame,
    accentColor: Color
) {
    when (storyFrame) {
        StoryTemplateFrame.NONE -> Unit
        StoryTemplateFrame.INSTA_STORY_BORDER -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDD2A7B),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "📸 GÜNÜN HİKAYESİ",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
        StoryTemplateFrame.WHATSAPP_STATUS -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(3.dp, Color(0xFF25D366), RoundedCornerShape(18.dp))
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF128C7E),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "💚 DURUM PAYLAŞIMI",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
        StoryTemplateFrame.POLAROID_MEMORY -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(12.dp, Color(0xFFFDFBF7), RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFFFDFBF7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kalpten Bir Anı • Aşka Dair ♥",
                        color = Color(0xFF3E1326),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        StoryTemplateFrame.GOLD_ROYAL_FRAME -> {
            val gold = Color(0xFFFFD54F)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(2.5.dp, gold, RoundedCornerShape(18.dp))
                    .padding(5.dp)
                    .border(1.dp, gold.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = "✦ ÖZEL KOLEKSİYON ✦",
                    color = gold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                )
            }
        }
        StoryTemplateFrame.EDITORIAL_MAGAZINE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .border(1.5.dp, accentColor, RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ROMANCE • EDITION",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(accentColor.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

@Composable
fun FilterEffectOverlay(filterPreset: FilterPreset) {
    when (filterPreset) {
        FilterPreset.NORMAL -> Unit
        FilterPreset.WARM_GLOW -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.25f),
                                Color(0xFFFF8A65).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        FilterPreset.ROSE_BLOOM -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF80AB).copy(alpha = 0.25f),
                                Color(0xFFE91E63).copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        FilterPreset.VINTAGE_SEPIA -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF704214).copy(alpha = 0.30f))
            )
        }
        FilterPreset.NOIR -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }
        FilterPreset.MIDNIGHT_VELVET -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF80DEEA).copy(alpha = 0.2f),
                                Color(0xFF1A237E).copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        }
    }
}
