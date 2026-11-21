package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityCommentEntity
import com.example.data.local.CommunityQuoteEntity
import com.example.ui.LoveQuotesViewModel
import com.example.ui.components.AdBannerCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityQuotesScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToStore: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quotes by viewModel.approvedCommunityQuotes.collectAsState()
    val likedIds by viewModel.likedCommunityQuoteIds.collectAsState()
    val commentsByQuoteId by viewModel.commentsByQuoteId.collectAsState()
    val designTheme by viewModel.designTheme.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()
    val pendingCount by viewModel.pendingCommunityQuotesCount.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var activeCommentQuote by remember { mutableStateOf<CommunityQuoteEntity?>(null) }
    var activeVisualShareQuote by remember { mutableStateOf<CommunityQuoteEntity?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Tümü", "Aşk Sözleri", "Romantik Sözler", "Günaydın Mesajları", "Gece Mesajları", "Özel")

    val filteredQuotes = remember(quotes, selectedCategoryFilter, searchQuery) {
        quotes.filter { q ->
            val matchCat = (selectedCategoryFilter == null || selectedCategoryFilter == "Tümü" || q.category == selectedCategoryFilter)
            val matchQuery = searchQuery.isBlank() ||
                    q.text.contains(searchQuery, ignoreCase = true) ||
                    q.author.contains(searchQuery, ignoreCase = true) ||
                    q.submitterName.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("community_quotes_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Admin Shortcut
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Topluluk Aşk Sözleri",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Kullanıcıların paylaştığı ve beğendiği romantik sözler",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Admin Shortcut Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { onNavigateToAdmin() }
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Yönetici Paneli",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (pendingCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$pendingCount",
                                            color = MaterialTheme.colorScheme.onError,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Topluluk sözlerinde ara...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Ara")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Live Community Chat Room Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToChat() }
                        .testTag("community_open_chat_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🗣️ Canlı Topluluk Sohbeti",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Diğer üyelerle sohbet odalarında tanış, şiir paylaş ve etkileşime geç",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Katıl",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Category Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = (selectedCategoryFilter == cat) || (selectedCategoryFilter == null && cat == "Tümü")
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (cat == "Tümü") null else cat
                            },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (entitlementState.shouldShowAds) {
                item {
                    AdBannerCard(
                        shouldShowAds = true,
                        onUpgradeClick = onNavigateToStore,
                        onAdFailedNetwork = { viewModel.onAdBannerNetworkFailed() }
                    )
                }
            }

            // Empty state
            if (filteredQuotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💌", fontSize = 36.sp)
                            Text(
                                text = "Henüz bu kategoride söz bulunmuyor.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showSubmitDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("İlk Sözü Sen Paylaş!")
                            }
                        }
                    }
                }
            } else {
                // List of Community Quotes
                items(filteredQuotes, key = { it.id }) { quote ->
                    val isLiked = likedIds.contains(quote.id.toString())
                    val quoteComments = commentsByQuoteId[quote.id].orEmpty()
                    CommunityQuoteItemCard(
                        quote = quote,
                        isLiked = isLiked,
                        comments = quoteComments,
                        onLikeToggle = {
                            viewModel.toggleCommunityQuoteLike(quote.id)
                            val toastMsg = if (!isLiked) "Kalp bırakıldı! ❤️ (Söz başına 1 beğeni)" else "Kalp kaldırıldı"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        },
                        onOpenComments = { activeCommentQuote = quote },
                        onVisualShare = { activeVisualShareQuote = quote },
                        onSaveToFavorites = {
                            viewModel.saveToFavoritesLibrary(
                                title = "Topluluk: ${quote.category}",
                                text = quote.text,
                                author = quote.author,
                                category = quote.category,
                                sourceLabel = "Topluluk"
                            ) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenInStudio = {
                            viewModel.loadCustomQuoteIntoStudio(quote.text, quote.author)
                            onNavigateToStudio()
                        }
                    )
                }
            }
        }

        // Floating Action Button to submit quote
        FloatingActionButton(
            onClick = {
                if (!adminSettings.submissionsEnabled) {
                    Toast.makeText(context, "Yönetici şu anda yeni söz alımını geçici olarak kapattı.", Toast.LENGTH_LONG).show()
                } else {
                    showSubmitDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .testTag("submit_quote_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Söz Paylaş")
                Text("Söz Paylaş", fontWeight = FontWeight.Bold)
            }
        }

        // Submit Quote Dialog
        if (showSubmitDialog) {
            SubmitQuoteDialog(
                viewModel = viewModel,
                onDismiss = { showSubmitDialog = false }
            )
        }

        // Comments Interaction Sheet
        activeCommentQuote?.let { targetQuote ->
            CommunityCommentsSheet(
                quote = targetQuote,
                comments = commentsByQuoteId[targetQuote.id].orEmpty(),
                onAddComment = { authorName, commentText, onDone ->
                    viewModel.addCommunityComment(
                        quoteId = targetQuote.id,
                        authorName = authorName,
                        commentText = commentText,
                        onResult = onDone
                    )
                },
                onDeleteComment = { commentId ->
                    viewModel.deleteCommunityComment(commentId)
                },
                onDismiss = { activeCommentQuote = null }
            )
        }

        // Social Media Visual Card Share Dialog
        activeVisualShareQuote?.let { targetQuote ->
            VisualShareQuoteDialog(
                quote = targetQuote,
                activeDesignTheme = designTheme,
                onDismiss = { activeVisualShareQuote = null }
            )
        }
    }
}

