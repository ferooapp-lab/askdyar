package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FsPublicProfile
import com.example.data.local.ChatMessageEntity
import com.example.data.model.ChatHistoryMerger
import com.example.data.model.ChatRoom
import com.example.ui.LoveQuotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GoldBadgeBg = Color(0xFFFFF8E1)
private val GoldBadgeText = Color(0xFFB45309)
private val GoldBadgeBorder = Color(0xFFF59E0B)

@Composable
fun ChatScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdminChatControl: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val publishedMessages by viewModel.publishedChatMessages.collectAsState()
    val likedMessageIds by viewModel.likedChatMessageIds.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val activeUserAccount by viewModel.activeUserAccount.collectAsState()
    val slowModeRemaining by viewModel.chatSlowModeRemainingSeconds.collectAsState()
    val pendingChatCount by viewModel.pendingChatCount.collectAsState()
    val publicProfilesByUid by viewModel.publicProfilesByUid.collectAsState()
    val chatSenderUidByMessageId by viewModel.chatSenderUidByMessageId.collectAsState()
    val chatHistoryByRoom by viewModel.chatHistoryMessagesByRoom.collectAsState()
    val hasMoreHistoryByRoom by viewModel.hasMoreChatHistoryByRoom.collectAsState()
    val isLoadingHistory by viewModel.isLoadingChatHistory.collectAsState()

    var selectedRoom by remember { mutableStateOf(ChatRoom.GENEL) }
    var isHistoryMode by remember { mutableStateOf(false) }
    var historySearchQuery by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ChatMessageEntity?>(null) }
    var pinAsAdmin by remember { mutableStateOf(false) }
    var mutingMessageTarget by remember { mutableStateOf<ChatMessageEntity?>(null) }
    var selectedProfileMessage by remember { mutableStateOf<ChatMessageEntity?>(null) }

    LaunchedEffect(selectedRoom) {
        viewModel.loadInitialChatHistoryIfNeeded(selectedRoom)
    }

    val roomPagedHistory = chatHistoryByRoom[selectedRoom.id].orEmpty()
    val hasMoreHistory = hasMoreHistoryByRoom[selectedRoom.id] ?: true

    val mergedRoomMessages = remember(publishedMessages, roomPagedHistory, selectedRoom) {
        ChatHistoryMerger.mergePagedAndLive(
            loadedHistory = roomPagedHistory,
            liveMessages = publishedMessages,
            roomId = selectedRoom.id,
            includeAllStatuses = false
        )
    }

    val displayMessages = remember(mergedRoomMessages, isHistoryMode, historySearchQuery) {
        if (isHistoryMode) {
            ChatHistoryMerger.filterByQuery(mergedRoomMessages, historySearchQuery)
        } else {
            mergedRoomMessages
        }
    }

    val pinnedMessage = remember(mergedRoomMessages) {
        mergedRoomMessages.lastOrNull { it.isPinned }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedRoom, isHistoryMode, displayMessages.size) {
        if (!isHistoryMode && displayMessages.isNotEmpty()) {
            listState.animateScrollToItem(displayMessages.lastIndex)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("tr")) }
    val historyDateFormat = remember { SimpleDateFormat("dd MMM HH:mm", Locale("tr")) }
    val nowMillis = System.currentTimeMillis()
    val isUserChatMuted = activeUserAccount?.let { user ->
        val stillRestricted = user.isRestricted && (user.restrictedUntil == null || user.restrictedUntil > nowMillis)
        stillRestricted && !user.canChat
    } ?: false

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // 1. Top Header
        Surface(
            tonalElevation = 3.dp,
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
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Topluluk Sohbeti",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = selectedRoom.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (sessionState.isAdmin) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToAdminChatControl() }
                                    .testTag("chat_admin_control_shortcut")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Sohbet Kontrolü",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (pendingChatCount > 0) "Kontrol ($pendingChatCount)" else "Kontrol",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            val statusLabel = when {
                                !adminSettings.chatEnabled -> "🔒 Kapalı"
                                adminSettings.chatRequireApproval -> "🛡️ Onaylı"
                                adminSettings.chatSlowModeSeconds > 0 -> "⏳ ${adminSettings.chatSlowModeSeconds}sn"
                                else -> "● Canlı"
                            }
                            val statusBg = if (!adminSettings.chatEnabled) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                Color(0xFFE8F5E9)
                            }
                            val statusTint = if (!adminSettings.chatEnabled) {
                                MaterialTheme.colorScheme.error
                            } else {
                                Color(0xFF2E7D32)
                            }
                            Surface(shape = RoundedCornerShape(10.dp), color = statusBg) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusTint,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Room Switcher Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(ChatRoom.values()) { room ->
                        val count = publishedMessages.count { it.roomId == room.id }
                        val isSelected = selectedRoom == room
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedRoom = room
                                replyTarget = null
                            },
                            label = {
                                Text(
                                    text = "${room.emoji} ${room.title} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("chat_room_chip_${room.id}")
                        )
                    }
                }

                // Live vs Mesaj Geçmişi View Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !isHistoryMode,
                        onClick = { isHistoryMode = false },
                        label = {
                            Text(
                                text = "● Canlı Sohbet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_mode_live_chip")
                    )
                    FilterChip(
                        selected = isHistoryMode,
                        onClick = {
                            isHistoryMode = true
                            viewModel.loadInitialChatHistoryIfNeeded(selectedRoom)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Mesaj Geçmişi (${mergedRoomMessages.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_mode_history_chip")
                    )
                }

                // Search bar & live stats in Mesaj Geçmişi view
                if (isHistoryMode) {
                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { historySearchQuery = it },
                        placeholder = { Text("Geçmiş mesajlarda veya isimlerde ara...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (historySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { historySearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Aramayı Temizle")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_history_search_input")
                    )
                }
            }
        }

        // 2. Pinned Admin Message Banner
        pinnedMessage?.let { pinned ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("chat_pinned_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Sabit Mesaj",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sabitlenen Yönetici Duyurusu • ${pinned.senderName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = pinned.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Optional Approval Mode Notice
        if (adminSettings.chatRequireApproval && !sessionState.isAdmin) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🛡️ Moderatör onaylı sohbet aktif: Gönderdiğiniz mesajlar yönetici onayından sonra odada görünür.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // 3. Messages Feed (Canlı + Mesaj Geçmişi)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (displayMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(selectedRoom.emoji, fontSize = 40.sp)
                        Text(
                            text = if (isHistoryMode && historySearchQuery.isNotBlank()) {
                                "Aramanızla eşleşen geçmiş mesaj yok"
                            } else {
                                "${selectedRoom.title} odası henüz sakin"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isHistoryMode && hasMoreHistory) {
                                "Daha eski mesajları buluttan yüklemeyi deneyebilirsiniz."
                            } else {
                                "İlk mesajı yazarak samimi sohbeti başlatabilirsiniz!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasMoreHistory) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.loadOlderChatMessages(selectedRoom) { loadedCount, _ ->
                                        if (loadedCount == 0) {
                                            Toast.makeText(context, "Daha eski mesaj bulunamadı.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isLoadingHistory,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .sizeIn(minHeight = 48.dp)
                                    .testTag("chat_load_older_button")
                            ) {
                                if (isLoadingHistory) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Önceki Mesajları Yükle")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Load Older History Header Item
                    item(key = "history_loader_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (hasMoreHistory) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.loadOlderChatMessages(selectedRoom) { loadedCount, more ->
                                            val feedback = when {
                                                loadedCount > 0 -> "$loadedCount önceki mesaj yüklendi 📜"
                                                !more -> "Tüm mesaj geçmişi yüklendi ✓"
                                                else -> "Yeni geçmiş mesaj bulunamadı."
                                            }
                                            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isLoadingHistory,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .sizeIn(minHeight = 48.dp)
                                        .testTag(if (isHistoryMode) "chat_load_older_button" else "chat_live_load_older_button")
                                ) {
                                    if (isLoadingHistory) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Geçmiş yükleniyor...", fontSize = 12.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isHistoryMode) "Daha Eski Mesajları Yükle" else "Önceki Mesajları Yükle",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (isHistoryMode) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "✓ ${selectedRoom.title} odasının tüm mesaj geçmişi yüklendi (${mergedRoomMessages.size} mesaj)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(displayMessages, key = { it.id }) { msg ->
                        val senderUid = chatSenderUidByMessageId[msg.id].orEmpty()
                        val senderProfile = publicProfilesByUid[senderUid]
                        val isOwnMessage = when {
                            senderUid.isNotBlank() && senderUid == sessionState.uid -> true
                            sessionState.isAdmin && msg.senderRole == "ADMIN" -> true
                            sessionState.isUser && msg.senderUserId > 0L && msg.senderUserId == sessionState.userId -> true
                            else -> false
                        }
                        val isLiked = likedMessageIds.contains(msg.id.toString())
                        val isPremiumSender = msg.senderRole != "ADMIN" &&
                            (msg.isVipSender || senderProfile?.isPremium == true)

                        ChatMessageBubbleRow(
                            message = msg,
                            isOwnMessage = isOwnMessage,
                            isLiked = isLiked,
                            isViewerAdmin = sessionState.isAdmin,
                            isPremiumSender = isPremiumSender,
                            formattedTime = if (isHistoryMode) {
                                historyDateFormat.format(Date(msg.createdAt))
                            } else {
                                timeFormat.format(Date(msg.createdAt))
                            },
                            onOpenProfile = { selectedProfileMessage = msg },
                            onLikeClick = { viewModel.toggleChatMessageLike(msg.id) },
                            onReplyClick = { replyTarget = msg },
                            onCopyClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Sohbet Mesajı", msg.message))
                                Toast.makeText(context, "Mesaj kopyalandı", Toast.LENGTH_SHORT).show()
                            },
                            onTogglePin = {
                                viewModel.togglePinChatMessage(msg)
                                Toast.makeText(
                                    context,
                                    if (msg.isPinned) "Sabitleme kaldırıldı" else "Mesaj odaya sabitlendi 📌",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onHideMessage = {
                                viewModel.hideChatMessage(msg.id)
                                Toast.makeText(context, "Mesaj sohbetten gizlendi", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteMessage = {
                                viewModel.deleteChatMessage(msg.id)
                                Toast.makeText(context, "Mesaj kalıcı olarak silindi", Toast.LENGTH_SHORT).show()
                            },
                            onMuteUser = {
                                mutingMessageTarget = msg
                            }
                        )
                    }
                }
            }
        }

        // 4. Bottom Composer / Moderation Lock Area
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when {
                    !adminSettings.chatEnabled && !sessionState.isAdmin -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_closed_notice")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = adminSettings.chatClosedMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    isUserChatMuted && !sessionState.isAdmin -> {
                        val reasonText = activeUserAccount?.restrictionReason.orEmpty().ifBlank { "Sohbet kuralları" }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF3E0),
                            border = BorderStroke(1.dp, Color(0xFFEF6C00).copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_muted_notice")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100)
                                )
                                Column {
                                    Text(
                                        text = "Sohbette Susturuldunuz",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "Sebep: $reasonText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }
                    }

                    !sessionState.isLoggedIn -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sohbete yazmak için Google hesabıyla giriş yapın",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = onOpenAuth,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                                ) {
                                    Text("Giriş Yap", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {
                        replyTarget?.let { target ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Yanıtlanıyor: ${target.senderName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = target.message,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { replyTarget = null },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Yanıtı İptal Et", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (sessionState.isAdmin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = pinAsAdmin,
                                    onClick = { pinAsAdmin = !pinAsAdmin },
                                    leadingIcon = {
                                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    label = { Text("Yönetici Duyurusu Olarak Sabitle", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Text(
                                    text = "${messageInput.length}/500",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (slowModeRemaining > 0 || messageInput.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (slowModeRemaining > 0) "⏳ Yavaş mod: $slowModeRemaining sn bekleyin" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "${messageInput.length}/${adminSettings.chatMaxCharCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (messageInput.length > adminSettings.chatMaxCharCount) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                placeholder = {
                                    Text(
                                        if (sessionState.isAdmin) "Yönetici mesajınızı yazın..."
                                        else "${selectedRoom.title} odasına yaz..."
                                    )
                                },
                                maxLines = 3,
                                shape = RoundedCornerShape(22.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_message_input")
                            )

                            FilledIconButton(
                                onClick = {
                                    val textToSend = messageInput
                                    val reply = replyTarget
                                    val shouldPin = pinAsAdmin
                                    viewModel.sendChatMessage(
                                        room = selectedRoom,
                                        message = textToSend,
                                        replyTo = reply,
                                        pinAsAdmin = shouldPin
                                    ) { ok, feedback ->
                                        if (ok) {
                                            messageInput = ""
                                            replyTarget = null
                                            pinAsAdmin = false
                                            if (adminSettings.chatRequireApproval && !sessionState.isAdmin) {
                                                Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = messageInput.trim().length >= 2 && (sessionState.isAdmin || slowModeRemaining == 0),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Mesaj Gönder"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Chat User Profile Card Dialog (with Premium badge + Shared Quotes + Received Hearts)
    selectedProfileMessage?.let { targetMsg ->
        val targetUid = chatSenderUidByMessageId[targetMsg.id].orEmpty()
        val profile = publicProfilesByUid[targetUid]
        ChatUserProfileDialog(
            message = targetMsg,
            profile = profile,
            isViewerAdmin = sessionState.isAdmin,
            canMuteByUid = targetUid.isNotBlank() && targetMsg.senderRole != "ADMIN",
            onToggleMute = { muted ->
                viewModel.muteUserInChatByUid(
                    targetUid = targetUid,
                    muted = muted,
                    durationHours = 24,
                    reason = "Sohbet moderasyonu"
                )
                Toast.makeText(
                    context,
                    if (muted) "${targetMsg.senderName} 24 saat susturuldu 🔇" else "${targetMsg.senderName} susturması kaldırıldı 🔊",
                    Toast.LENGTH_SHORT
                ).show()
                selectedProfileMessage = null
            },
            onDismiss = { selectedProfileMessage = null }
        )
    }

    // 6. Quick Admin Mute Confirmation Dialog
    mutingMessageTarget?.let { targetMsg ->
        val targetUid = chatSenderUidByMessageId[targetMsg.id].orEmpty()
        val canMuteUser = targetUid.isNotBlank() || targetMsg.senderUserId > 0L
        var muteReason by remember(targetMsg.id) { mutableStateOf("Sohbet kurallarına aykırı mesaj") }
        AlertDialog(
            onDismissRequest = { mutingMessageTarget = null },
            title = { Text("${targetMsg.senderName} — Sohbet İşlemi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (canMuteUser) {
                            "Bu kullanıcıyı 24 saat boyunca sohbetten susturabilir ve mesajını gizleyebilirsiniz."
                        } else {
                            "Kullanıcı kimliği bulunamadığı için mesajı kalıcı olarak silebilirsiniz."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (canMuteUser) {
                        OutlinedTextField(
                            value = muteReason,
                            onValueChange = { muteReason = it },
                            label = { Text("Susturma Sebebi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (canMuteUser) {
                    Button(
                        onClick = {
                            if (targetUid.isNotBlank()) {
                                viewModel.muteUserInChatByUid(
                                    targetUid = targetUid,
                                    muted = true,
                                    durationHours = 24,
                                    reason = muteReason
                                )
                            } else {
                                viewModel.muteUserInChat(
                                    userId = targetMsg.senderUserId,
                                    muted = true,
                                    durationHours = 24,
                                    reason = muteReason
                                )
                            }
                            viewModel.hideChatMessage(targetMsg.id, muteReason)
                            mutingMessageTarget = null
                            Toast.makeText(context, "${targetMsg.senderName} 24 saat susturuldu 🔇", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Text("24 Saat Sustur & Gizle")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.deleteChatMessage(targetMsg.id)
                            mutingMessageTarget = null
                            Toast.makeText(context, "Mesaj silindi", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Mesajı Sil")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mutingMessageTarget = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun ChatUserProfileDialog(
    message: ChatMessageEntity,
    profile: FsPublicProfile?,
    isViewerAdmin: Boolean,
    canMuteByUid: Boolean,
    onToggleMute: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isAdminUser = message.senderRole == "ADMIN" || profile?.role == "ADMIN"
    val isPremiumUser = !isAdminUser && (message.isVipSender || profile?.isPremium == true)
    val displayName = profile?.displayName?.takeIf { it.isNotBlank() } ?: message.senderName
    val isCurrentlyMuted = profile?.isMutedInChat == true

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("chat_user_profile_dialog"),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        isAdminUser -> MaterialTheme.colorScheme.primary
                        isPremiumUser -> GoldBadgeBg
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    border = if (isPremiumUser) BorderStroke(2.dp, GoldBadgeBorder) else null,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(4.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                isAdminUser -> "🛡️"
                                isPremiumUser -> "👑"
                                else -> displayName.take(1).uppercase()
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isAdminUser -> MaterialTheme.colorScheme.onPrimary
                                isPremiumUser -> GoldBadgeText
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    when {
                        isAdminUser -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Yönetici 🛡️",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        isPremiumUser -> {
                            val planSuffix = profile?.vipPlanName?.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GoldBadgeBg,
                                border = BorderStroke(1.dp, GoldBadgeBorder.copy(alpha = 0.65f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = GoldBadgeText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Premium Üye 👑$planSuffix",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldBadgeText
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Standart Üye",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    profile?.partnerName?.takeIf { it.isNotBlank() }?.let { partner ->
                        Text(
                            text = "💞 Partner: $partner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${profile?.sharedQuotesCount ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Paylaşılan Sözler",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${profile?.receivedHeartsCount ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Alınan Kalpler",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isViewerAdmin && canMuteByUid) {
                    OutlinedButton(
                        onClick = { onToggleMute(!isCurrentlyMuted) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isCurrentlyMuted) Color(0xFF2E7D32) else Color(0xFFE65100)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                    ) {
                        Text(
                            text = if (isCurrentlyMuted) "Susturmayı Kaldır 🔊" else "24 Saat Sohbette Sustur 🔇",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
private fun ChatMessageBubbleRow(
    message: ChatMessageEntity,
    isOwnMessage: Boolean,
    isLiked: Boolean,
    isViewerAdmin: Boolean,
    isPremiumSender: Boolean,
    formattedTime: String,
    onOpenProfile: () -> Unit,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onCopyClick: () -> Unit,
    onTogglePin: () -> Unit,
    onHideMessage: () -> Unit,
    onDeleteMessage: () -> Unit,
    onMuteUser: () -> Unit
) {
    val isAdminMsg = message.senderRole == "ADMIN"
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_message_${message.id}"),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwnMessage) {
            Surface(
                shape = CircleShape,
                color = when {
                    isAdminMsg -> MaterialTheme.colorScheme.primary
                    isPremiumSender -> GoldBadgeBg
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                border = if (isPremiumSender) BorderStroke(1.2.dp, GoldBadgeBorder) else null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenProfile)
                    .testTag("chat_avatar_${message.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            isAdminMsg -> "👑"
                            isPremiumSender -> "👑"
                            else -> message.senderName.take(1).uppercase()
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isAdminMsg -> MaterialTheme.colorScheme.onPrimary
                            isPremiumSender -> GoldBadgeText
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        val bubbleShape = if (isOwnMessage) {
            RoundedCornerShape(topStart = 18.dp, topEnd = 6.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
        } else {
            RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
        }

        val containerColor = when {
            isAdminMsg -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            isOwnMessage -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        }

        ElevatedCard(
            shape = bubbleShape,
            modifier = Modifier
                .widthIn(min = 140.dp, max = 304.dp)
                .border(
                    width = if (isAdminMsg || isPremiumSender) 1.2.dp else 0.8.dp,
                    color = when {
                        isAdminMsg -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        isPremiumSender -> GoldBadgeBorder.copy(alpha = 0.55f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    },
                    shape = bubbleShape
                )
        ) {
            Column(
                modifier = Modifier
                    .background(containerColor)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Sender Header (clickable to open User Profile Card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenProfile)
                            .padding(vertical = 2.dp)
                            .testTag("chat_sender_header_${message.id}")
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAdminMsg) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isAdminMsg) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "YÖNETİCİ",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        } else if (isPremiumSender) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldBadgeBg,
                                border = BorderStroke(0.8.dp, GoldBadgeBorder.copy(alpha = 0.7f)),
                                modifier = Modifier.testTag("chat_premium_badge_${message.id}")
                            ) {
                                Text(
                                    text = "Premium 👑",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldBadgeText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Sabitli",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mesaj Seçenekleri",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profili Görüntüle") },
                                leadingIcon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onOpenProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Yanıtla") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onReplyClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Kopyala") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onCopyClick()
                                }
                            )
                            if (isViewerAdmin) {
                                DropdownMenuItem(
                                    text = { Text(if (message.isPinned) "Sabiti Kaldır" else "Odaya Sabitle 📌") },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onTogglePin()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mesajı Gizle") },
                                    leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onHideMessage()
                                    }
                                )
                                if (!isAdminMsg) {
                                    DropdownMenuItem(
                                        text = { Text("Kullanıcıyı Sustur 🔇") },
                                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            onMuteUser()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Kalıcı Sil", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDeleteMessage()
                                    }
                                )
                            }
                        }
                    }
                }

                // Quoted Reply Snippet
                if (!message.replyToSnippet.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                            Text(
                                text = message.replyToSenderName ?: "Yanıt",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = message.replyToSnippet,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Message Body
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                // Footer: Timestamp + Heart Like + Reply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onReplyClick() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Yanıtla",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Yanıtla",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLikeClick() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Beğen",
                                modifier = Modifier.size(14.dp),
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (message.likesCount > 0) {
                                Text(
                                    text = "${message.likesCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
