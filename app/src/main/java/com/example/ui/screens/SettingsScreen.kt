package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LoveQuotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: LoveQuotesViewModel,
    onOpenReminders: () -> Unit = {},
    onOpenCommunity: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenAnnouncements: () -> Unit = {},
    onOpenAdminContact: () -> Unit = {},
    onOpenThemePicker: () -> Unit = {},
    onOpenMusicSheet: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenStore: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionState by viewModel.sessionState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val activeDesignTheme by viewModel.designTheme.collectAsState()
    val isAmbientPlaying by viewModel.isAmbientPlaying.collectAsState()
    val activeMusicTitle by viewModel.currentMusicTrackTitle.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val cloudBackupStatus by viewModel.cloudBackupStatus.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()
    val exportedJson by viewModel.exportedBackupJson.collectAsState()
    val backupMessage by viewModel.backupStatusMessage.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()
    val quotes by viewModel.quotesWithFavorites.collectAsState()
    val savedFavoriteQuotes by viewModel.savedFavoriteQuotes.collectAsState()
    val communityQuotes by viewModel.approvedCommunityQuotes.collectAsState()
    val pendingCount by viewModel.pendingCommunityQuotesCount.collectAsState()
    val unreadBroadcastCount by viewModel.unreadBroadcastCount.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var showWidgetDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val totalFavs = quotes.count { it.isFavorite } + notes.count { it.isFavorite } + savedFavoriteQuotes.size
    val lastSyncStr = remember(cloudBackupStatus.lastBackupTimestamp) {
        if (cloudBackupStatus.lastBackupTimestamp > 0) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")).format(Date(cloudBackupStatus.lastBackupTimestamp))
        } else {
            "Henüz yedekleme yapılmadı"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ayarlar & Hesap",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tema, müzik, hesap ve yedekleme tercihleri",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. Hesap & Oturum Kartı
        item {
            SettingsGroupHeader("Hesap & Oturum")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onOpenProfile() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (sessionState.isAdmin) Icons.Default.Shield else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = sessionState.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = when (sessionState.role) {
                                            "ADMIN" -> "Yönetici"
                                            else -> "Üye"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = when {
                                    sessionState.email.isNotBlank() -> sessionState.email
                                    sessionState.isAdmin -> "Firebase Yönetici hesabı aktif"
                                    else -> "Google ile Firebase buluta bağlı"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.logoutSession() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .testTag("settings_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Çıkış Yap",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Çıkış", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Görünüm & Romantik Fon Müziği
        item {
            SettingsGroupHeader("Görünüm & Ses")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_customizer_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Color Palette Picker Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenThemePicker() }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("open_theme_palette_picker_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SettingsIconBadge(Icons.Default.Palette, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Renk Teması", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${activeDesignTheme.title} • ${activeDesignTheme.subtitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(activeDesignTheme.primaryPreview))
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(activeDesignTheme.accentPreview))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Dark / Light / System compact segmented row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Görünüm Modu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionButton("Sistem", themeMode == "system", { viewModel.setThemeMode("system") }, Modifier.weight(1f))
                            ThemeOptionButton("Açık", themeMode == "light", { viewModel.setThemeMode("light") }, Modifier.weight(1f))
                            ThemeOptionButton("Karanlık", themeMode == "dark", { viewModel.setThemeMode("dark") }, Modifier.weight(1f))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Custom Romantic Background Music Row
                    SettingsActionRow(
                        icon = Icons.Default.MusicNote,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        title = "Romantik Fon Müziği",
                        subtitle = if (isAmbientPlaying) "Çalıyor: $activeMusicTitle 🎵" else "Cihazdan müzik ekle veya melodi seç",
                        badgeText = if (isAmbientPlaying) "Açık" else null,
                        onClick = onOpenMusicSheet,
                        modifier = Modifier.testTag("settings_music_studio_row")
                    )
                }
            }
        }

        // 3. Kütüphane, Bildirim & Topluluk
        item {
            SettingsGroupHeader("Kütüphane & Topluluk")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsActionRow(
                        icon = Icons.Default.Favorite,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Favorilerim Kütüphanesi",
                        subtitle = "$totalFavs kayıtlı aşk sözü, AI üretimi ve not",
                        onClick = onOpenFavorites,
                        modifier = Modifier.testTag("settings_favorites_card")
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsActionRow(
                        icon = Icons.Default.Campaign,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        title = "Duyurular & Bildirimler",
                        subtitle = "Yönetici duyuruları ve gelen kutusu",
                        badgeCount = unreadBroadcastCount,
                        onClick = onOpenAnnouncements,
                        modifier = Modifier.testTag("settings_announcements_card")
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsActionRow(
                        icon = Icons.Default.Notifications,
                        iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Günlük Söz & Hatırlatıcılar",
                        subtitle = "Günlük bildirim saatini ve özel günleri yönet",
                        onClick = onOpenReminders
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsActionRow(
                        icon = Icons.Default.People,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        title = "Topluluk Aşk Sözleri",
                        subtitle = "${communityQuotes.size} onaylı söz • Kendi sözünü paylaş",
                        onClick = onOpenCommunity
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsActionRow(
                        icon = Icons.Default.Forum,
                        iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Topluluk Sohbet Odaları",
                        subtitle = "Diğer üyelerle anlık sohbet et ve etkileşime geç 💬",
                        onClick = onOpenChat,
                        modifier = Modifier.testTag("settings_chat_row")
                    )
                }
            }
        }

        // 4. Veri, Senkronizasyon & Widget
        item {
            SettingsGroupHeader("Veri, Yedekleme & Widget")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Compact Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(count = notes.size.toString(), label = "Not")
                        StatItem(count = totalFavs.toString(), label = "Favori")
                        StatItem(count = reminders.size.toString(), label = "Hatırlatıcı")
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Cloud Backup Status Indicator & Manual Trigger
                    val syncState = cloudBackupStatus.state
                    val statusTint = when (syncState) {
                        com.example.data.model.CloudSyncState.SYNCED -> Color(0xFF2E7D32)
                        com.example.data.model.CloudSyncState.PENDING_CHANGES -> Color(0xFFEF6C00)
                        com.example.data.model.CloudSyncState.NEVER_BACKED_UP -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val statusBg = when (syncState) {
                        com.example.data.model.CloudSyncState.SYNCED -> Color(0xFFE8F5E9)
                        com.example.data.model.CloudSyncState.PENDING_CHANGES -> Color(0xFFFFF3E0)
                        com.example.data.model.CloudSyncState.NEVER_BACKED_UP -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val statusIcon = when (syncState) {
                        com.example.data.model.CloudSyncState.SYNCED -> Icons.Default.CloudDone
                        com.example.data.model.CloudSyncState.PENDING_CHANGES -> Icons.Default.Warning
                        com.example.data.model.CloudSyncState.NEVER_BACKED_UP -> Icons.Default.CloudOff
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("settings_cloud_backup_card"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                SettingsIconBadge(Icons.Default.CloudSync, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
                                Column {
                                    Text("Bulut Yedekleme Durumu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Son yedekleme: $lastSyncStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = statusBg,
                                modifier = Modifier.testTag("cloud_backup_status_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = syncState.label,
                                        tint = statusTint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = syncState.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusTint
                                    )
                                }
                            }
                        }

                        // Cloud Capacity Meter (Free 10-item limit vs Premium Unlimited)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (cloudBackupStatus.isUnlimited) Color(0xFFE8F5E9).copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (cloudBackupStatus.isUnlimited) {
                                            "∞ Sınırsız Premium Yedekleme"
                                        } else {
                                            "Bulut Kapasitesi: ${cloudBackupStatus.totalLocalItemsCount} / ${cloudBackupStatus.backupLimit} kayıt"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cloudBackupStatus.isUnlimited) Color(0xFF2E7D32)
                                        else if (cloudBackupStatus.isOverFreeLimit) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!cloudBackupStatus.isUnlimited) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFFF8E1),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpenStore() }
                                                .testTag("settings_unlock_unlimited_backup_chip")
                                        ) {
                                            Text(
                                                text = "Sınırsız Yap 👑",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                if (!cloudBackupStatus.isUnlimited) {
                                    val progress = (cloudBackupStatus.totalLocalItemsCount.toFloat() /
                                        cloudBackupStatus.backupLimit.coerceAtLeast(1)).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = if (cloudBackupStatus.isOverFreeLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    if (cloudBackupStatus.isOverFreeLimit) {
                                        Text(
                                            text = "Ücretsiz 10 kayıt sınırını aştın. Tüm anılarını güvenle yedeklemek için Sınırsız Kapasite'ye geç!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        if (cloudBackupStatus.hasSnapshot) {
                            Text(
                                text = "Bulutta kayıtlı: ${cloudBackupStatus.syncedNotesCount} not • ${cloudBackupStatus.syncedFavoritesCount} favori • ${cloudBackupStatus.syncedRemindersCount} hatırlatıcı" +
                                    if (cloudBackupStatus.unsyncedNotesCount > 0) " (${cloudBackupStatus.unsyncedNotesCount} yeni değişiklik)" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        syncMessage?.let { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.syncWithCloud() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                                    .testTag("settings_manual_cloud_backup_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Yedekleniyor...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buluta Şimdi Yedekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (cloudBackupStatus.hasSnapshot) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.restoreLastCloudSnapshot { _, feedback ->
                                            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isSyncing,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .sizeIn(minHeight = 48.dp)
                                        .testTag("settings_restore_cloud_snapshot_button")
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Yedeği Geri Yükle", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Yükle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsActionRow(
                        icon = Icons.Default.Storage,
                        iconBg = MaterialTheme.colorScheme.surfaceVariant,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Yedek Oluştur & Geri Yükle",
                        subtitle = "Notlar ve favoriler için JSON yedekleme",
                        onClick = { showBackupDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsActionRow(
                        icon = Icons.Default.Widgets,
                        iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Ana Ekran Widget'ı Kurulumu",
                        subtitle = "Telefon ana ekranına günlük söz widget'ı ekle",
                        onClick = { showWidgetDialog = true }
                    )
                }
            }
        }

        // 5. VIP Mağaza & Reklamsız Deneyim
        item {
            SettingsGroupHeader("VIP Üyelik & Mağaza")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                SettingsActionRow(
                    icon = Icons.Default.WorkspacePremium,
                    iconBg = Color(0xFFFFF8E1),
                    iconTint = Color(0xFFD97706),
                    title = "VIP Mağaza & Aşk Parası",
                    subtitle = if (entitlementState.isVip) {
                        "VIP Aktif ✨ • ${entitlementState.loveCoins} Aşk Parası"
                    } else {
                        "Reklamsız paket, VIP hikaye çerçeveleri & ${entitlementState.loveCoins} 🪙"
                    },
                    badgeText = if (entitlementState.isVip) "VIP" else "${entitlementState.loveCoins} 🪙",
                    onClick = onOpenStore,
                    modifier = Modifier.testTag("settings_store_card")
                )
            }
        }

        // 6. Destek & Yönetici Kontrolü
        item {
            SettingsGroupHeader("Destek & Yönetim")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsActionRow(
                        icon = Icons.Default.SupportAgent,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        title = "Yönetici ile İletişime Geç",
                        subtitle = "Öneri, destek mesajı gönder ve yanıtları gör",
                        onClick = onOpenAdminContact,
                        modifier = Modifier.testTag("settings_admin_contact_card")
                    )
                    if (sessionState.isAdmin) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsActionRow(
                            icon = Icons.Default.Shield,
                            iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = "Yönetici Kontrol Paneli",
                            subtitle = "Firebase anlık yönetim • Söz ekle, bakım modu & sohbet",
                            badgeCount = pendingCount,
                            onClick = onOpenAdmin,
                            modifier = Modifier.testTag("settings_admin_panel_card")
                        )
                    }
                }
            }
        }

        // 7. Play Store Yasal Politikalar & Hesap Silme
        item {
            SettingsGroupHeader("Yasal Bilgiler & Hesap Güvenliği")
            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsActionRow(
                        icon = Icons.Default.PrivacyTip,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Gizlilik Politikası & Veri Güvenliği",
                        subtitle = "KVKK, yerel veri saklama ve reklam gizlilik ilkeleri",
                        onClick = { showPrivacyPolicyDialog = true },
                        modifier = Modifier.testTag("settings_privacy_policy_row")
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsActionRow(
                        icon = Icons.Default.Description,
                        iconBg = MaterialTheme.colorScheme.surfaceVariant,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Kullanım Koşulları • v1.0",
                        subtitle = "Topluluk kuralları ve Play Store lisans şartları",
                        onClick = { showTermsDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsActionRow(
                        icon = Icons.Default.Share,
                        iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Aşka Dair'i Arkadaşlarınla Paylaş",
                        subtitle = "Sevdiklerine bu romantik uygulamayı öner",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "💖 Her gün en güzel aşk sözleri, görsel hikaye tasarımları ve çift oyunları için Aşka Dair uygulamasını keşfet!"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Uygulamayı Paylaş"))
                        }
                    )
                    if (sessionState.isUser) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsActionRow(
                            icon = Icons.Default.DeleteForever,
                            iconBg = MaterialTheme.colorScheme.errorContainer,
                            iconTint = MaterialTheme.colorScheme.error,
                            title = "Hesabımı ve Verilerimi Kalıcı Sil",
                            subtitle = "Play Store veri silme hakkı kapsamında hesabını kaldır",
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier.testTag("settings_delete_account_row")
                        )
                    }
                }
            }
        }
    }

    // Backup & Restore Modal Dialog
    if (showBackupDialog) {
        var restoreText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Yedekleme & Geri Yükleme", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.generateBackupJson() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yedek Oluştur", fontSize = 12.sp)
                        }
                    }

                    backupMessage?.let { status ->
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    exportedJson?.let { json ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hazır (${json.length} bayt)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Row {
                                    TextButton(
                                        onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            cm.setPrimaryClip(ClipData.newPlainText("Aşka Dair Yedeği", json))
                                            Toast.makeText(context, "Kopyalandı 📋", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kopyala", fontSize = 12.sp)
                                    }
                                    TextButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_SUBJECT, "Aşka Dair Veri Yedeği")
                                                putExtra(Intent.EXTRA_TEXT, json)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Yedeği Paylaş"))
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Paylaş", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = restoreText,
                        onValueChange = { restoreText = it },
                        label = { Text("Geri Yüklenecek JSON Metni") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreText.isNotBlank()) {
                            viewModel.restoreBackup(restoreText)
                            showBackupDialog = false
                        }
                    },
                    enabled = restoreText.isNotBlank()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geri Yükle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Widget Guide Modal Dialog
    if (showWidgetDialog) {
        AlertDialog(
            onDismissRequest = { showWidgetDialog = false },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Ana Ekran Aşk Sözü Widget'ı", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Uygulamayı açmadan telefonunuzun ana ekranında her gün yeni bir aşk sözü görmek için:\n\n" +
                        "1. Ana ekranda boş bir alana basılı tutun.\n" +
                        "2. 'Widget'lar' menüsünü açın.\n" +
                        "3. 'Aşka Dair' widget'ını seçip ana ekranınıza sürükleyin.\n\n" +
                        "Widget üzerindeki yenile simgesine dokunarak anında yeni bir söze geçebilirsiniz! 💌",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(onClick = { showWidgetDialog = false }) {
                    Text("Anladım")
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Gizlilik Politikası & Veri Güvenliği", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "• Yerel Veri Depolama: Notlarınız, favorileriniz, sesli kayıtlarınız ve hatırlatıcılarınız cihazınızda şifreli/özel uygulama alanında tutulur.\n\n" +
                        "• Yapay Zeka Özellikleri: İsim akrostişi ve aşk sözü oluşturma talepleri güvenli sunucu bağlantısı üzerinden işlenir.\n\n" +
                        "• Reklam ve Ödeme: Ücretsiz kullanıcılar için Google AdMob reklamları gösterilebilir; satın almalar Google Play Faturalandırma güvencesiyle yürütülür.\n\n" +
                        "• Hesap Silme Hakkı: İstediğiniz an Ayarlar menüsünden hesabınızı kalıcı olarak silebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            icon = { Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Kullanım Koşulları • v1.0", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Aşka Dair Topluluğu içinde paylaştığınız tüm sözler ve yorumlar sevgi, saygı ve genel ahlak ilkelerine uygun olmalıdır. " +
                        "Yönetici ekibi topluluk güvenliğini korumak adına uygunsuz paylaşımları düzenleme veya kaldırma hakkına sahiptir.\n\n" +
                        "Satın alınan VIP abonelikler Google Play hesap ayarlarınızdan dilediğiniz zaman iptal edilebilir.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("Kabul Ediyorum")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hesabı Kalıcı Olarak Sil", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Hesabınızı (${sessionState.email.ifBlank { sessionState.displayName }}) kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentUserAccountAndData { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        showDeleteAccountDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Evet, Hesabımı Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    containerColor: Color,
    tintColor: Color
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    badgeText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            SettingsIconBadge(icon, iconBg, iconTint)
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (badgeCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$badgeCount",
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (!badgeText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
