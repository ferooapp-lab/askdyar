package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.AdminSettings
import com.example.data.model.ChatRoom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminChatControlTab(
    settings: AdminSettings,
    allMessages: List<ChatMessageEntity>,
    users: List<UserAccountEntity>,
    onSaveSettings: (AdminSettings) -> Unit,
    onSendAdminMessage: (room: ChatRoom, message: String, pin: Boolean) -> Unit,
    onApproveMessage: (Long) -> Unit,
    onHideMessage: (Long) -> Unit,
    onTogglePinMessage: (ChatMessageEntity) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onClearRoom: (String?) -> Unit,
    onMuteUser: (userId: Long, muted: Boolean, reason: String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM HH:mm", Locale("tr")) }

    var chatEnabled by remember(settings) { mutableStateOf(settings.chatEnabled) }
    var closedMessage by remember(settings) { mutableStateOf(settings.chatClosedMessage) }
    var allowGuests by remember(settings) { mutableStateOf(settings.chatAllowGuests) }
    var requireApproval by remember(settings) { mutableStateOf(settings.chatRequireApproval) }
    var slowModeSec by remember(settings) { mutableIntStateOf(settings.chatSlowModeSeconds) }
    var maxChars by remember(settings) { mutableIntStateOf(settings.chatMaxCharCount) }
    var blockedWordsCsv by remember(settings) { mutableStateOf(settings.chatBlockedWordsCsv) }

    var adminRoom by remember { mutableStateOf(ChatRoom.GENEL) }
    var adminBroadcastMsg by remember { mutableStateOf("") }
    var pinAdminMsg by remember { mutableStateOf(true) }

    var messageFilter by remember { mutableIntStateOf(0) } // 0=Tümü, 1=Onay Bekleyen, 2=Sabitlenen, 3=Gizlenen
    var roomClearTarget by remember { mutableStateOf<ChatRoom?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val pendingMessages = remember(allMessages) { allMessages.filter { it.status == "PENDING" } }
    val pinnedMessages = remember(allMessages) { allMessages.filter { it.isPinned && it.status == "PUBLISHED" } }
    val hiddenMessages = remember(allMessages) { allMessages.filter { it.status == "HIDDEN" } }

    val filteredMessages = remember(allMessages, messageFilter) {
        when (messageFilter) {
            1 -> allMessages.filter { it.status == "PENDING" }
            2 -> allMessages.filter { it.isPinned }
            3 -> allMessages.filter { it.status == "HIDDEN" }
            else -> allMessages
        }
    }

    val now = System.currentTimeMillis()
    val mutedUsers = remember(users, now) {
        users.filter { user ->
            !user.canChat && user.isRestricted && (user.restrictedUntil == null || user.restrictedUntil > now)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 64.dp),
        modifier = Modifier.testTag("admin_chat_control_tab")
    ) {
        // 1. Master Chat Switch & Access Control
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (chatEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error,
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (chatEnabled) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = if (chatEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Column {
                                Text(
                                    text = "Topluluk Sohbeti Kontrolü",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (chatEnabled) "AÇIK: Kullanıcılar sohbet odalarında etkileşimde"
                                    else "KAPALI: Sohbet yazımı yönetici tarafından durduruldu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (chatEnabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Switch(
                            checked = chatEnabled,
                            onCheckedChange = { enabled ->
                                chatEnabled = enabled
                                onSaveSettings(
                                    settings.copy(
                                        chatEnabled = enabled,
                                        chatClosedMessage = closedMessage,
                                        chatAllowGuests = allowGuests,
                                        chatRequireApproval = requireApproval,
                                        chatSlowModeSeconds = slowModeSec,
                                        chatMaxCharCount = maxChars,
                                        chatBlockedWordsCsv = blockedWordsCsv
                                    )
                                )
                            },
                            modifier = Modifier.testTag("admin_chat_master_switch")
                        )
                    }

                    OutlinedTextField(
                        value = closedMessage,
                        onValueChange = { closedMessage = it },
                        label = { Text("Sohbet Kapalıyken Gösterilecek Mesaj") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    AdminToggleRow(
                        title = "Misafirler Sohbete Yazabilsin",
                        subtitle = "Kapalıysa sadece kayıtlı üyeler mesaj gönderebilir",
                        checked = allowGuests,
                        onCheckedChange = { allowGuests = it }
                    )

                    AdminToggleRow(
                        title = "Mesajları Admin Onayından Geçir",
                        subtitle = "Aktifken yeni mesajlar önce onay kuyruğuna düşer",
                        checked = requireApproval,
                        onCheckedChange = { requireApproval = it }
                    )
                }
            }
        }

        // 2. Slow Mode, Length & Profanity Filter Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Yavaş Mod & İçerik Filtresi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text("Mesaj Gönderme Aralığı (Yavaş Mod):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "Kapalı", 10 to "10 sn", 30 to "30 sn", 60 to "60 sn").forEach { (sec, label) ->
                            FilterChip(
                                selected = slowModeSec == sec,
                                onClick = { slowModeSec = sec },
                                label = { Text(label, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Text("Maksimum Mesaj Uzunluğu ($maxChars karakter):", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = maxChars.toFloat(),
                        onValueChange = { maxChars = it.toInt() },
                        valueRange = 100f..500f,
                        steps = 7
                    )

                    OutlinedTextField(
                        value = blockedWordsCsv,
                        onValueChange = { blockedWordsCsv = it },
                        label = { Text("Yasaklı Kelimeler (Virgülle ayır)") },
                        placeholder = { Text("Örn: küfür,hakaret,spam,reklam") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            onSaveSettings(
                                settings.copy(
                                    chatEnabled = chatEnabled,
                                    chatClosedMessage = closedMessage.trim().ifBlank { "Sohbet odaları şu anda kapalıdır." },
                                    chatAllowGuests = allowGuests,
                                    chatRequireApproval = requireApproval,
                                    chatSlowModeSeconds = slowModeSec,
                                    chatMaxCharCount = maxChars,
                                    chatBlockedWordsCsv = blockedWordsCsv
                                )
                            )
                            Toast.makeText(context, "Sohbet kontrol kuralları kaydedildi! 💾", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                            .testTag("admin_chat_save_rules_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sohbet Kurallarını Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Direct Admin Message / Pin Announcement in Chat
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sohbet Odasına Yönetici Mesajı Gönder 👑",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChatRoom.values().forEach { room ->
                            FilterChip(
                                selected = adminRoom == room,
                                onClick = { adminRoom = room },
                                label = { Text("${room.emoji} ${room.title}", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = adminBroadcastMsg,
                        onValueChange = { adminBroadcastMsg = it },
                        label = { Text("Yönetici Mesajı") },
                        placeholder = { Text("Örn: Akşam 21:00'de en güzel şiir etkinliğimiz başlıyor!") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    AdminToggleRow(
                        title = "Odanın Üstüne Sabitle 📌",
                        subtitle = "Odaya giren tüm kullanıcılar üstte bu mesajı görür",
                        checked = pinAdminMsg,
                        onCheckedChange = { pinAdminMsg = it }
                    )

                    Button(
                        onClick = {
                            onSendAdminMessage(adminRoom, adminBroadcastMsg, pinAdminMsg)
                            adminBroadcastMsg = ""
                        },
                        enabled = adminBroadcastMsg.trim().length >= 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                            .testTag("admin_chat_send_official_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yönetici Olarak Gönder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Muted Users Quick Unmute List
        if (mutedUsers.isNotEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF6C00).copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sohbette Susturulan Üyeler (${mutedUsers.size}) 🔇",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        mutedUsers.forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.fullName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = user.restrictionReason.ifBlank { "Sohbet susturması aktif" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        onMuteUser(user.id, false, "")
                                        Toast.makeText(context, "${user.fullName} susturması kaldırıldı 🔊", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Susturmayı Aç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Live Message Moderation Feed Header + Clear Room Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sohbet Mesajları Moderasyonu (${allMessages.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    TextButton(
                        onClick = { showClearAllDialog = true },
                        enabled = allMessages.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tümünü Temizle", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        listOf(
                            0 to "Tümü (${allMessages.size})",
                            1 to "Onay Bekleyen (${pendingMessages.size})",
                            2 to "Sabitlenen (${pinnedMessages.size})",
                            3 to "Gizlenen (${hiddenMessages.size})"
                        )
                    ) { (idx, label) ->
                        FilterChip(
                            selected = messageFilter == idx,
                            onClick = { messageFilter = idx },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ChatRoom.values()) { room ->
                        OutlinedButton(
                            onClick = { roomClearTarget = room },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${room.emoji} ${room.title} Temizle", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        if (filteredMessages.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu filtrede mesaj bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredMessages, key = { it.id }) { msg ->
                val room = ChatRoom.fromId(msg.roomId)
                val statusColor = when (msg.status) {
                    "PENDING" -> Color(0xFFEF6C00)
                    "HIDDEN" -> MaterialTheme.colorScheme.error
                    else -> Color(0xFF2E7D32)
                }
                val statusLabel = when (msg.status) {
                    "PENDING" -> "ONAY BEKLİYOR"
                    "HIDDEN" -> "GİZLENDİ"
                    else -> "YAYINDA"
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (msg.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "${room.emoji} ${room.title}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (msg.isPinned) {
                                    Text("📌", fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = dateFormat.format(Date(msg.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${msg.senderName} (${msg.senderRole})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = msg.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (msg.status != "PUBLISHED") {
                                Button(
                                    onClick = { onApproveMessage(msg.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Onayla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { onTogglePinMessage(msg) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.sizeIn(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (msg.isPinned) "Sabiti Kaldır" else "Sabitle", fontSize = 11.sp)
                            }

                            if (msg.status == "PUBLISHED") {
                                OutlinedButton(
                                    onClick = { onHideMessage(msg.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gizle", fontSize = 11.sp)
                                }
                            }

                            if (msg.senderUserId > 0L && msg.senderRole != "ADMIN") {
                                OutlinedButton(
                                    onClick = {
                                        onMuteUser(msg.senderUserId, true, "Sohbet moderasyonu")
                                        onHideMessage(msg.id)
                                        Toast.makeText(context, "${msg.senderName} sohbette susturuldu 🔇", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sustur", fontSize = 11.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = { onDeleteMessage(msg.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.sizeIn(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sil", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    roomClearTarget?.let { targetRoom ->
        AlertDialog(
            onDismissRequest = { roomClearTarget = null },
            title = { Text("${targetRoom.title} Odasını Temizle", fontWeight = FontWeight.Bold) },
            text = { Text("Bu odadaki tüm sohbet mesajları kalıcı olarak silinecek. Onaylıyor musunuz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearRoom(targetRoom.id)
                        roomClearTarget = null
                        Toast.makeText(context, "${targetRoom.title} temizlendi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Odayı Temizle")
                }
            },
            dismissButton = {
                TextButton(onClick = { roomClearTarget = null }) { Text("İptal") }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Tüm Sohbet Odalarını Temizle", fontWeight = FontWeight.Bold) },
            text = { Text("Tüm sohbet odalarındaki mesajlar kalıcı olarak silinecek. Emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearRoom(null)
                        showClearAllDialog = false
                        Toast.makeText(context, "Tüm sohbet odaları temizlendi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hepsini Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}
