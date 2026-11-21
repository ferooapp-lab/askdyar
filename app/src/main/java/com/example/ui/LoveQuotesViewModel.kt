package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.FsPublicProfile
import com.example.data.firebase.FsUserDoc
import com.example.data.firebase.GoogleAuthManager
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.stableLongIdFromDocId
import com.example.data.local.AdminMessageEntity
import com.example.data.local.AdminQuoteEntity
import com.example.data.local.BroadcastNotificationEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CommunityCommentEntity
import com.example.data.local.CommunityQuoteEntity
import com.example.data.local.CustomMusicTrackEntity
import com.example.data.local.EngagementNotificationEntity
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.SavedFavoriteQuoteEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.AcrosticPoemBuilder
import com.example.data.model.AdminSettings
import com.example.data.model.AiQuoteResponse
import com.example.data.model.BackgroundImagePreset
import com.example.data.model.CardStyleOptions
import com.example.data.model.ChatHistoryMerger
import com.example.data.model.ChatModerationHelper
import com.example.data.model.ChatRoom
import com.example.data.model.CloudBackupStatus
import com.example.data.model.CompatibilityCalculator
import com.example.data.model.CompatibilityResult
import com.example.data.model.CoupleBucketItem
import com.example.data.model.CoupleQuestion
import com.example.data.model.CuratedQuotes
import com.example.data.model.DefaultCoupleBucketList
import com.example.data.model.DefaultCoupleQuestions
import com.example.data.model.DefaultLoveCoupons
import com.example.data.model.DefaultLoveJarReasons
import com.example.data.model.EmotionalJournalEntry
import com.example.data.model.EntitlementState
import com.example.data.model.FilterPreset
import com.example.data.model.JournalSecurityHelper
import com.example.data.model.LoveCounterInfo
import com.example.data.model.LoveCoupon
import com.example.data.model.LoveLanguageType
import com.example.data.model.LoveLetterTemplate
import com.example.data.model.LoveLetterTemplates
import com.example.data.model.LoveQuote
import com.example.data.model.LoveTimeCapsule
import com.example.data.model.MonetizationCatalog
import com.example.data.model.MoodAnalysisResult
import com.example.data.model.MoodInsights30DaySummary
import com.example.data.model.MoodInsightsCalculator
import com.example.data.model.PurchaseRecord
import com.example.data.model.QuoteSection
import com.example.data.model.RomanticMission
import com.example.data.model.RomanticMissions
import com.example.data.model.SpecialCountdownEvent
import com.example.data.model.StoreProduct
import com.example.data.model.StoryTemplateFrame
import com.example.data.model.StudioTypographyPreset
import com.example.data.model.ThemePreset
import com.example.data.model.ZodiacData
import com.example.data.model.ZodiacSign
import com.example.data.repository.AiRepository
import com.example.data.repository.AiRepositoryImpl
import com.example.ui.theme.AppDesignTheme
import com.example.util.AdBlockerDetector
import com.example.util.AdManager
import com.example.util.AmbientSoundHelper
import com.example.util.AudioRecordHelper
import com.example.util.BuiltInAmbientPresets
import com.example.util.NotificationHelper
import com.example.util.PlayBillingController
import com.example.util.QuoteTtsHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AiQuoteUiState {
    object Idle : AiQuoteUiState
    object Loading : AiQuoteUiState
    data class Success(val quote: AiQuoteResponse) : AiQuoteUiState
    data class Error(val message: String) : AiQuoteUiState
}

sealed interface MoodAnalysisUiState {
    object Loading : MoodAnalysisUiState
    data class Success(val result: MoodAnalysisResult) : MoodAnalysisUiState
    data class Error(val message: String) : MoodAnalysisUiState
}

data class CardStudioState(
    val quoteText: String = "Sen kocaman çöllerde bir kalabalık gibisin, kocaman denizlerde ender bir balık gibisin.",
    val author: String = "Özdemir Asaf",
    val themePreset: ThemePreset = CardStyleOptions.themePresets[0],
    val backgroundImagePreset: BackgroundImagePreset? = null,
    val typographyPreset: StudioTypographyPreset = StudioTypographyPreset.PLAYFAIR,
    val filterPreset: FilterPreset = FilterPreset.NORMAL,
    val fontSizeSp: Float = 22f,
    val isCentered: Boolean = true,
    val showQuotesDecoration: Boolean = true,
    val cardFormat: String = "card", // "card" (4:5), "story" (9:16), "square" (1:1)
    val sticker: String? = "💖",
    val dedicationName: String = "",
    val storyFrame: StoryTemplateFrame = StoryTemplateFrame.NONE
)

