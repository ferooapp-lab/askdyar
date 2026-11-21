package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoupleTriviaData
import com.example.data.model.CoupleTruthOrDareData
import com.example.data.model.GiftBudget
import com.example.data.model.GiftSetting
import com.example.data.model.GiftSurpriseFinderData
import com.example.data.model.LoveTimeCapsule
import com.example.data.model.MilestonesTimelineCalculator
import com.example.data.model.TruthDareLevel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =========================================================================
// 1. "SENİ ÖZLEDİM" CANLI KALP & ÖPÜCÜK SAYACI
// =========================================================================
@Composable
fun MissYouLiveCard(
    partnerName: String,
    totalCount: Int,
    todayCount: Int,
    onSendPing: () -> Unit
) {
    val context = LocalContext.current
    var pulseTrigger by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pulseTrigger) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "missYouPulse"
    )

    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger) {
            delay(220)
            pulseTrigger = false
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                RoundedCornerShape(24.dp)
            )
            .testTag("miss_you_live_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💓 Seni Özledim Kalbi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$partnerName aklına her düştüğünde dokun",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Bugün: $todayCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        pulseTrigger = true
                        onSendPing()
                        Toast.makeText(context, "$partnerName için kalpten öpücük gönderildi! 💋", Toast.LENGTH_SHORT).show()
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(92.dp)
                        .scale(scale)
                        .testTag("miss_you_heart_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Seni Özledim Gönder",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "${totalCount}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Toplam $totalCount kez sevgi sinyali gönderildi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val msg = "$partnerName, şu an seni çok özledim! 💖 Bugün tam ${todayCount.coerceAtLeast(1)}. kez kalbimden sana öpücük gönderdim! 💋"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(msg)}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(share, "Paylaş"))
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp'tan 'Seni Özledim' At", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// =========================================================================
// 2. AŞK KAVANOZU ("Bana Neden Aşıksın?" Kavanozu)
// =========================================================================
@Composable
fun LoveJarCard(
    reasons: List<String>,
    onAddReason: (String) -> Unit,
    onSaveToFavorites: (String) -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember(reasons.size) { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val currentReason = reasons.getOrNull(currentIndex.coerceIn(0, (reasons.size - 1).coerceAtLeast(0)))
        ?: "Gülüşün dünyama baharı getiriyor."

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("love_jar_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🫙", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Aşk Kavanozu (${reasons.size} Not)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "'Seni neden seviyorum?' kavanozundan bir not çek",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Yeni Sebep Ekle", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentReason,
                    transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(150))) },
                    label = "love_jar_anim"
                ) { reason ->
                    Text(
                        text = "💌 \"$reason\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 23.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (reasons.size > 1) {
                            var next = reasons.indices.random()
                            if (next == currentIndex) next = (currentIndex + 1) % reasons.size
                            currentIndex = next
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kavanozdan Çek", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Not Ekle", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        onSaveToFavorites(currentReason)
                        Toast.makeText(context, "Favorilerim kütüphanene eklendi! ❤️", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favoriye Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Kavanoza Sevgi Notu Ekle 🫙", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newReason,
                    onValueChange = { newReason = it },
                    label = { Text("Onu sevme sebebin") },
                    placeholder = { Text("Örn: Bana her baktığında gülümsemen...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newReason.isNotBlank()) {
                            onAddReason(newReason)
                            currentIndex = 0
                            showAddDialog = false
                            Toast.makeText(context, "Not kavanoza eklendi! 💌", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newReason.isNotBlank()
                ) {
                    Text("Kavanoza At")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            }
        )
    }
}

// =========================================================================
// 3. DOĞRULUK MU CESARET Mİ? (ÇİFTLER İÇİN)
// =========================================================================
@Composable
fun TruthOrDareCard(
    completedCount: Int,
    onCompleteChallenge: () -> Unit
) {
    val context = LocalContext.current
    var selectedLevel by remember { mutableStateOf(TruthDareLevel.SWEET) }
    var currentPrompt by remember { mutableStateOf(CoupleTruthOrDareData.prompts.first()) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("truth_or_dare_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎭", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Doğruluk mu Cesaret mi?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Çiftlere özel 3 seviyeli heyecan oyunu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "✅ $completedCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TruthDareLevel.values()) { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = {
                            selectedLevel = level
                            currentPrompt = CoupleTruthOrDareData.drawPrompt(level, null)
                        },
                        label = { Text("${level.emoji} ${level.label}", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (currentPrompt.isTruth) "💬 DOĞRULUK" else "🔥 CESARET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentPrompt.prompt,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 23.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { currentPrompt = CoupleTruthOrDareData.drawPrompt(selectedLevel, true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Doğruluk 💬", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { currentPrompt = CoupleTruthOrDareData.drawPrompt(selectedLevel, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cesaret 🔥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = {
                        onCompleteChallenge()
                        currentPrompt = CoupleTruthOrDareData.drawPrompt(selectedLevel, null)
                        Toast.makeText(context, "Görev tamamlandı! +1 puan 🏆", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Görevi Tamamladım", tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}

// =========================================================================
// 4. AŞK ZAMAN KAPSÜLÜ (Geleceğe Kilitli Mektup)
// =========================================================================
@Composable
fun TimeCapsuleCard(
    capsules: List<LoveTimeCapsule>,
    onCreateCapsule: (title: String, message: String, author: String, delayMillis: Long) -> Unit,
    onDeleteCapsule: (String) -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var openedCapsule by remember { mutableStateOf<LoveTimeCapsule?>(null) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("time_capsule_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⏳", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Aşk Zaman Kapsülü",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gelecek tarihe kilitli sürpriz mektuplar bırak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kapsül", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                capsules.forEach { cap ->
                    val unlocked = cap.isUnlocked()
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (unlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (unlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (unlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = cap.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${cap.author} • ${cap.remainingText()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (unlocked) {
                                    TextButton(onClick = { openedCapsule = cap }) {
                                        Text("Aç 💌", fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(onClick = { onDeleteCapsule(cap.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        val durationOptions = listOf(
            "1 Dakika (Hızlı Test)" to 60_000L,
            "7 Gün Sonra" to 7L * 86_400_000L,
            "1 Ay Sonra" to 30L * 86_400_000L,
            "6 Ay Sonra" to 180L * 86_400_000L,
            "1 Yıl Sonra" to 365L * 86_400_000L
        )
        var selectedIdx by remember { mutableIntStateOf(1) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Yeni Zaman Kapsülü Kilitle 🔒", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Kapsül Başlığı") },
                        placeholder = { Text("Örn: 1. Yılımıza Notum") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Yazan") },
                        placeholder = { Text("Örn: Gelecekteki Ben") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Geleceğe Mesajın") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Kilit Süresi Seç:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(durationOptions.indices.toList()) { idx ->
                            FilterChip(
                                selected = selectedIdx == idx,
                                onClick = { selectedIdx = idx },
                                label = { Text(durationOptions[idx].first, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            onCreateCapsule(title, message, author, durationOptions[selectedIdx].second)
                            showCreateDialog = false
                            Toast.makeText(context, "Zaman kapsülü mühürlendi! 🔒", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = message.isNotBlank()
                ) {
                    Text("Mühürle 🔒")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("İptal") }
            }
        )
    }

    openedCapsule?.let { cap ->
        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")) }
        AlertDialog(
            onDismissRequest = { openedCapsule = null },
            title = { Text("🔓 ${cap.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${cap.message}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        text = "— ${cap.author} (${dateFormat.format(Date(cap.createdAt))})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { openedCapsule = null }) { Text("Kapat 💖") }
            }
        )
    }
}

// =========================================================================
// 5. 2 KİŞİLİK ÇİFT QUIZİ ("Birbirimizi Ne Kadar Tanıyoruz?")
// =========================================================================
@Composable
fun CoupleTriviaQuizCard() {
    val questions = CoupleTriviaData.questions
    // phase: 0 = idle/start, 1 = partner 1 answering, 2 = handoff, 3 = partner 2 guessing, 4 = result
    var phase by remember { mutableIntStateOf(0) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val p1Answers = remember { mutableStateListOf<Int>() }
    val p2Answers = remember { mutableStateListOf<Int>() }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("couple_trivia_quiz_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = "Birbirimizi Ne Kadar Tanıyoruz?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "2 kişilik yan yana oynanan uyumluluk quizi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (phase) {
                0 -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎮 Nasıl Oynanır?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "1. Partner 1 kendi cevaplarını gizlice seçer.\n2. Telefon Partner 2'ye verilir ve tahminleri alınır.\n3. Sonunda çift uyum skorunuz açıklanır!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            p1Answers.clear()
                            p2Answers.clear()
                            currentIndex = 0
                            phase = 1
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Oyunu Başlat (Partner 1)", fontWeight = FontWeight.Bold)
                    }
                }
                1, 3 -> {
                    val q = questions[currentIndex]
                    val playerLabel = if (phase == 1) "👤 Partner 1 Cevaplıyor" else "👥 Partner 2 Tahmin Ediyor"
                    Text(
                        text = "$playerLabel (${currentIndex + 1}/${questions.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1f) / questions.size },
                        modifier = Modifier.fillMaxWidth().clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(q.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    q.options.forEachIndexed { idx, opt ->
                        OutlinedButton(
                            onClick = {
                                if (phase == 1) {
                                    p1Answers.add(idx)
                                    if (currentIndex < questions.lastIndex) {
                                        currentIndex++
                                    } else {
                                        phase = 2
                                    }
                                } else {
                                    p2Answers.add(idx)
                                    if (currentIndex < questions.lastIndex) {
                                        currentIndex++
                                    } else {
                                        phase = 4
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(opt, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontSize = 12.sp)
                        }
                    }
                }
                2 -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📱 Telefonu Partner 2'ye Ver!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Partner 1 tüm cevaplarını gizlice kaydetti. Şimdi sıra sende!",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    currentIndex = 0
                                    phase = 3
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Partner 2 Tahmine Başlasın", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {
                    val matches = questions.indices.count { i ->
                        p1Answers.getOrNull(i) == p2Answers.getOrNull(i)
                    }
                    val pct = (matches * 100) / questions.size
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🏆 %$pct Eşleşme ($matches / ${questions.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = when {
                                    pct >= 80 -> "Ruh İkizisiniz! Birbirinizi gözlerinizden anlıyorsunuz 💖"
                                    pct >= 50 -> "Harika Uyum! Birlikte sohbet ettikçe daha da güçleniyorsunuz ✨"
                                    else -> "Tatlı Keşif Zamanı! Bu akşam uzun bir kahve sohbeti sizi bekliyor ☕"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { phase = 0 },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tekrar Oyna 🔄")
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. AKILLI HEDİYE & SÜRPRİZ BULUCU
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GiftAndSurpriseFinderCard(
    onSaveIdeaToNotes: (String, String) -> Unit
) {
    val context = LocalContext.current
    var selectedBudget by remember { mutableStateOf<GiftBudget?>(null) }
    var selectedSetting by remember { mutableStateOf<GiftSetting?>(null) }
    val matchedIdeas = remember(selectedBudget, selectedSetting) {
        GiftSurpriseFinderData.findMatches(selectedBudget, selectedSetting)
    }
    var ideaIndex by remember(selectedBudget, selectedSetting) { mutableIntStateOf(0) }
    val idea = matchedIdeas[ideaIndex.coerceIn(0, (matchedIdeas.size - 1).coerceAtLeast(0))]

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("gift_surprise_finder_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = "Sürpriz & Hediye Fikri Bulucu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bütçe ve ortama göre anında romantik plan bul",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GiftBudget.values().forEach { b ->
                    FilterChip(
                        selected = selectedBudget == b,
                        onClick = { selectedBudget = if (selectedBudget == b) null else b },
                        label = { Text(b.label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                GiftSetting.values().forEach { s ->
                    FilterChip(
                        selected = selectedSetting == s,
                        onClick = { selectedSetting = if (selectedSetting == s) null else s },
                        label = { Text(s.label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${idea.emoji} ${idea.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = idea.description,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 19.sp
                    )
                    Text(
                        text = "💡 İpucu: ${idea.stepTip}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        ideaIndex = (ideaIndex + 1) % matchedIdeas.size
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Başka Fikir (${matchedIdeas.size})", fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        onSaveIdeaToNotes(idea.title, "${idea.description}\n\nİpucu: ${idea.stepTip}")
                        Toast.makeText(context, "Sürpriz fikri notlarına eklendi! 🎁", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Notlara Kaydet", fontSize = 12.sp)
                }
            }
        }
    }
}

// =========================================================================
// 7. İLİŞKİ KİLOMETRE TAŞLARI ZAMAN TÜNELİ
// =========================================================================
@Composable
fun MilestonesTimelineCard(currentDays: Long) {
    val milestones = remember(currentDays) { MilestonesTimelineCalculator.compute(currentDays) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .testTag("milestones_timeline_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = "İlişki Kilometre Taşları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentDays günlük aşk yolculuğunuzun durakları",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                milestones.forEach { m ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${m.emoji} ${m.title}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (m.isReached) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = if (m.isReached) "Tamamlandı ✅" else "${m.daysDelta} gün kaldı",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (m.isReached) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { m.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape),
                            color = if (m.isReached) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
