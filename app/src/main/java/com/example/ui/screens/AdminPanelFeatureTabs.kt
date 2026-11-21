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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdminQuoteEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.AdminSettings
import com.example.data.model.EntitlementState
import com.example.data.model.QuoteSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminAddQuoteTab(
    adminQuotes: List<AdminQuoteEntity>,
    onAddQuote: (text: String, author: String, sectionId: String, subCategory: String, tagsCsv: String, isFeatured: Boolean) -> Unit,
    onUpdateQuote: (AdminQuoteEntity) -> Unit,
    onFeatureQuote: (AdminQuoteEntity) -> Unit,
    onDeleteQuote: (Long) -> Unit
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var authorInput by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf(QuoteSection.ASK_SOZLERI) }
    var subCategoryInput by remember { mutableStateOf("Editör Seçimi") }
    var tagsInput by remember { mutableStateOf("Aşk, Romantik") }
    var featureAsDaily by remember { mutableStateOf(false) }

    var editingAdminQuote by remember { mutableStateOf<AdminQuoteEntity?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Uygulamaya Yeni Söz Ekle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Eklediğin sözler anında ana sayfadaki kategori listesinde yayınlanır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuoteSection.values().forEach { sec ->
                            FilterChip(
                                selected = selectedSection == sec,
                                onClick = { selectedSection = sec },
                                label = { Text("${sec.emoji} ${sec.title}", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Söz Metni") },
                        placeholder = { Text("Örn: Kalbinin ritmiyle hayat bulan en güzel şiirim sensin...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_quote_text_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = authorInput,
                            onValueChange = { authorInput = it },
                            label = { Text("Yazar") },
                            placeholder = { Text("Anonim / Şair") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_add_quote_author_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = subCategoryInput,
                            onValueChange = { subCategoryInput = it },
                            label = { Text("Alt Başlık") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Etiketler (Virgülle ayır)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Günün Sözü Olarak Öne Çıkar",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ana sayfada Günün Seçileni kartında hemen gösterilir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = featureAsDaily,
                            onCheckedChange = { featureAsDaily = it }
                        )
                    }

                    Button(
                        onClick = {
                            onAddQuote(
                                textInput,
                                authorInput,
                                selectedSection.id,
                                subCategoryInput,
                                tagsInput,
                                featureAsDaily
                            )
                            textInput = ""
                            authorInput = ""
                            featureAsDaily = false
                        },
                        enabled = textInput.trim().length >= 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                            .testTag("admin_add_quote_submit_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sözü Uygulamaya Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Yönetici Tarafından Eklenen Sözler (${adminQuotes.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (adminQuotes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Henüz yönetici sözü eklenmedi. Yukarıdaki formdan anında yeni aşk sözleri ekleyebilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(adminQuotes, key = { it.id }) { quote ->
                val secTitle = QuoteSection.values().find { it.id == quote.sectionId }?.title ?: "Aşk Sözleri"
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (quote.isFeaturedDaily) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "$secTitle • ${quote.subCategory}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (quote.isFeaturedDaily) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "⭐ GÜNÜN SÖZÜ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "\"${quote.text}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "— ${quote.author}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        onFeatureQuote(quote)
                                        Toast.makeText(context, "Günün sözü olarak ayarlandı ⭐", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (quote.isFeaturedDaily) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = "Günün Sözü Yap",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { editingAdminQuote = quote },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { onDeleteQuote(quote.id) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingAdminQuote?.let { target ->
        var editText by remember(target.id) { mutableStateOf(target.text) }
        var editAuthor by remember(target.id) { mutableStateOf(target.author) }
        var editSub by remember(target.id) { mutableStateOf(target.subCategory) }

        AlertDialog(
            onDismissRequest = { editingAdminQuote = null },
            title = { Text("Yönetici Sözünü Düzenle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("Söz Metni") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAuthor,
                        onValueChange = { editAuthor = it },
                        label = { Text("Yazar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSub,
                        onValueChange = { editSub = it },
                        label = { Text("Alt Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateQuote(
                            target.copy(
                                text = editText.trim(),
                                author = editAuthor.trim().ifBlank { "Aşk Sözleri Editörü" },
                                subCategory = editSub.trim().ifBlank { "Editör Seçimi" }
                            )
                        )
                        editingAdminQuote = null
                    },
                    enabled = editText.trim().length >= 5
                ) {
                    Text("Güncelle")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAdminQuote = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun AdminMaintenanceAndSystemTab(
    settings: AdminSettings,
    entitlementState: EntitlementState = EntitlementState(),
    onToggleTestVip: (Boolean) -> Unit = {},
    onSaveSettings: (AdminSettings) -> Unit
) {
    var maintenanceMode by remember(settings) { mutableStateOf(settings.maintenanceMode) }
    var maintenanceTitle by remember(settings) { mutableStateOf(settings.maintenanceTitle) }
    var maintenanceMessage by remember(settings) { mutableStateOf(settings.maintenanceMessage) }
    var maintenanceEta by remember(settings) { mutableStateOf(settings.maintenanceEstimatedTime) }
    var bannerText by remember(settings) { mutableStateOf(settings.announcementBannerText) }
    var allowRegistration by remember(settings) { mutableStateOf(settings.allowUserRegistration) }
    var allowCustomMusic by remember(settings) { mutableStateOf(settings.allowCustomMusic) }
    var allowComments by remember(settings) { mutableStateOf(settings.allowCommunityComments) }
    var adsEnabled by remember(settings) { mutableStateOf(settings.adsEnabled) }
    var rewardCoinsPerAd by remember(settings) { mutableIntStateOf(settings.rewardCoinsPerAd) }
    var interstitialCooldownSeconds by remember(settings) { mutableIntStateOf(settings.interstitialCooldownSeconds) }
    var adBlockerGuardEnabled by remember(settings) { mutableStateOf(settings.adBlockerGuardEnabled) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Master Maintenance Mode Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (maintenanceMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (maintenanceMode) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
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
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = if (maintenanceMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Uygulamayı Bakım Moduna Al",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (maintenanceMode) "AKTİF: Kullanıcılar bakım ekranını görür" else "KAPALI: Uygulama tüm kullanıcılara açık",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (maintenanceMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = maintenanceMode,
                            onCheckedChange = { enabled ->
                                maintenanceMode = enabled
                                onSaveSettings(
                                    settings.copy(
                                        maintenanceMode = enabled,
                                        maintenanceTitle = maintenanceTitle,
                                        maintenanceMessage = maintenanceMessage,
                                        maintenanceEstimatedTime = maintenanceEta,
                                        announcementBannerText = bannerText,
                                        allowUserRegistration = allowRegistration,
                                        allowCustomMusic = allowCustomMusic,
                                        allowCommunityComments = allowComments,
                                        adsEnabled = adsEnabled,
                                        rewardCoinsPerAd = rewardCoinsPerAd,
                                        interstitialCooldownSeconds = interstitialCooldownSeconds,
                                        adBlockerGuardEnabled = adBlockerGuardEnabled
                                    )
                                )
                            },
                            modifier = Modifier.testTag("admin_maintenance_switch")
                        )
                    }

                    OutlinedTextField(
                        value = maintenanceTitle,
                        onValueChange = { maintenanceTitle = it },
                        label = { Text("Bakım Ekranı Başlığı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = maintenanceMessage,
                        onValueChange = { maintenanceMessage = it },
                        label = { Text("Kullanıcılara Gösterilecek Bakım Mesajı") },
                        minLines = 3,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = maintenanceEta,
                        onValueChange = { maintenanceEta = it },
                        label = { Text("Tahmini Süre") },
                        placeholder = { Text("Örn: Yaklaşık 15 dakika") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Live Dashboard Announcement Banner
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ana Sayfa Canlı Duyuru Şeridi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buraya yazdığın mesaj tüm kullanıcıların ana sayfasının üst panelinde dikkat çekici bir bant olarak görünür (kapatmak için boş bırak).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = bannerText,
                        onValueChange = { bannerText = it },
                        label = { Text("Canlı Üst Duyuru Metni (Opsiyonel)") },
                        placeholder = { Text("Örn: 🌹 Sevgililer Günü özel sözleri ve temaları eklendi!") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // App-Wide Feature Controls
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Uygulama İçi Özellik Kontrolleri",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    AdminToggleRow(
                        title = "Yeni Kullanıcı Kayıtları Açık",
                        subtitle = "Kullanıcıların Kayıt Ol bölümünden yeni hesap açmasına izin ver",
                        checked = allowRegistration,
                        onCheckedChange = { allowRegistration = it }
                    )
                    AdminToggleRow(
                        title = "Kullanıcı Özel Fon Müziği Ekleme",
                        subtitle = "Kullanıcıların kendi MP3/WAV fon müziklerini eklemesine izin ver",
                        checked = allowCustomMusic,
                        onCheckedChange = { allowCustomMusic = it }
                    )
                    AdminToggleRow(
                        title = "Topluluk Yorumları Açık",
                        subtitle = "Topluluk sözlerinin altına yorum bırakılmasına izin ver",
                        checked = allowComments,
                        onCheckedChange = { allowComments = it }
                    )
                }
            }
        }

        // AdMob & Monetization Management Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("admin_monetization_card"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Reklam & Gelir Yönetimi 💎",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Tahmini Gelir",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale("tr"), "%.2f", entitlementState.estimatedRevenueTry)} ₺",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "VIP / Paket • Coin",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${entitlementState.totalVipOrPackPurchases} VIP • ${entitlementState.totalCoinPurchases} Coin",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    AdminToggleRow(
                        title = "Uygulama İçi Reklamlar Aktif (AdMob)",
                        subtitle = "VIP veya Reklamsız Paket sahipleri reklamları asla görmez",
                        checked = adsEnabled,
                        onCheckedChange = { adsEnabled = it }
                    )

                    Column {
                        Text(
                            text = "Ödüllü Video Başına Aşk Parası: +$rewardCoinsPerAd 🪙",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = rewardCoinsPerAd.toFloat(),
                            onValueChange = { rewardCoinsPerAd = (it.toInt() / 5) * 5 },
                            valueRange = 5f..50f,
                            steps = 8
                        )
                    }

                    Column {
                        Text(
                            text = "Geçiş Reklamı Bekleme Süresi: ${interstitialCooldownSeconds} sn",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = interstitialCooldownSeconds.toFloat(),
                            onValueChange = { interstitialCooldownSeconds = (it.toInt() / 30) * 30 },
                            valueRange = 60f..600f,
                            steps = 17
                        )
                    }

                    AdminToggleRow(
                        title = "Yönetici Test VIP Modu",
                        subtitle = if (entitlementState.isVip) "VIP ayrıcalıkları test için aktif (${entitlementState.vipPlanName.ifBlank { "VIP" }})"
                        else "VIP çerçeveleri ve reklamsız modu test etmek için aç",
                        checked = entitlementState.isVip,
                        onCheckedChange = onToggleTestVip
                    )

                    AdminToggleRow(
                        title = "Reklam Engelleyici Algılama Koruması",
                        subtitle = "Sadece ücretsiz modda aktif DNS/reklam engelleyici tespit edilen kullanıcılara uyarı gösterir",
                        checked = adBlockerGuardEnabled,
                        onCheckedChange = { adBlockerGuardEnabled = it }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSaveSettings(
                        settings.copy(
                            maintenanceMode = maintenanceMode,
                            maintenanceTitle = maintenanceTitle.ifBlank { "Kısa Bir Aşk Molası 🛠️" },
                            maintenanceMessage = maintenanceMessage.ifBlank { "Bakım çalışması yapılıyor." },
                            maintenanceEstimatedTime = maintenanceEta,
                            announcementBannerText = bannerText.trim(),
                            allowUserRegistration = allowRegistration,
                            allowCustomMusic = allowCustomMusic,
                            allowCommunityComments = allowComments,
                            adsEnabled = adsEnabled,
                            rewardCoinsPerAd = rewardCoinsPerAd,
                            interstitialCooldownSeconds = interstitialCooldownSeconds,
                            adBlockerGuardEnabled = adBlockerGuardEnabled
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 50.dp)
                    .testTag("admin_save_maintenance_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bakım & Sistem Ayarlarını Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