@Composable
fun CommunityQuoteItemCard(
    quote: CommunityQuoteEntity,
    isLiked: Boolean,
    comments: List<CommunityCommentEntity> = emptyList(),
    onLikeToggle: () -> Unit,
    onOpenComments: () -> Unit = {},
    onVisualShare: () -> Unit = {},
    onOpenInStudio: () -> Unit,
    onSaveToFavorites: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
    )
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = if (isLiked) 0.5f else 0.2f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Category & Submitter Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = quote.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Paylaşan: ${quote.submitterName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quote Text
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Author
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Latest Comment Inline Preview
            comments.lastOrNull()?.let { latest ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenComments() }
                        .testTag("comment_preview_${quote.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💬 ${latest.authorName}: ${latest.commentText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (comments.size > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+${comments.size - 1} yorum",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Interaction & Visual Share Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Heart (Kalp) Button (Anti-Spam 1 Like per User)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isLiked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clickable { onLikeToggle() }
                            .testTag("like_button_${quote.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Kalp Bırak",
                                tint = heartColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .scale(heartScale)
                            )
                            Text(
                                text = "${quote.likesCount}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Comment (Yorum) Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clickable { onOpenComments() }
                            .testTag("comment_button_${quote.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Yorum Yap",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = if (comments.isNotEmpty()) "${comments.size}" else "Yorum",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Visual Social Media Image Share Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clickable { onVisualShare() }
                            .testTag("visual_share_button_${quote.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Sosyal Medyada Görsel Paylaş",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Görsel",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save to Personal Favorites Library
                    IconButton(
                        onClick = onSaveToFavorites,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Favorilerim Kütüphanesine Kaydet",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // WhatsApp Share
                    IconButton(
                        onClick = {
                            val sendText = "\"${quote.text}\"\n\n— ${quote.author} (Paylaşan: ${quote.submitterName})"
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
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp Paylaş",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Copy to Clipboard
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Aşk Sözü", "\"${quote.text}\"\n— ${quote.author}"))
                            Toast.makeText(context, "Kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Open in Visual Card Studio
                    IconButton(
                        onClick = onOpenInStudio,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Stüdyoda Düzenle",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitQuoteDialog(
    viewModel: LoveQuotesViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val adminSettings by viewModel.adminSettings.collectAsState()

    var quoteText by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var submitterName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Aşk Sözleri") }

    val categories = listOf("Aşk Sözleri", "Romantik Sözler", "Günaydın Mesajları", "Gece Mesajları", "Özel")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💖", fontSize = 20.sp)
                Text("Aşk Sözü Paylaş", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Restrictions notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (adminSettings.autoApprove) {
                                "✨ Otomatik onay aktif; sözünüz anında yayınlanacaktır."
                            } else {
                                "🛡️ Sözünüz yayınlanmadan önce yönetici onayına sunulacaktır."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Submitter Name
                OutlinedTextField(
                    value = submitterName,
                    onValueChange = { submitterName = it },
                    label = { Text("Adınız / Takma Adınız") },
                    placeholder = { Text("Örn: Eren, Gizemli Âşık") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quote Text
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = {
                        if (it.length <= adminSettings.maxCharCount) {
                            quoteText = it
                        }
                    },
                    label = { Text("Aşk Sözünüz") },
                    placeholder = { Text("Gönlünden geçen en güzel sözü yaz...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = "${quoteText.length} / ${adminSettings.maxCharCount} karakter (En az ${adminSettings.minCharCount})",
                            color = if (quoteText.length < adminSettings.minCharCount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // Author Name
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Yazar / Kaynak (Opsiyonel)") },
                    placeholder = { Text("Örn: Kendime ait, Cemal Süreya") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category selection chips
                Text(
                    text = "Kategori Seçin:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = (selectedCategory == cat)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitCommunityQuote(
                        text = quoteText,
                        author = authorName,
                        category = selectedCategory,
                        submitterName = submitterName
                    ) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        if (success) {
                            onDismiss()
                        }
                    }
                },
                enabled = quoteText.length >= adminSettings.minCharCount,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Gönder", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
