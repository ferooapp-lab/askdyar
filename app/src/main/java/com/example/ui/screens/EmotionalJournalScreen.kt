package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.EmotionalJournalEntry
import com.example.data.model.JournalCalendarDay
import com.example.data.model.JournalDateHelper
import com.example.data.model.JournalDateScope
import com.example.data.model.JournalMood
import com.example.data.model.JournalSecurityHelper
import com.example.ui.LoveQuotesViewModel
import com.example.ui.theme.EditorialQuoteFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmotionalJournalScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateBack: () -> Unit,
    onOpenInStudio: (EmotionalJournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val entries by viewModel.journalEntries.collectAsState()
    val isPinEnabled by viewModel.isJournalPinEnabled.collectAsState()
    val isUnlocked by viewModel.isJournalUnlocked.collectAsState()
    val isPrivacyShieldOn by viewModel.isJournalPrivacyShieldOn.collectAsState()

    var dateScope by remember { mutableStateOf(JournalDateScope.ALL) }
    var selectedDateKey by remember {
        mutableStateOf(JournalDateHelper.toDateKey(System.currentTimeMillis()))
    }
    var selectedMood by remember { mutableStateOf<JournalMood?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showCalendarJumpPicker by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var editorTargetEntry by remember { mutableStateOf<EmotionalJournalEntry?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }
    var deleteConfirmEntry by remember { mutableStateOf<EmotionalJournalEntry?>(null) }
    var revealedLockedEntryIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    AnimatedContent(
        targetState = (!isPinEnabled || isUnlocked),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "journalVaultTransition",
        modifier = modifier
            .fillMaxSize()
            .testTag("emotional_journal_screen")
    ) { vaultOpen ->
        if (!vaultOpen) {
            JournalPinLockGate(
                onNavigateBack = onNavigateBack,
                onVerifyPin = { candidate -> viewModel.unlockJournalWithPin(candidate) }
            )
        } else {
            val calendarRibbonDays = remember(entries) {
                JournalDateHelper.buildCalendarRibbonDays(entries)
            }
            val filteredEntries = remember(entries, dateScope, selectedDateKey, selectedMood, searchQuery) {
                JournalDateHelper.filterEntries(
                    entries = entries,
                    scope = dateScope,
                    selectedDateKey = selectedDateKey,
                    selectedMood = selectedMood,
                    searchQuery = searchQuery
                )
            }
            val dateSections = remember(filteredEntries) {
                JournalDateHelper.groupEntriesByDate(filteredEntries)
            }
            val distinctDatesCount = remember(entries) {
                entries.map { it.dateKey }.distinct().size
            }
            val favoritesCount = remember(entries) {
                entries.count { it.isFavorite }
            }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val defaultDateKey = if (dateScope == JournalDateScope.SELECTED_DAY) {
                                selectedDateKey
                            } else {
                                JournalDateHelper.toDateKey(System.currentTimeMillis())
                            }
                            editorTargetEntry = EmotionalJournalEntry(
                                dateKey = defaultDateKey,
                                memoryTimestamp = JournalDateHelper.parseDateKeyToNoonMillis(defaultDateKey)
                            )
                            showEditorDialog = true
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Yeni Anı Yaz", fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .testTag("journal_add_fab")
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Top Navigation & Security Actions Bar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                        .testTag("journal_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Geri Dön",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Duygusal Günlük",
                                        fontFamily = PlayfairDisplayFontFamily,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Tarih bazlı, kişiye özel güvenli aşk arşivin",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleJournalPrivacyShield() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("journal_privacy_shield_btn")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPrivacyShieldOn) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isPrivacyShieldOn) {
                                                    Icons.Default.VisibilityOff
                                                } else {
                                                    Icons.Default.Visibility
                                                },
                                                contentDescription = if (isPrivacyShieldOn) {
                                                    "Gizlilik Perdesini Kapat"
                                                } else {
                                                    "Gizlilik Perdesini Aç"
                                                },
                                                tint = if (isPrivacyShieldOn) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { showSecurityDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("journal_vault_lock_btn")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPinEnabled) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isPinEnabled) {
                                                    Icons.Default.Lock
                                                } else {
                                                    Icons.Default.LockOpen
                                                },
                                                contentDescription = "Günlük Kasa Güvenliği",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Private Vault Banner + Summary Stats Card
                    item {
                        JournalSecurityHeroCard(
                            totalMemories = entries.size,
                            distinctDatesCount = distinctDatesCount,
                            favoritesCount = favoritesCount,
                            isPinEnabled = isPinEnabled,
                            isPrivacyShieldOn = isPrivacyShieldOn,
                            onOpenSecurity = { showSecurityDialog = true },
                            onLockNow = {
                                if (isPinEnabled) {
                                    viewModel.lockJournalNow()
                                } else {
                                    showSecurityDialog = true
                                }
                            }
                        )
                    }

                    // 3. Date-Based Navigation: Scope Chips + Calendar Picker Button
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { showCalendarJumpPicker = true }
                                            .testTag("journal_open_calendar_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = "Takvimden Tarih Seç",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Takvimden Seç",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                                items(JournalDateScope.values()) { scope ->
                                    val isSelected = (dateScope == scope)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { dateScope = scope },
                                        label = {
                                            Text(
                                                text = scope.label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            // Interactive Calendar Day Ribbon
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("journal_date_ribbon")
                            ) {
                                items(calendarRibbonDays, key = { it.dateKey }) { day ->
                                    val isDaySelected =
                                        (dateScope == JournalDateScope.SELECTED_DAY && selectedDateKey == day.dateKey)
                                    JournalCalendarDayChip(
                                        day = day,
                                        isSelected = isDaySelected,
                                        onClick = {
                                            if (isDaySelected) {
                                                dateScope = JournalDateScope.ALL
                                            } else {
                                                selectedDateKey = day.dateKey
                                                dateScope = JournalDateScope.SELECTED_DAY
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Search & Mood Filter Row
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Anı başlığı, duygu veya yer ara...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Aramayı Temizle"
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .testTag("journal_search_input")
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedMood == null,
                                        onClick = { selectedMood = null },
                                        label = { Text("Tüm Hisler") },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                                items(JournalMood.values()) { mood ->
                                    val isSel = (selectedMood == mood)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedMood = if (isSel) null else mood },
                                        label = { Text("${mood.emoji} ${mood.label}") },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Active filter banner when filtering by a specific date
                    if (dateScope == JournalDateScope.SELECTED_DAY) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📅 ${JournalDateHelper.formatFullTurkishDate(selectedDateKey)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { dateScope = JournalDateScope.ALL }) {
                                        Text("Tümünü Gör", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 5. Empty State or Date-Grouped Memory Timeline
                    if (dateSections.isEmpty()) {
                        item {
                            JournalEmptyStateCard(
                                isFiltered = (dateScope != JournalDateScope.ALL || selectedMood != null || searchQuery.isNotBlank()),
                                selectedDateLabel = if (dateScope == JournalDateScope.SELECTED_DAY) {
                                    JournalDateHelper.formatFullTurkishDate(selectedDateKey)
                                } else null,
                                onAddMemory = {
                                    val targetKey = if (dateScope == JournalDateScope.SELECTED_DAY) {
                                        selectedDateKey
                                    } else {
                                        JournalDateHelper.toDateKey(System.currentTimeMillis())
                                    }
                                    editorTargetEntry = EmotionalJournalEntry(
                                        dateKey = targetKey,
                                        memoryTimestamp = JournalDateHelper.parseDateKeyToNoonMillis(targetKey)
                                    )
                                    showEditorDialog = true
                                },
                                onResetFilters = {
                                    dateScope = JournalDateScope.ALL
                                    selectedMood = null
                                    searchQuery = ""
                                }
                            )
                        }
                    } else {
                        dateSections.forEach { section ->
                            item(key = "header_${section.dateKey}") {
                                JournalDateSectionHeader(
                                    dateLabel = section.formattedHeader,
                                    count = section.entries.size,
                                    onAddForDate = {
                                        editorTargetEntry = EmotionalJournalEntry(
                                            dateKey = section.dateKey,
                                            memoryTimestamp = JournalDateHelper.parseDateKeyToNoonMillis(section.dateKey)
                                        )
                                        showEditorDialog = true
                                    }
                                )
                            }

                            items(
                                items = section.entries,
                                key = { "entry_${it.id}_${it.docId}" }
                            ) { entry ->
                                val isEntryMasked = isPrivacyShieldOn ||
                                    (entry.isPrivateLocked && !revealedLockedEntryIds.contains(entry.id))
                                EmotionalJournalEntryCard(
                                    entry = entry,
                                    isMasked = isEntryMasked,
                                    onToggleMaskReveal = {
                                        revealedLockedEntryIds = if (revealedLockedEntryIds.contains(entry.id)) {
                                            revealedLockedEntryIds - entry.id
                                        } else {
                                            revealedLockedEntryIds + entry.id
                                        }
                                    },
                                    onToggleFavorite = { viewModel.toggleJournalFavorite(entry) },
                                    onTogglePrivateLock = { viewModel.toggleJournalEntryPrivateLock(entry) },
                                    onEdit = {
                                        editorTargetEntry = entry
                                        showEditorDialog = true
                                    },
                                    onDelete = { deleteConfirmEntry = entry },
                                    onOpenInStudio = { onOpenInStudio(entry) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Calendar Date Jump Picker
    if (showCalendarJumpPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = JournalDateHelper.parseDateKeyToNoonMillis(selectedDateKey)
        )
        DatePickerDialog(
            onDismissRequest = { showCalendarJumpPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { picked ->
                            selectedDateKey = JournalDateHelper.toDateKey(picked)
                            dateScope = JournalDateScope.SELECTED_DAY
                        }
                        showCalendarJumpPicker = false
                    }
                ) {
                    Text("Tarihi Seç", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarJumpPicker = false }) {
                    Text("Vazgeç")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Vault Security PIN Modal
    if (showSecurityDialog) {
        JournalVaultSecurityDialog(
            isPinEnabled = isPinEnabled,
            isPrivacyShieldOn = isPrivacyShieldOn,
            onTogglePrivacyShield = { viewModel.toggleJournalPrivacyShield() },
            onLockNow = {
                viewModel.lockJournalNow()
                showSecurityDialog = false
            },
            onEnablePin = { pin ->
                viewModel.enableJournalPin(pin) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) showSecurityDialog = false
                }
            },
            onDisablePin = { currentPin ->
                viewModel.disableJournalPin(currentPin) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) showSecurityDialog = false
                }
            },
            onDismiss = { showSecurityDialog = false }
        )
    }

    // Add / Edit Memory Dialog
    if (showEditorDialog && editorTargetEntry != null) {
        JournalEntryEditorDialog(
            initialEntry = editorTargetEntry!!,
            onDismiss = {
                showEditorDialog = false
                editorTargetEntry = null
            },
            onSave = { updated ->
                viewModel.saveJournalEntry(updated) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) {
                        selectedDateKey = updated.dateKey
                        showEditorDialog = false
                        editorTargetEntry = null
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    deleteConfirmEntry?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmEntry = null },
            title = { Text("Anıyı Sil") },
            text = {
                Text("\"${target.title}\" başlıklı bu günlük kaydını kalıcı olarak silmek istediğine emin misin?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteJournalEntry(target) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        deleteConfirmEntry = null
                    }
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmEntry = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun JournalSecurityHeroCard(
    totalMemories: Int,
    distinctDatesCount: Int,
    favoritesCount: Int,
    isPinEnabled: Boolean,
    isPrivacyShieldOn: Boolean,
    onOpenSecurity: () -> Unit,
    onLockNow: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                        )
                    )
                ),
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_romantic_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.12f,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Yalnızca Sana Özel Bulut Kasası",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Başka hiçbir kullanıcı veya yönetici erişemez",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPinEnabled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { if (isPinEnabled) onLockNow() else onOpenSecurity() }
                    ) {
                        Text(
                            text = if (isPinEnabled) "🔒 Kilitle" else "🛡️ PIN Ekle",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPinEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JournalStatPill(
                        value = totalMemories.toString(),
                        label = "Anı",
                        modifier = Modifier.weight(1f)
                    )
                    JournalStatPill(
                        value = distinctDatesCount.toString(),
                        label = "Özel Tarih",
                        modifier = Modifier.weight(1f)
                    )
                    JournalStatPill(
                        value = favoritesCount.toString(),
                        label = "Favori Anı",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isPrivacyShieldOn) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Gizlilik Perdesi açık: Anı metinleri meraklı gözlerden gizleniyor.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalStatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun JournalCalendarDayChip(
    day: JournalCalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        day.entriesCount > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        day.entriesCount > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderStroke = when {
        isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        day.isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = borderStroke,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("journal_day_chip_${day.dateKey}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = if (day.isToday) "Bugün" else day.shortWeekday,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
            Text(
                text = day.dayNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
            Text(
                text = day.shortMonth,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            day.entriesCount > 0 && isSelected -> MaterialTheme.colorScheme.onPrimary
                            day.entriesCount > 0 -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }
                    )
            )
        }
    }
}

@Composable
private fun JournalDateSectionHeader(
    dateLabel: String,
    count: Int,
    onAddForDate: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                )
            }
            Column {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count anı kaydı",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TextButton(onClick = onAddForDate) {
            Text(
                text = "+ Bu Güne Yaz",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmotionalJournalEntryCard(
    entry: EmotionalJournalEntry,
    isMasked: Boolean,
    onToggleMaskReveal: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePrivateLock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenInStudio: () -> Unit
) {
    val mood = entry.mood
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            )
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = if (entry.isFavorite) 0.35f else 0.14f)
                ),
                RoundedCornerShape(22.dp)
            )
            .testTag("journal_entry_card_${entry.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Meta Row: Mood badge + Location + Favorite & Privacy icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "${mood.emoji} ${mood.label}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    if (entry.locationOrMoment.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = entry.locationOrMoment,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePrivateLock,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isPrivateLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (entry.isPrivateLocked) "Anı Gizli Kilitli" else "Anıyı Gizli Kilitle",
                            tint = if (entry.isPrivateLocked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Özel Anı Favorisi",
                            tint = if (entry.isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Title
            Text(
                text = entry.title,
                fontFamily = PlayfairDisplayFontFamily,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Body or Veil Mask
            if (isMasked) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (entry.isPrivateLocked) onToggleMaskReveal()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (entry.isPrivateLocked) {
                                "Gizli anı koruması aktif. Okumak için dokunun."
                            } else {
                                "Gizlilik perdesi açık. İçeriği görmek için üstteki göz simgesine dokunun."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = entry.content,
                    fontFamily = EditorialQuoteFontFamily,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 25.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                )
                if (entry.isPrivateLocked) {
                    Text(
                        text = "🔒 Gizli anı açık (Tekrar gizlemek için dokun)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onToggleMaskReveal() }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenInStudio) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Kart Yap", style = MaterialTheme.typography.labelMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalEmptyStateCard(
    isFiltered: Boolean,
    selectedDateLabel: String?,
    onAddMemory: () -> Unit,
    onResetFilters: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = when {
                    selectedDateLabel != null -> "Bu Tarihte Henüz Anı Yok"
                    isFiltered -> "Filtreye Uygun Anı Bulunamadı"
                    else -> "Duygusal Günlüğün Seni Bekliyor"
                },
                fontFamily = PlayfairDisplayFontFamily,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = when {
                    selectedDateLabel != null ->
                        "$selectedDateLabel için kalbinden geçen bir anıyı veya küçük bir aşk notunu hemen ekleyebilirsin."
                    isFiltered ->
                        "Farklı bir tarih veya duygu filtresi seçerek tüm anılarını görüntüleyebilirsin."
                    else ->
                        "İlk buluşmanızı, kalbini ısıtan sözleri ve o güne dair hislerini tarih bazlı güvenle sakla."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onAddMemory) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anı Ekle")
                }
                if (isFiltered) {
                    OutlinedButton(onClick = onResetFilters) {
                        Text("Filtreyi Temizle")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryEditorDialog(
    initialEntry: EmotionalJournalEntry,
    onDismiss: () -> Unit,
    onSave: (EmotionalJournalEntry) -> Unit
) {
    var title by remember { mutableStateOf(initialEntry.title) }
    var content by remember { mutableStateOf(initialEntry.content) }
    var locationOrMoment by remember { mutableStateOf(initialEntry.locationOrMoment) }
    var selectedMood by remember { mutableStateOf(JournalMood.fromId(initialEntry.moodId)) }
    var dateKey by remember { mutableStateOf(initialEntry.dateKey) }
    var memoryTimestamp by remember { mutableStateOf(initialEntry.memoryTimestamp) }
    var isFavorite by remember { mutableStateOf(initialEntry.isFavorite) }
    var isPrivateLocked by remember { mutableStateOf(initialEntry.isPrivateLocked) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEdit = initialEntry.docId.isNotBlank() || initialEntry.id != 0L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Anıyı Düzenle ✨" else "Yeni Duygusal Anı 📖",
                        fontFamily = PlayfairDisplayFontFamily,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Kapat")
                    }
                }

                // Memory Date Selector Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .testTag("journal_editor_date_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Anı Tarihi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = JournalDateHelper.formatFullTurkishDate(dateKey),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = "Değiştir",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Mood Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "O Anki Duygun",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JournalMood.values().forEach { mood ->
                            val selected = (selectedMood == mood)
                            FilterChip(
                                selected = selected,
                                onClick = { selectedMood = mood },
                                label = { Text("${mood.emoji} ${mood.label}") },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Anı Başlığı *") },
                    placeholder = { Text("Örn: İlk Yağmur Yürüyüşümüz") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_editor_title")
                )

                OutlinedTextField(
                    value = locationOrMoment,
                    onValueChange = { if (it.length <= 100) locationOrMoment = it },
                    label = { Text("Yer veya Özel An (İsteğe Bağlı)") },
                    placeholder = { Text("Örn: Sahil Kahvesi • Yıldönümü") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_editor_location")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 6000) content = it },
                    label = { Text("Duyguların ve Anı Notun *") },
                    placeholder = { Text(selectedMood.toneHint + "...") },
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_editor_content")
                )

                // Privacy & Favorite Toggles
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "❤️ Özel Anı Olarak İşaretle",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Özel Anılar filtresinde öne çıkar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔒 Anı Kartında Metni Gizle",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Listede sadece başlık görünür, dokununca açılır",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = isPrivateLocked, onCheckedChange = { isPrivateLocked = it })
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = title.trim().isNotEmpty() && content.trim().isNotEmpty(),
                        onClick = {
                            onSave(
                                initialEntry.copy(
                                    title = title.trim(),
                                    content = content.trim(),
                                    locationOrMoment = locationOrMoment.trim(),
                                    moodId = selectedMood.id,
                                    dateKey = dateKey,
                                    memoryTimestamp = memoryTimestamp,
                                    isFavorite = isFavorite,
                                    isPrivateLocked = isPrivateLocked
                                )
                            )
                        },
                        modifier = Modifier.testTag("journal_editor_save_btn")
                    ) {
                        Text(if (isEdit) "Güncelle" else "Güvenle Kaydet")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = memoryTimestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            memoryTimestamp = JournalDateHelper.normalizeDateMillis(picked)
                            dateKey = JournalDateHelper.toDateKey(memoryTimestamp)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Tamam", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun JournalPinLockGate(
    onNavigateBack: () -> Unit,
    onVerifyPin: (String) -> Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri Dön"
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Text(
                text = "Duygusal Günlük Kilitli",
                fontFamily = PlayfairDisplayFontFamily,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Özel aşk anılarını görüntülemek için 4 haneli kasa şifreni gir",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // 4-dot PIN display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                repeat(4) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }

            errorMessage?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            // Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        row.forEach { key ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        errorMessage = null
                                        when (key) {
                                            "C" -> enteredPin = ""
                                            "DEL" -> if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                            else -> if (enteredPin.length < 4) {
                                                val next = enteredPin + key
                                                enteredPin = next
                                                if (next.length == 4) {
                                                    if (!onVerifyPin(next)) {
                                                        errorMessage = "Hatalı PIN, lütfen tekrar dene."
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("journal_pin_key_$key")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (key == "DEL") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Sil",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (key == "C") {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun JournalVaultSecurityDialog(
    isPinEnabled: Boolean,
    isPrivacyShieldOn: Boolean,
    onTogglePrivacyShield: () -> Unit,
    onLockNow: () -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Günlük Kasa Güvenliği 🔒",
                fontFamily = PlayfairDisplayFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gizlilik Perdesi",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Açıkken tüm anı içerikleri maskelenir",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isPrivacyShieldOn, onCheckedChange = { onTogglePrivacyShield() })
                }

                HorizontalDivider()

                if (isPinEnabled) {
                    Text(
                        text = "4 haneli kasa kilidi aktif. Günlüğü hemen kilitleyebilir veya mevcut PIN'i girerek kilidi kaldırabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(4)
                            pinInput = digits
                            validationError = null
                        },
                        label = { Text("Mevcut 4 Haneli PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = onLockNow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Günlüğü Şimdi Kilitle")
                    }
                } else {
                    Text(
                        text = "Günlüğüne girerken sorulacak 4 haneli özel bir şifre belirle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { raw ->
                            pinInput = raw.filter { it.isDigit() }.take(4)
                            validationError = null
                        },
                        label = { Text("Yeni 4 Haneli PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { raw ->
                            confirmPinInput = raw.filter { it.isDigit() }.take(4)
                            validationError = null
                        },
                        label = { Text("PIN Tekrar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                validationError?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isPinEnabled) {
                        if (!JournalSecurityHelper.isValidPinFormat(pinInput)) {
                            validationError = "Lütfen mevcut 4 haneli PIN kodunu girin."
                        } else {
                            onDisablePin(pinInput)
                        }
                    } else {
                        when {
                            !JournalSecurityHelper.isValidPinFormat(pinInput) -> {
                                validationError = "PIN tam 4 rakamdan oluşmalıdır."
                            }
                            pinInput != confirmPinInput -> {
                                validationError = "Girdiğin PIN kodları birbiriyle eşleşmiyor."
                            }
                            else -> onEnablePin(pinInput)
                        }
                    }
                }
            ) {
                Text(if (isPinEnabled) "PIN Kilidini Kaldır" else "PIN Kilidini Aç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
