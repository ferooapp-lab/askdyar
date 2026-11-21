package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LoveQuotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class LibraryItem(
    val uniqueKey: String,
    val title: String,
    val text: String,
    val author: String,
    val category: String,
    val sourceBadge: String,
    val timestamp: Long
) {
    class CuratedFav(
        val quoteId: String,
        title: String,
        text: String,
        author: String,
        category: String
    ) : LibraryItem("curated_$quoteId", title, text, author, category, "Hazır Sözler 💖", 0L)

    class CustomSavedFav(
        val savedId: Long,
        title: String,
        text: String,
        author: String,
        category: String,
        sourceLabel: String,
        createdAt: Long
    ) : LibraryItem("saved_$savedId", title, text, author, category, "$sourceLabel ✨", createdAt)

    class NoteFav(
        val noteId: Long,
        title: String,
        text: String,
        author: String,
        tags: String,
        updatedAt: Long
    ) : LibraryItem("note_$noteId", title, text, author, tags.ifBlank { "Aşk Notu" }, "Notlarım 📝", updatedAt)
}

@Composable
fun FavoritesLibraryScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quotes by viewModel.quotesWithFavorites.collectAsState()
    val savedFavorites by viewModel.savedFavoriteQuotes.collectAsState()
    val notes by viewModel.allNotes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceFilter by remember { mutableStateOf("Tümü") }
    var showAddDialog by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    val allLibraryItems = remember(quotes, savedFavorites, notes) {
        val list = mutableListOf<LibraryItem>()
        savedFavorites.forEach { saved ->
            list.add(
                LibraryItem.CustomSavedFav(
                    savedId = saved.id,
                    title = saved.title,
                    text = saved.text,
                    author = saved.author,
                    category = saved.category,
                    sourceLabel = saved.sourceLabel,
                    createdAt = saved.createdAt
                )
            )
        }
        quotes.filter { it.isFavorite }.forEach { q ->
            list.add(
                LibraryItem.CuratedFav(
                    quoteId = q.id,
                    title = q.subCategory.ifBlank { q.section.title },
                    text = q.text,
                    author = q.author,
                    category = q.section.title
                )
            )
        }
        notes.filter { it.isFavorite }.forEach { n ->
            list.add(
                LibraryItem.NoteFav(
                    noteId = n.id,
                    title = n.title,
                    text = n.content,
                    author = n.author,
                    tags = n.tags,
                    updatedAt = n.updatedAt
                )
            )
        }
        list
    }

    val filteredItems = remember(allLibraryItems, selectedSourceFilter, searchQuery) {
        allLibraryItems.filter { item ->
            val matchFilter = when (selectedSourceFilter) {
                "Özel Koleksiyon" -> item is LibraryItem.CustomSavedFav
                "Hazır Sözler" -> item is LibraryItem.CuratedFav
                "Notlarım" -> item is LibraryItem.NoteFav
                else -> true
            }
            val matchSearch = searchQuery.isBlank() ||
                item.text.contains(searchQuery, ignoreCase = true) ||
                item.author.contains(searchQuery, ignoreCase = true) ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    val filterTabs = listOf("Tümü", "Özel Koleksiyon", "Hazır Sözler", "Notlarım")

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_library_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header with Back Button & Stats
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
                            modifier = Modifier.testTag("favorites_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri Dön"
                            )
                        }
                        Column {
                            Text(
                                text = "Favorilerim Kütüphanesi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Beğendiğin ve kişisel olarak sakladığın tüm sözler",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${allLibraryItems.size}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Summary Banner
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            LibraryStatPill("Özel Kayıt", savedFavorites.size)
                            LibraryStatPill("Hazır Söz", quotes.count { it.isFavorite })
                            LibraryStatPill("Favori Not", notes.count { it.isFavorite })
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Favorilerim kütüphanesinde ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("favorites_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTabs) { tab ->
                        val isSelected = (selectedSourceFilter == tab)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSourceFilter = tab },
                            label = { Text(tab, fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Content
            if (filteredItems.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (allLibraryItems.isEmpty())
                                    "Kişisel kütüphanen henüz boş 💌"
                                else
                                    "Aramana uygun favori söz bulunamadı",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sözlerin yanındaki kalp veya Favorilere Kaydet simgesine dokunarak ya da '+' butonuyla kendi özel sözlerini ekleyerek kişisel arşivini oluşturabilirsin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("İlk Favori Sözünü Ekle")
                            }
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.uniqueKey }) { item ->
                    FavoriteLibraryItemCard(
                        item = item,
                        onOpenInStudio = {
                            viewModel.loadCustomQuoteIntoStudio(item.text, item.author)
                            onNavigateToStudio()
                        },
                        onRemove = {
                            when (item) {
                                is LibraryItem.CustomSavedFav -> viewModel.removeFromFavoritesLibrary(item.savedId)
                                is LibraryItem.CuratedFav -> viewModel.toggleQuoteFavorite(item.quoteId)
                                is LibraryItem.NoteFav -> {
                                    notes.find { it.id == item.noteId }?.let { viewModel.toggleNoteFavorite(it) }
                                }
                            }
                            Toast.makeText(context, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // FAB to add personal favorite quote directly
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("add_favorite_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Favori Söz Ekle")
                Text("Favori Ekle", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AddPersonalFavoriteDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, text, author, category ->
                viewModel.saveToFavoritesLibrary(
                    title = title,
                    text = text,
                    author = author,
                    category = category,
                    sourceLabel = "Kişisel Arşiv"
                ) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun LibraryStatPill(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FavoriteLibraryItemCard(
    item: LibraryItem,
    onOpenInStudio: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr")) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = item.sourceBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (item.timestamp > 0L) {
                    Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.title.isNotBlank() && item.title != item.category) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "\"${item.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${item.author}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val sendText = "\"${item.text}\"\n\n— ${item.author}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(sendText)}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, sendText)
                                }
                                context.startActivity(Intent.createChooser(share, "Paylaş"))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(19.dp))
                    }
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Favori Söz", "\"${item.text}\"\n— ${item.author}"))
                            Toast.makeText(context, "Kopyalandı! 📋", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp))
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "\"${item.text}\"\n\n— ${item.author}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Paylaş"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = onOpenInStudio) {
                        Icon(Icons.Default.Palette, contentDescription = "Kart Yap", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Favorilerden Çıkar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPersonalFavoriteDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, text: String, author: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Aşk Sözleri") }

    val categories = listOf("Aşk Sözleri", "Romantik", "Şiir", "Günaydın", "İyi Geceler", "Anlamlı")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Favorilerime Söz Kaydet ❤️", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık / Hatıra (Opsiyonel)") },
                    placeholder = { Text("Örn: İlk Yıldönümü Sözümüz") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Aşk Sözü veya Dize") },
                    placeholder = { Text("Saklamak istediğin özel sözü yaz...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Yazar / Kime Ait?") },
                    placeholder = { Text("Örn: Ben, Nazım Hikmet...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
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
                    if (text.isNotBlank()) {
                        onSave(title, text, author, selectedCategory)
                    }
                },
                enabled = text.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}
