package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LoveQuote
import com.example.data.model.QuoteSection
import com.example.ui.LoveQuotesViewModel
import com.example.ui.components.AdBannerCard
import com.example.ui.theme.EditorialQuoteFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToNewNote: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToLoveTools: () -> Unit,
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToAnnouncements: () -> Unit = {},
    onNavigateToAdminContact: () -> Unit = {},
    onOpenThemePicker: () -> Unit = {},
    onOpenMusicSheet: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quotes by viewModel.quotesWithFavorites.collectAsState()
    val savedFavoriteQuotes by viewModel.savedFavoriteQuotes.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val activeSection by viewModel.activeSection.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAmbientPlaying by viewModel.isAmbientPlaying.collectAsState()
    val activeMusicTitle by viewModel.currentMusicTrackTitle.collectAsState()
    val speakingQuoteId by viewModel.speakingQuoteId.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()
    val pendingCommunityCount by viewModel.pendingCommunityQuotesCount.collectAsState()
    val unreadBroadcastCount by viewModel.unreadBroadcastCount.collectAsState()
    val engagementNotifications by viewModel.engagementNotifications.collectAsState()
    val unreadEngagementCount by viewModel.unreadEngagementCount.collectAsState()
    val activeUserAccount by viewModel.activeUserAccount.collectAsState()
    val loveStreak by viewModel.loveStreak.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()
    val spotlightQuote by viewModel.spotlightQuote.collectAsState()
    val publishedChatCount by viewModel.publishedChatCount.collectAsState()

    val totalFavoritesCount = remember(quotes, savedFavoriteQuotes, allNotes) {
        quotes.count { it.isFavorite } + savedFavoriteQuotes.size + allNotes.count { it.isFavorite }
    }

    var selectedSubCategory by remember { mutableStateOf<String?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showNotificationCenterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(activeSection) {
        selectedSubCategory = null
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Günaydın ☀️"
            in 12..17 -> "Mutlu Günler ✨"
            in 18..22 -> "İyi Akşamlar 🌇"
            else -> "İyi Geceler 🌙"
        }
    }

    val sectionQuotes = remember(quotes, activeSection) {
        quotes.filter { it.section == activeSection }
    }

    val availableSubCategories = remember(sectionQuotes) {
        sectionQuotes.map { it.subCategory }.filter { it.isNotBlank() }.distinct()
    }

    val filteredQuotes = remember(sectionQuotes, selectedSubCategory, showOnlyFavorites, searchQuery) {
        sectionQuotes.filter { quote ->
            val matchesSubCat = (selectedSubCategory == null || quote.subCategory == selectedSubCategory)
            val matchesFav = !showOnlyFavorites || quote.isFavorite
            val matchesSearch = searchQuery.isBlank() ||
                quote.text.contains(searchQuery, ignoreCase = true) ||
                quote.author.contains(searchQuery, ignoreCase = true) ||
                quote.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesSubCat && matchesFav && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Sleek Modern Editorial Top Panel
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                                    Color.Transparent
                                )
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .testTag("dashboard_modern_top_panel"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_romantic_banner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.14f,
                        modifier = Modifier.matchParentSize()
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_aska_dair_emblem),
                                        contentDescription = "Aşka Dair",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = greeting,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onNavigateToLoveTools() }
                                        ) {
                                            Text(
                                                text = "🔥 $loveStreak Gün",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Aşka Dair",
                                        fontFamily = PlayfairDisplayFontFamily,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                        // Compact Modern Action Cluster (48dp touch targets)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenMusicSheet,
                                modifier = Modifier.testTag("dashboard_music_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isAmbientPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isAmbientPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                            contentDescription = "Fon Müziği Stüdyosu",
                                            tint = if (isAmbientPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showNotificationCenterSheet = true },
                                modifier = Modifier.testTag("dashboard_notification_center_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (unreadEngagementCount > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Notifications,
                                                contentDescription = "Etkileşim Bildirim Merkezi",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                    if (unreadEngagementCount > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 2.dp, y = (-2).dp)
                                                .size(16.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = unreadEngagementCount.coerceAtMost(9).toString(),
                                                    color = MaterialTheme.colorScheme.onError,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = onNavigateToAnnouncements,
                                modifier = Modifier.testTag("dashboard_announcements_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Campaign,
                                                contentDescription = "Duyurular",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                    if (unreadBroadcastCount > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 2.dp, y = (-2).dp)
                                                .size(16.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = unreadBroadcastCount.coerceAtMost(9).toString(),
                                                    color = MaterialTheme.colorScheme.onError,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = onOpenThemePicker,
                                modifier = Modifier.testTag("dashboard_theme_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = "Renk Paleti",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.testTag("dashboard_settings_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "Ayarlar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Bar inside Top Panel: User Profile Chip + Quick Favorites / Admin
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onNavigateToProfile() }
                                .testTag("dashboard_profile_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (sessionState.isAdmin) Icons.Default.Shield else Icons.Default.Person,
                                    contentDescription = "Hesap",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when {
                                        sessionState.isAdmin -> "Yönetici Paneli"
                                        else -> sessionState.displayName
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onNavigateToFavorites() }
                                .testTag("dashboard_favorites_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorilerim",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Favoriler ($totalFavoritesCount)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                }
            }
        }

        // Optional Live Admin Announcement Ticker
        if (adminSettings.announcementBannerText.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onNavigateToAnnouncements() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = adminSettings.announcementBannerText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Live Playing Music Mini-Bar
        if (isAmbientPlaying) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onOpenMusicSheet() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Çalıyor: $activeMusicTitle",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { viewModel.toggleAmbientMusic() }) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Müziği Durdur",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 1.5 Sleek Quick Access Action Strip (Community, Music Studio, Themes, Announcements, Reminders, Contact Admin)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    QuickActionPill(
                        label = if (entitlementState.isVip) "👑 VIP Aktif" else "👑 VIP / 🪙 ${entitlementState.loveCoins}",
                        onClick = onNavigateToStore,
                        isHighlighted = true,
                        testTag = "quick_pill_store"
                    )
                }
                item {
                    QuickActionPill(
                        label = "📖 Duygusal Günlük",
                        onClick = onNavigateToJournal,
                        isHighlighted = true,
                        testTag = "quick_pill_journal"
                    )
                }
                item {
                    QuickActionPill(
                        label = "👤 Profilim",
                        onClick = onNavigateToProfile,
                        isHighlighted = true,
                        testTag = "quick_pill_profile"
                    )
                }
                item {
                    QuickActionPill(
                        label = "🖼️ Görsel Gönderi",
                        onClick = onNavigateToStudio,
                        isHighlighted = true,
                        testTag = "quick_pill_visual_post"
                    )
                }
                item {
                    val chatLabel = if (publishedChatCount > 0) "🗣️ Sohbet ($publishedChatCount)" else "🗣️ Sohbet Odası"
                    QuickActionPill(
                        label = chatLabel,
                        onClick = onNavigateToChat,
                        isHighlighted = true,
                        testTag = "quick_pill_chat"
                    )
                }
                item {
                    val commLabel = if (pendingCommunityCount > 0) "💬 Söz Paylaş ($pendingCommunityCount)" else "💬 Söz Paylaş"
                    QuickActionPill(
                        label = commLabel,
                        onClick = onNavigateToCommunity,
                        isHighlighted = true,
                        testTag = "quick_pill_community"
                    )
                }
                item {
                    QuickActionPill(
                        label = "🎵 Fon Müziğim",
                        onClick = onOpenMusicSheet,
                        isHighlighted = true,
                        testTag = "quick_pill_music"
                    )
                }
                item {
                    QuickActionPill(
                        label = "🎨 Renk Paleti",
                        onClick = onOpenThemePicker,
                        testTag = "quick_pill_themes"
                    )
                }
                item {
                    QuickActionPill(
                        label = "⏰ Hatırlatıcı",
                        onClick = onNavigateToReminders,
                        testTag = "quick_pill_reminders"
                    )
                }
                item {
                    QuickActionPill(
                        label = "📩 Admine Yaz",
                        onClick = onNavigateToAdminContact,
                        testTag = "quick_pill_admin_contact"
                    )
                }
                if (sessionState.isAdmin) {
                    item {
                        QuickActionPill(
                            label = "🛡️ Yönetici",
                            onClick = onNavigateToAdmin,
                            testTag = "quick_pill_admin"
                        )
                    }
                }
            }
        }

        // Active User Restriction Banner (if restricted by Admin)
        activeUserAccount?.let { user ->
            val now = System.currentTimeMillis()
            val isRestrictedNow = user.isRestricted && (user.restrictedUntil == null || user.restrictedUntil > now)
            if (isRestrictedNow) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("dashboard_user_restriction_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "⚠️ Hesabınızda Yönetici Kısıtlaması Var",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            val restrictedParts = buildList {
                                if (!user.canSubmitQuotes) add("Söz Paylaşma")
                                if (!user.canComment) add("Yorum Yapma")
                                if (!user.canUploadMusic) add("Özel Müzik")
                                if (!user.canChat) add("Sohbet")
                            }
                            val reasonText = if (user.restrictionReason.isNotBlank()) " • Sebep: ${user.restrictionReason}" else ""
                            Text(
                                text = "Kısıtlı: ${restrictedParts.joinToString(", ").ifBlank { "Genel" }}$reasonText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 1.75 In-App Engagement Notification Center Card (Likes, Comments, Approvals)
        item {
            DashboardNotificationCenterCard(
                notifications = engagementNotifications,
                unreadCount = unreadEngagementCount,
                onNotificationClick = { item ->
                    viewModel.markEngagementNotificationRead(item.id)
                    onNavigateToCommunity()
                },
                onMarkAllRead = {
                    viewModel.markAllEngagementNotificationsRead()
                },
                onOpenFullCenter = {
                    showNotificationCenterSheet = true
                }
            )
        }

        // 2. Hero Card — "GÜNÜN SEÇİLENİ" (Clean Editorial Gradient Surface)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .testTag("hero_section_quote_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Decorative quote watermark
                    Text(
                        text = "“",
                        fontSize = 84.sp,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = (-14).dp)
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(activeSection.emoji, fontSize = 14.sp)
                                    Text(
                                        text = "GÜNÜN SEÇİLENİ • ${activeSection.title.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.pickRandomDailyQuote()
                                    Toast.makeText(context, "Yeni söz seçildi ✨", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Rastgele Çek",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AnimatedContent(
                            targetState = dailyQuote,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(320)) + slideInVertically(animationSpec = tween(320)) { h -> h / 3 })
                                    .togetherWith(fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { h -> -h / 3 })
                            },
                            label = "daily_quote_card_transition"
                        ) { targetQuote ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "\"${targetQuote.text}\"",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 18.sp,
                                        lineHeight = 28.sp
                                    ),
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = EditorialQuoteFontFamily,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "— ${targetQuote.author}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val sendText = "\"${dailyQuote.text}\"\n\n— ${dailyQuote.author}"
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(sendText)}")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, sendText)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Paylaş"))
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "WhatsApp ile Gönder",
                                        tint = Color(0xFF25D366)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("Aşk Sözü", "\"${dailyQuote.text}\"\n— ${dailyQuote.author}"))
                                        Toast.makeText(context, "Söz kopyalandı 💌", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Kopyala",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, activeSection.title)
                                            putExtra(Intent.EXTRA_TEXT, "\"${dailyQuote.text}\"\n\n— ${dailyQuote.author}")
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Paylaş"))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Paylaş",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleQuoteFavorite(dailyQuote.id) }
                                ) {
                                    Icon(
                                        imageVector = if (dailyQuote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favoriye Ekle",
                                        tint = if (dailyQuote.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val isHeroSpeaking = speakingQuoteId == "hero_${dailyQuote.id}"
                                IconButton(
                                    onClick = {
                                        viewModel.toggleSpeakQuote("hero_${dailyQuote.id}", dailyQuote.text, dailyQuote.author)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isHeroSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                        contentDescription = if (isHeroSpeaking) "Sesli Okumayı Durdur" else "Sesli Dinle",
                                        tint = if (isHeroSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.loadQuoteIntoStudio(dailyQuote)
                                    onNavigateToStudio()
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kart Yap", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Clean Category Pills Selector
        item {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QuoteSection.values()) { sec ->
                    val isSelected = (activeSection == sec)
                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        label = "sectionBg"
                    )
                    val animatedTextColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "sectionText"
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = animatedBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { viewModel.setActiveSection(sec) }
                            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sec.emoji, fontSize = 15.sp)
                            Text(
                                text = sec.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = animatedTextColor
                            )
                        }
                    }
                }
            }
        }

        // 4. Sub-category & Favorites Chips
        item {
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("❤️ Favoriler", fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = (selectedSubCategory == null && !showOnlyFavorites),
                        onClick = {
                            selectedSubCategory = null
                            showOnlyFavorites = false
                        },
                        label = { Text("Tümü (${sectionQuotes.size})", fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                items(availableSubCategories) { subCat ->
                    val count = sectionQuotes.count { it.subCategory == subCat }
                    val isSelected = (selectedSubCategory == subCat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubCategory = if (isSelected) null else subCat },
                        label = { Text("$subCat ($count)", fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        // 5. Sleek Search Input
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("${activeSection.title} içinde ara...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Ara")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("dashboard_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        }

        // AdMob Banner for non-VIP users
        if (entitlementState.shouldShowAds) {
            item {
                AdBannerCard(
                    shouldShowAds = true,
                    onUpgradeClick = onNavigateToStore,
                    onAdFailedNetwork = { viewModel.onAdBannerNetworkFailed() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // 6. Clean Section Title & Counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeSection.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = activeSection.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${filteredQuotes.size} söz",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 7. Airy Quote List
        if (filteredQuotes.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showOnlyFavorites) "Bu kategoride henüz favori sözün yok 💔" else "Aradığınız kriterde söz bulunamadı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredQuotes, key = { it.id }) { quote ->
                QuoteListItem(
                    quote = quote,
                    isSpeaking = speakingQuoteId == quote.id,
                    onToggleSpeak = { viewModel.toggleSpeakQuote(quote.id, quote.text, quote.author) },
                    onToggleFavorite = { viewModel.toggleQuoteFavorite(quote.id) },
                    onOpenInStudio = {
                        viewModel.loadQuoteIntoStudio(quote)
                        onNavigateToStudio()
                    }
                )
            }
        }
    }

    if (showNotificationCenterSheet) {
        EngagementNotificationCenterSheet(
            notifications = engagementNotifications,
            unreadCount = unreadEngagementCount,
            onNotificationClick = { item ->
                viewModel.markEngagementNotificationRead(item.id)
                showNotificationCenterSheet = false
                onNavigateToCommunity()
            },
            onMarkAllRead = { viewModel.markAllEngagementNotificationsRead() },
            onDeleteItem = { id -> viewModel.deleteEngagementNotification(id) },
            onClearAll = { viewModel.clearAllEngagementNotifications() },
            onDismiss = { showNotificationCenterSheet = false }
        )
    }

    spotlightQuote?.let { targetQuote ->
        val isSpotlightFav = quotes.find { it.id == targetQuote.id }?.isFavorite == true
        AlertDialog(
            onDismissRequest = { viewModel.dismissSpotlightQuote() },
            modifier = Modifier.testTag("morning_quote_spotlight_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🌅", fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Günün Sabah Sözü",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${targetQuote.section.title} • ${targetQuote.subCategory.ifBlank { "İlham" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "\"${targetQuote.text}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "— ${targetQuote.author}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleQuoteFavorite(targetQuote.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isSpotlightFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSpotlightFav) "Favori" else "Beğen", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleSpeakQuote(targetQuote.id, targetQuote.text, targetQuote.author) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (speakingQuoteId == targetQuote.id) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dinle", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Sabah Sözü", "\"${targetQuote.text}\"\n— ${targetQuote.author}"))
                                Toast.makeText(context, "Söz kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kopyala", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadQuoteIntoStudio(targetQuote)
                        viewModel.dismissSpotlightQuote()
                        onNavigateToStudio()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("spotlight_open_studio_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kart Tasarla", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSpotlightQuote() }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
fun QuoteListItem(
    quote: LoveQuote,
    isSpeaking: Boolean = false,
    onToggleSpeak: () -> Unit = {},
    onToggleFavorite: () -> Unit,
    onOpenInStudio: () -> Unit
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("quote_card_${quote.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = quote.subCategory.ifBlank { quote.section.title },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (quote.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.5.sp,
                    lineHeight = 25.sp
                ),
                fontFamily = EditorialQuoteFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val sendText = "\"${quote.text}\"\n\n— ${quote.author}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(sendText)}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, sendText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Paylaş"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp ile Gönder",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Aşk Sözü", "\"${quote.text}\"\n— ${quote.author}"))
                            Toast.makeText(context, "Söz panoya kopyalandı ✨", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, quote.section.title)
                                putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\"\n\n— ${quote.author}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Sözü Paylaş"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(onClick = onToggleSpeak) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Sesli Okumayı Durdur" else "Sesli Dinle",
                            tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(onClick = onOpenInStudio) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Stüdyoda Aç",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            if (quote.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quote.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    label: String,
    onClick: () -> Unit,
    isHighlighted: Boolean = false,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(
            1.dp,
            if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}
