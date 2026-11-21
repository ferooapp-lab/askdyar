package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.UserAccountEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminUsersManagementTab(
    users: List<UserAccountEntity>,
    onToggleBan: (userId: Long, banned: Boolean, reason: String) -> Unit,
    onApplyRestriction: (
        userId: Long,
        isRestricted: Boolean,
        canSubmitQuotes: Boolean,
        canComment: Boolean,
        canUploadMusic: Boolean,
        canChat: Boolean,
        reason: String,
        durationHours: Int?
    ) -> Unit,
    onDeleteUser: (Long) -> Unit
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var statusFilter by remember { mutableIntStateOf(0) } // 0=Tümü, 1=Aktif, 2=Kısıtlı, 3=Banlı

    var restrictingUser by remember { mutableStateOf<UserAccountEntity?>(null) }
    var banningUser by remember { mutableStateOf<UserAccountEntity?>(null) }
    var deletingUser by remember { mutableStateOf<UserAccountEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr")) }
    val now = System.currentTimeMillis()

    fun UserAccountEntity.hasActiveRestriction(): Boolean {
        return !isBlocked && isRestricted && (restrictedUntil == null || restrictedUntil > now)
    }

    val filtered = remember(users, search, statusFilter) {
        users.filter { user ->
            val matchQuery = search.isBlank() ||
                user.fullName.contains(search, ignoreCase = true) ||
                user.email.contains(search, ignoreCase = true)
            val matchStatus = when (statusFilter) {
                1 -> !user.isBlocked && !user.hasActiveRestriction()
                2 -> user.hasActiveRestriction()
                3 -> user.isBlocked
                else -> true
            }
            matchQuery && matchStatus
        }
    }

    val activeCount = users.count { !it.isBlocked && !it.hasActiveRestriction() }
    val restrictedCount = users.count { it.hasActiveRestriction() }
    val bannedCount = users.count { it.isBlocked }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 64.dp),
        modifier = Modifier.testTag("admin_users_tab")
    ) {
        // Search Field + Google Auto-Onboarding Info
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Ad veya e-posta ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                Text(
                    text = "Kullanıcılar Google ile giriş yaptığında Firebase üzerinden anlık olarak buraya eklenir.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserStatChip("Toplam", "${users.size}", MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f), MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                    statusFilter = 0
                }
                UserStatChip("Aktif", "$activeCount", Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f)) {
                    statusFilter = 1
                }
                UserStatChip("Kısıtlı", "$restrictedCount", Color(0xFFFFF8E1), Color(0xFFEF6C00), Modifier.weight(1f)) {
                    statusFilter = 2
                }
                UserStatChip("Banlı", "$bannedCount", MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), MaterialTheme.colorScheme.error, Modifier.weight(1f)) {
                    statusFilter = 3
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(
                        0 to "Tümü (${users.size})",
                        1 to "Aktif ($activeCount)",
                        2 to "Kısıtlı ($restrictedCount)",
                        3 to "Banlı ($bannedCount)"
                    )
                ) { (idx, label) ->
                    FilterChip(
                        selected = statusFilter == idx,
                        onClick = { statusFilter = idx },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (users.isEmpty()) "Henüz kayıtlı kullanıcı bulunmuyor." else "Bu filtrede kullanıcı yok.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kullanıcılar Google hesabıyla giriş yaptığında anlık olarak burada listelenir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { user ->
                val isBanned = user.isBlocked
                val isRestricted = user.hasActiveRestriction()

                val borderColor = when {
                    isBanned -> MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                    isRestricted -> Color(0xFFEF6C00).copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .testTag("admin_user_card_${user.id}"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isBanned -> MaterialTheme.colorScheme.errorContainer
                                        isRestricted -> Color(0xFFFFF3E0)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when {
                                                isBanned -> Icons.Default.Block
                                                isRestricted -> Icons.Default.Warning
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                isBanned -> MaterialTheme.colorScheme.error
                                                isRestricted -> Color(0xFFEF6C00)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = user.fullName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when {
                                                isBanned -> MaterialTheme.colorScheme.error
                                                isRestricted -> Color(0xFFEF6C00)
                                                else -> Color(0xFF2E7D32)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    isBanned -> "BANLI"
                                                    isRestricted -> "KISITLI"
                                                    else -> "AKTİF"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Partner: ${user.partnerName} • ${dateFormat.format(Date(user.createdAt))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Active Ban / Restriction Details Chip Strip
                        if (isBanned || isRestricted) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isBanned) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                                else Color(0xFFFFF8E1),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isRestricted) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (!user.canSubmitQuotes) {
                                                RestrictionPill("Söz Paylaşımı Kapalı")
                                            }
                                            if (!user.canComment) {
                                                RestrictionPill("Yorum Yapma Kapalı")
                                            }
                                            if (!user.canUploadMusic) {
                                                RestrictionPill("Müzik Ekleme Kapalı")
                                            }
                                            if (!user.canChat) {
                                                RestrictionPill("Sohbette Susturuldu")
                                            }
                                        }
                                        user.restrictedUntil?.let { untilMs ->
                                            Text(
                                                text = "Bitiş: ${SimpleDateFormat("dd MMM HH:mm", Locale("tr")).format(Date(untilMs))}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    if (user.restrictionReason.isNotBlank()) {
                                        Text(
                                            text = "Sebep: ${user.restrictionReason}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isBanned) MaterialTheme.colorScheme.error else Color(0xFFE65100)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        // Instant Moderation Action Bar (Restrict / Ban / Delete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { restrictingUser = user },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                                    .testTag("user_restrict_btn_${user.id}")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isRestricted) "Kısıtı Yönet" else "Kısıtla",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Button(
                                onClick = {
                                    if (isBanned) {
                                        onToggleBan(user.id, false, "")
                                    } else {
                                        banningUser = user
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBanned) Color(0xFF2E7D32) else Color(0xFFE65100)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                                    .testTag("user_ban_btn_${user.id}")
                            ) {
                                Icon(
                                    imageVector = if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBanned) "Banı Aç" else "Banla",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = { deletingUser = user },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .sizeIn(minHeight = 48.dp)
                                    .testTag("user_delete_btn_${user.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Kullanıcıyı Sil", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Sil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Instant Restriction Dialog
    restrictingUser?.let { target ->
        var restrictQuotes by remember(target.id) { mutableStateOf(!target.canSubmitQuotes) }
        var restrictComments by remember(target.id) { mutableStateOf(!target.canComment) }
        var restrictMusic by remember(target.id) { mutableStateOf(!target.canUploadMusic) }
        var restrictChat by remember(target.id) { mutableStateOf(!target.canChat) }
        var durationHours by remember(target.id) { mutableStateOf<Int?>(24) }
        var reason by remember(target.id) { mutableStateOf(target.restrictionReason) }

        AlertDialog(
            onDismissRequest = { restrictingUser = null },
            title = { Text("${target.fullName} — Kısıtlama", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminToggleRow(
                        title = "Söz Paylaşımını Kısıtla",
                        subtitle = "Toplulukta yeni söz paylaşamasın",
                        checked = restrictQuotes,
                        onCheckedChange = { restrictQuotes = it }
                    )
                    AdminToggleRow(
                        title = "Yorum Yapmayı Kısıtla",
                        subtitle = "Sözlerin altına yorum bırakamasın",
                        checked = restrictComments,
                        onCheckedChange = { restrictComments = it }
                    )
                    AdminToggleRow(
                        title = "Özel Müzik Eklemeyi Kısıtla",
                        subtitle = "Cihazdan müzik yükleyemesin",
                        checked = restrictMusic,
                        onCheckedChange = { restrictMusic = it }
                    )
                    AdminToggleRow(
                        title = "Sohbet Odasında Sustur",
                        subtitle = "Topluluk sohbetine mesaj yazamasın",
                        checked = restrictChat,
                        onCheckedChange = { restrictChat = it }
                    )

                    Text("Süre:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(24 to "24 Saat", 168 to "7 Gün", null to "Kalıcı").forEach { (hrs, label) ->
                            FilterChip(
                                selected = durationHours == hrs,
                                onClick = { durationHours = hrs },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Kısıtlama Sebebi (Opsiyonel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (target.isRestricted) {
                        TextButton(
                            onClick = {
                                onApplyRestriction(target.id, false, true, true, true, true, "", null)
                                restrictingUser = null
                            }
                        ) {
                            Text("Tüm Kısıtlamaları Kaldır ✓", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val anyRestricted = restrictQuotes || restrictComments || restrictMusic || restrictChat
                        onApplyRestriction(
                            target.id,
                            anyRestricted,
                            !restrictQuotes,
                            !restrictComments,
                            !restrictMusic,
                            !restrictChat,
                            if (anyRestricted) reason.ifBlank { "Topluluk kuralları gereği kısıtlandı" } else "",
                            if (anyRestricted) durationHours else null
                        )
                        restrictingUser = null
                    }
                ) {
                    Text("Anında Uygula")
                }
            },
            dismissButton = {
                TextButton(onClick = { restrictingUser = null }) { Text("İptal") }
            }
        )
    }

    // 2. Instant Ban Confirmation Dialog
    banningUser?.let { target ->
        var banReason by remember(target.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { banningUser = null },
            title = { Text("${target.fullName} Kullanıcısını Banla 🚫", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kullanıcı anında sistemden çıkarılacak ve tekrar giriş yapamayacaktır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = banReason,
                        onValueChange = { banReason = it },
                        label = { Text("Ban Sebebi (Opsiyonel)") },
                        placeholder = { Text("Örn: Kurallara aykırı paylaşım") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBan(target.id, true, banReason)
                        banningUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Anında Banla")
                }
            },
            dismissButton = {
                TextButton(onClick = { banningUser = null }) { Text("Vazgeç") }
            }
        )
    }

    // 3. Instant Delete Confirmation Dialog
    deletingUser?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = { Text("Kullanıcıyı Kalıcı Sil", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "${target.fullName} (${target.email}) hesabı anında ve kalıcı olarak silinecek. Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target.id)
                        deletingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Kalıcı Olarak Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) { Text("İptal") }
            }
        )
    }

}

@Composable
private fun UserStatChip(
    label: String,
    count: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = textColor)
        }
    }
}

@Composable
private fun RestrictionPill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFEF6C00).copy(alpha = 0.16f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE65100),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
