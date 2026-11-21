package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.AdminMessageEntity
import com.example.data.local.BroadcastNotificationEntity
import com.example.data.local.CommunityQuoteEntity
import com.example.data.model.AdminSettings
import com.example.ui.LoveQuotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateBack: () -> Unit,
    initialTabIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adminSettings by viewModel.adminSettings.collectAsState()
    val adminQuotes by viewModel.adminQuotes.collectAsState()
    val allUsers by viewModel.allUserAccounts.collectAsState()

    val pendingQuotes by viewModel.pendingCommunityQuotes.collectAsState()
    val approvedQuotes by viewModel.approvedCommunityQuotes.collectAsState()
    val rejectedQuotes by viewModel.rejectedCommunityQuotes.collectAsState()
    val adminMessages by viewModel.allAdminMessages.collectAsState()
    val pendingMessagesCount by viewModel.pendingAdminMessagesCount.collectAsState()
    val allBroadcasts by viewModel.allBroadcastNotifications.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()
    val allChatMessages by viewModel.allChatMessagesDesc.collectAsState()
    val pendingChatCount by viewModel.pendingChatCount.collectAsState()

    var selectedTabIndex by remember(initialTabIndex) { mutableIntStateOf(initialTabIndex.coerceIn(0, 7)) }
    var moderationFilter by remember { mutableIntStateOf(0) } // 0=Bekleyen, 1=Yayında, 2=Reddedilen
    var editingQuote by remember { mutableStateOf<CommunityQuoteEntity?>(null) }
    var rejectingQuote by remember { mutableStateOf<CommunityQuoteEntity?>(null) }

    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_panel_screen")
    ) {
        // Modern Admin Top Header
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Yönetici Kontrol Paneli",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Firebase Gerçek Zamanlı Yönetici • Tam Yetki",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Maintenance Status Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (adminSettings.maintenanceMode) MaterialTheme.colorScheme.errorContainer
                        else Color(0xFFE8F5E9),
                        onClick = {
                            val nextMode = !adminSettings.maintenanceMode
                            viewModel.updateAdminSettings(adminSettings.copy(maintenanceMode = nextMode))
                            Toast.makeText(
                                context,
                                if (nextMode) "Bakım Modu Aktif Edildi 🛠️" else "Bakım Modu Kapatıldı ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("admin_header_maintenance_pill")
                    ) {
                        Text(
                            text = if (adminSettings.maintenanceMode) "🛠️ Bakımda" else "● Yayında",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (adminSettings.maintenanceMode) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Compact Live Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "✍️ ${adminQuotes.size} Söz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "⏳ ${pendingQuotes.size} Onay",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "👥 ${allUsers.size} Üye",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Scrollable Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Söz Ekle ✍️") },
                modifier = Modifier.testTag("admin_tab_add_quote")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Bakım Modu 🛠️")
                        if (adminSettings.maintenanceMode) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.error, modifier = Modifier.size(8.dp)) {}
                        }
                    }
                },
                modifier = Modifier.testTag("admin_tab_maintenance")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Moderasyon")
                        if (pendingQuotes.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${pendingQuotes.size}",
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("admin_tab_moderation")
            )
            Tab(
                selected = selectedTabIndex == 3,
                onClick = { selectedTabIndex = 3 },
                text = { Text("Kullanıcılar (${allUsers.size})") },
                modifier = Modifier.testTag("admin_tab_users")
            )
            Tab(
                selected = selectedTabIndex == 4,
                onClick = { selectedTabIndex = 4 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Mesajlar 📩")
                        if (pendingMessagesCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$pendingMessagesCount",
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("admin_tab_messages")
            )
            Tab(
                selected = selectedTabIndex == 5,
                onClick = { selectedTabIndex = 5 },
                text = { Text("Duyuru Gönder 📢") },
                modifier = Modifier.testTag("admin_tab_broadcast")
            )
            Tab(
                selected = selectedTabIndex == 6,
                onClick = { selectedTabIndex = 6 },
                text = { Text("Kurallar ⚙️") },
                modifier = Modifier.testTag("admin_tab_rules")
            )
            Tab(
                selected = selectedTabIndex == 7,
                onClick = { selectedTabIndex = 7 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Sohbet Kontrolü 💬")
                        if (pendingChatCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$pendingChatCount",
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("admin_tab_chat_control")
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    AdminAddQuoteTab(
                        adminQuotes = adminQuotes,
                        onAddQuote = { text, author, sectionId, subCat, tags, featured ->
                            viewModel.addAdminQuote(text, author, sectionId, subCat, tags, featured) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onUpdateQuote = { updated ->
                            viewModel.updateAdminQuote(updated)
                            Toast.makeText(context, "Söz güncellendi! 💾", Toast.LENGTH_SHORT).show()
                        },
                        onFeatureQuote = { quote -> viewModel.setFeaturedAdminQuote(quote) },
                        onDeleteQuote = { id ->
                            viewModel.deleteAdminQuote(id)
                            Toast.makeText(context, "Söz silindi.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                1 -> {
                    AdminMaintenanceAndSystemTab(
                        settings = adminSettings,
                        entitlementState = entitlementState,
                        onToggleTestVip = { enabled ->
                            viewModel.toggleAdminTestVip(enabled)
                            Toast.makeText(
                                context,
                                if (enabled) "Test VIP aktif edildi 👑" else "Test VIP kapatıldı",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSaveSettings = { newSettings ->
                            viewModel.updateAdminSettings(newSettings)
                            Toast.makeText(context, "Bakım ve sistem ayarları kaydedildi! 💾", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                2 -> {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    0 to "Bekleyen (${pendingQuotes.size})",
                                    1 to "Yayında (${approvedQuotes.size})",
                                    2 to "Reddedilen (${rejectedQuotes.size})"
                                )
                            ) { (idx, label) ->
                                FilterChip(
                                    selected = moderationFilter == idx,
                                    onClick = { moderationFilter = idx },
                                    label = { Text(label, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        when (moderationFilter) {
                            0 -> ModerationQuotesList(
                                quotes = pendingQuotes,
                                emptyText = "Şu anda onay bekleyen söz bulunmuyor 🎉",
                                onApprove = { id ->
                                    viewModel.approveCommunityQuote(id)
                                    Toast.makeText(context, "Söz onaylandı ve yayınlandı! ✅", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { quote -> editingQuote = quote },
                                onReject = { quote -> rejectingQuote = quote },
                                onDelete = { id -> viewModel.deleteCommunityQuote(id) },
                                isPendingTab = true
                            )
                            1 -> ModerationQuotesList(
                                quotes = approvedQuotes,
                                emptyText = "Henüz yayınlanmış topluluk sözü yok.",
                                onApprove = {},
                                onEdit = { quote -> editingQuote = quote },
                                onReject = { quote -> rejectingQuote = quote },
                                onDelete = { id -> viewModel.deleteCommunityQuote(id) },
                                isPendingTab = false
                            )
                            else -> ModerationQuotesList(
                                quotes = rejectedQuotes,
                                emptyText = "Reddedilmiş söz bulunmuyor.",
                                onApprove = { id ->
                                    viewModel.approveCommunityQuote(id)
                                    Toast.makeText(context, "Söz onaylandı ve yayına alındı! ✅", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { quote -> editingQuote = quote },
                                onReject = {},
                                onDelete = { id -> viewModel.deleteCommunityQuote(id) },
                                isPendingTab = false
                            )
                        }
                    }
                }
                3 -> {
                    AdminUsersManagementTab(
                        users = allUsers,
                        onToggleBan = { id, banned, reason ->
                            viewModel.toggleUserBlocked(id, banned, reason)
                            Toast.makeText(
                                context,
                                if (banned) "Kullanıcı anında banlandı 🚫" else "Kullanıcı banı kaldırıldı ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onApplyRestriction = { id, restricted, canQuotes, canComments, canMusic, canChat, reason, durationHours ->
                            viewModel.applyUserRestrictions(
                                userId = id,
                                isRestricted = restricted,
                                canSubmitQuotes = canQuotes,
                                canComment = canComments,
                                canUploadMusic = canMusic,
                                canChat = canChat,
                                reason = reason,
                                durationHours = durationHours
                            )
                            Toast.makeText(
                                context,
                                if (restricted) "Kullanıcı kısıtlamaları anında uygulandı ⚠️" else "Kullanıcı kısıtlamaları kaldırıldı ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDeleteUser = { id ->
                            viewModel.deleteUserAccount(id)
                            Toast.makeText(context, "Kullanıcı kalıcı olarak silindi.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                4 -> {
                    AdminMessagesTab(
                        messages = adminMessages,
                        onReply = { id, reply ->
                            viewModel.replyToAdminMessage(id, reply)
                            Toast.makeText(context, "Yanıtınız kullanıcıya iletildi! ✅", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { id ->
                            viewModel.deleteAdminMessage(id)
                            Toast.makeText(context, "Mesaj silindi.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                5 -> {
                    AdminBroadcastTab(
                        broadcasts = allBroadcasts,
                        onSendBroadcast = { title, msg, cat, emoji, push ->
                            viewModel.sendBroadcastToAllUsers(
                                title = title,
                                message = msg,
                                category = cat,
                                emoji = emoji,
                                alsoShowSystemNotification = push
                            ) { _, feedback ->
                                Toast.makeText(context, feedback, Toast.LENGTH_LONG).show()
                            }
                        },
                        onDeleteBroadcast = { id ->
                            viewModel.deleteBroadcast(id)
                            Toast.makeText(context, "Duyuru kaldırıldı.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                6 -> {
                    AdminRestrictionsTab(
                        settings = adminSettings,
                        onSaveSettings = { newSettings ->
                            viewModel.updateAdminSettings(newSettings)
                            Toast.makeText(context, "Kısıtlama ve kurallar kaydedildi! 💾", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                else -> {
                    AdminChatControlTab(
                        settings = adminSettings,
                        allMessages = allChatMessages,
                        users = allUsers,
                        onSaveSettings = { newSettings ->
                            viewModel.updateAdminSettings(newSettings)
                        },
                        onSendAdminMessage = { room, msg, pin ->
                            viewModel.sendAdminChatMessage(room, msg, pin) { _, feedback ->
                                Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onApproveMessage = { id ->
                            viewModel.approveChatMessage(id)
                            Toast.makeText(context, "Sohbet mesajı onaylandı ✅", Toast.LENGTH_SHORT).show()
                        },
                        onHideMessage = { id ->
                            viewModel.hideChatMessage(id)
                            Toast.makeText(context, "Mesaj sohbetten gizlendi", Toast.LENGTH_SHORT).show()
                        },
                        onTogglePinMessage = { msg ->
                            viewModel.togglePinChatMessage(msg)
                            Toast.makeText(
                                context,
                                if (msg.isPinned) "Sabitleme kaldırıldı" else "Mesaj odaya sabitlendi 📌",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDeleteMessage = { id ->
                            viewModel.deleteChatMessage(id)
                            Toast.makeText(context, "Mesaj silindi.", Toast.LENGTH_SHORT).show()
                        },
                        onClearRoom = { roomId ->
                            viewModel.clearChatRoom(roomId)
                        },
                        onMuteUser = { userId, muted, reason ->
                            viewModel.muteUserInChat(
                                userId = userId,
                                muted = muted,
                                durationHours = if (muted) 24 else null,
                                reason = reason
                            )
                        }
                    )
                }
            }
        }
    }

    // Edit Quote Dialog
    editingQuote?.let { quote ->
        AdminEditQuoteDialog(
            quote = quote,
            onDismiss = { editingQuote = null },
            onSave = { updatedQuote, shouldApprove ->
                val finalQuote = if (shouldApprove) {
                    updatedQuote.copy(status = "APPROVED", approvedAt = System.currentTimeMillis())
                } else updatedQuote
                viewModel.updateCommunityQuote(finalQuote)
                editingQuote = null
                Toast.makeText(context, if (shouldApprove) "Söz düzenlendi ve onaylandı! ✅" else "Söz güncellendi! 💾", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Reject Dialog with Reason
    rejectingQuote?.let { quote ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectingQuote = null },
            title = { Text("Sözü Reddet", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${quote.text.take(60)}...\" sözünü reddetmek üzeresiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reddetme Gerekçesi (Opsiyonel)") },
                        placeholder = { Text("Örn: Uygunsuz içerik, imla hataları") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectCommunityQuote(quote.id, reason.ifBlank { "Yönetici tarafından reddedildi" })
                        rejectingQuote = null
                        Toast.makeText(context, "Söz reddedildi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reddet")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingQuote = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
fun ModerationQuotesList(
    quotes: List<CommunityQuoteEntity>,
    emptyText: String,
    onApprove: (Long) -> Unit,
    onEdit: (CommunityQuoteEntity) -> Unit,
    onReject: (CommunityQuoteEntity) -> Unit,
    onDelete: (Long) -> Unit,
    isPendingTab: Boolean
) {
    if (quotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        items(quotes, key = { it.id }) { quote ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (quote.status) {
                            "APPROVED" -> Color(0xFF4CAF50).copy(alpha = 0.4f)
                            "REJECTED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        },
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Status & Meta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (quote.status) {
                                "APPROVED" -> Color(0xFFE8F5E9)
                                "REJECTED" -> Color(0xFFFFEBEE)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = when (quote.status) {
                                    "APPROVED" -> "YAYINDA ✅"
                                    "REJECTED" -> "REDDEDİLDİ ❌"
                                    else -> "ONAY BEKLİYOR ⏳"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (quote.status) {
                                    "APPROVED" -> Color(0xFF2E7D32)
                                    "REJECTED" -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = dateFormat.format(Date(quote.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quote Text
                    Text(
                        text = "\"${quote.text}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Author & Submitter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kategori: ${quote.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "— ${quote.author} (${quote.submitterName})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (quote.status == "REJECTED" && !quote.moderationNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Red Gerekçesi: ${quote.moderationNote}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Moderation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete Button
                        IconButton(onClick = { onDelete(quote.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Edit Button
                        IconButton(onClick = { onEdit(quote) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isPendingTab) {
                            // Reject Button
                            OutlinedButton(
                                onClick = { onReject(quote) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reddet", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Approve Button
                            Button(
                                onClick = { onApprove(quote.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Onayla", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else if (quote.status == "REJECTED") {
                            Button(
                                onClick = { onApprove(quote.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("Tekrar Onayla", fontSize = 12.sp)
                            }
                        } else if (quote.status == "APPROVED") {
                            OutlinedButton(
                                onClick = { onReject(quote) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Yayından Kaldır", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEditQuoteDialog(
    quote: CommunityQuoteEntity,
    onDismiss: () -> Unit,
    onSave: (CommunityQuoteEntity, Boolean) -> Unit
) {
    var editedText by remember { mutableStateOf(quote.text) }
    var editedAuthor by remember { mutableStateOf(quote.author) }
    var editedCategory by remember { mutableStateOf(quote.category) }
    var editedSubmitter by remember { mutableStateOf(quote.submitterName) }

    val categories = listOf("Aşk Sözleri", "Romantik Sözler", "Günaydın Mesajları", "Gece Mesajları", "Özel")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Kullanıcı Sözünü Düzenle", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Söz Metni") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = editedAuthor,
                    onValueChange = { editedAuthor = it },
                    label = { Text("Yazar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = editedSubmitter,
                    onValueChange = { editedSubmitter = it },
                    label = { Text("Gönderen Kişi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Kategori:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = editedCategory == cat,
                            onClick = { editedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val updated = quote.copy(
                            text = editedText.trim(),
                            author = editedAuthor.trim(),
                            category = editedCategory,
                            submitterName = editedSubmitter.trim()
                        )
                        onSave(updated, false)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Kaydet")
                }

                Button(
                    onClick = {
                        val updated = quote.copy(
                            text = editedText.trim(),
                            author = editedAuthor.trim(),
                            category = editedCategory,
                            submitterName = editedSubmitter.trim()
                        )
                        onSave(updated, true)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Kaydet & Onayla")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun AdminRestrictionsTab(
    settings: AdminSettings,
    onSaveSettings: (AdminSettings) -> Unit
) {
    var submissionsEnabled by remember { mutableStateOf(settings.submissionsEnabled) }
    var dailyLimit by remember { mutableIntStateOf(settings.dailyLimitPerUser) }
    var minChar by remember(settings.minCharCount) { mutableIntStateOf(settings.minCharCount) }
    var maxChar by remember(settings.maxCharCount) { mutableIntStateOf(settings.maxCharCount) }
    var autoApprove by remember(settings.autoApprove) { mutableStateOf(settings.autoApprove) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Toggle Submissions
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Söz Gönderimlerini İzin Ver",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (submissionsEnabled) "Kullanıcılar yeni söz paylaşabilir" else "Yeni söz gönderimleri geçici olarak durduruldu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = submissionsEnabled,
                        onCheckedChange = { submissionsEnabled = it }
                    )
                }
            }
        }

        // Daily Submission Limit
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kullanıcı Başına Günlük Söz Sınırı",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bir kişi günde en fazla $dailyLimit adet söz paylaşabilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = dailyLimit.toFloat(),
                        onValueChange = { dailyLimit = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }
        }

        // Min & Max Character Length
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Karakter Sınırları (Uzunluk Kısıtlaması)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Min: $minChar karakter • Maks: $maxChar karakter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Minimum Karakter ($minChar):", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = minChar.toFloat(),
                        onValueChange = { minChar = it.toInt() },
                        valueRange = 5f..30f,
                        steps = 24
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Maksimum Karakter ($maxChar):", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = maxChar.toFloat(),
                        onValueChange = { maxChar = it.toInt() },
                        valueRange = 100f..500f,
                        steps = 39
                    )
                }
            }
        }

        // Auto Approve Toggle
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Otomatik Onaylama Modu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (autoApprove) "Tüm sözler admin onayına girmeden anında yayına girer" else "Tüm sözler admin panelinde onay bekler (Önerilen)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoApprove,
                        onCheckedChange = { autoApprove = it }
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = settings.copy(
                        submissionsEnabled = submissionsEnabled,
                        dailyLimitPerUser = dailyLimit,
                        minCharCount = minChar,
                        maxCharCount = maxChar,
                        autoApprove = autoApprove
                    )
                    onSaveSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_save_rules_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ayarları Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminMessagesTab(
    messages: List<AdminMessageEntity>,
    onReply: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")) }
    var replyingMessage by remember { mutableStateOf<AdminMessageEntity?>(null) }

    if (messages.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Kullanıcılardan henüz gelen mesaj bulunmuyor 📩",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isReplied = msg.status == "REPLIED"
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isReplied) Color(0xFF4CAF50).copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isReplied) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isReplied) "YANITLANDI ✅" else "YENİ MESAJ ⏳",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReplied) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(msg.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${msg.senderName}${if (msg.senderEmail.isNotBlank()) " (${msg.senderEmail})" else ""}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Konu: ${msg.topic}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = msg.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        msg.adminReply?.let { reply ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Yanıtınız:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = reply,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onDelete(msg.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { replyingMessage = msg },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isReplied) "Yanıtı Düzenle" else "Yanıtla", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    replyingMessage?.let { targetMsg ->
        var replyInput by remember(targetMsg.id) { mutableStateOf(targetMsg.adminReply ?: "") }
        AlertDialog(
            onDismissRequest = { replyingMessage = null },
            title = { Text("${targetMsg.senderName} Mesajına Yanıt", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${targetMsg.message}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = replyInput,
                        onValueChange = { replyInput = it },
                        label = { Text("Yönetici Yanıtı") },
                        placeholder = { Text("Kullanıcıya iletilecek yanıtınızı yazın...") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyInput.isNotBlank()) {
                            onReply(targetMsg.id, replyInput.trim())
                            replyingMessage = null
                        }
                    },
                    enabled = replyInput.isNotBlank()
                ) {
                    Text("Yanıtı Gönder")
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingMessage = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminBroadcastTab(
    broadcasts: List<BroadcastNotificationEntity>,
    onSendBroadcast: (title: String, message: String, category: String, emoji: String, pushNotification: Boolean) -> Unit,
    onDeleteBroadcast: (Long) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")) }

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Duyuru" to "📢") }
    var sendSystemPush by remember { mutableStateOf(true) }

    val categories = listOf(
        "Duyuru" to "📢",
        "Günün Özel Mesajı" to "💌",
        "Etkinlik" to "🎉",
        "Güncelleme" to "✨"
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Tüm Kullanıcılara Bildirim Gönder",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Gönderdiğiniz duyuru tüm kullanıcıların Duyurular gelen kutusuna düşer ve isteğe bağlı anlık cihaz bildirimi olarak gösterilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { item ->
                            FilterChip(
                                selected = selectedCategory.first == item.first,
                                onClick = { selectedCategory = item },
                                label = { Text("${item.second} ${item.first}", fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Bildirim Başlığı") },
                        placeholder = { Text("Örn: Hafta Sonuna Özel Romantik Sözler Eklendi!") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_broadcast_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Bildirim İçeriği") },
                        placeholder = { Text("Tüm kullanıcılara iletilecek duyuru mesajını yazın...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_broadcast_message_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cihaz Bildirimi (Push) Göster",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Bildirim çubuğunda anında uyarı gösterir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = sendSystemPush,
                            onCheckedChange = { enabled ->
                                sendSystemPush = enabled
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                    if (perm != PackageManager.PERMISSION_GRANTED) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            }
                        )
                    }

                    Button(
                        onClick = {
                            if (sendSystemPush && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                if (perm != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            onSendBroadcast(
                                title,
                                message,
                                selectedCategory.first,
                                selectedCategory.second,
                                sendSystemPush
                            )
                            title = ""
                            message = ""
                        },
                        enabled = title.isNotBlank() && message.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_send_broadcast_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tüm Kullanıcılara Bildirim Gönder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Gönderilmiş Duyurular (${broadcasts.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(broadcasts, key = { it.id }) { item ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${item.emoji} ${item.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.category} • ${dateFormat.format(Date(item.sentAt))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDeleteBroadcast(item.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Duyuruyu Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

