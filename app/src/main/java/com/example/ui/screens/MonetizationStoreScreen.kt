package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BillingProductKind
import com.example.data.model.MonetizationCatalog
import com.example.data.model.StoreProduct
import com.example.ui.LoveQuotesViewModel
import com.example.ui.theme.PlayfairDisplayFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonetizationStoreScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val entitlement by viewModel.entitlementState.collectAsState()
    val billingReady by viewModel.billingReady.collectAsState()
    val livePrices by viewModel.liveFormattedPrices.collectAsState()

    var selectedPlanIdx by remember { mutableIntStateOf(0) } // 0 = Yearly, 1 = Monthly
    var pendingPreviewProduct by remember { mutableStateOf<StoreProduct?>(null) }
    var showRewardPreviewDialog by remember { mutableStateOf(false) }

    fun displayPrice(product: StoreProduct): String = livePrices[product.skuId] ?: product.priceDisplay

    fun triggerPurchase(product: StoreProduct) {
        viewModel.purchaseStoreProduct(
            activity = activity,
            product = product,
            onNeedPreviewFallback = { pendingPreviewProduct = product },
            onCompleted = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
        )
    }

    val goldAccent = Color(0xFFFFD54F)
    val vipGoldBg = Color(0xFFFFF8E1)
    val vipGoldText = Color(0xFF92400E)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("monetization_store_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Back, Restore Purchases & Live Coin Balance
        item {
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
                            .testTag("store_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön"
                        )
                    }
                    Column {
                        Text(
                            text = "Aşka Dair Premium",
                            fontFamily = PlayfairDisplayFontFamily,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (billingReady) "Google Play Billing Aktif ✓" else "Premium & Özel Paketler",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (billingReady) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (billingReady) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            viewModel.restorePlayPurchases { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("store_restore_purchases_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Satın Alımları Geri Yükle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = vipGoldBg,
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("store_coin_balance_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 14.sp)
                            Text(
                                text = "${entitlement.loveCoins}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = vipGoldText
                            )
                        }
                    }
                }
            }
        }

        // 1. Luxury Hero Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, goldAccent.copy(alpha = 0.65f), RoundedCornerShape(28.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_premium_club_banner),
                        contentDescription = "Aşka Dair VIP Kulübü",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xEB260416),
                                        Color(0xCC4A0E2E),
                                        Color(0x883A0B2E)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Surface(
                                    shape = CircleShape,
                                    color = goldAccent.copy(alpha = 0.22f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (entitlement.isVip) "VIP Üyeliğin Aktif 👑" else "Sınırsız Romantizmin Kilidini Aç",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (entitlement.isVip) {
                                            entitlement.vipPlanName.ifBlank { "Premium VIP Üye" }
                                        } else {
                                            "Reklamsız • Özel Tasarımlar • Sınırsız Bulut Yedekleme"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.88f)
                                    )
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StoreStatusChip(
                                label = if (entitlement.shouldShowAds) "Reklamlı Mod" else "Reklamsız ✓"
                            )
                            StoreStatusChip(
                                label = if (entitlement.hasUnlimitedCloudBackup) "☁️ Sınırsız Yedekleme" else "☁️ 10 Kayıt Kotası"
                            )
                            if (entitlement.hasSpecialDesignPack) {
                                StoreStatusChip(label = "🎨 Tüm Tasarımlar Açık")
                            }
                            if (entitlement.hasUnlimitedAiAccess) {
                                StoreStatusChip(label = "👑 VIP Rozet & AI")
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive Subscription Switcher Card (Yıllık %42 Tasarruf / Aylık)
        item {
            val plans = MonetizationCatalog.vipSubscriptions
            val selectedPlan = plans.getOrElse(selectedPlanIdx) { plans.first() }
            val isPlanOwned = entitlement.isVip && entitlement.vipPlanName == selectedPlan.title

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                    .testTag("store_premium_plan_switcher_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Segmented Switcher
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            plans.forEachIndexed { idx, plan ->
                                val active = selectedPlanIdx == idx
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPlanIdx = idx }
                                        .testTag("store_plan_tab_${plan.skuId}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (idx == 0) "Yıllık Plan" else "Aylık Plan",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        plan.badge?.let { badge ->
                                            Text(
                                                text = badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (active) goldAccent else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Selected Plan Price & Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedPlan.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = displayPrice(selectedPlan),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = vipGoldBg,
                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "+${selectedPlan.coinGrant} 🪙 Hediye",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = vipGoldText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Key Benefits Checklist
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Tüm banner, geçiş ve reklam kesintilerini tamamen kaldır",
                            "Stüdyo VIP temaları ve hikaye çerçevelerine sınırsız erişim",
                            "10 kayıt sınırını kaldır: Sınırsız bulut yedekleme kapasitesi (∞)",
                            "VIP Çift Rozeti & Sınırsız AI İlişki Koçu erişimi"
                        ).forEach { perk ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = perk,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { triggerPurchase(selectedPlan) },
                        enabled = !isPlanOwned,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 52.dp)
                            .testTag("store_product_${selectedPlan.skuId}")
                    ) {
                        Icon(
                            imageVector = if (isPlanOwned) Icons.Default.CheckCircle else Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlanOwned) "Bu Plan Aktif ✓" else "Premium'a Geç — ${displayPrice(selectedPlan)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 3. Free vs Premium Feature Comparison Table
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_feature_comparison_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ücretsiz vs Premium Karşılaştırması",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Özellik",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "Ücretsiz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Premium 👑",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    MonetizationCatalog.featureComparison.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${row.emoji} ${row.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = row.freeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = row.premiumText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 4. User-Friendly One-Time Feature Packs
        item {
            Text(
                text = "💎 KULLANICI DOSTU ÖZEL PAKETLER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        items(MonetizationCatalog.oneTimePacks, key = { it.skuId }) { product ->
            val owned = when {
                entitlement.isVip -> true
                product.grantsAdFree -> entitlement.isAdFree
                product.grantsDesignPack || product.grantsAllFrames -> entitlement.hasSpecialDesignPack
                product.grantsUnlimitedBackup -> entitlement.hasUnlimitedCloudBackup
                product.grantsVipBadgeAndAi -> entitlement.hasUnlimitedAiAccess
                else -> false
            }
            StoreProductCard(
                product = product,
                formattedPrice = displayPrice(product),
                isOwned = owned,
                onBuyClick = { triggerPurchase(product) }
            )
        }

        // 5. Rewarded Video Free Coin Station
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_rewarded_ad_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ödüllü Video ile Bedava Coin",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Kısa video izle, anında +${entitlement.rewardCoinsPerAd} Aşk Parası kazan!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.watchRewardedAdForCoins(
                                activity = activity,
                                onOfflinePreviewNeeded = { showRewardPreviewDialog = true },
                                onRewardEarned = { gained ->
                                    Toast.makeText(
                                        context,
                                        "Tebrikler! +$gained Aşk Parası hesabına eklendi 🪙",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .testTag("store_watch_reward_ad_button")
                    ) {
                        Text("+${entitlement.rewardCoinsPerAd} 🪙", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // 6. Consumable Love Coin Packs
        item {
            Text(
                text = "🪙 AŞK PARASI PAKETLERİ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        items(MonetizationCatalog.coinPacks, key = { it.skuId }) { product ->
            StoreProductCard(
                product = product,
                formattedPrice = displayPrice(product),
                isOwned = false,
                onBuyClick = { triggerPurchase(product) }
            )
        }

        // 7. Purchase & Reward History
        if (entitlement.purchaseHistory.isNotEmpty()) {
            item {
                Text(
                    text = "🧾 SON İŞLEM GEÇMİŞİ",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(entitlement.purchaseHistory.take(8), key = { it.id }) { tx ->
                val dateStr = remember(tx.timestamp) {
                    SimpleDateFormat("dd MMM, HH:mm", Locale("tr")).format(Date(tx.timestamp))
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tx.source} • $dateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = tx.amountDisplay,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Play Store Preview / Confirmation Modal (when running outside live Play Console test track)
    pendingPreviewProduct?.let { prod ->
        AlertDialog(
            onDismissRequest = { pendingPreviewProduct = null },
            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(prod.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = prod.subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tutar: ${displayPrice(prod)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Google Play Console SKU: ${prod.skuId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Not: Canlı Play Store oturumu algılanmadığından test/önizleme modunda bu ürünü anında aktifleştirebilirsiniz.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completePreviewPurchase(prod) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        pendingPreviewProduct = null
                    }
                ) {
                    Text("Satın Almayı Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPreviewProduct = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // Offline / Emulator Rewarded Ad Preview Modal
    if (showRewardPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showRewardPreviewDialog = false },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300)) },
            title = { Text("Sponsorlu Ödül Görevi", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Reklam sunucusu şu anda çevrimdışı veya test önizleme modunda. " +
                        "Günlük sponsorlu ödülünü şimdi talep ederek +${entitlement.rewardCoinsPerAd} Aşk Parası kazanabilirsin!",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.grantRewardedAdFallbackCoins { gained ->
                            Toast.makeText(
                                context,
                                "Harika! +$gained Aşk Parası hesabına eklendi 🪙",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showRewardPreviewDialog = false
                    }
                ) {
                    Text("+${entitlement.rewardCoinsPerAd} Coin Al")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRewardPreviewDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
private fun StoreStatusChip(label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StoreProductCard(
    product: StoreProduct,
    formattedPrice: String,
    isOwned: Boolean,
    onBuyClick: () -> Unit
) {
    val accentIcon = when {
        product.kind == BillingProductKind.SUBSCRIPTION -> Icons.Default.WorkspacePremium
        product.kind == BillingProductKind.CONSUMABLE_COINS -> Icons.Default.MonetizationOn
        product.grantsAdFree -> Icons.Default.Block
        product.grantsUnlimitedBackup -> Icons.Default.CloudDone
        product.grantsDesignPack || product.grantsAllFrames -> Icons.Default.Palette
        else -> Icons.Default.AutoAwesome
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isOwned) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                else Modifier
            )
            .testTag("store_product_${product.skuId}"),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                                imageVector = accentIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            product.badge?.let { badge ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = product.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isOwned) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.sizeIn(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aktif", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onBuyClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.sizeIn(minHeight = 48.dp)
                    ) {
                        Text("Sahip Ol", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
