package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.DailyQuoteDeepLink
import com.example.R
import com.example.data.firebase.authStateFlow
import com.example.ui.components.AdBlockerNoticeDialog
import com.example.ui.screens.AdminContactScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiAndInsightsScreen
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CommunityQuotesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmotionalJournalScreen
import com.example.ui.screens.FavoritesLibraryScreen
import com.example.ui.screens.LoveToolsScreen
import com.example.ui.screens.MaintenanceModeScreen
import com.example.ui.screens.MonetizationStoreScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.RomanticMusicPlayerSheet
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.ThemePalettePickerSheet
import com.example.ui.theme.AppDesignTheme
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Sözler", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_dashboard"),
    AI_HUB("AI & Analiz", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai_hub"),
    LOVE_TOOLS("Aşk Alanı", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "nav_love_tools"),
    STUDIO("Stüdyo", Icons.Filled.Palette, Icons.Outlined.Palette, "nav_studio"),
    NOTES("Notlarım", Icons.Filled.EditNote, Icons.Outlined.EditNote, "nav_notes")
}

@Composable
fun MainScreen(
    deepLinkQuote: DailyQuoteDeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val firebaseUser by Firebase.auth.authStateFlow()
        .collectAsState(initial = Firebase.auth.currentUser)

    val activeUser = firebaseUser
    if (activeUser == null) {
        MyApplicationTheme(
            darkTheme = isSystemInDarkTheme(),
            designTheme = AppDesignTheme.RUBY_VELVET
        ) {
            AuthScreen(allowBack = false)
        }
    } else {
        key(activeUser.uid) {
            val viewModel: LoveQuotesViewModel = viewModel(
                key = activeUser.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                        val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                        LoveQuotesViewModel(app, db)
                    }
                }
            )
            AuthenticatedMainContent(
                viewModel = viewModel,
                deepLinkQuote = deepLinkQuote,
                onDeepLinkConsumed = onDeepLinkConsumed
            )
        }
    }
}

