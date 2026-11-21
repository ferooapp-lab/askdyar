package com.example.ui.screens

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityCommentEntity
import com.example.data.local.CommunityQuoteEntity
import com.example.ui.theme.AppDesignTheme
import com.example.util.SocialCardFormat
import com.example.util.SocialCardPalette
import com.example.util.SocialImageShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCommentsSheet(
    quote: CommunityQuoteEntity,
    comments: List<CommunityCommentEntity>,
    onAddComment: (authorName: String, commentText: String, onDone: (Boolean, String) -> Unit) -> Unit,
    onDeleteComment: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var authorName by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }

    val quickReplies = listOf(
        "Çok güzel! ❤️",
        "Tam kalbimden vurdu ✨",
        "Harika bir söz 👏",
        "Sevgilime gönderdim 💌"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("community_comments_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sheet Header
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
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Yorumlar (${comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Bu aşk sözü hakkında düşünceni paylaş",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat")
                }
            }

            // Quote Preview
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "\"${quote.text}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "— ${quote.author} • Paylaşan: ${quote.submitterName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Existing Comments List
            if (comments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Henüz yorum yapılmamış. İlk yorumu sen bırak! 💬",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 230.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        val dateText = remember(comment.createdAt) {
                            SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                                .format(Date(comment.createdAt))
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = comment.authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = comment.commentText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteComment(comment.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Yorumu Sil",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Quick Reply Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickReplies) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { commentText = preset }
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Input Form
            OutlinedTextField(
                value = authorName,
                onValueChange = { authorName = it },
                label = { Text("Adınız / Rumuz (Opsiyonel)") },
                placeholder = { Text("Örn: Deniz") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comment_author_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Yorumunuz...") },
                    placeholder = { Text("Bu söz hakkında ne düşünüyorsun?") },
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_text_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Button(
                    onClick = {
                        onAddComment(authorName, commentText) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                commentText = ""
                            }
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("send_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Yorum Gönder",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VisualShareQuoteDialog(
    quote: CommunityQuoteEntity,
    activeDesignTheme: AppDesignTheme,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val initialPalette = remember(activeDesignTheme) {
        when (activeDesignTheme) {
            AppDesignTheme.BLUSH_PEONY -> SocialImageShareHelper.palettes.find { it.id == "pastel_pembe" }
            AppDesignTheme.MIDNIGHT_INDIGO -> SocialImageShareHelper.palettes.find { it.id == "gece_yarisi_mavisi" }
            AppDesignTheme.LAVENDER_TWILIGHT -> SocialImageShareHelper.palettes.find { it.id == "lavanta_ruyasi" }
            AppDesignTheme.EMERALD_ROMANCE -> SocialImageShareHelper.palettes.find { it.id == "zumrut_altin" }
            else -> SocialImageShareHelper.palettes.find { it.id == "romantik_kirmizi" }
        } ?: SocialImageShareHelper.palettes.first()
    }

    var selectedPalette by remember { mutableStateOf<SocialCardPalette>(initialPalette) }
    var selectedFormat by remember { mutableStateOf(SocialCardFormat.POST_4_5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("visual_share_quote_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Sosyal Medya Görsel Kartı", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paylaşılan bu sözü yüksek çözünürlüklü PNG karta dönüştürüp Instagram, WhatsApp veya X hesabında paylaşabilirsin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Live Miniature Preview
                val previewRatio = when (selectedFormat) {
                    SocialCardFormat.POST_4_5 -> 4f / 5f
                    SocialCardFormat.SQUARE_1_1 -> 1f
                    SocialCardFormat.STORY_9_16 -> 0.72f
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (selectedFormat == SocialCardFormat.STORY_9_16) 0.72f else 0.92f)
                            .aspectRatio(previewRatio)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(selectedPalette.startColor),
                                        Color(selectedPalette.endColor)
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                Color(selectedPalette.accentColor).copy(alpha = 0.55f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(selectedPalette.accentColor).copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = quote.category.uppercase(),
                                        color = Color(selectedPalette.accentColor),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text("💖", fontSize = 16.sp)
                            }

                            Text(
                                text = "\"${quote.text}\"",
                                color = Color(selectedPalette.textColor),
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "— ${quote.author}",
                                    color = Color(selectedPalette.accentColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Paylaşan: ${quote.submitterName} • Aşka Dair",
                                    color = Color(selectedPalette.textColor).copy(alpha = 0.65f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // Palette Selector
                Text(
                    text = "Kart Renk Paleti",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SocialImageShareHelper.palettes) { palette ->
                        val isSelected = selectedPalette.id == palette.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPalette = palette },
                            label = { Text(palette.name, fontSize = 11.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(palette.startColor), Color(palette.accentColor))
                                            )
                                        )
                                )
                            }
                        )
                    }
                }

                // Format Selector
                Text(
                    text = "Paylaşım Boyutu",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SocialCardFormat.values().forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt.label, fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bitmap = SocialImageShareHelper.renderQuoteCardBitmap(
                        quoteText = quote.text,
                        author = quote.author,
                        categoryLabel = quote.category,
                        submitterOrDedication = "Paylaşan: ${quote.submitterName}",
                        sticker = "💖",
                        palette = selectedPalette,
                        format = selectedFormat
                    )
                    val result = SocialImageShareHelper.shareBitmapToSocialMedia(
                        context = context,
                        bitmap = bitmap,
                        shareCaption = "\"${quote.text}\"\n— ${quote.author}\n\n❤️ Aşka Dair Topluluğu"
                    )
                    result.onFailure {
                        Toast.makeText(
                            context,
                            "Görsel paylaşımı başlatılamadı: ${it.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("confirm_visual_share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Görsel Paylaş", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