data class AuthSessionState(
    val role: String? = null, // "USER", "ADMIN", or null
    val userId: Long = 0L,
    val uid: String = "",
    val fullName: String = "",
    val email: String = ""
) {
    val isLoggedIn: Boolean get() = role != null
    val isAdmin: Boolean get() = role == "ADMIN"
    val isUser: Boolean get() = role == "USER"
    val displayName: String
        get() = when (role) {
            "ADMIN" -> "Yönetici"
            "USER" -> fullName.ifBlank { "Kullanıcı" }
            else -> "Giriş Yap"
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoveQuotesViewModel(
    private val application: Application,
    firestore: FirebaseFirestore
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestoreRepo = FirestoreRepository(application, firestore, auth)
    private val aiRepository: AiRepository = AiRepositoryImpl()
    private val audioHelper = AudioRecordHelper(application)
    private val ambientHelper = AmbientSoundHelper(application)
    private val ttsHelper = QuoteTtsHelper(application)
    private val adManager = AdManager(application)
    private var playBillingController: PlayBillingController? = null

    private val alarmPrefs = application.getSharedPreferences("romantic_prefs", Context.MODE_PRIVATE)

    // Reactive Firebase UID scoped to current authenticated account
    private val uidFlow: StateFlow<String> = auth.authStateFlow()
        .map { it?.uid.orEmpty() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, auth.currentUser?.uid.orEmpty())

    private val currentUid: String get() = auth.currentUser?.uid ?: uidFlow.value

    // Primary user document & admin status
    private val userDocFlow: StateFlow<FsUserDoc?> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(null) else firestoreRepo.observeUserDoc(uid)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isAdminAuthenticated: StateFlow<Boolean> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(false) else firestoreRepo.observeIsAdmin(uid)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val sessionState: StateFlow<AuthSessionState> = combine(
        uidFlow,
        userDocFlow,
        isAdminAuthenticated
    ) { uid, userDoc, isAdmin ->
        if (uid.isBlank()) {
            AuthSessionState(role = null)
        } else {
            val firebaseUser = auth.currentUser
            val resolvedName = userDoc?.fullName?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: "Aşk Yolcusu"
            val resolvedEmail = userDoc?.email?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.email.orEmpty()
            AuthSessionState(
                role = if (isAdmin) "ADMIN" else "USER",
                userId = stableLongIdFromDocId(uid),
                uid = uid,
                fullName = resolvedName,
                email = resolvedEmail
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AuthSessionState(role = null))

    val activeUserAccount: StateFlow<UserAccountEntity?> = combine(
        uidFlow,
        userDocFlow
    ) { uid, doc ->
        if (uid.isNotBlank() && doc != null) {
            doc.toUserAccountEntity(stableLongIdFromDocId(uid))
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Public profiles map (uid -> FsPublicProfile) for Chat badges & User Profile Cards
    val publicProfilesByUid: StateFlow<Map<String, FsPublicProfile>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyMap()) else firestoreRepo.observePublicProfiles()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val chatSenderUidByMessageId: StateFlow<Map<Long, String>> = firestoreRepo.chatSenderUidByMessageId
    val userUidByLegacyId: StateFlow<Map<Long, String>> = firestoreRepo.userUidByLegacyId

    private val _accountStatusNotice = MutableStateFlow<String?>(null)
    val accountStatusNotice: StateFlow<String?> = _accountStatusNotice.asStateFlow()

    // Admin Settings & Admin-managed collections
    val adminSettings: StateFlow<AdminSettings> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(AdminSettings()) else firestoreRepo.observeAdminSettings()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AdminSettings())

    val adminQuotes: StateFlow<List<AdminQuoteEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeAdminQuotes()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserAccounts: StateFlow<List<UserAccountEntity>> = combine(uidFlow, isAdminAuthenticated) { uid, isAdmin ->
        uid to isAdmin
    }.flatMapLatest { (uid, isAdmin) ->
        if (uid.isNotBlank() && isAdmin) firestoreRepo.observeAllUsersForAdmin() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User-scoped collections
    val allNotes: StateFlow<List<NoteEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeNotes(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<EmotionalJournalEntry>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeJournalEntries(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val journalVaultPrefs = application.getSharedPreferences("journal_vault_prefs", Context.MODE_PRIVATE)
    private val _isJournalPinEnabled = MutableStateFlow(false)
    val isJournalPinEnabled: StateFlow<Boolean> = _isJournalPinEnabled.asStateFlow()

    private val _isJournalUnlocked = MutableStateFlow(true)
    val isJournalUnlocked: StateFlow<Boolean> = _isJournalUnlocked.asStateFlow()

    private val _isJournalPrivacyShieldOn = MutableStateFlow(false)
    val isJournalPrivacyShieldOn: StateFlow<Boolean> = _isJournalPrivacyShieldOn.asStateFlow()

    val allReminders: StateFlow<List<ReminderEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeReminders(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val curatedFavoriteIdsFlow: StateFlow<List<String>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeCuratedFavoriteIds(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotesWithFavorites: StateFlow<List<LoveQuote>> = combine(
        curatedFavoriteIdsFlow,
        adminQuotes
    ) { favIds, addedAdminQuotes ->
        val mappedAdminQuotes = addedAdminQuotes.map { entity ->
            val section = QuoteSection.values().find { it.id == entity.sectionId } ?: QuoteSection.ASK_SOZLERI
            val quoteId = "admin_${entity.id}"
            LoveQuote(
                id = quoteId,
                text = entity.text,
                author = entity.author,
                section = section,
                subCategory = entity.subCategory.ifBlank { "Editör Seçimi" },
                tags = entity.tagsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                isFavorite = favIds.contains(quoteId)
            )
        }
        val mappedCurated = CuratedQuotes.quotes.map { quote ->
            quote.copy(isFavorite = favIds.contains(quote.id))
        }
        mappedAdminQuotes + mappedCurated
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CuratedQuotes.quotes)

    val savedFavoriteQuotes: StateFlow<List<SavedFavoriteQuoteEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeSavedFavoriteQuotes(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customMusicTracks: StateFlow<List<CustomMusicTrackEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeMusicTracks(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Broadcasts & Support messages
    val allBroadcastNotifications: StateFlow<List<BroadcastNotificationEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList())
            else firestoreRepo.observeBroadcasts(userDocFlow.map { it?.readBroadcastIds?.toSet() ?: emptySet() })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadBroadcastCount: StateFlow<Int> = allBroadcastNotifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allAdminMessages: StateFlow<List<AdminMessageEntity>> = combine(uidFlow, isAdminAuthenticated) { uid, isAdmin ->
        uid to isAdmin
    }.flatMapLatest { (uid, isAdmin) ->
        if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeSupportMessages(uid, isAdmin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingAdminMessagesCount: StateFlow<Int> = allAdminMessages
        .map { list -> list.count { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // In-App Engagement Notifications
    val engagementNotifications: StateFlow<List<EngagementNotificationEntity>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeEngagementNotifications(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadEngagementCount: StateFlow<Int> = engagementNotifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Community Quotes & Comments
    val allCommunityQuotes: StateFlow<List<CommunityQuoteEntity>> = combine(uidFlow, isAdminAuthenticated) { uid, isAdmin ->
        uid to isAdmin
    }.flatMapLatest { (uid, isAdmin) ->
        if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeCommunityQuotes(uid, isAdmin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedCommunityQuotes: StateFlow<List<CommunityQuoteEntity>> = allCommunityQuotes
        .map { list -> list.filter { it.status == "APPROVED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCommunityQuotes: StateFlow<List<CommunityQuoteEntity>> = allCommunityQuotes
        .map { list -> list.filter { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rejectedCommunityQuotes: StateFlow<List<CommunityQuoteEntity>> = allCommunityQuotes
        .map { list -> list.filter { it.status == "REJECTED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCommunityQuotesCount: StateFlow<Int> = pendingCommunityQuotes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val commentsByQuoteId: StateFlow<Map<Long, List<CommunityCommentEntity>>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyMap())
            else firestoreRepo.observeCommunityCommentsForQuotes(allCommunityQuotes)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val likedCommunityQuoteIds: StateFlow<Set<String>> = firestoreRepo.likedCommunityQuoteDocIds
        .map { docIds -> docIds.map { stableLongIdFromDocId(it).toString() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Community Chat: Live Snapshot + Cursor-Paginated Mesaj Geçmişi
    val allChatMessagesDesc: StateFlow<List<ChatMessageEntity>> = combine(uidFlow, isAdminAuthenticated) { uid, isAdmin ->
        uid to isAdmin
    }.flatMapLatest { (uid, isAdmin) ->
        if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeLiveChatMessages(uid, isAdmin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedChatMessages: StateFlow<List<ChatMessageEntity>> = allChatMessagesDesc
        .map { list ->
            list.filter { it.status == "PUBLISHED" }
                .sortedWith(compareBy<ChatMessageEntity> { it.createdAt }.thenBy { it.id })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingChatMessages: StateFlow<List<ChatMessageEntity>> = allChatMessagesDesc
        .map { list -> list.filter { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingChatCount: StateFlow<Int> = pendingChatMessages
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val publishedChatCount: StateFlow<Int> = publishedChatMessages
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val likedChatMessageIds: StateFlow<Set<String>> = firestoreRepo.likedChatMessageDocIds
        .map { docIds -> docIds.map { stableLongIdFromDocId(it).toString() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _chatHistoryMessagesByRoom = MutableStateFlow<Map<String, List<ChatMessageEntity>>>(emptyMap())
    val chatHistoryMessagesByRoom: StateFlow<Map<String, List<ChatMessageEntity>>> = _chatHistoryMessagesByRoom.asStateFlow()

    private val _hasMoreChatHistoryByRoom = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val hasMoreChatHistoryByRoom: StateFlow<Map<String, Boolean>> = _hasMoreChatHistoryByRoom.asStateFlow()

    private val _isLoadingChatHistory = MutableStateFlow(false)
    val isLoadingChatHistory: StateFlow<Boolean> = _isLoadingChatHistory.asStateFlow()

    private val chatHistoryCursorsByRoom = mutableMapOf<String, DocumentSnapshot?>()

    private val _chatSlowModeRemainingSeconds = MutableStateFlow(0)
    val chatSlowModeRemainingSeconds: StateFlow<Int> = _chatSlowModeRemainingSeconds.asStateFlow()
    private var chatSlowModeTimerJob: Job? = null

    // Purchases & Monetization Entitlements
    private val purchaseHistoryFlow: StateFlow<List<PurchaseRecord>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observePurchases(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entitlementState: StateFlow<EntitlementState> = combine(
        userDocFlow,
        adminSettings,
        purchaseHistoryFlow
    ) { doc, cfg, history ->
        doc?.toEntitlementState(cfg, history) ?: EntitlementState(
            adsEnabledByAdmin = cfg.adsEnabled,
            rewardCoinsPerAd = cfg.rewardCoinsPerAd
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EntitlementState())

    // Love Tools subcollections & fields synced in Firebase
    private val loveJarPairsFlow: StateFlow<List<Pair<String, String>>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeLoveJar(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loveJarReasons: StateFlow<List<String>> = loveJarPairsFlow
        .map { pairs ->
            if (pairs.isEmpty()) DefaultLoveJarReasons.initialReasons else pairs.map { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultLoveJarReasons.initialReasons)

    val timeCapsules: StateFlow<List<LoveTimeCapsule>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeTimeCapsules(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val specialCountdowns: StateFlow<List<SpecialCountdownEvent>> = uidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else firestoreRepo.observeCountdowns(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loveCounterInfo: StateFlow<LoveCounterInfo> = userDocFlow
        .map { doc -> doc?.toLoveCounterInfo() ?: LoveCounterInfo() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoveCounterInfo())

    val loveStreak: StateFlow<Int> = userDocFlow
        .map { it?.loveStreak ?: 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val todayMoodCheckIn: StateFlow<Triple<String, String, Int>?> = userDocFlow
        .map { doc ->
            if (doc == null) return@map null
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (doc.todayMoodDate != today || doc.todayMoodLabel.isBlank()) return@map null
            Triple(doc.todayMoodLabel, doc.todayMoodEmoji.ifBlank { "💖" }, doc.todayMoodScore)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val moodInsights30Day: StateFlow<MoodInsights30DaySummary> = combine(
        allNotes,
        userDocFlow
    ) { notes, doc ->
        val historyMap = parseMoodHistoryMap(doc)
        MoodInsightsCalculator.build30DayInsights(notes, historyMap)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MoodInsightsCalculator.build30DayInsights(emptyList(), emptyMap())
    )

    val savedLoveLanguage: StateFlow<LoveLanguageType?> = userDocFlow
        .map { doc ->
            doc?.loveLanguageName?.takeIf { it.isNotBlank() }?.let { name ->
                LoveLanguageType.values().find { it.name == name }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val missYouTotalCount: StateFlow<Int> = userDocFlow
        .map { it?.missYouTotalCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val missYouTodayCount: StateFlow<Int> = userDocFlow
        .map { doc ->
            if (doc == null) 0 else {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                if (doc.missYouDay == today) doc.missYouTodayCount else 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val truthDareCompletedCount: StateFlow<Int> = userDocFlow
        .map { it?.truthDareCompletedCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val loveCoupons: StateFlow<List<LoveCoupon>> = userDocFlow
        .map { doc ->
            val redeemed = doc?.redeemedCouponIds?.toSet() ?: emptySet()
            DefaultLoveCoupons.list.map { c -> c.copy(isRedeemed = redeemed.contains(c.id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultLoveCoupons.list)

    val bucketList: StateFlow<List<CoupleBucketItem>> = userDocFlow
        .map { doc ->
            val done = doc?.doneBucketIds?.toSet() ?: emptySet()
            DefaultCoupleBucketList.list.map { b -> b.copy(isDone = done.contains(b.id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultCoupleBucketList.list)

    val ourSong: StateFlow<Pair<String, String>> = userDocFlow
        .map { doc -> (doc?.ourSongTitle ?: "") to (doc?.ourSongArtist ?: "") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "" to "")

    // Preferences synced from /users/{uid}
    val themeMode: StateFlow<String> = userDocFlow
        .map { it?.themeMode ?: "system" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val designTheme: StateFlow<AppDesignTheme> = userDocFlow
        .map { AppDesignTheme.fromId(it?.designTheme ?: "ruby_velvet") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppDesignTheme.RUBY_VELVET)

    val isDailyNotificationEnabled: StateFlow<Boolean> = userDocFlow
        .map { it?.dailyNotifEnabled ?: alarmPrefs.getBoolean("daily_notif_enabled", true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyNotificationTime: StateFlow<Pair<Int, Int>> = userDocFlow
        .map { doc ->
            if (doc != null) doc.dailyNotifHour to doc.dailyNotifMinute
            else alarmPrefs.getInt("daily_notif_hour", 8) to alarmPrefs.getInt("daily_notif_minute", 30)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8 to 30)

    val dailyNotificationTone: StateFlow<String> = userDocFlow
        .map { it?.dailyNotifTone ?: alarmPrefs.getString("daily_notif_tone", "MIXED") ?: "MIXED" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MIXED")

    // UI transient states
    private val _activeSection = MutableStateFlow(QuoteSection.ASK_SOZLERI)
    val activeSection: StateFlow<QuoteSection> = _activeSection.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _dailyQuote = MutableStateFlow(CuratedQuotes.askSozleri[0])
    val dailyQuote: StateFlow<LoveQuote> = _dailyQuote.asStateFlow()

    private val _spotlightQuote = MutableStateFlow<LoveQuote?>(null)
    val spotlightQuote: StateFlow<LoveQuote?> = _spotlightQuote.asStateFlow()

    private val _studioState = MutableStateFlow(CardStudioState())
    val studioState: StateFlow<CardStudioState> = _studioState.asStateFlow()

    private val _currentMission = MutableStateFlow(RomanticMissions.missions[0])
    val currentMission: StateFlow<RomanticMission> = _currentMission.asStateFlow()

    private val _compatibilityResult = MutableStateFlow<CompatibilityResult?>(null)
    val compatibilityResult: StateFlow<CompatibilityResult?> = _compatibilityResult.asStateFlow()

    // Ambient & Custom Music Player
    private val _isAmbientPlaying = MutableStateFlow(false)
    val isAmbientPlaying: StateFlow<Boolean> = _isAmbientPlaying.asStateFlow()

    private val _currentMusicTrackTitle = MutableStateFlow("Romantik Müzik Kutusu")
    val currentMusicTrackTitle: StateFlow<String> = _currentMusicTrackTitle.asStateFlow()

    private val _currentMusicTrackKey = MutableStateFlow("preset_music_box")
    val currentMusicTrackKey: StateFlow<String> = _currentMusicTrackKey.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.7f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    // Text-to-Speech
    private val _speakingQuoteId = MutableStateFlow<String?>(null)
    val speakingQuoteId: StateFlow<String?> = _speakingQuoteId.asStateFlow()

    // AI states
    private val _aiQuoteState = MutableStateFlow<AiQuoteUiState>(AiQuoteUiState.Idle)
    val aiQuoteState: StateFlow<AiQuoteUiState> = _aiQuoteState.asStateFlow()

    private val _moodAnalysisState = MutableStateFlow<MoodAnalysisUiState>(
        MoodAnalysisUiState.Success(aiRepository.getBaselineMoodAnalysis(emptyList()))
    )
    val moodAnalysisState: StateFlow<MoodAnalysisUiState> = _moodAnalysisState.asStateFlow()

    private val _acrosticPoemResult = MutableStateFlow<String?>(null)
    val acrosticPoemResult: StateFlow<String?> = _acrosticPoemResult.asStateFlow()

    private val _isAcrosticLoading = MutableStateFlow(false)
    val isAcrosticLoading: StateFlow<Boolean> = _isAcrosticLoading.asStateFlow()

    private val _situationMessageState = MutableStateFlow<AiQuoteUiState>(AiQuoteUiState.Idle)
    val situationMessageState: StateFlow<AiQuoteUiState> = _situationMessageState.asStateFlow()

    // Interactive couple helpers
    private val _currentQuestion = MutableStateFlow(DefaultCoupleQuestions.questions[0])
    val currentQuestion: StateFlow<CoupleQuestion> = _currentQuestion.asStateFlow()

    private val _selectedZodiac1 = MutableStateFlow(ZodiacData.signs[0])
    val selectedZodiac1: StateFlow<ZodiacSign> = _selectedZodiac1.asStateFlow()

    private val _selectedZodiac2 = MutableStateFlow(ZodiacData.signs[1])
    val selectedZodiac2: StateFlow<ZodiacSign> = _selectedZodiac2.asStateFlow()

    private val _zodiacResult = MutableStateFlow(
        ZodiacData.calculateZodiacMatch(ZodiacData.signs[0], ZodiacData.signs[1])
    )
    val zodiacResult: StateFlow<CompatibilityResult> = _zodiacResult.asStateFlow()

    private val _selectedLetterTemplate = MutableStateFlow(LoveLetterTemplates.templates[0])
    val selectedLetterTemplate: StateFlow<LoveLetterTemplate> = _selectedLetterTemplate.asStateFlow()

    private val _letterPartnerName = MutableStateFlow("Sevgilim")
    val letterPartnerName: StateFlow<String> = _letterPartnerName.asStateFlow()

    private val _letterMyName = MutableStateFlow("Ben")
    val letterMyName: StateFlow<String> = _letterMyName.asStateFlow()

    private val _letterTrait = MutableStateFlow("masum gülüşüne")
    val letterTrait: StateFlow<String> = _letterTrait.asStateFlow()

    private val _generatedLetter = MutableStateFlow(
        LoveLetterTemplates.buildLetter(
            LoveLetterTemplates.templates[0],
            "Ben",
            "Sevgilim",
            "masum gülüşüne"
        )
    )
    val generatedLetter: StateFlow<String> = _generatedLetter.asStateFlow()

    // Audio voice note recording
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingTimerSeconds = MutableStateFlow(0)
    val recordingTimerSeconds: StateFlow<Int> = _recordingTimerSeconds.asStateFlow()

    private val _currentlyPlayingAudio = MutableStateFlow<String?>(null)
    val currentlyPlayingAudio: StateFlow<String?> = _currentlyPlayingAudio.asStateFlow()

    private var recordingTimerJob: Job? = null

    // Cloud Backup status
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    val lastSyncTime: StateFlow<Long> = userDocFlow
        .map { it?.lastSyncAt ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _exportedBackupJson = MutableStateFlow<String?>(null)
    val exportedBackupJson: StateFlow<String?> = _exportedBackupJson.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    private val localBackupCountsFlow = combine(
        allNotes,
        allReminders,
        quotesWithFavorites,
        savedFavoriteQuotes
    ) { notes, reminders, curated, savedFavs ->
        val unsynced = notes.count { !it.isSyncedWithCloud }
        val totalFavs = curated.count { it.isFavorite } + savedFavs.size
        Triple(unsynced, notes.size + totalFavs + reminders.size, reminders.size)
    }

    val cloudBackupStatus: StateFlow<CloudBackupStatus> = combine(
        localBackupCountsFlow,
        userDocFlow,
        entitlementState
    ) { (unsyncedNotes, totalItems, _), doc, entitlement ->
        CloudBackupStatus(
            lastBackupTimestamp = doc?.lastSyncAt ?: 0L,
            syncedNotesCount = doc?.syncedNotesCount ?: 0,
            syncedFavoritesCount = doc?.syncedFavsCount ?: 0,
            syncedRemindersCount = doc?.syncedRemindersCount ?: 0,
            unsyncedNotesCount = unsyncedNotes,
            hasSnapshot = !doc?.lastBackupJson.isNullOrBlank(),
            totalLocalItemsCount = totalItems,
            backupLimit = MonetizationCatalog.FREE_CLOUD_BACKUP_LIMIT,
            isUnlimited = entitlement.hasUnlimitedCloudBackup
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CloudBackupStatus(backupLimit = MonetizationCatalog.FREE_CLOUD_BACKUP_LIMIT)
    )

    // Billing & AdBlocker
    private val _billingReady = MutableStateFlow(false)
    val billingReady: StateFlow<Boolean> = _billingReady.asStateFlow()

    private val _liveFormattedPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val liveFormattedPrices: StateFlow<Map<String, String>> = _liveFormattedPrices.asStateFlow()

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    private val _isAdBlockerDetected = MutableStateFlow(false)
    val isAdBlockerDetected: StateFlow<Boolean> = _isAdBlockerDetected.asStateFlow()

    private val _adBlockerReason = MutableStateFlow("")
    val adBlockerReason: StateFlow<String> = _adBlockerReason.asStateFlow()

    private val _isCheckingAdBlocker = MutableStateFlow(false)
    val isCheckingAdBlocker: StateFlow<Boolean> = _isCheckingAdBlocker.asStateFlow()

    private var lastAdBlockerCheckAt: Long = 0L

    // Engagement tracking baseline per session so author receives notifications cleanly under owner-only rules
    private var baselineQuotesInitialized = false
    private val lastKnownOwnQuoteState = mutableMapOf<Long, Pair<String, Int>>() // quoteId -> (status, likesCount)
    private var baselineCommentsInitialized = false
    private val lastKnownCommentCounts = mutableMapOf<Long, Int>()
    private var lastSyncedProfileKey: String? = null

    init {
        NotificationHelper.createNotificationChannels(application)
        pickRandomDailyQuote()
        adManager.preloadAds()

        // Initialize Firebase user doc, public profile & defaults whenever authenticated user attaches
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                firestoreRepo.ensureUserInitialized(user)
            }
        }

        // Sync daily alarm preferences cache & enforce instant ban
        viewModelScope.launch {
            userDocFlow.collect { doc ->
                if (doc == null) return@collect
                alarmPrefs.edit()
                    .putBoolean("daily_notif_enabled", doc.dailyNotifEnabled)
                    .putInt("daily_notif_hour", doc.dailyNotifHour)
                    .putInt("daily_notif_minute", doc.dailyNotifMinute)
                    .putString("daily_notif_tone", doc.dailyNotifTone)
                    .apply()

                if (doc.dailyNotifEnabled) {
                    NotificationHelper.scheduleDailyQuoteAlarm(application, doc.dailyNotifHour, doc.dailyNotifMinute)
                } else {
                    NotificationHelper.cancelDailyQuoteAlarm(application)
                }

                if (doc.isBlocked && !isAdminAuthenticated.value) {
                    val reasonMsg = if (doc.restrictionReason.isNotBlank()) " Sebep: ${doc.restrictionReason}" else ""
                    _accountStatusNotice.value = "Hesabınız yönetici tarafından anında banlandı.$reasonMsg"
                    logoutSession()
                }
            }
        }

        // Keep /public_profiles/{uid} synced when shared quotes, received hearts, or VIP status change
        viewModelScope.launch {
            combine(
                userDocFlow,
                isAdminAuthenticated,
                allCommunityQuotes,
                sessionState
            ) { doc, isAdmin, quotes, session ->
                if (doc == null || session.uid.isBlank()) return@combine null
                val ownQuotes = quotes.filter { q ->
                    q.submitterName.equals(session.displayName, ignoreCase = true) ||
                        q.submitterName.equals(doc.fullName, ignoreCase = true)
                }
                val sharedCount = ownQuotes.size
                val heartsCount = ownQuotes.sumOf { it.likesCount }
                val isPremium = doc.isVip || doc.hasVipBadgeAndAi
                val key = "${session.uid}|${session.displayName}|${doc.partnerName}|$isAdmin|$isPremium|${doc.vipPlanName}|$sharedCount|$heartsCount"
                key to listOf(
                    session.uid,
                    session.displayName,
                    doc.partnerName,
                    isAdmin,
                    isPremium,
                    doc.vipPlanName,
                    sharedCount,
                    heartsCount
                )
            }.collect { pair ->
                if (pair != null && pair.first != lastSyncedProfileKey) {
                    lastSyncedProfileKey = pair.first
                    val args = pair.second
                    firestoreRepo.updatePublicProfileStats(
                        uid = args[0] as String,
                        displayName = args[1] as String,
                        partnerName = args[2] as String,
                        isAdmin = args[3] as Boolean,
                        isPremium = args[4] as Boolean,
                        vipPlanName = args[5] as String,
                        sharedQuotesCount = args[6] as Int,
                        receivedHeartsCount = args[7] as Int
                    )
                }
            }
        }

        // Hydrate liked community quote IDs & owner notifications on live engagement deltas
        viewModelScope.launch {
            allCommunityQuotes.collect { quotes ->
                val uid = currentUid
                if (uid.isNotBlank() && quotes.isNotEmpty()) {
                    firestoreRepo.refreshLikedCommunityIds(uid, quotes)
                    val myName = sessionState.value.displayName
                    val ownQuotes = quotes.filter {
                        it.submitterName.equals(myName, ignoreCase = true) ||
                            it.submitterName.equals(userDocFlow.value?.fullName ?: "", ignoreCase = true)
                    }
                    if (!baselineQuotesInitialized) {
                        ownQuotes.forEach { q -> lastKnownOwnQuoteState[q.id] = q.status to q.likesCount }
                        baselineQuotesInitialized = true
                    } else {
                        ownQuotes.forEach { q ->
                            val prev = lastKnownOwnQuoteState[q.id]
                            if (prev != null) {
                                val docId = firestoreRepo.getQuoteDocId(q.id) ?: q.id.toString()
                                if (prev.first == "PENDING" && q.status == "APPROVED") {
                                    firestoreRepo.insertOwnNotification(
                                        uid = uid,
                                        type = "APPROVED",
                                        quoteDocId = docId,
                                        quotePreview = q.text.take(68),
                                        actorName = "Yönetici",
                                        message = "Paylaştığın söz onaylandı ve topluluk akışında yayınlandı! ✅"
                                    )
                                }
                                if (q.likesCount > prev.second) {
                                    firestoreRepo.insertOwnNotification(
                                        uid = uid,
                                        type = "LIKE",
                                        quoteDocId = docId,
                                        quotePreview = q.text.take(68),
                                        actorName = "Topluluk Okuru",
                                        message = "paylaştığın söze kalp bıraktı ❤️ (Toplam ${q.likesCount})"
                                    )
                                }
                            }
                            lastKnownOwnQuoteState[q.id] = q.status to q.likesCount
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            commentsByQuoteId.collect { commentsMap ->
                val uid = currentUid
                if (uid.isBlank() || commentsMap.isEmpty()) return@collect
                val myName = sessionState.value.displayName
                val ownQuoteIds = allCommunityQuotes.value.filter {
                    it.submitterName.equals(myName, ignoreCase = true) ||
                        it.submitterName.equals(userDocFlow.value?.fullName ?: "", ignoreCase = true)
                }.associateBy { it.id }

                if (!baselineCommentsInitialized) {
                    ownQuoteIds.keys.forEach { qId ->
                        lastKnownCommentCounts[qId] = commentsMap[qId]?.size ?: 0
                    }
                    baselineCommentsInitialized = true
                } else {
                    ownQuoteIds.forEach { (qId, qEntity) ->
                        val currentList = commentsMap[qId].orEmpty()
                        val prevCount = lastKnownCommentCounts[qId] ?: currentList.size
                        if (currentList.size > prevCount) {
                            val newest = currentList.firstOrNull()
                            val docId = firestoreRepo.getQuoteDocId(qId) ?: qId.toString()
                            if (newest != null && !newest.authorName.equals(myName, ignoreCase = true)) {
                                firestoreRepo.insertOwnNotification(
                                    uid = uid,
                                    type = "COMMENT",
                                    quoteDocId = docId,
                                    quotePreview = qEntity.text.take(68),
                                    actorName = newest.authorName,
                                    message = "\"${newest.commentText.take(64)}\" yorumunu bıraktı 💬"
                                )
                            }
                        }
                        lastKnownCommentCounts[qId] = currentList.size
                    }
                }
            }
        }

        // Hydrate liked chat message IDs on live chat emission
        viewModelScope.launch {
            publishedChatMessages.collect { msgs ->
                val uid = currentUid
                if (uid.isNotBlank() && msgs.isNotEmpty()) {
                    firestoreRepo.refreshLikedChatIds(uid, msgs)
                }
            }
        }

        // Billing setup
        val billingController = PlayBillingController(
            context = application,
            onProductPurchased = { product, source ->
                val uid = currentUid
                val doc = userDocFlow.value ?: FsUserDoc(uid = uid)
                if (uid.isNotBlank()) {
                    viewModelScope.launch {
                        firestoreRepo.applyStoreProductPurchase(uid, doc, product, source)
                    }
                }
                if (product.grantsVip || product.grantsAdFree) {
                    _isAdBlockerDetected.value = false
                }
            },
            onConnectionChanged = { ready ->
                _billingReady.value = ready
            },
            onSyncExistingEntitlements = { activeProducts ->
                val uid = currentUid
                val doc = userDocFlow.value ?: FsUserDoc(uid = uid)
                if (uid.isNotBlank() && activeProducts.isNotEmpty()) {
                    viewModelScope.launch {
                        firestoreRepo.restoreActiveEntitlementsFromPlay(uid, doc, activeProducts)
                    }
                }
            },
            onBillingStatusMessage = { msg ->
                _billingStatusMessage.value = msg
            }
        ).also { it.startConnection() }
        playBillingController = billingController

        viewModelScope.launch {
            billingController.liveFormattedPrices.collect { prices ->
                _liveFormattedPrices.value = prices
            }
        }

        viewModelScope.launch {
            allNotes.collect { notes ->
                refreshMoodAnalysisWithNotes(notes)
            }
        }

        startChatSlowModeCountdownIfNeeded()
        checkForAdBlocker()

        viewModelScope.launch {
            uidFlow.collect { uid ->
                syncJournalVaultStateForUid(uid)
            }
        }
    }

    private fun syncJournalVaultStateForUid(uid: String) {
        if (uid.isBlank()) {
            _isJournalPinEnabled.value = false
            _isJournalUnlocked.value = false
            return
        }
        val hash = journalVaultPrefs.getString("pin_hash_$uid", null)
        val hasPin = !hash.isNullOrBlank()
        _isJournalPinEnabled.value = hasPin
        _isJournalUnlocked.value = !hasPin
        _isJournalPrivacyShieldOn.value = journalVaultPrefs.getBoolean("shield_$uid", false)
    }

    private fun parseMoodHistoryMap(doc: FsUserDoc?): Map<String, Triple<String, String, Int>> {
        val result = mutableMapOf<String, Triple<String, String, Int>>()
        val raw = doc?.moodCheckInHistoryJson
        if (!raw.isNullOrBlank()) {
            try {
                val obj = JSONObject(raw)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = obj.optJSONObject(key) ?: continue
                    result[key] = Triple(
                        item.optString("label", "Huzurlu"),
                        item.optString("emoji", "💖"),
                        item.optInt("score", 90)
                    )
                }
            } catch (_: Exception) {
            }
        }
        if (doc != null && doc.todayMoodDate.isNotBlank() && doc.todayMoodLabel.isNotBlank()) {
            result[doc.todayMoodDate] = Triple(
                doc.todayMoodLabel,
                doc.todayMoodEmoji.ifBlank { "💖" },
                doc.todayMoodScore
            )
        }
        return result
    }

    // ===============================================================
    // Quotes Navigation & Favorites
    // ===============================================================
    fun setActiveSection(section: QuoteSection) {
        _activeSection.value = section
        _searchQuery.value = ""
        val currentQuotes = when (section) {
            QuoteSection.ASK_SOZLERI -> CuratedQuotes.askSozleri
            QuoteSection.ROMANTIK_SOZLER -> CuratedQuotes.romantikSozler
            QuoteSection.GUNAYDIN_MESAJLARI -> CuratedQuotes.gunaydinMesajlari
            QuoteSection.GECE_MESAJLARI -> CuratedQuotes.geceMesajlari
        }
        _dailyQuote.value = currentQuotes.random()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTagFilter(tag: String?) {
        _selectedTag.value = tag
    }

    fun pickRandomDailyQuote() {
        val currentQuotes = when (_activeSection.value) {
            QuoteSection.ASK_SOZLERI -> CuratedQuotes.askSozleri
            QuoteSection.ROMANTIK_SOZLER -> CuratedQuotes.romantikSozler
            QuoteSection.GUNAYDIN_MESAJLARI -> CuratedQuotes.gunaydinMesajlari
            QuoteSection.GECE_MESAJLARI -> CuratedQuotes.geceMesajlari
        }
        _dailyQuote.value = currentQuotes.random()
    }

    fun toggleQuoteFavorite(quoteId: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.toggleQuoteFavorite(uid, quoteId)
        }
    }

    fun toggleNoteFavorite(note: NoteEntity) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.saveNote(uid, note.copy(isFavorite = !note.isFavorite))
        }
    }

    // ===============================================================
    // Love Tools (Synced to Firebase /users/{uid})
    // ===============================================================
    fun updateLoveCounter(name1: String, name2: String, startTime: Long, title: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.updateUserFields(
                uid,
                mapOf(
                    "loveCounterMyName" to name1.trim().ifBlank { "Ben" }.take(80),
                    "loveCounterPartnerName" to name2.trim().ifBlank { "Sevgilim" }.take(80),
                    "partnerName" to name2.trim().ifBlank { "Sevgilim" }.take(100),
                    "loveCounterStartDate" to startTime,
                    "loveCounterTitle" to title.trim().ifBlank { "İlk Günümüzden Beri" }.take(120)
                )
            )
        }
    }

    fun pickRandomMission() {
        _currentMission.value = RomanticMissions.missions.random()
    }

    fun calculateLoveCompatibility(name1: String, name2: String) {
        _compatibilityResult.value = CompatibilityCalculator.calculate(name1, name2)
    }

    fun toggleCouponRedeemed(couponId: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        val current = userDocFlow.value?.redeemedCouponIds?.toMutableSet() ?: mutableSetOf()
        if (current.contains(couponId)) current.remove(couponId) else current.add(couponId)
        viewModelScope.launch {
            firestoreRepo.updateUserFields(uid, mapOf("redeemedCouponIds" to current.toList()))
        }
    }

    fun toggleBucketItem(itemId: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        val current = userDocFlow.value?.doneBucketIds?.toMutableSet() ?: mutableSetOf()
        if (current.contains(itemId)) current.remove(itemId) else current.add(itemId)
        viewModelScope.launch {
            firestoreRepo.updateUserFields(uid, mapOf("doneBucketIds" to current.toList()))
        }
    }

    fun pickNextQuestion() {
        val next = DefaultCoupleQuestions.questions
            .filter { it.id != _currentQuestion.value.id }
            .random()
        _currentQuestion.value = next
    }

    fun updateZodiacSigns(sign1: ZodiacSign, sign2: ZodiacSign) {
        _selectedZodiac1.value = sign1
        _selectedZodiac2.value = sign2
        _zodiacResult.value = ZodiacData.calculateZodiacMatch(sign1, sign2)
    }

    fun updateLoveLetter(
        template: LoveLetterTemplate,
        myName: String,
        partner: String,
        trait: String
    ) {
        _selectedLetterTemplate.value = template
        _letterMyName.value = myName
        _letterPartnerName.value = partner
        _letterTrait.value = trait
        _generatedLetter.value = LoveLetterTemplates.buildLetter(template, myName, partner, trait)
    }

    fun updateOurSong(title: String, artist: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.updateUserFields(
                uid,
                mapOf(
                    "ourSongTitle" to title.trim().take(150),
                    "ourSongArtist" to artist.trim().take(150)
                )
            )
        }
    }

    fun recordMoodCheckIn(label: String, emoji: String, score: Int) {
        val uid = currentUid
        if (uid.isBlank()) return
        val currentJson = userDocFlow.value?.moodCheckInHistoryJson ?: "{}"
        viewModelScope.launch {
            firestoreRepo.updateMoodCheckIn(uid, currentJson, label, emoji, score)
        }
        val current = _moodAnalysisState.value
        if (current is MoodAnalysisUiState.Success) {
            val updatedDays = current.result.days.mapIndexed { idx, day ->
                if (idx == current.result.days.lastIndex) day.copy(moodScore = score, emoji = emoji) else day
            }
            val newOverall = (updatedDays.map { it.moodScore }.average()).toInt().coerceIn(50, 99)
            _moodAnalysisState.value = MoodAnalysisUiState.Success(
                current.result.copy(days = updatedDays, overallScore = newOverall)
            )
        }
    }

    fun saveLoveLanguageResult(type: LoveLanguageType) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.updateUserFields(uid, mapOf("loveLanguageName" to type.name))
        }
    }

    fun addSpecialCountdown(title: String, emoji: String, daysFromNow: Int) {
        val uid = currentUid
        if (uid.isBlank() || title.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.addCountdown(uid, title, emoji, daysFromNow)
        }
    }

    fun deleteSpecialCountdown(id: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.deleteCountdown(uid, id)
        }
    }

    fun sendMissYouPing() {
        val uid = currentUid
        if (uid.isBlank()) return
        val doc = userDocFlow.value
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val nextToday = if (doc?.missYouDay == today) doc.missYouTodayCount + 1 else 1
        val nextTotal = (doc?.missYouTotalCount ?: 0) + 1
        viewModelScope.launch {
            firestoreRepo.updateUserFields(
                uid,
                mapOf(
                    "missYouTotalCount" to nextTotal,
                    "missYouTodayCount" to nextToday,
                    "missYouDay" to today
                )
            )
        }
    }

    fun addLoveJarReason(reason: String) {
        val uid = currentUid
        if (uid.isBlank() || reason.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.addLoveJarReason(uid, reason)
        }
    }

    fun deleteLoveJarReason(reason: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        val pair = loveJarPairsFlow.value.firstOrNull { it.second == reason } ?: return
        viewModelScope.launch {
            firestoreRepo.deleteLoveJarReason(uid, pair.first)
        }
    }

    fun createTimeCapsule(
        title: String,
        message: String,
        author: String,
        unlockDelayMillis: Long
    ) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.createTimeCapsule(uid, title, message, author, unlockDelayMillis)
        }
    }

    fun deleteTimeCapsule(id: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.deleteTimeCapsule(uid, id)
        }
    }

    fun completeTruthOrDare() {
        val uid = currentUid
        if (uid.isBlank()) return
        val nextCount = (userDocFlow.value?.truthDareCompletedCount ?: 0) + 1
        viewModelScope.launch {
            firestoreRepo.updateUserFields(uid, mapOf("truthDareCompletedCount" to nextCount))
        }
    }

    // ===============================================================
    // Ambient & Custom Romantic Music Player
    // ===============================================================
    fun toggleAmbientMusic() {
        val currentKey = _currentMusicTrackKey.value
        if (_isAmbientPlaying.value) {
            ambientHelper.stopAmbient()
            _isAmbientPlaying.value = false
        } else if (currentKey.startsWith("custom_")) {
            val trackId = currentKey.removePrefix("custom_").toLongOrNull()
            val track = customMusicTracks.value.find { it.id == trackId }
            if (track != null) {
                val started = ambientHelper.startAudioFile(track.filePath)
                _isAmbientPlaying.value = started
                if (started) _currentMusicTrackTitle.value = track.title
            } else {
                playAmbientPreset("preset_music_box")
            }
        } else {
            playAmbientPreset(currentKey)
        }
    }

    fun playAmbientPreset(presetKey: String) {
        val preset = BuiltInAmbientPresets.presets.find { it.key == presetKey } ?: BuiltInAmbientPresets.presets[0]
        val started = ambientHelper.startPreset(preset.key)
        _isAmbientPlaying.value = started
        if (started) {
            _currentMusicTrackKey.value = preset.key
            _currentMusicTrackTitle.value = preset.title
        }
    }

    fun playCustomMusicTrack(track: CustomMusicTrackEntity) {
        val started = ambientHelper.startAudioFile(track.filePath)
        _isAmbientPlaying.value = started
        if (started) {
            _currentMusicTrackKey.value = "custom_${track.id}"
            _currentMusicTrackTitle.value = track.title
        }
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _musicVolume.value = clamped
        ambientHelper.setVolume(clamped)
    }

    fun importCustomMusicFromUri(
        uri: Uri,
        customTitle: String? = null,
        artistName: String = "Kişisel Fon Müziği",
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Müzik eklemek için Google hesabınızla giriş yapın.")
            return
        }
        if (!adminSettings.value.allowCustomMusic) {
            onResult(false, "Özel fon müziği ekleme şu anda yönetici tarafından kapatılmıştır.")
            return
        }
        val activeUser = activeUserAccount.value
        if (activeUser != null) {
            if (activeUser.isBlocked) {
                onResult(false, "Hesabınız banlandığı için müzik ekleyemezsiniz.")
                return
            }
            val now = System.currentTimeMillis()
            val restricted = activeUser.isRestricted && (activeUser.restrictedUntil == null || activeUser.restrictedUntil > now)
            if (restricted && !activeUser.canUploadMusic) {
                onResult(false, "Özel müzik ekleme yetkiniz yönetici tarafından kısıtlanmıştır.")
                return
            }
        }
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                ambientHelper.importAudioUri(uri)
            }
            imported.onSuccess { (filePath, suggestedName) ->
                val finalTitle = customTitle?.trim()?.ifBlank { suggestedName } ?: suggestedName
                val adder = sessionState.value.displayName
                val res = firestoreRepo.addCustomMusicTrack(uid, finalTitle, artistName, filePath, adder)
                res.onSuccess { newId ->
                    val started = ambientHelper.startAudioFile(filePath)
                    _isAmbientPlaying.value = started
                    if (started) {
                        _currentMusicTrackKey.value = "custom_$newId"
                        _currentMusicTrackTitle.value = finalTitle
                    }
                    onResult(true, "\"$finalTitle\" eklendi ve çalmaya başladı! 🎶")
                }.onFailure { err ->
                    onResult(false, err.localizedMessage ?: "Müzik buluta kaydedilemedi.")
                }
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Ses dosyası eklenemedi.")
            }
        }
    }

    fun deleteCustomMusicTrack(track: CustomMusicTrackEntity) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            if (_currentMusicTrackKey.value == "custom_${track.id}") {
                ambientHelper.stopAmbient()
                _isAmbientPlaying.value = false
                _currentMusicTrackKey.value = "preset_music_box"
                _currentMusicTrackTitle.value = "Romantik Müzik Kutusu"
            }
            firestoreRepo.deleteCustomMusicTrack(uid, track.id, track.filePath)
        }
    }

    // Text-to-Speech
    fun toggleSpeakQuote(quoteId: String, text: String, author: String) {
        if (_speakingQuoteId.value == quoteId) {
            ttsHelper.stop()
            _speakingQuoteId.value = null
        } else {
            _speakingQuoteId.value = quoteId
            ttsHelper.speakQuote(text, author) { isSpeaking ->
                if (!isSpeaking && _speakingQuoteId.value == quoteId) {
                    _speakingQuoteId.value = null
                }
            }
        }
    }

    // ===============================================================
    // Notes & Reminders
    // ===============================================================
    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        author: String = "Ben",
        tags: String = "",
        voiceAudioPath: String? = null,
        voiceDurationSeconds: Int = 0,
        backgroundTheme: String = "ruby_velvet",
        filterStyle: String = "normal",
        isFavorite: Boolean = false
    ) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.saveNote(
                uid,
                NoteEntity(
                    id = id,
                    title = title.ifBlank { "Romantik Not" },
                    content = content,
                    author = author.ifBlank { "Ben" },
                    tags = tags,
                    voiceAudioPath = voiceAudioPath,
                    voiceDurationSeconds = voiceDurationSeconds,
                    backgroundTheme = backgroundTheme,
                    filterStyle = filterStyle,
                    isFavorite = isFavorite,
                    isSyncedWithCloud = true
                )
            )
        }
    }

    fun deleteNote(noteId: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.deleteNoteById(uid, noteId)
        }
    }

    fun saveReminder(
        id: Long = 0,
        title: String,
        note: String,
        targetTimestamp: Long,
        reminderType: String
    ) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            val res = firestoreRepo.saveReminder(
                uid,
                ReminderEntity(
                    id = id,
                    title = title,
                    note = note,
                    targetTimestamp = targetTimestamp,
                    reminderType = reminderType
                )
            )
            res.onSuccess { finalId ->
                NotificationHelper.scheduleReminder(
                    application,
                    finalId,
                    title,
                    note,
                    targetTimestamp
                )
            }
        }
    }

    fun toggleReminderCompleted(reminder: ReminderEntity) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            val newStatus = !reminder.isCompleted
            firestoreRepo.setReminderCompleted(uid, reminder.id, newStatus)
            if (newStatus) {
                NotificationHelper.cancelReminder(application, reminder.id)
            } else {
                NotificationHelper.scheduleReminder(
                    application,
                    reminder.id,
                    reminder.title,
                    reminder.note,
                    reminder.targetTimestamp
                )
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            NotificationHelper.cancelReminder(application, reminderId)
            firestoreRepo.deleteReminderById(uid, reminderId)
        }
    }

    // ===============================================================
    // Visual Card Studio Actions
    // ===============================================================
    fun updateStudioQuote(text: String, author: String) {
        _studioState.value = _studioState.value.copy(quoteText = text, author = author)
    }

    fun updateStudioTheme(preset: ThemePreset) {
        _studioState.value = _studioState.value.copy(
            themePreset = preset,
            backgroundImagePreset = null
        )
    }

    fun updateStudioBackgroundImage(imagePreset: BackgroundImagePreset?) {
        _studioState.value = _studioState.value.copy(backgroundImagePreset = imagePreset)
    }

    fun updateStudioTypography(typography: StudioTypographyPreset) {
        _studioState.value = _studioState.value.copy(typographyPreset = typography)
    }

    fun updateStudioFilter(preset: FilterPreset) {
        _studioState.value = _studioState.value.copy(filterPreset = preset)
    }

    fun updateStudioFontSize(sizeSp: Float) {
        _studioState.value = _studioState.value.copy(fontSizeSp = sizeSp)
    }

    fun updateStudioFormat(format: String) {
        _studioState.value = _studioState.value.copy(cardFormat = format)
    }

    fun updateStudioSticker(sticker: String?) {
        _studioState.value = _studioState.value.copy(sticker = sticker)
    }

    fun updateStudioDedication(name: String) {
        _studioState.value = _studioState.value.copy(dedicationName = name)
    }

    fun toggleStudioCentered() {
        _studioState.value = _studioState.value.copy(isCentered = !_studioState.value.isCentered)
    }

    fun toggleStudioQuotesDecoration() {
        _studioState.value = _studioState.value.copy(showQuotesDecoration = !_studioState.value.showQuotesDecoration)
    }

    fun updateStudioStoryFrame(frame: StoryTemplateFrame) {
        _studioState.value = _studioState.value.copy(storyFrame = frame)
    }

    fun applyStoryTemplatePreset(frame: StoryTemplateFrame) {
        _studioState.value = _studioState.value.copy(
            cardFormat = "story",
            storyFrame = frame
        )
    }

    fun loadQuoteIntoStudio(quote: LoveQuote) {
        _studioState.value = _studioState.value.copy(
            quoteText = quote.text,
            author = quote.author
        )
    }

    fun loadCustomQuoteIntoStudio(text: String, author: String) {
        _studioState.value = _studioState.value.copy(
            quoteText = text,
            author = author
        )
    }

    fun loadNoteIntoStudio(note: NoteEntity) {
        val matchedTheme = CardStyleOptions.themePresets.find { it.id == note.backgroundTheme }
            ?: CardStyleOptions.themePresets[0]
        val matchedFilter = FilterPreset.values().find { it.id == note.filterStyle }
            ?: FilterPreset.NORMAL

        _studioState.value = _studioState.value.copy(
            quoteText = note.content,
            author = note.author,
            themePreset = matchedTheme,
            filterPreset = matchedFilter
        )
    }

    // ===============================================================
    // Audio Voice Note Recording
    // ===============================================================
    fun startRecordingAudio(): String? {
        val path = audioHelper.startRecording()
        if (path != null) {
            _isRecording.value = true
            _recordingTimerSeconds.value = 0
            recordingTimerJob?.cancel()
            recordingTimerJob = viewModelScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordingTimerSeconds.value += 1
                }
            }
        }
        return path
    }

    fun stopRecordingAudio(): Pair<String?, Int> {
        recordingTimerJob?.cancel()
        val path = audioHelper.stopRecording()
        val duration = _recordingTimerSeconds.value
        _isRecording.value = false
        _recordingTimerSeconds.value = 0
        return Pair(path, duration)
    }

    fun playAudio(path: String) {
        if (_currentlyPlayingAudio.value == path) {
            stopAudio()
            return
        }
        _currentlyPlayingAudio.value = path
        audioHelper.startPlaying(path) {
            _currentlyPlayingAudio.value = null
        }
    }

    fun stopAudio() {
        audioHelper.stopPlaying()
        _currentlyPlayingAudio.value = null
    }

    // ===============================================================
    // Cloud Backup & Export / Restore
    // ===============================================================
    fun syncWithCloud() {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Firebase bulut yedeği hazırlanıyor..."
            val favIds = curatedFavoriteIdsFlow.value
            val result = firestoreRepo.syncWithCloud(
                uid = uid,
                notes = allNotes.value,
                curatedFavIds = favIds,
                savedFavCount = savedFavoriteQuotes.value.size,
                reminders = allReminders.value,
                entitlement = entitlementState.value
            )
            result.onSuccess { message ->
                _syncMessage.value = message
            }.onFailure { error ->
                _syncMessage.value = "Senkronizasyon hatası: ${error.localizedMessage}"
            }
            _isSyncing.value = false
        }
    }

    fun restoreLastCloudSnapshot(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val uid = currentUid
        if (uid.isBlank()) return
        val snapshot = userDocFlow.value?.lastBackupJson.orEmpty()
        if (snapshot.isBlank()) {
            val msg = "Bulutta geri yüklenecek yedek bulunamadı."
            _syncMessage.value = msg
            onResult(false, msg)
            return
        }
        viewModelScope.launch {
            val res = firestoreRepo.restoreFromJson(uid, snapshot)
            res.onSuccess { count ->
                val msg = "Bulut yedeğinden $count kayıt başarıyla geri yüklendi! ☁️"
                _syncMessage.value = msg
                onResult(true, msg)
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Bulut yedeği geri yüklenemedi."
                _syncMessage.value = msg
                onResult(false, msg)
            }
        }
    }

    fun generateBackupJson() {
        viewModelScope.launch {
            val json = firestoreRepo.createBackupJson(
                notes = allNotes.value,
                curatedFavIds = curatedFavoriteIdsFlow.value,
                reminders = allReminders.value
            )
            _exportedBackupJson.value = json
            _backupStatusMessage.value = "Yedek başarıyla oluşturuldu! Paylaşabilir veya kopyalayabilirsiniz."
        }
    }

    fun clearExportedJson() {
        _exportedBackupJson.value = null
        _backupStatusMessage.value = null
    }

    fun restoreBackup(jsonString: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            val result = firestoreRepo.restoreFromJson(uid, jsonString)
            result.onSuccess { count ->
                _backupStatusMessage.value = "Başarılı! Toplam $count kayıt Firebase hesabına geri yüklendi."
            }.onFailure { err ->
                _backupStatusMessage.value = "Hata: ${err.localizedMessage ?: "Yedek dosyası okunamadı"}"
            }
        }
    }

    // ===============================================================
    // Theme & Notification Preferences
    // ===============================================================
    fun setThemeMode(mode: String) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.updateUserFields(uid, mapOf("themeMode" to mode))
        }
    }

    fun setDesignTheme(theme: AppDesignTheme) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.updateUserFields(uid, mapOf("designTheme" to theme.id))
        }
    }

    fun updateDailyNotificationTime(hour: Int, minute: Int) {
        val uid = currentUid
        alarmPrefs.edit()
            .putInt("daily_notif_hour", hour)
            .putInt("daily_notif_minute", minute)
            .apply()
        if (isDailyNotificationEnabled.value) {
            NotificationHelper.scheduleDailyQuoteAlarm(application, hour, minute)
        }
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                firestoreRepo.updateUserFields(
                    uid,
                    mapOf("dailyNotifHour" to hour, "dailyNotifMinute" to minute)
                )
            }
        }
    }

    fun toggleDailyNotification(enabled: Boolean) {
        val uid = currentUid
        alarmPrefs.edit().putBoolean("daily_notif_enabled", enabled).apply()
        val (h, m) = dailyNotificationTime.value
        if (enabled) {
            NotificationHelper.scheduleDailyQuoteAlarm(application, h, m)
        } else {
            NotificationHelper.cancelDailyQuoteAlarm(application)
        }
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                firestoreRepo.updateUserFields(uid, mapOf("dailyNotifEnabled" to enabled))
            }
        }
    }

    fun setDailyNotificationTone(tone: String) {
        val uid = currentUid
        alarmPrefs.edit().putString("daily_notif_tone", tone).apply()
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                firestoreRepo.updateUserFields(uid, mapOf("dailyNotifTone" to tone))
            }
        }
    }

    fun triggerTestNotification() {
        NotificationHelper.createNotificationChannels(application)
        val tone = dailyNotificationTone.value
        val morningQuote = CuratedQuotes.pickMorningQuote(tone)
        NotificationHelper.showDailyQuoteNotification(
            context = application,
            quoteText = morningQuote.text,
            author = morningQuote.author,
            quoteId = morningQuote.id,
            sectionId = morningQuote.section.id,
            tonePreference = tone
        )
    }

    fun openQuoteFromNotification(
        quoteId: String?,
        text: String?,
        author: String?,
        sectionId: String?
    ) {
        val matched = quoteId?.takeIf { it.isNotBlank() }?.let { CuratedQuotes.findById(it) }
        val resolvedSection = matched?.section
            ?: QuoteSection.values().find { it.id == sectionId }
            ?: QuoteSection.GUNAYDIN_MESAJLARI
        val resolvedQuote = matched ?: LoveQuote(
            id = quoteId?.ifBlank { "daily_spotlight" } ?: "daily_spotlight",
            text = text?.takeIf { it.isNotBlank() } ?: _dailyQuote.value.text,
            author = author?.takeIf { it.isNotBlank() } ?: _dailyQuote.value.author,
            section = resolvedSection,
            subCategory = "Sabah İlhamı"
        )
        _activeSection.value = resolvedQuote.section
        _dailyQuote.value = resolvedQuote
        _spotlightQuote.value = resolvedQuote
    }

    fun dismissSpotlightQuote() {
        _spotlightQuote.value = null
    }

    // ===============================================================
    // AI Quote Generator, Mood Analysis, Acrostic & Coach
    // ===============================================================
    fun generateAiQuote(theme: String) {
        viewModelScope.launch {
            _aiQuoteState.value = AiQuoteUiState.Loading
            val result = aiRepository.generateQuote(theme)
            result.onSuccess { res ->
                _aiQuoteState.value = AiQuoteUiState.Success(res)
            }.onFailure { err ->
                _aiQuoteState.value = AiQuoteUiState.Error(err.localizedMessage ?: "Söz üretilemedi")
            }
        }
    }

    fun resetAiQuote() {
        _aiQuoteState.value = AiQuoteUiState.Idle
    }

    fun refreshMoodAnalysis() {
        refreshMoodAnalysisWithNotes(allNotes.value)
    }

    private fun refreshMoodAnalysisWithNotes(notes: List<NoteEntity>) {
        viewModelScope.launch {
            val result = aiRepository.analyzeMoodTrends(notes)
            result.onSuccess { res ->
                _moodAnalysisState.value = MoodAnalysisUiState.Success(res)
            }.onFailure {
                if (_moodAnalysisState.value !is MoodAnalysisUiState.Success) {
                    _moodAnalysisState.value = MoodAnalysisUiState.Success(aiRepository.getBaselineMoodAnalysis(notes))
                }
            }
        }
    }

    fun saveAiQuoteAsNote(quote: AiQuoteResponse) {
        saveNote(
            title = "AI Söz: ${quote.theme.ifBlank { "Romantik" }}",
            content = quote.text,
            author = quote.author,
            tags = "AI Sözü, ${quote.theme.ifBlank { "Romantik" }}",
            backgroundTheme = "ruby_velvet",
            filterStyle = "soft_glow"
        )
    }

    fun generateAcrosticPoem(partnerName: String) {
        viewModelScope.launch {
            _isAcrosticLoading.value = true
            try {
                val result = aiRepository.generateAcrosticPoem(partnerName)
                result.onSuccess { poem ->
                    _acrosticPoemResult.value = poem
                }.onFailure {
                    _acrosticPoemResult.value = AcrosticPoemBuilder.buildFallbackAcrostic(partnerName)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _acrosticPoemResult.value = AcrosticPoemBuilder.buildFallbackAcrostic(partnerName)
            } finally {
                _isAcrosticLoading.value = false
            }
        }
    }

    fun generateSituationMessage(situation: String) {
        viewModelScope.launch {
            _situationMessageState.value = AiQuoteUiState.Loading
            try {
                val result = aiRepository.generateSituationMessage(situation)
                result.onSuccess { res ->
                    _situationMessageState.value = AiQuoteUiState.Success(res)
                }.onFailure { err ->
                    _situationMessageState.value = AiQuoteUiState.Error(err.localizedMessage ?: "Mesaj oluşturulamadı")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _situationMessageState.value = AiQuoteUiState.Error(e.localizedMessage ?: "Mesaj oluşturulamadı")
            }
        }
    }

    // ===============================================================
    // Personal Favorites Library
    // ===============================================================
    fun saveToFavoritesLibrary(
        title: String,
        text: String,
        author: String = "Anonim",
        category: String = "Romantik",
        sourceLabel: String = "Kişisel Favori",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Favorilere eklemek için giriş yapmalısınız.")
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.addSavedFavoriteQuote(uid, title, text, author, category, sourceLabel)
            result.onSuccess { added ->
                if (added) {
                    onResult(true, "Favorilerim kütüphanene eklendi! ❤️")
                } else {
                    onResult(false, "Bu söz zaten Favorilerim kütüphanende kayıtlı.")
                }
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Favorilere eklenemedi.")
            }
        }
    }

    fun removeFromFavoritesLibrary(id: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.removeSavedFavoriteQuote(uid, id)
        }
    }

    // ===============================================================
    // Admin Contact Messages & Broadcasts
    // ===============================================================
    fun sendMessageToAdmin(
        senderName: String,
        senderEmail: String,
        topic: String,
        message: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Destek mesajı göndermek için giriş yapmalısınız.")
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.sendMessageToAdmin(uid, senderName, senderEmail, topic, message)
            result.onSuccess {
                onResult(true, "Mesajınız yöneticiye başarıyla iletildi! En kısa sürede yanıtlanacaktır. 📩")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Mesaj gönderilemedi.")
            }
        }
    }

    fun replyToAdminMessage(messageId: Long, reply: String) {
        viewModelScope.launch {
            firestoreRepo.replyToAdminMessage(messageId, reply)
        }
    }

    fun deleteAdminMessage(messageId: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteAdminMessage(messageId)
        }
    }

    fun sendBroadcastToAllUsers(
        title: String,
        message: String,
        category: String,
        emoji: String,
        alsoShowSystemNotification: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        if (title.isBlank() || message.isBlank()) {
            onResult(false, "Başlık ve bildirim mesajı boş bırakılamaz.")
            return
        }
        viewModelScope.launch {
            val res = firestoreRepo.sendBroadcastNotification(title, message, category, emoji)
            res.onSuccess { id ->
                if (alsoShowSystemNotification) {
                    NotificationHelper.showBroadcastNotification(
                        context = application,
                        broadcastId = id,
                        title = title.trim(),
                        message = message.trim(),
                        emoji = emoji.ifBlank { "📢" }
                    )
                }
                onResult(true, "Bildirim tüm kullanıcılara gönderildi! 📢")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Duyuru gönderilemedi.")
            }
        }
    }

    fun markBroadcastAsRead(id: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.markBroadcastAsRead(uid, id)
        }
    }

    fun markAllBroadcastsAsRead() {
        val uid = currentUid
        if (uid.isBlank()) return
        val ids = allBroadcastNotifications.value.map { it.id }
        viewModelScope.launch {
            firestoreRepo.markAllBroadcastsAsRead(uid, ids)
        }
    }

    fun deleteBroadcast(id: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteBroadcast(id)
        }
    }

    // ===============================================================
    // Community Quotes, Moderation & Engagement Notifications
    // ===============================================================
    fun submitCommunityQuote(
        text: String,
        author: String,
        category: String,
        submitterName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Toplulukta söz paylaşmak için giriş yapmalısınız.")
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.submitCommunityQuote(
                uid = uid,
                userDoc = userDocFlow.value,
                settings = adminSettings.value,
                text = text,
                author = author,
                category = category,
                submitterName = submitterName.ifBlank { sessionState.value.displayName }
            )
            result.onSuccess { quote ->
                val msg = if (quote.status == "APPROVED") {
                    "Harika! Sözünüz anında onaylandı ve Topluluk Sözleri akışında yayınlandı! 💖"
                } else {
                    "Sözünüz başarıyla gönderildi! Yönetici onayından sonra toplulukta yayınlanacaktır. 💌"
                }
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Söz gönderilemedi.")
            }
        }
    }

    fun toggleCommunityQuoteLike(quoteId: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.toggleCommunityQuoteLike(uid, quoteId)
        }
    }

    fun addCommunityComment(
        quoteId: Long,
        authorName: String,
        commentText: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Yorum yapmak için giriş yapmalısınız.")
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.addCommunityComment(
                uid = uid,
                userDoc = userDocFlow.value,
                quoteId = quoteId,
                authorName = authorName.ifBlank { sessionState.value.displayName },
                commentText = commentText
            )
            result.onSuccess {
                onResult(true, "Yorumunuz eklendi! 💬")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Yorum eklenemedi.")
            }
        }
    }

    fun deleteCommunityComment(commentId: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteCommunityComment(commentId)
        }
    }

    fun approveCommunityQuote(quoteId: Long) {
        viewModelScope.launch {
            firestoreRepo.approveCommunityQuote(quoteId)
        }
    }

    fun rejectCommunityQuote(quoteId: Long, note: String?) {
        viewModelScope.launch {
            firestoreRepo.rejectCommunityQuote(quoteId, note)
        }
    }

    fun updateCommunityQuote(quote: CommunityQuoteEntity) {
        viewModelScope.launch {
            firestoreRepo.updateCommunityQuote(quote)
        }
    }

    fun deleteCommunityQuote(quoteId: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteCommunityQuote(quoteId)
        }
    }

    fun markEngagementNotificationRead(id: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.markEngagementRead(uid, id)
        }
    }

    fun markAllEngagementNotificationsRead() {
        val uid = currentUid
        if (uid.isBlank()) return
        val ids = engagementNotifications.value.map { it.id }
        viewModelScope.launch {
            firestoreRepo.markAllEngagementRead(uid, ids)
        }
    }

    fun deleteEngagementNotification(id: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.deleteEngagementNotification(uid, id)
        }
    }

    fun clearAllEngagementNotifications() {
        val uid = currentUid
        if (uid.isBlank()) return
        val ids = engagementNotifications.value.map { it.id }
        viewModelScope.launch {
            firestoreRepo.clearAllEngagementNotifications(uid, ids)
        }
    }

    // ===============================================================
    // Admin Official Quotes & Settings
    // ===============================================================
    fun updateAdminSettings(newSettings: AdminSettings) {
        viewModelScope.launch {
            firestoreRepo.saveAdminSettings(newSettings)
        }
        val updatedEntitlement = entitlementState.value
        if (!updatedEntitlement.shouldShowAds || !newSettings.adBlockerGuardEnabled) {
            _isAdBlockerDetected.value = false
        }
    }

    fun addAdminQuote(
        text: String,
        author: String,
        sectionId: String,
        subCategory: String,
        tagsCsv: String,
        isFeaturedDaily: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = firestoreRepo.addAdminQuote(text, author, sectionId, subCategory, tagsCsv, isFeaturedDaily)
            result.onSuccess { added ->
                if (isFeaturedDaily) {
                    val section = QuoteSection.values().find { it.id == sectionId } ?: QuoteSection.ASK_SOZLERI
                    _dailyQuote.value = LoveQuote(
                        id = "admin_${added.id}",
                        text = added.text,
                        author = added.author,
                        section = section,
                        subCategory = added.subCategory,
                        tags = added.tagsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    )
                }
                onResult(true, "Yeni söz başarıyla uygulamaya eklendi! ✨")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Söz eklenemedi.")
            }
        }
    }

    fun updateAdminQuote(quote: AdminQuoteEntity) {
        viewModelScope.launch {
            firestoreRepo.updateAdminQuote(quote)
        }
    }

    fun setFeaturedAdminQuote(quote: AdminQuoteEntity) {
        viewModelScope.launch {
            firestoreRepo.setFeaturedAdminQuote(quote.id)
            val section = QuoteSection.values().find { it.id == quote.sectionId } ?: QuoteSection.ASK_SOZLERI
            _dailyQuote.value = LoveQuote(
                id = "admin_${quote.id}",
                text = quote.text,
                author = quote.author,
                section = section,
                subCategory = quote.subCategory,
                tags = quote.tagsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
            )
        }
    }

    fun deleteAdminQuote(id: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteAdminQuote(id)
        }
    }

    // ===============================================================
    // Firebase Auth Sign-Out & Admin User Moderation
    // ===============================================================
    fun logoutSession() {
        ambientHelper.stopAmbient()
        _isAmbientPlaying.value = false
        recordingTimerJob?.cancel()
        chatSlowModeTimerJob?.cancel()
        audioHelper.stopPlaying()
        ttsHelper.stop()
        viewModelScope.launch {
            withContext(NonCancellable) {
                GoogleAuthManager.signOut(application, auth)
            }
        }
    }

    fun clearAccountStatusNotice() {
        _accountStatusNotice.value = null
    }

    fun toggleUserBlocked(userId: Long, blocked: Boolean, reason: String = "") {
        viewModelScope.launch {
            firestoreRepo.setUserAccountBanned(userId, blocked, reason)
        }
    }

    fun applyUserRestrictions(
        userId: Long,
        isRestricted: Boolean,
        canSubmitQuotes: Boolean,
        canComment: Boolean,
        canUploadMusic: Boolean,
        canChat: Boolean = true,
        reason: String,
        durationHours: Int?
    ) {
        viewModelScope.launch {
            val until = if (isRestricted && durationHours != null && durationHours > 0) {
                System.currentTimeMillis() + (durationHours * 3_600_000L)
            } else null
            firestoreRepo.applyUserRestrictions(
                userId = userId,
                isRestricted = isRestricted,
                canSubmitQuotes = canSubmitQuotes,
                canComment = canComment,
                canUploadMusic = canUploadMusic,
                canChat = canChat,
                reason = reason,
                restrictedUntil = until
            )
        }
    }

    fun deleteUserAccount(userId: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteUserAccountByAdmin(userId)
        }
    }

    fun deleteCurrentUserAccountAndData(onComplete: (String) -> Unit) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            val res = withContext(NonCancellable) {
                firestoreRepo.deleteOwnAccountAndData(uid)
            }
            res.onSuccess {
                onComplete("Hesabınız ve tüm Firebase bulut verileriniz kalıcı olarak silindi.")
            }.onFailure { err ->
                onComplete(err.localizedMessage ?: "Hesap silinirken bir hata oluştu.")
            }
        }
    }

    // ===============================================================
    // Monetization, Purchases, Coins & Targeted Ad-Blocker Guard
    // ===============================================================
    fun purchaseStoreProduct(
        activity: Activity?,
        product: StoreProduct,
        onNeedPreviewFallback: () -> Unit,
        onCompleted: (String) -> Unit
    ) {
        val launchedPlay = playBillingController?.launchPurchaseFlow(activity, product) == true
        if (!launchedPlay) {
            onNeedPreviewFallback()
        } else {
            onCompleted("${product.title} Google Play ödeme akışı başlatıldı.")
        }
    }

    fun completePreviewPurchase(product: StoreProduct, onResult: (String) -> Unit) {
        val uid = currentUid
        if (uid.isBlank()) return
        val doc = userDocFlow.value ?: FsUserDoc(uid = uid)
        viewModelScope.launch {
            firestoreRepo.applyStoreProductPurchase(uid, doc, product, "Mağaza Önizleme")
            if (product.grantsVip || product.grantsAdFree) {
                _isAdBlockerDetected.value = false
            }
            onResult("Teşekkürler! \"${product.title}\" hesabına tanımlandı ✨")
        }
    }

    fun restorePlayPurchases(onResult: (String) -> Unit) {
        val controller = playBillingController
        if (controller == null || !_billingReady.value) {
            onResult("Google Play bağlantısı hazır değil, bulut ayrıcalıklarınız aktif durumda.")
            return
        }
        controller.queryActivePurchases { restoredCount ->
            if (!entitlementState.value.shouldShowAds) {
                _isAdBlockerDetected.value = false
            }
            if (restoredCount > 0) {
                onResult("$restoredCount aktif satın alım başarıyla geri yüklendi! 👑")
            } else {
                onResult("Google Play hesabınızda aktif başka abonelik bulunamadı.")
            }
        }
    }

    fun clearBillingStatusMessage() {
        _billingStatusMessage.value = null
    }

    fun watchRewardedAdForCoins(
        activity: Activity?,
        onOfflinePreviewNeeded: () -> Unit,
        onRewardEarned: (Int) -> Unit
    ) {
        val rewardAmount = adminSettings.value.rewardCoinsPerAd.coerceAtLeast(5)
        val uid = currentUid
        val coins = entitlementState.value.loveCoins
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                if (uid.isNotBlank()) {
                    viewModelScope.launch {
                        firestoreRepo.grantRewardedCoins(uid, coins, rewardAmount, "Ödüllü Video İzleme")
                    }
                }
                onRewardEarned(rewardAmount)
            },
            onOfflineFallback = onOfflinePreviewNeeded
        )
    }

    fun grantRewardedAdFallbackCoins(onRewardEarned: (Int) -> Unit) {
        val uid = currentUid
        if (uid.isBlank()) return
        val rewardAmount = adminSettings.value.rewardCoinsPerAd.coerceAtLeast(5)
        val coins = entitlementState.value.loveCoins
        viewModelScope.launch {
            firestoreRepo.grantRewardedCoins(uid, coins, rewardAmount, "Sponsorlu Ödül Görevi")
            onRewardEarned(rewardAmount)
        }
    }

    fun unlockStoryFrameWithCoins(
        frame: StoryTemplateFrame,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = currentUid
        val doc = userDocFlow.value
        if (uid.isBlank() || doc == null) return
        viewModelScope.launch {
            val res = firestoreRepo.spendCoinsToUnlockItem(uid, doc, frame.id, frame.title, frame.coinCost)
            res.onSuccess {
                _studioState.value = _studioState.value.copy(storyFrame = frame)
                onResult(true, "\"${frame.title}\" çerçevesi kalıcı olarak açıldı! ✨")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Yetersiz Aşk Parası.")
            }
        }
    }

    fun unlockStudioThemeWithCoins(
        preset: ThemePreset,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = currentUid
        val doc = userDocFlow.value
        if (uid.isBlank() || doc == null) return
        viewModelScope.launch {
            val res = firestoreRepo.spendCoinsToUnlockItem(uid, doc, preset.id, preset.name, preset.coinCost)
            res.onSuccess {
                _studioState.value = _studioState.value.copy(
                    themePreset = preset,
                    backgroundImagePreset = null
                )
                onResult(true, "\"${preset.name}\" tasarım teması kalıcı olarak açıldı! ✨")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Yetersiz Aşk Parası.")
            }
        }
    }

    fun checkForAdBlocker(forceRefresh: Boolean = false) {
        val shouldCheck = entitlementState.value.shouldShowAds && adminSettings.value.adBlockerGuardEnabled
        if (!shouldCheck) {
            _isAdBlockerDetected.value = false
            return
        }
        val now = System.currentTimeMillis()
        if (!forceRefresh && now - lastAdBlockerCheckAt < 60_000L) return
        lastAdBlockerCheckAt = now

        viewModelScope.launch {
            _isCheckingAdBlocker.value = true
            try {
                val res = AdBlockerDetector.detect(application)
                if (entitlementState.value.shouldShowAds && adminSettings.value.adBlockerGuardEnabled) {
                    _isAdBlockerDetected.value = res.adBlockerDetected
                    _adBlockerReason.value = res.signalReason
                } else {
                    _isAdBlockerDetected.value = false
                }
            } finally {
                _isCheckingAdBlocker.value = false
            }
        }
    }

    fun onAdBannerNetworkFailed() {
        checkForAdBlocker(forceRefresh = false)
    }

    fun dismissAdBlockerNotice() {
        _isAdBlockerDetected.value = false
    }

    fun notifyVisualCardExported(activity: Activity?) {
        val shouldShow = entitlementState.value.shouldShowAds
        val cooldown = adminSettings.value.interstitialCooldownSeconds
        adManager.showInterstitialIfEligible(activity, shouldShow, cooldown)
    }

    fun toggleAdminTestVip(enabled: Boolean) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.setTestVipStatus(uid, enabled)
            if (!enabled.not()) {
                _isAdBlockerDetected.value = false
            }
        }
    }

    // ===============================================================
    // Community Chat, Paginated "Mesaj Geçmişi" & Admin Chat Control
    // ===============================================================
    fun loadInitialChatHistoryIfNeeded(room: ChatRoom) {
        if (_chatHistoryMessagesByRoom.value[room.id] == null && !_isLoadingChatHistory.value) {
            loadOlderChatMessages(room)
        }
    }

    fun loadOlderChatMessages(room: ChatRoom, onComplete: (Int, Boolean) -> Unit = { _, _ -> }) {
        if (_isLoadingChatHistory.value) return
        viewModelScope.launch {
            _isLoadingChatHistory.value = true
            try {
                val cursor = chatHistoryCursorsByRoom[room.id]
                val res = firestoreRepo.loadChatHistoryPage(
                    roomId = room.id,
                    cursor = cursor,
                    isAdmin = isAdminAuthenticated.value,
                    pageSize = 20L
                )
                res.onSuccess { page ->
                    chatHistoryCursorsByRoom[room.id] = page.nextCursor
                    val existing = _chatHistoryMessagesByRoom.value[room.id].orEmpty()
                    val combined = ChatHistoryMerger.mergePagedAndLive(
                        loadedHistory = existing,
                        liveMessages = page.messages,
                        roomId = room.id,
                        includeAllStatuses = isAdminAuthenticated.value
                    )
                    _chatHistoryMessagesByRoom.value = _chatHistoryMessagesByRoom.value + (room.id to combined)
                    _hasMoreChatHistoryByRoom.value = _hasMoreChatHistoryByRoom.value + (room.id to page.hasMore)
                    onComplete(page.messages.size, page.hasMore)
                }.onFailure {
                    onComplete(0, false)
                }
            } finally {
                _isLoadingChatHistory.value = false
            }
        }
    }

    private fun startChatSlowModeCountdownIfNeeded() {
        chatSlowModeTimerJob?.cancel()
        val slowSec = adminSettings.value.chatSlowModeSeconds
        val session = sessionState.value
        if (session.isAdmin || slowSec <= 0) {
            _chatSlowModeRemainingSeconds.value = 0
            return
        }
        val initial = ChatModerationHelper.checkSlowModeRemainingSeconds(
            lastSentAtMillis = firestoreRepo.getLastChatSentAt(),
            slowModeSeconds = slowSec
        )
        _chatSlowModeRemainingSeconds.value = initial
        if (initial > 0) {
            chatSlowModeTimerJob = viewModelScope.launch {
                while (_chatSlowModeRemainingSeconds.value > 0) {
                    delay(1000L)
                    _chatSlowModeRemainingSeconds.value = ChatModerationHelper.checkSlowModeRemainingSeconds(
                        lastSentAtMillis = firestoreRepo.getLastChatSentAt(),
                        slowModeSeconds = adminSettings.value.chatSlowModeSeconds
                    )
                }
            }
        }
    }

    fun sendChatMessage(
        room: ChatRoom,
        message: String,
        replyTo: ChatMessageEntity? = null,
        pinAsAdmin: Boolean = false,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Sohbete yazmak için giriş yapın.")
            return
        }
        val session = sessionState.value
        val entitlement = entitlementState.value
        val senderRole = session.role ?: "USER"
        viewModelScope.launch {
            val res = firestoreRepo.sendChatMessage(
                uid = uid,
                userDoc = userDocFlow.value,
                settings = adminSettings.value,
                roomId = room.id,
                senderName = session.displayName,
                senderRole = senderRole,
                isPremiumSender = entitlement.isVip || entitlement.hasVipBadgeAndAi,
                vipPlanName = entitlement.vipPlanName,
                message = message,
                replyTo = replyTo,
                pinAsAdmin = pinAsAdmin
            )
            res.onSuccess { inserted ->
                startChatSlowModeCountdownIfNeeded()
                val msg = when {
                    inserted.status == "PENDING" -> "Mesajınız yönetici onayına gönderildi ⏳"
                    inserted.isPinned -> "Sabit yönetici mesajı yayınlandı 📌"
                    else -> "Mesaj gönderildi"
                }
                onResult(true, msg)
            }.onFailure { err ->
                startChatSlowModeCountdownIfNeeded()
                onResult(false, err.localizedMessage ?: "Mesaj gönderilemedi.")
            }
        }
    }

    fun sendAdminChatMessage(
        room: ChatRoom,
        message: String,
        pinMessage: Boolean,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            val res = firestoreRepo.sendChatMessage(
                uid = uid,
                userDoc = userDocFlow.value,
                settings = adminSettings.value,
                roomId = room.id,
                senderName = "Yönetici 👑",
                senderRole = "ADMIN",
                isPremiumSender = true,
                vipPlanName = "Yönetici",
                message = message,
                replyTo = null,
                pinAsAdmin = pinMessage
            )
            res.onSuccess {
                onResult(true, if (pinMessage) "Sabit yönetici mesajı odaya eklendi 📌" else "Yönetici mesajı gönderildi 👑")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Mesaj gönderilemedi.")
            }
        }
    }

    fun toggleChatMessageLike(messageId: Long) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.toggleChatMessageLike(uid, messageId)
        }
    }

    fun approveChatMessage(id: Long) {
        viewModelScope.launch {
            firestoreRepo.approveChatMessage(id)
        }
    }

    fun hideChatMessage(id: Long, note: String? = null) {
        viewModelScope.launch {
            firestoreRepo.hideChatMessage(id, note)
        }
    }

    fun togglePinChatMessage(msg: ChatMessageEntity) {
        viewModelScope.launch {
            firestoreRepo.togglePinChatMessage(msg.id, msg.roomId, !msg.isPinned)
        }
    }

    fun deleteChatMessage(id: Long) {
        viewModelScope.launch {
            firestoreRepo.deleteChatMessage(id)
        }
    }

    fun clearChatRoom(roomId: String?) {
        viewModelScope.launch {
            firestoreRepo.clearChatRoom(roomId)
            if (roomId == null) {
                chatHistoryCursorsByRoom.clear()
                _chatHistoryMessagesByRoom.value = emptyMap()
                _hasMoreChatHistoryByRoom.value = emptyMap()
            } else {
                chatHistoryCursorsByRoom.remove(roomId)
                _chatHistoryMessagesByRoom.value = _chatHistoryMessagesByRoom.value - roomId
                _hasMoreChatHistoryByRoom.value = _hasMoreChatHistoryByRoom.value - roomId
            }
        }
    }

    fun muteUserInChat(
        userId: Long,
        muted: Boolean,
        durationHours: Int? = 24,
        reason: String = "Sohbet kuralları ihlali"
    ) {
        if (userId <= 0L) return
        viewModelScope.launch {
            val until = if (muted && durationHours != null && durationHours > 0) {
                System.currentTimeMillis() + (durationHours * 3_600_000L)
            } else null
            firestoreRepo.setUserChatMuted(userId, muted, reason, until)
        }
    }

    fun muteUserInChatByUid(
        targetUid: String,
        muted: Boolean,
        durationHours: Int? = 24,
        reason: String = "Sohbet kuralları ihlali"
    ) {
        if (targetUid.isBlank()) return
        viewModelScope.launch {
            val until = if (muted && durationHours != null && durationHours > 0) {
                System.currentTimeMillis() + (durationHours * 3_600_000L)
            } else null
            firestoreRepo.setUserChatMutedByUid(targetUid, muted, reason, until)
        }
    }

    // ===============================================================
    // Private Emotional Journal ("Duygusal Günlük") & Vault Security
    // ===============================================================
    fun unlockJournalWithPin(pin: String): Boolean {
        val uid = currentUid
        if (uid.isBlank()) return false
        val storedHash = journalVaultPrefs.getString("pin_hash_$uid", null)
        val ok = JournalSecurityHelper.verifyPin(uid, pin, storedHash)
        if (ok) {
            _isJournalUnlocked.value = true
        }
        return ok
    }

    fun lockJournalNow() {
        if (_isJournalPinEnabled.value) {
            _isJournalUnlocked.value = false
        }
    }

    fun enableJournalPin(pin: String, onResult: (Boolean, String) -> Unit) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "PIN kilidi için oturum açık olmalıdır.")
            return
        }
        if (!JournalSecurityHelper.isValidPinFormat(pin)) {
            onResult(false, "Lütfen 4 rakamdan oluşan bir PIN belirleyin.")
            return
        }
        val hash = JournalSecurityHelper.hashPin(uid, pin)
        journalVaultPrefs.edit().putString("pin_hash_$uid", hash).apply()
        _isJournalPinEnabled.value = true
        _isJournalUnlocked.value = true
        onResult(true, "Günlük kasası 4 haneli PIN ile koruma altına alındı 🔒")
    }

    fun disableJournalPin(currentPin: String, onResult: (Boolean, String) -> Unit) {
        val uid = currentUid
        if (uid.isBlank()) return
        val storedHash = journalVaultPrefs.getString("pin_hash_$uid", null)
        if (!JournalSecurityHelper.verifyPin(uid, currentPin, storedHash)) {
            onResult(false, "Mevcut PIN hatalı.")
            return
        }
        journalVaultPrefs.edit().remove("pin_hash_$uid").apply()
        _isJournalPinEnabled.value = false
        _isJournalUnlocked.value = true
        onResult(true, "Günlük PIN kilidi kaldırıldı.")
    }

    fun toggleJournalPrivacyShield() {
        val uid = currentUid
        val next = !_isJournalPrivacyShieldOn.value
        _isJournalPrivacyShieldOn.value = next
        if (uid.isNotBlank()) {
            journalVaultPrefs.edit().putBoolean("shield_$uid", next).apply()
        }
    }

    fun saveJournalEntry(
        entry: EmotionalJournalEntry,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val uid = currentUid
        if (uid.isBlank()) {
            onResult(false, "Anınızı kaydetmek için giriş yapmalısınız.")
            return
        }
        viewModelScope.launch {
            val isEdit = entry.docId.isNotBlank() || entry.id != 0L
            val res = firestoreRepo.saveJournalEntry(uid, entry)
            res.onSuccess {
                onResult(
                    true,
                    if (isEdit) "Günlük anısı güncellendi ✨"
                    else "Anınız güvenle günlüğünüze kaydedildi 💖"
                )
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Günlük kaydı kaydedilemedi.")
            }
        }
    }

    fun toggleJournalFavorite(entry: EmotionalJournalEntry) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.toggleJournalFavorite(uid, entry)
        }
    }

    fun toggleJournalEntryPrivateLock(entry: EmotionalJournalEntry) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            firestoreRepo.toggleJournalPrivateLock(uid, entry)
        }
    }

    fun deleteJournalEntry(
        entry: EmotionalJournalEntry,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val uid = currentUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            val res = firestoreRepo.deleteJournalEntry(uid, entry)
            res.onSuccess {
                onResult(true, "Anı günlükten silindi.")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Anı silinemedi.")
            }
        }
    }

    fun loadJournalEntryIntoStudio(entry: EmotionalJournalEntry) {
        val partnerOrSelf = sessionState.value.displayName
        _studioState.value = _studioState.value.copy(
            quoteText = entry.content.take(360),
            author = if (entry.title.isNotBlank()) "${entry.title} • $partnerOrSelf" else partnerOrSelf
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioHelper.cleanUp()
        ambientHelper.stopAmbient()
        ttsHelper.release()
        playBillingController?.endConnection()
    }
}
