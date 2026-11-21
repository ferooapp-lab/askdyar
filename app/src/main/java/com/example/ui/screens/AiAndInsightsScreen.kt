package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AiQuoteUiState
import com.example.ui.LoveQuotesViewModel

@Composable
fun AiAndInsightsScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToNewNote: () -> Unit = {},
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_insights_screen")
    ) {
        // Modern Header + Segmented Pill Switcher
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.AutoAwesome else Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "AI Stüdyo & Duygu Analizi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Yapay zeka ile aşk sözleri üret ve 30 günlük duygu trendini izle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Modern Segmented Switcher
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SegmentedHubTab(
                        title = "✨ AI Söz & Şiir",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_ai_generator")
                    )
                    SegmentedHubTab(
                        title = "📊 Duygu İçgörüleri",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_mood_analysis")
                    )
                }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)).togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "ai_hub_tab_transition"
        ) { tab ->
            when (tab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. AI Aşk Sözü Üreticisi (Extracted from cluttered Home)
                        item {
                            AiQuoteGeneratorCard(
                                viewModel = viewModel,
                                onNavigateToStudio = onNavigateToStudio
                            )
                        }

                        // 2. AI İsim Akrostiş Şiir Üreticisi
                        item {
                            AiAcrosticPoemCard(
                                viewModel = viewModel,
                                onNavigateToStudio = onNavigateToStudio
                            )
                        }

                        // 3. AI İlişki Durum Mesajı Koçu
                        item {
                            AiSituationCoachCard(
                                viewModel = viewModel,
                                onNavigateToStudio = onNavigateToStudio
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Interactive Today's Mood Check-in
                        item {
                            DailyMoodCheckInCard(viewModel = viewModel)
                        }

                        // 2. 30-Day Mood Insights & Romantic Consistency Dashboard
                        item {
                            MoodInsights30DayCard(
                                viewModel = viewModel,
                                onNavigateToNewNote = onNavigateToNewNote
                            )
                        }

                        // 3. 7 Günlük Gemini AI Duygu & Aşk Analizi Grafiği
                        item {
                            MoodTrendsDashboardCard(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedHubTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabText"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyMoodCheckInCard(
    viewModel: LoveQuotesViewModel
) {
    val context = LocalContext.current
    val todayCheckIn by viewModel.todayMoodCheckIn.collectAsState()

    val moods = listOf(
        Triple("Çok Aşık", "😍", 98),
        Triple("Huzurlu", "🕊️", 92),
        Triple("Tutkulu", "🔥", 95),
        Triple("Özlem Dolu", "💌", 87),
        Triple("Hassas", "🌧️", 76)
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Bugünkü Kalp & Ruh Halin Nasıl?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (todayCheckIn != null)
                    "Bugünkü kaydın: ${todayCheckIn!!.second} ${todayCheckIn!!.first} (%${todayCheckIn!!.third})"
                else
                    "Bir duygu seçerek bugünün aşk endeksi grafiğini güncelle",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { (label, emoji, score) ->
                    val isSelected = todayCheckIn?.first == label
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.recordMoodCheckIn(label, emoji, score)
                            Toast.makeText(context, "Bugünkü ruh halin kaydedildi: $emoji $label", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("$emoji $label", fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiAcrosticPoemCard(
    viewModel: LoveQuotesViewModel,
    onNavigateToStudio: () -> Unit
) {
    val context = LocalContext.current
    val poemResult by viewModel.acrosticPoemResult.collectAsState()
    val isLoading by viewModel.isAcrosticLoading.collectAsState()
    var partnerName by remember { mutableStateOf("") }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .testTag("ai_acrostic_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "AI İsim Akrostiş Şairi ✍️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sevgilinin isminin baş harflerinden sana özel şiir yaz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = partnerName,
                onValueChange = { if (it.length <= 20) partnerName = it },
                label = { Text("Sevgilinin Adı") },
                placeholder = { Text("Örn: Zeynep, Ferhat, Elif...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (partnerName.isNotBlank()) {
                        viewModel.generateAcrosticPoem(partnerName)
                    } else {
                        Toast.makeText(context, "Lütfen önce bir isim yazın", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Akrostiş Şiir Yazılıyor...")
                } else {
                    Text("İsme Özel Akrostiş Üret", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = !poemResult.isNullOrBlank()) {
                poemResult?.let { poem ->
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = poem,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.saveToFavoritesLibrary(
                                                title = "Akrostiş: ${partnerName.ifBlank { "Sevgilim" }}",
                                                text = poem,
                                                author = "AI Akrostiş Şairi",
                                                category = "Akrostiş",
                                                sourceLabel = "AI Akrostiş"
                                            ) { _, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorilerime Ekle", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.saveNote(
                                                title = "Akrostiş: ${partnerName.ifBlank { "Sevgilim" }}",
                                                content = poem,
                                                author = "AI Akrostiş Şairi",
                                                tags = "Akrostiş, Şiir"
                                            )
                                            Toast.makeText(context, "Akrostiş notlarına kaydedildi! 💌", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Notlara Kaydet", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.loadCustomQuoteIntoStudio(poem, partnerName.ifBlank { "Aşkım" })
                                            onNavigateToStudio()
                                        }
                                    ) {
                                        Icon(Icons.Default.Palette, contentDescription = "Kart Yap", tint = MaterialTheme.colorScheme.tertiary)
                                    }
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(poem)}")
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val share = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, poem)
                                                }
                                                context.startActivity(Intent.createChooser(share, "Paylaş"))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                    }
                                    IconButton(
                                        onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            cm.setPrimaryClip(ClipData.newPlainText("Akrostiş", poem))
                                            Toast.makeText(context, "Kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiSituationCoachCard(
    viewModel: LoveQuotesViewModel,
    onNavigateToStudio: () -> Unit
) {
    val context = LocalContext.current
    val situationState by viewModel.situationMessageState.collectAsState()
    var selectedSituation by remember { mutableStateOf("Barışma & Özür") }
    var customSituation by remember { mutableStateOf("") }

    val situations = listOf(
        "Barışma & Özür",
        "İlk Buluşma Heyecanı",
        "Uzak Mesafe Özlemi",
        "İş Stresi Desteği",
        "Yıldönümü Kutlaması",
        "Sürpriz İltifat"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                RoundedCornerShape(22.dp)
            )
            .testTag("ai_situation_coach_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "AI İlişki Durum Asistanı 💬",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Anlık durumuna göre en doğru romantik mesajı hazırla",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                situations.forEach { sit ->
                    FilterChip(
                        selected = customSituation.isBlank() && selectedSituation == sit,
                        onClick = {
                            selectedSituation = sit
                            customSituation = ""
                        },
                        label = { Text(sit, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = customSituation,
                onValueChange = { customSituation = it },
                label = { Text("Veya kendi durumunu yaz (Opsiyonel)") },
                placeholder = { Text("Örn: Sınav öncesi moral, küçük bir kırgınlık...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_custom_situation_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            val activeSituation = customSituation.trim().ifBlank { selectedSituation }
            val isLoading = situationState is AiQuoteUiState.Loading
            Button(
                onClick = { viewModel.generateSituationMessage(activeSituation) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_situation_message_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mesaj Hazırlanıyor...")
                } else {
                    Text("Duruma Özel Mesaj Hazırla", fontWeight = FontWeight.Bold)
                }
            }

            if (situationState is AiQuoteUiState.Error) {
                val errMsg = (situationState as AiQuoteUiState.Error).message
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            AnimatedVisibility(visible = situationState is AiQuoteUiState.Success) {
                if (situationState is AiQuoteUiState.Success) {
                    val res = (situationState as AiQuoteUiState.Success).quote
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_situation_result_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "\"${res.text}\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.saveToFavoritesLibrary(
                                                title = "AI Durum: $activeSituation",
                                                text = res.text,
                                                author = res.author,
                                                category = activeSituation,
                                                sourceLabel = "AI Koç"
                                            ) { _, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorilerime Ekle", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.loadCustomQuoteIntoStudio(res.text, res.author)
                                            onNavigateToStudio()
                                        }
                                    ) {
                                        Icon(Icons.Default.Palette, contentDescription = "Kart Yap", tint = MaterialTheme.colorScheme.tertiary)
                                    }
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(res.text)}")
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val share = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, res.text)
                                                }
                                                context.startActivity(Intent.createChooser(share, "Paylaş"))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                    }
                                    IconButton(
                                        onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            cm.setPrimaryClip(ClipData.newPlainText("Mesaj", res.text))
                                            Toast.makeText(context, "Kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
