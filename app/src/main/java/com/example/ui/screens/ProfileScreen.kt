package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityQuoteEntity
import com.example.ui.LoveQuotesViewModel
import com.example.ui.theme.EditorialQuoteFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily

@Composable
fun ProfileScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToMoodInsights: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToStore: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val allCommunityQuotes by viewModel.allCommunityQuotes.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val quotesWithFavorites by viewModel.quotesWithFavorites.collectAsState()
    val savedFavoriteQuotes by viewModel.savedFavoriteQuotes.collectAsState()
    val loveCounterInfo by viewModel.loveCounterInfo.collectAsState()
    val loveStreak by viewModel.loveStreak.collectAsState()
    val moodInsights30Day by viewModel.moodInsights30Day.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()

    val myDisplayName = sessionState.displayName
    val userSharedQuotes = remember(allCommunityQuotes, myDisplayName, sessionState.isUser) {
        allCommunityQuotes.filter { quote ->
            quote.submitterName.equals(myDisplayName, ignoreCase = true) ||
                (!sessionState.isUser && quote.submitterName.equals("Misafir Âşık", ignoreCase = true))
        }
    }

    val totalSharedQuotesCount = userSharedQuotes.size
    val approvedSharedCount = userSharedQuotes.count { it.status == "APPROVED" }
    val pendingSharedCount = userSharedQuotes.count { it.status == "PENDING" }
    val totalReceivedHearts = remember(userSharedQuotes) {
        userSharedQuotes.sumOf { it.likesCount }
    }
    val totalFavoritesCount = remember(quotesWithFavorites, savedFavoriteQuotes, allNotes) {
        quotesWithFavorites.count { it.isFavorite } + savedFavoriteQuotes.size + allNotes.count { it.isFavorite }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen")
    ) {
        val isWide = maxWidth >= 600.dp
        val dynamicHorizontalPadding by animateDpAsState(
            targetValue = if (isWide) 28.dp else 16.dp,
            label = "profileDynamicHPadding"
        )
        val dynamicCardPadding by animateDpAsState(
            targetValue = if (isWide) 24.dp else 18.dp,
            label = "profileDynamicCardPadding"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = dynamicHorizontalPadding,
                end = dynamicHorizontalPadding,
                top = 12.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri Dön",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Column {
                            Text(
                                text = "Profilim & Etkileşim",
                                fontFamily = PlayfairDisplayFontFamily,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Paylaşılan sözler, kalpler ve romantik istatistiklerin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. User Identity Material 3 Card with Soft Shadow & Dynamic Padding
            item {
                SoftProfileCard(
                    innerPadding = dynamicCardPadding,
                    modifier = Modifier.testTag("profile_identity_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val initials = remember(myDisplayName) {
                            myDisplayName.trim()
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString("") { it.first().uppercase() }
                                .ifBlank { "A" }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                                            )
                                        )
                                    )
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = myDisplayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = when {
                                    sessionState.email.isNotBlank() -> sessionState.email
                                    sessionState.isAdmin -> "Firebase Yönetici hesabı aktif"
                                    else -> "Firebase bulut hesabınız senkronize"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                                ) {
                                    Text(
                                        text = "🔥 $loveStreak Gün Seri",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                                ) {
                                    Text(
                                        text = "💞 ${loveCounterInfo.calculateDays()} Gün Birlikte",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (entitlementState.isVip) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClick = onNavigateToStore)
                                ) {
                                    Text(
                                        text = if (entitlementState.isVip) "👑 VIP • 🪙 ${entitlementState.loveCoins}"
                                        else "🪙 ${entitlementState.loveCoins} Aşk Parası",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAuth,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .sizeIn(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hesap Değiştir")
                        }
                        if (sessionState.isAdmin) {
                            Button(
                                onClick = onNavigateToAdmin,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Yönetici")
                            }
                        } else {
                            Button(
                                onClick = onNavigateToCommunity,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Söz Paylaş")
                            }
                        }
                    }
                }
            }

            // 2. Primary Engagement Cards: Total Shared Quotes & Received Hearts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        title = "Paylaşılan Sözler",
                        value = totalSharedQuotesCount.toString(),
                        subtitle = if (totalSharedQuotesCount > 0) "$approvedSharedCount yayında • $pendingSharedCount onayda" else "İlk sözünü topluluğa gönder",
                        icon = Icons.Default.FormatQuote,
                        containerTint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        dynamicPadding = dynamicCardPadding,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_shared_quotes_card")
                    )

                    ProfileStatCard(
                        title = "Alınan Kalpler",
                        value = totalReceivedHearts.toString(),
                        subtitle = if (totalReceivedHearts > 0) "Okurlardan gelen toplam ❤️" else "Sözlerin beğenildikçe artar",
                        icon = Icons.Default.Favorite,
                        containerTint = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        accentColor = Color(0xFFE91E63),
                        dynamicPadding = dynamicCardPadding,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_received_hearts_card")
                    )
                }
            }

            // 3. Secondary Stats & 30-Day Consistency Snapshot Card
            item {
                SoftProfileCard(
                    innerPadding = dynamicCardPadding,
                    onClick = onNavigateToMoodInsights,
                    modifier = Modifier.testTag("profile_consistency_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Insights,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "30 Günlük Duygu & Tutarlılık",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%${moodInsights30Day.consistencyPercent} tutarlılık • ${moodInsights30Day.activeDaysCount}/30 aktif gün • ${moodInsights30Day.totalNotesLast30Days} not",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Detayları Gör",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 4. Quick Library & Studio Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoftProfileCard(
                        innerPadding = 14.dp,
                        onClick = onNavigateToStudio,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Görsel Stüdyo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Gönderi Tasarla", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    SoftProfileCard(
                        innerPadding = 14.dp,
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text("Favorilerim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("$totalFavoritesCount kayıtlı", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 4.5 VIP & Love Coins Store Card
            item {
                SoftProfileCard(
                    innerPadding = dynamicCardPadding,
                    onClick = onNavigateToStore,
                    modifier = Modifier.testTag("profile_store_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (entitlementState.isVip) Icons.Default.WorkspacePremium else Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (entitlementState.isVip) "VIP Aşk Kulübü Aktif 👑" else "VIP Mağaza & Aşk Parası",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entitlementState.loveCoins} Aşk Parası • " +
                                        if (entitlementState.shouldShowAds) "Özel çerçeveleri aç & reklamları kaldır"
                                        else "Reklamsız ayrıcalıklı deneyim aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Mağazaya Git",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 4.6 Secure Emotional Journal Vault Card
            item {
                SoftProfileCard(
                    innerPadding = dynamicCardPadding,
                    onClick = onNavigateToJournal,
                    modifier = Modifier.testTag("profile_journal_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Duygusal Günlüğüm 🔒",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tarih bazlı özel anılar • Yalnızca sana özel kasa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Günlüğü Aç",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 5. My Shared Quotes List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paylaştığım Sözler ($totalSharedQuotesCount)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tümünü Gör",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToCommunity)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (userSharedQuotes.isEmpty()) {
                item {
                    SoftProfileCard(innerPadding = dynamicCardPadding) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "Henüz toplulukla söz paylaşmadın",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "İlk romantik sözünü paylaş, diğer okurlardan kalpler ❤️ ve yorumlar toplamaya başla!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onNavigateToCommunity,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.sizeIn(minHeight = 48.dp)
                            ) {
                                Text("İlk Sözünü Paylaş", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(userSharedQuotes, key = { it.id }) { quote ->
                    UserSharedQuoteItemCard(
                        quote = quote,
                        dynamicPadding = dynamicCardPadding,
                        onCreateVisualPost = {
                            viewModel.loadCustomQuoteIntoStudio(quote.text, quote.author)
                            onNavigateToStudio()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoftProfileCard(
    innerPadding: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
            .let { base ->
                if (onClick != null) base.clip(cardShape).clickable(onClick = onClick) else base
            }
    ) {
        Column(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerTint: Color,
    accentColor: Color,
    dynamicPadding: Dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.16f),
                spotColor = accentColor.copy(alpha = 0.22f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(containerTint.copy(alpha = 0.45f), MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(dynamicPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.14f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UserSharedQuoteItemCard(
    quote: CommunityQuoteEntity,
    dynamicPadding: Dp,
    onCreateVisualPost: () -> Unit
) {
    SoftProfileCard(innerPadding = dynamicPadding) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusLabel = when (quote.status) {
                "APPROVED" -> "Yayında ✅"
                "PENDING" -> "Onay Bekliyor ⏳"
                else -> "Düzenleme Gerekli"
            }
            val statusBg = when (quote.status) {
                "APPROVED" -> MaterialTheme.colorScheme.primaryContainer
                "PENDING" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
            Surface(shape = RoundedCornerShape(10.dp), color = statusBg) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE91E63).copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Alınan Kalpler",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${quote.likesCount} Kalp",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "\"${quote.text}\"",
            fontFamily = EditorialQuoteFontFamily,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(
                onClick = onCreateVisualPost,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Görsel Yap", fontSize = 12.sp)
            }
        }
    }
}
