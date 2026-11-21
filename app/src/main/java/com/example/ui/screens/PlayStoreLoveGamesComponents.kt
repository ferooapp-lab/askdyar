package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateWheelData
import com.example.data.model.DateWheelSlice
import com.example.data.model.DynamicMood
import com.example.data.model.LoveBadge
import com.example.data.model.LoveLanguageQuizData
import com.example.data.model.LoveLanguageType
import com.example.data.model.RomanticDiceData
import com.example.data.model.RomanticUniverseGenerator
import com.example.data.model.SpecialCountdownEvent
import kotlinx.coroutines.launch

/**
 * 1. 1,200+ Çalışan Dinamik Aşk Sözü Motoru
 */
@Composable
fun InfiniteQuoteEngineSection(
    onSaveToNotes: (String, String) -> Unit,
    onOpenInStudio: (String, String) -> Unit,
    onSaveToFavorites: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var selectedMood by remember { mutableStateOf(DynamicMood.TUTKU) }
    var endearmentIdx by remember { mutableIntStateOf(0) }
    var templateIdx by remember { mutableIntStateOf(0) }

    val currentQuote = remember(selectedMood, endearmentIdx, templateIdx) {
        RomanticUniverseGenerator.generate(selectedMood, endearmentIdx, templateIdx)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .testTag("infinite_quote_engine_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                    Text("✨", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Sonsuz Söz Motoru (${RomanticUniverseGenerator.totalVariationsCount}+)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Duygu ve hitap seçerek anlık binlerce özgün söz üret",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mood Selector
            Text("Duygu & An Seç:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(DynamicMood.values()) { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = mood },
                        label = { Text("${mood.emoji} ${mood.label}", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Endearment Selector
            Text("Sevgiline Hitap Şekli:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(RomanticUniverseGenerator.endearments) { idx, word ->
                    FilterChip(
                        selected = endearmentIdx == idx,
                        onClick = { endearmentIdx = idx },
                        label = { Text(word, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Generated Quote Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentQuote,
                    transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(150))) },
                    label = "dynamic_quote_anim"
                ) { text ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "\"$text\"",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 24.sp
                        )
                    }
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
                        templateIdx = (templateIdx + 1) % 12
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yeni Kombinasyon", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row {
                    IconButton(onClick = {
                        onSaveToFavorites(
                            selectedMood.label,
                            currentQuote,
                            RomanticUniverseGenerator.endearments[endearmentIdx]
                        )
                    }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorilerime Ekle", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        onSaveToNotes(selectedMood.label, currentQuote)
                        Toast.makeText(context, "Söz notlarına kaydedildi! 💌", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Notlara Kaydet", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        onOpenInStudio(currentQuote, RomanticUniverseGenerator.endearments[endearmentIdx])
                    }) {
                        Icon(Icons.Default.Palette, contentDescription = "Kart Yap", tint = MaterialTheme.colorScheme.tertiary)
                    }
                    IconButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Aşk Sözü", currentQuote))
                        Toast.makeText(context, "Kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(currentQuote)}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, currentQuote)
                            }
                            context.startActivity(Intent.createChooser(share, "Paylaş"))
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }
                }
            }
        }
    }
}

/**
 * 2. Randevu Karar Çarkı (Spinning Date Night Wheel)
 */
@Composable
fun DateNightWheelSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var selectedSlice by remember { mutableStateOf<DateWheelSlice?>(DateWheelData.slices.first()) }
    val slices = DateWheelData.slices

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .testTag("date_wheel_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🎡", fontSize = 24.sp)
                Column {
                    Text(
                        text = "Romantik Randevu Çarkı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bugün ne yapsak kararsızsan çarkı çevir!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pointer Triangle + Spinning Wheel Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                Canvas(modifier = Modifier.size(170.dp)) {
                    val sweep = 360f / slices.size
                    rotate(degrees = rotation.value) {
                        slices.forEachIndexed { index, slice ->
                            drawArc(
                                color = Color(slice.colorHex),
                                startAngle = index * sweep - 90f,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height)
                            )
                        }
                    }
                    // Center pin
                    drawCircle(
                        color = Color.White,
                        radius = 18.dp.toPx(),
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                    drawCircle(
                        color = Color(0xFFE91E63),
                        radius = 9.dp.toPx(),
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }

                // Top Indicator Arrow
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(22.dp)
                ) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height)
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, color = Color(0xFF212121))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            selectedSlice?.let { slice ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${slice.emoji} ${slice.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slice.detail,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!isSpinning) {
                            scope.launch {
                                isSpinning = true
                                val extraSpins = (3..5).random() * 360f
                                val targetSliceIndex = slices.indices.random()
                                val sweep = 360f / slices.size
                                val sliceTargetAngle = 360f - (targetSliceIndex * sweep + sweep / 2f)
                                val currentMod = rotation.value % 360f
                                val delta = extraSpins + sliceTargetAngle - currentMod
                                rotation.animateTo(
                                    targetValue = rotation.value + delta,
                                    animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing)
                                )
                                selectedSlice = slices[targetSliceIndex]
                                isSpinning = false
                            }
                        }
                    },
                    enabled = !isSpinning,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSpinning) "Çark Dönüyor..." else "Çarkı Çevir!", fontWeight = FontWeight.Bold)
                }

                selectedSlice?.let { slice ->
                    OutlinedButton(
                        onClick = {
                            val msg = "🎡 Bugünkü Randevu Planımız: ${slice.emoji} ${slice.title}\n${slice.detail}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(intent, "Planı Paylaş"))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Paylaş")
                    }
                }
            }
        }
    }
}

/**
 * 3. Romantik Aşk Zarı (Couple Love Dice)
 */