@Composable
private fun AuthenticatedMainContent(
    viewModel: LoveQuotesViewModel,
    deepLinkQuote: DailyQuoteDeepLink?,
    onDeepLinkConsumed: () -> Unit
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val designTheme by viewModel.designTheme.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()
    val accountStatusNotice by viewModel.accountStatusNotice.collectAsState()
    val entitlementState by viewModel.entitlementState.collectAsState()
    val isAdBlockerDetected by viewModel.isAdBlockerDetected.collectAsState()
    val adBlockerReason by viewModel.adBlockerReason.collectAsState()
    val isCheckingAdBlocker by viewModel.isCheckingAdBlocker.collectAsState()
    val billingStatusMessage by viewModel.billingStatusMessage.collectAsState()

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    LaunchedEffect(billingStatusMessage) {
        billingStatusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearBillingStatusMessage()
        }
    }

    MyApplicationTheme(darkTheme = isDark, designTheme = designTheme) {
        var currentDestination by rememberSaveable { mutableStateOf(MainDestination.DASHBOARD) }
        var aiHubInitialTab by rememberSaveable { mutableIntStateOf(0) }
        var showRemindersScreen by rememberSaveable { mutableStateOf(false) }
        var showCommunityScreen by rememberSaveable { mutableStateOf(false) }
        var showAdminScreen by rememberSaveable { mutableStateOf(false) }
        var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
        var showProfileScreen by rememberSaveable { mutableStateOf(false) }
        var showFavoritesScreen by rememberSaveable { mutableStateOf(false) }
        var showAdminContactScreen by rememberSaveable { mutableStateOf(false) }
        var showAnnouncementsScreen by rememberSaveable { mutableStateOf(false) }
        var showThemePaletteSheet by rememberSaveable { mutableStateOf(false) }
        var showMusicSheet by rememberSaveable { mutableStateOf(false) }
        var showStoreScreen by rememberSaveable { mutableStateOf(false) }
        var showChatScreen by rememberSaveable { mutableStateOf(false) }
        var showJournalScreen by rememberSaveable { mutableStateOf(false) }
        var adminInitialTab by rememberSaveable { mutableIntStateOf(0) }

        fun closeAllSubScreens() {
            showAdminScreen = false
            showAdminContactScreen = false
            showAnnouncementsScreen = false
            showFavoritesScreen = false
            showCommunityScreen = false
            showRemindersScreen = false
            showSettingsScreen = false
            showProfileScreen = false
            showStoreScreen = false
            showChatScreen = false
            showJournalScreen = false
        }

        LaunchedEffect(deepLinkQuote) {
            if (deepLinkQuote != null) {
                closeAllSubScreens()
                currentDestination = MainDestination.DASHBOARD
                viewModel.openQuoteFromNotification(
                    quoteId = deepLinkQuote.quoteId,
                    text = deepLinkQuote.text,
                    author = deepLinkQuote.author,
                    sectionId = deepLinkQuote.sectionId
                )
                onDeepLinkConsumed()
            }
        }

        when {
            adminSettings.maintenanceMode && !sessionState.isAdmin && !showAdminScreen -> {
                MaintenanceModeScreen(
                    settings = adminSettings,
                    onRetryCheck = {
                        val msg = if (adminSettings.maintenanceMode) {
                            "Bakım çalışması hala devam ediyor 🛠️"
                        } else {
                            "Bakım tamamlandı! ✅"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    onSwitchAccount = {
                        viewModel.logoutSession()
                    }
                )
            }

            else -> {
                val hasSubScreenOpen = showAdminScreen ||
                    showAdminContactScreen ||
                    showAnnouncementsScreen ||
                    showFavoritesScreen ||
                    showCommunityScreen ||
                    showRemindersScreen ||
                    showSettingsScreen ||
                    showProfileScreen ||
                    showStoreScreen ||
                    showChatScreen ||
                    showJournalScreen

                BackHandler(enabled = hasSubScreenOpen || currentDestination != MainDestination.DASHBOARD) {
                    when {
                        showAdminScreen -> showAdminScreen = false
                        showChatScreen -> showChatScreen = false
                        showJournalScreen -> showJournalScreen = false
                        showStoreScreen -> showStoreScreen = false
                        showAdminContactScreen -> showAdminContactScreen = false
                        showAnnouncementsScreen -> showAnnouncementsScreen = false
                        showFavoritesScreen -> showFavoritesScreen = false
                        showCommunityScreen -> showCommunityScreen = false
                        showRemindersScreen -> showRemindersScreen = false
                        showSettingsScreen -> showSettingsScreen = false
                        showProfileScreen -> showProfileScreen = false
                        else -> currentDestination = MainDestination.DASHBOARD
                    }
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        Column {
                            HorizontalDivider(
                                thickness = 0.8.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
                            )
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                tonalElevation = 4.dp,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                MainDestination.values().forEach { destination ->
                                    val isSelected = (!hasSubScreenOpen && currentDestination == destination)
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            closeAllSubScreens()
                                            if (destination == MainDestination.AI_HUB) {
                                                aiHubInitialTab = 0
                                            }
                                            currentDestination = destination
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                                contentDescription = destination.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = destination.title,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag(destination.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when {
                            showAdminScreen && sessionState.isAdmin -> {
                                AdminPanelScreen(
                                    viewModel = viewModel,
                                    initialTabIndex = adminInitialTab,
                                    onNavigateBack = {
                                        showAdminScreen = false
                                        adminInitialTab = 0
                                    }
                                )
                            }
                            showChatScreen -> {
                                ChatScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showChatScreen = false },
                                    onNavigateToAdminChatControl = {
                                        if (sessionState.isAdmin) {
                                            adminInitialTab = 7
                                            showAdminScreen = true
                                        }
                                    },
                                    onOpenAuth = { viewModel.logoutSession() }
                                )
                            }
                            showJournalScreen -> {
                                EmotionalJournalScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showJournalScreen = false },
                                    onOpenInStudio = { entry ->
                                        viewModel.loadJournalEntryIntoStudio(entry)
                                        showJournalScreen = false
                                        currentDestination = MainDestination.STUDIO
                                    }
                                )
                            }
                            showStoreScreen -> {
                                MonetizationStoreScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showStoreScreen = false }
                                )
                            }
                            showProfileScreen -> {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showProfileScreen = false },
                                    onNavigateToCommunity = {
                                        showProfileScreen = false
                                        showCommunityScreen = true
                                    },
                                    onNavigateToStudio = {
                                        showProfileScreen = false
                                        currentDestination = MainDestination.STUDIO
                                    },
                                    onNavigateToMoodInsights = {
                                        showProfileScreen = false
                                        aiHubInitialTab = 1
                                        currentDestination = MainDestination.AI_HUB
                                    },
                                    onNavigateToFavorites = {
                                        showProfileScreen = false
                                        showFavoritesScreen = true
                                    },
                                    onNavigateToSettings = {
                                        showProfileScreen = false
                                        showSettingsScreen = true
                                    },
                                    onNavigateToAdmin = {
                                        if (sessionState.isAdmin) {
                                            showProfileScreen = false
                                            showAdminScreen = true
                                        }
                                    },
                                    onNavigateToStore = {
                                        showProfileScreen = false
                                        showStoreScreen = true
                                    },
                                    onNavigateToJournal = {
                                        showProfileScreen = false
                                        showJournalScreen = true
                                    },
                                    onOpenAuth = { viewModel.logoutSession() }
                                )
                            }
                            showAdminContactScreen -> {
                                AdminContactScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showAdminContactScreen = false }
                                )
                            }
                            showAnnouncementsScreen -> {
                                AnnouncementsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showAnnouncementsScreen = false }
                                )
                            }
                            showFavoritesScreen -> {
                                FavoritesLibraryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showFavoritesScreen = false },
                                    onNavigateToStudio = {
                                        showFavoritesScreen = false
                                        currentDestination = MainDestination.STUDIO
                                    }
                                )
                            }
                            showCommunityScreen -> {
                                CommunityQuotesScreen(
                                    viewModel = viewModel,
                                    onNavigateToStudio = {
                                        showCommunityScreen = false
                                        currentDestination = MainDestination.STUDIO
                                    },
                                    onNavigateToAdmin = {
                                        if (sessionState.isAdmin) {
                                            adminInitialTab = 0
                                            showAdminScreen = true
                                        }
                                    },
                                    onNavigateToStore = {
                                        showCommunityScreen = false
                                        showStoreScreen = true
                                    },
                                    onNavigateToChat = {
                                        showCommunityScreen = false
                                        showChatScreen = true
                                    }
                                )
                            }
                            showRemindersScreen -> {
                                RemindersScreen(viewModel = viewModel)
                            }
                            showSettingsScreen -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onOpenReminders = {
                                        showSettingsScreen = false
                                        showRemindersScreen = true
                                    },
                                    onOpenCommunity = {
                                        showSettingsScreen = false
                                        showCommunityScreen = true
                                    },
                                    onOpenChat = {
                                        showSettingsScreen = false
                                        showChatScreen = true
                                    },
                                    onOpenAdmin = {
                                        if (sessionState.isAdmin) {
                                            adminInitialTab = 0
                                            showAdminScreen = true
                                        }
                                    },
                                    onOpenFavorites = {
                                        showSettingsScreen = false
                                        showFavoritesScreen = true
                                    },
                                    onOpenAnnouncements = {
                                        showSettingsScreen = false
                                        showAnnouncementsScreen = true
                                    },
                                    onOpenAdminContact = {
                                        showSettingsScreen = false
                                        showAdminContactScreen = true
                                    },
                                    onOpenThemePicker = { showThemePaletteSheet = true },
                                    onOpenMusicSheet = { showMusicSheet = true },
                                    onOpenAuth = { viewModel.logoutSession() },
                                    onOpenProfile = {
                                        showSettingsScreen = false
                                        showProfileScreen = true
                                    },
                                    onOpenStore = {
                                        showSettingsScreen = false
                                        showStoreScreen = true
                                    },
                                    onNavigateBack = { showSettingsScreen = false }
                                )
                            }
                            else -> {
                                when (currentDestination) {
                                    MainDestination.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToStudio = { currentDestination = MainDestination.STUDIO },
                                        onNavigateToNewNote = { currentDestination = MainDestination.NOTES },
                                        onNavigateToReminders = { showRemindersScreen = true },
                                        onNavigateToLoveTools = { currentDestination = MainDestination.LOVE_TOOLS },
                                        onNavigateToCommunity = { showCommunityScreen = true },
                                        onNavigateToChat = { showChatScreen = true },
                                        onNavigateToAdmin = {
                                            if (sessionState.isAdmin) {
                                                adminInitialTab = 0
                                                showAdminScreen = true
                                            }
                                        },
                                        onNavigateToSettings = { showSettingsScreen = true },
                                        onNavigateToFavorites = { showFavoritesScreen = true },
                                        onNavigateToAnnouncements = { showAnnouncementsScreen = true },
                                        onNavigateToAdminContact = { showAdminContactScreen = true },
                                        onOpenThemePicker = { showThemePaletteSheet = true },
                                        onOpenMusicSheet = { showMusicSheet = true },
                                        onOpenAuth = { viewModel.logoutSession() },
                                        onNavigateToProfile = { showProfileScreen = true },
                                        onNavigateToStore = { showStoreScreen = true },
                                        onNavigateToJournal = { showJournalScreen = true }
                                    )
                                    MainDestination.AI_HUB -> AiAndInsightsScreen(
                                        viewModel = viewModel,
                                        onNavigateToStudio = { currentDestination = MainDestination.STUDIO },
                                        onNavigateToNewNote = { currentDestination = MainDestination.NOTES },
                                        initialTab = aiHubInitialTab
                                    )
                                    MainDestination.LOVE_TOOLS -> LoveToolsScreen(
                                        viewModel = viewModel,
                                        onNavigateToStudio = { currentDestination = MainDestination.STUDIO },
                                        onNavigateToFavorites = { showFavoritesScreen = true },
                                        onOpenMusicSheet = { showMusicSheet = true }
                                    )
                                    MainDestination.STUDIO -> StudioScreen(
                                        viewModel = viewModel,
                                        onOpenStore = { showStoreScreen = true }
                                    )
                                    MainDestination.NOTES -> NotesScreen(
                                        viewModel = viewModel,
                                        onOpenInStudio = { note ->
                                            viewModel.loadNoteIntoStudio(note)
                                            currentDestination = MainDestination.STUDIO
                                        },
                                        onNavigateToJournal = { showJournalScreen = true }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showThemePaletteSheet) {
                    ThemePalettePickerSheet(
                        activeDesignTheme = designTheme,
                        themeMode = themeMode,
                        onSelectDesignTheme = { viewModel.setDesignTheme(it) },
                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                        onDismiss = { showThemePaletteSheet = false }
                    )
                }

                if (showMusicSheet) {
                    RomanticMusicPlayerSheet(
                        viewModel = viewModel,
                        onDismiss = { showMusicSheet = false }
                    )
                }

                if (isAdBlockerDetected && entitlementState.shouldShowAds && adminSettings.adBlockerGuardEnabled) {
                    AdBlockerNoticeDialog(
                        reason = adBlockerReason,
                        isChecking = isCheckingAdBlocker,
                        onRetryCheck = { viewModel.checkForAdBlocker(forceRefresh = true) },
                        onGoPremium = {
                            viewModel.dismissAdBlockerNotice()
                            closeAllSubScreens()
                            showStoreScreen = true
                        },
                        onDismiss = { viewModel.dismissAdBlockerNotice() }
                    )
                }
            }
        }

        accountStatusNotice?.let { notice ->
            AlertDialog(
                onDismissRequest = { viewModel.clearAccountStatusNotice() },
                title = { Text("Hesap Bildirimi") },
                text = { Text(notice) },
                confirmButton = {
                    Button(onClick = { viewModel.clearAccountStatusNotice() }) {
                        Text("Tamam")
                    }
                }
            )
        }
    }
}