@Composable
fun RomanticDiceSection() {
    var currentRoll by remember { mutableStateOf(RomanticDiceData.roll()) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .testTag("romantic_dice_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🎲", fontSize = 24.sp)
                Column {
                    Text(
                        text = "Çiftler İçin Aşk Zarı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sırayla zarı atın, çıkan romantik görevi hemen uygulayın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ZAR 1 • EYLEM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentRoll.second,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ZAR 2 • SÜRE / ŞART", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentRoll.third,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { currentRoll = RomanticDiceData.roll() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zarları At (${currentRoll.first})", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 4. 5 Sevgi Dili Testi (Interactive Quiz)
 */
@Composable
fun LoveLanguageQuizSection(
    savedLanguage: LoveLanguageType?,
    onSaveResult: (LoveLanguageType) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateListOf<LoveLanguageType>() }
    var isTakingQuiz by remember { mutableStateOf(savedLanguage == null) }

    val questions = LoveLanguageQuizData.questions

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .testTag("love_language_quiz_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                    Text("🗣️", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "5 Sevgi Dili Keşif Testi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kalbinin en çok hangi sevgi dilini konuştuğunu keşfet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isTakingQuiz && savedLanguage != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${savedLanguage.emoji} Senin Sevgi Dilin: ${savedLanguage.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(savedLanguage.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Partner İpucu: ${savedLanguage.partnerTip}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        selectedAnswers.clear()
                        currentIndex = 0
                        isTakingQuiz = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Testi Tekrar Çöz")
                }
            } else {
                val q = questions[currentIndex]
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Soru ${currentIndex + 1} / ${questions.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    q.options.forEach { (optionText, langType) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAnswers.add(langType)
                                    if (currentIndex < questions.lastIndex) {
                                        currentIndex += 1
                                    } else {
                                        val result = LoveLanguageQuizData.evaluate(selectedAnswers)
                                        onSaveResult(result)
                                        isTakingQuiz = false
                                    }
                                }
                        ) {
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. Çift Nefes & Kalp Senkronizasyonu (Animated Mindfulness)
 */
@Composable
fun CoupleBreathingSection() {
    var isActive by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val phaseText = if (!isActive) "Başlamak için dokunun" else if (pulse > 0.78f) "Derin Nefes Al & Kalbini Hisset 🌬️" else "Yavaşça Bırak & Huzurla Gülümse 💖"
    val primaryColor = MaterialTheme.colorScheme.primary

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .testTag("couple_breathing_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = primaryColor)
                Column {
                    Text(
                        text = "Çift Nefes & Kalp Senkronizasyonu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tartışma sonrası veya uyumadan önce birlikte 1 dakika sakinleşin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .clickable { isActive = !isActive }
            ) {
                val effectiveScale = if (isActive) pulse else 0.7f
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.14f),
                        radius = (size.minDimension / 2f) * effectiveScale
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.30f),
                        radius = (size.minDimension / 2.8f) * effectiveScale
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = (size.minDimension / 4.8f) * effectiveScale
                    )
                }
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Kalp Senkronizasyonu",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = phaseText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { isActive = !isActive },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isActive) "Duraklat" else "Nefes Egzersizini Başlat")
            }
        }
    }
}

/**
 * 6. Özel Günler Geri Sayım & Sevgi Serisi Rozetleri
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecialCountdownsAndBadgesSection(
    streakDays: Int,
    notesCount: Int,
    favoritesCount: Int,
    countdowns: List<SpecialCountdownEvent>,
    onAddCountdown: (title: String, emoji: String, daysFromNow: Int) -> Unit,
    onDeleteCountdown: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val badges = remember(streakDays, notesCount, favoritesCount) {
        listOf(
            LoveBadge("b1", "İlk Kıvılcım", "Uygulamayı sevgiyle açtın", "✨", streakDays >= 1),
            LoveBadge("b2", "3 Günlük Sadakat", "3 gün üst üste sevgi serisi", "🔥", streakDays >= 3),
            LoveBadge("b3", "Aşk Şairi", "En az 3 aşk notu kaydettin", "✍️", notesCount >= 3),
            LoveBadge("b4", "Kalp Koleksiyoneri", "En az 5 sözü favoriledin", "💖", favoritesCount >= 5)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Streak & Badges Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                .testTag("love_streak_badges_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF5722))
                        Column {
                            Text("Günlük Sevgi Serisi & Rozetler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Her gün romantizmle bağını canlı tut", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF5722).copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "🔥 $streakDays Gün",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE64A19),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.forEach { badge ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(if (badge.isUnlocked) badge.emoji else "🔒", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Special Countdowns Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                .testTag("special_countdowns_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Özel Günler Geri Sayımı 📅", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Doğum günü, yıldönümü ve tatil heyecanı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Özel Gün Ekle", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    countdowns.forEach { ev ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(ev.emoji, fontSize = 22.sp)
                                    Column {
                                        Text(ev.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (ev.daysRemaining() > 0) "${ev.daysRemaining()} gün kaldı ✨" else "Bugün o özel gün! 🎉",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(onClick = { onDeleteCountdown(ev.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("🌹") }
        var daysInput by remember { mutableStateOf("14") }
        val emojiChoices = listOf("🌹", "🎂", "💍", "✈️", "🥂", "💖")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Özel Gün Geri Sayımı Ekle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Etkinlik Adı") },
                        placeholder = { Text("Örn: Sevgilimin Doğum Günü") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = { daysInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Kaç Gün Kaldı?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        emojiChoices.forEach { e ->
                            FilterChip(
                                selected = emoji == e,
                                onClick = { emoji = e },
                                label = { Text(e, fontSize = 16.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = daysInput.toIntOrNull()?.coerceIn(1, 3650) ?: 14
                        if (title.isNotBlank()) {
                            onAddCountdown(title, emoji, days)
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            }
        )
    }
}
