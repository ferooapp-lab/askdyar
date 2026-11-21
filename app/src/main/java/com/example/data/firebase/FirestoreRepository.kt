package com.example.data.firebase

import android.content.Context
import android.util.Log
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
import com.example.data.model.AdminSettings
import com.example.data.model.BackupNoteItem
import com.example.data.model.BackupPayload
import com.example.data.model.BackupReminderItem
import com.example.data.model.ChatModerationHelper
import com.example.data.model.ChatRoom
import com.example.data.model.DefaultLoveJarReasons
import com.example.data.model.EmotionalJournalEntry
import com.example.data.model.EntitlementState
import com.example.data.model.JournalDateHelper
import com.example.data.model.JournalMood
import com.example.data.model.LoveTimeCapsule
import com.example.data.model.MonetizationCatalog
import com.example.data.model.PurchaseRecord
import com.example.data.model.SpecialCountdownEvent
import com.example.data.model.StoreProduct
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class ChatHistoryPageResult(
    val messages: List<ChatMessageEntity>,
    val nextCursor: DocumentSnapshot?,
    val hasMore: Boolean
)

class FirestoreRepository(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        const val PRIMARY_ADMIN_EMAIL = "ferhat.krnn65@gmail.com"
        private const val ALARM_CACHE_PREFS = "romantic_prefs"
        private const val KEY_ALARM_CACHE_JSON = "alarm_schedule_cache_json"
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(BackupPayload::class.java)

    // Bidirectional docId <-> stable Long ID registries for UI compatibility
    private val noteDocIds = ConcurrentHashMap<Long, String>()
    private val journalDocIds = ConcurrentHashMap<Long, String>()
    private val savedFavDocIds = ConcurrentHashMap<Long, String>()
    private val reminderDocIds = ConcurrentHashMap<Long, String>()
    private val musicDocIds = ConcurrentHashMap<Long, String>()
    private val notifDocIds = ConcurrentHashMap<Long, String>()
    private val adminQuoteDocIds = ConcurrentHashMap<Long, String>()
    private val broadcastDocIds = ConcurrentHashMap<Long, String>()
    private val supportMsgDocIds = ConcurrentHashMap<Long, String>()
    private val communityQuoteDocIds = ConcurrentHashMap<Long, String>()
    private val commentDocIds = ConcurrentHashMap<Long, String>()
    private val commentParentQuoteDocIds = ConcurrentHashMap<Long, String>()
    private val chatDocIds = ConcurrentHashMap<Long, String>()
    private val userUidsByLongId = ConcurrentHashMap<Long, String>()

    private val _chatSenderUidByMessageId = MutableStateFlow<Map<Long, String>>(emptyMap())
    val chatSenderUidByMessageId: StateFlow<Map<Long, String>> = _chatSenderUidByMessageId.asStateFlow()

    private val _userUidByLegacyId = MutableStateFlow<Map<Long, String>>(emptyMap())
    val userUidByLegacyId: StateFlow<Map<Long, String>> = _userUidByLegacyId.asStateFlow()

    private val _lastChatSentAtMs = MutableStateFlow(0L)
    fun getLastChatSentAt(): Long = _lastChatSentAtMs.value

    private val _likedCommunityQuoteDocIds = MutableStateFlow<Set<String>>(emptySet())
    val likedCommunityQuoteDocIds: StateFlow<Set<String>> = _likedCommunityQuoteDocIds.asStateFlow()

    private val _likedChatMessageDocIds = MutableStateFlow<Set<String>>(emptySet())
    val likedChatMessageDocIds: StateFlow<Set<String>> = _likedChatMessageDocIds.asStateFlow()

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun safeDocId(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(100).ifBlank { "item" }

    // ===============================================================
    // User / Admin Bootstrap & Session Initialization
    // ===============================================================
    suspend fun ensureUserInitialized(user: FirebaseUser): Result<Boolean> = withContext(Dispatchers.IO) {
        val uid = user.uid
        var isAdmin = false
        try {
            val adminRef = db.collection("admins").document(uid)
            val adminSnap = try {
                adminRef.get().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.GET, "admins/$uid", auth)
                null
            }

            if (adminSnap != null && adminSnap.exists()) {
                isAdmin = true
            } else if (
                user.isEmailVerified &&
                user.email.equals(PRIMARY_ADMIN_EMAIL, ignoreCase = true)
            ) {
                isAdmin = true
                try {
                    adminRef.set(
                        mapOf(
                            "uid" to uid,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.CREATE, "admins/$uid", auth)
                }
            }

            // Ensure private /users/{uid} doc exists
            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val displayName = (user.displayName ?: user.email?.substringBefore("@") ?: "Aşk Yolcusu")
                .trim()
                .ifBlank { "Aşk Yolcusu" }
                .take(100)
            val email = user.email.orEmpty().trim().take(160)

            val now = System.currentTimeMillis()
            val today = todayKey()
            var partnerName = "Sevgilim"
            var isPremium = false
            var vipPlan = ""
            var isBlocked = false
            var isMutedInChat = false

            if (!userSnap.exists()) {
                val createMap = FsUserDoc.initialCreateMap(
                    uid = uid,
                    fullName = displayName,
                    email = email,
                    todayKey = today,
                    defaultCounterStart = now - (1000L * 60 * 60 * 24 * 120)
                )
                userRef.set(createMap).await()
            } else {
                val existing = FsUserDoc.fromSnapshot(userSnap)
                partnerName = existing.partnerName
                isPremium = existing.isVip || existing.hasVipBadgeAndAi
                vipPlan = existing.vipPlanName
                isBlocked = existing.isBlocked
                isMutedInChat = existing.isRestricted && !existing.canChat &&
                    (existing.restrictedUntil == null || existing.restrictedUntil > now)

                // Check daily Love Streak update on login
                if (existing.streakLastDay != today) {
                    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calYesterday.time)
                    val nextStreak = if (existing.streakLastDay == yesterday) existing.loveStreak + 1 else 1
                    userRef.update(
                        mapOf(
                            "loveStreak" to nextStreak,
                            "streakLastDay" to today,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            // Ensure /public_profiles/{uid} exists & reflects latest role
            val profileRef = db.collection("public_profiles").document(uid)
            val profileSnap = profileRef.get().await()
            if (!profileSnap.exists()) {
                profileRef.set(
                    mapOf(
                        "uid" to uid,
                        "displayName" to displayName,
                        "partnerName" to partnerName,
                        "role" to if (isAdmin) "ADMIN" else "USER",
                        "isPremium" to isPremium,
                        "vipPlanName" to vipPlan.take(100),
                        "isBlocked" to false,
                        "isMutedInChat" to false,
                        "sharedQuotesCount" to 0,
                        "receivedHeartsCount" to 0,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val existingRole = profileSnap.getString("role") ?: "USER"
                val desiredRole = if (isAdmin) "ADMIN" else "USER"
                if (existingRole != desiredRole && isAdmin) {
                    profileRef.update(
                        mapOf(
                            "role" to desiredRole,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            // Seed default love_jar / countdowns / time_capsules if user's own subcollections are empty
            seedUserToolsIfEmpty(uid, displayName)

            // If Admin, ensure /app_config/admin_settings and sample broadcasts/chat exist
            if (isAdmin) {
                seedAdminDefaultsIfEmpty(uid)
            }

            Result.success(isAdmin)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid", auth)
            Result.failure(e)
        }
    }

    private suspend fun seedUserToolsIfEmpty(uid: String, displayName: String) {
        try {
            val jarSnap = db.collection("users").document(uid).collection("love_jar").limit(1).get().await()
            if (jarSnap.isEmpty) {
                val batch = db.batch()
                DefaultLoveJarReasons.initialReasons.take(8).forEachIndexed { idx, reason ->
                    val ref = db.collection("users").document(uid).collection("love_jar").document("init_$idx")
                    batch.set(
                        ref,
                        mapOf(
                            "reason" to reason.take(500),
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.commit().await()
            }

            val cdSnap = db.collection("users").document(uid).collection("countdowns").limit(1).get().await()
            if (cdSnap.isEmpty) {
                val now = System.currentTimeMillis()
                db.collection("users").document(uid).collection("countdowns").document("cd_1").set(
                    mapOf(
                        "title" to "Sevgililer Günü Buluşması",
                        "emoji" to "🌹",
                        "targetTimestamp" to (now + 1000L * 60 * 60 * 24 * 30),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                db.collection("users").document(uid).collection("countdowns").document("cd_2").set(
                    mapOf(
                        "title" to "Romantik Hafta Sonu Kaçamağı",
                        "emoji" to "✈️",
                        "targetTimestamp" to (now + 1000L * 60 * 60 * 24 * 12),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val tcSnap = db.collection("users").document(uid).collection("time_capsules").limit(1).get().await()
            if (tcSnap.isEmpty) {
                val now = System.currentTimeMillis()
                db.collection("users").document(uid).collection("time_capsules").document("tc_welcome").set(
                    mapOf(
                        "title" to "İlk Anı Kapsülümüz ✨",
                        "message" to "Bu uygulamayı kullanmaya başladığımız güzel günün hatırası: Birlikte nice mutlu anılar biriktirelim, her günümüz sevgiyle dolsun!",
                        "author" to displayName.take(100),
                        "unlockAt" to (now - 1000L),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "seedUserToolsIfEmpty skipped: ${e.message}")
        }
    }

    private suspend fun seedAdminDefaultsIfEmpty(adminUid: String) {
        try {
            val cfgRef = db.collection("app_config").document("admin_settings")
            if (!cfgRef.get().await().exists()) {
                cfgRef.set(FsMappers.adminSettingsToMap(AdminSettings())).await()
            }

            val bcSnap = db.collection("broadcasts").limit(1).get().await()
            if (bcSnap.isEmpty) {
                db.collection("broadcasts").add(
                    mapOf(
                        "title" to "Aşka Dair'e Hoş Geldiniz! 💖",
                        "message" to "Tüm verileriniz artık bulut hesabınızla canlı senkronize! Yeni Mesaj Geçmişi ve Premium rozetlerini sohbet bölümünde keşfedin.",
                        "category" to "Duyuru",
                        "emoji" to "💌",
                        "sentBy" to "Yönetici",
                        "sentAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val chatSnap = db.collection("chat_messages").limit(1).get().await()
            if (chatSnap.isEmpty) {
                val rooms = listOf(
                    Triple(
                        ChatRoom.GENEL.id,
                        "Aşka Dair Topluluk Sohbetine hoş geldiniz! Sevgi dolu, saygılı ve samimi sohbetler dileriz. Tüm oda kuralları ve moderasyon Yönetici ekibimiz güvencesindedir. 💖",
                        true
                    ),
                    Triple(
                        ChatRoom.SIIR_SOZ.id,
                        "Kendi yazdığınız dizeleri ve sevdiğinize özel kısa şiirleri burada paylaşabilirsiniz ✍️",
                        true
                    )
                )
                rooms.forEach { (rId, msg, pinned) ->
                    db.collection("chat_messages").add(
                        hashMapOf(
                            "roomId" to rId,
                            "senderUid" to adminUid,
                            "senderName" to "Yönetici 👑",
                            "senderRole" to "ADMIN",
                            "isPremiumSender" to true,
                            "vipPlanName" to "Yönetici",
                            "message" to msg,
                            "replyToMessageId" to null,
                            "replyToSenderName" to null,
                            "replyToSnippet" to null,
                            "status" to "PUBLISHED",
                            "isPinned" to pinned,
                            "likesCount" to 0,
                            "moderationNote" to null,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "seedAdminDefaultsIfEmpty skipped: ${e.message}")
        }
    }

    // ===============================================================
    // Real-time Listeners
    // ===============================================================
    private fun <T> observeQuery(
        query: Query,
        pathLabel: String,
        transform: (List<DocumentSnapshot>) -> T
    ): Flow<T> = callbackFlow {
        val reg: ListenerRegistration = query.addSnapshotListener { snap, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, pathLabel, auth)
                return@addSnapshotListener
            }
            if (snap != null) {
                try {
                    trySend(transform(snap.documents))
                } catch (e: Exception) {
                    Log.e("FirestoreRepository", "Transform error on $pathLabel", e)
                }
            }
        }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        handleFirestoreError(err, OperationType.LIST, pathLabel, auth)
    }

    fun observeIsAdmin(uid: String): Flow<Boolean> = callbackFlow {
        val reg = db.collection("admins").document(uid).addSnapshotListener { snap, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, "admins/$uid", auth)
                return@addSnapshotListener
            }
            val currentUser = auth.currentUser
            val emailIsPrimaryAdmin = currentUser?.isEmailVerified == true &&
                currentUser.email.equals(PRIMARY_ADMIN_EMAIL, ignoreCase = true)
            trySend((snap?.exists() == true) || emailIsPrimaryAdmin)
        }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        handleFirestoreError(err, OperationType.GET, "admins/$uid", auth)
        emit(false)
    }

    fun observeUserDoc(uid: String): Flow<FsUserDoc?> = callbackFlow {
        val reg = db.collection("users").document(uid).addSnapshotListener { snap, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, "users/$uid", auth)
                return@addSnapshotListener
            }
            if (snap != null && snap.exists()) {
                trySend(FsUserDoc.fromSnapshot(snap))
            } else {
                trySend(null)
            }
        }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        handleFirestoreError(err, OperationType.GET, "users/$uid", auth)
    }

    fun observeAdminSettings(): Flow<AdminSettings> = callbackFlow {
        val reg = db.collection("app_config").document("admin_settings").addSnapshotListener { snap, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, "app_config/admin_settings", auth)
                return@addSnapshotListener
            }
            trySend(if (snap != null && snap.exists()) FsMappers.adminSettingsFromSnapshot(snap) else AdminSettings())
        }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        handleFirestoreError(err, OperationType.GET, "app_config/admin_settings", auth)
        emit(AdminSettings())
    }

    fun observePublicProfiles(): Flow<Map<String, FsPublicProfile>> =
        observeQuery(db.collection("public_profiles"), "public_profiles") { docs ->
            docs.associate { doc ->
                val profile = FsPublicProfile.fromSnapshot(doc)
                profile.uid to profile
            }
        }

    fun observeAllUsersForAdmin(): Flow<List<UserAccountEntity>> =
        observeQuery(
            db.collection("users").orderBy("createdAt", Query.Direction.DESCENDING),
            "users"
        ) { docs ->
            val uidMap = HashMap<Long, String>()
            val list = docs.map { doc ->
                val userDoc = FsUserDoc.fromSnapshot(doc)
                val legacyId = stableLongIdFromDocId(doc.id)
                userUidsByLongId[legacyId] = doc.id
                uidMap[legacyId] = doc.id
                userDoc.toUserAccountEntity(legacyId)
            }
            _userUidByLegacyId.value = uidMap
            list
        }

    fun observeNotes(uid: String): Flow<List<NoteEntity>> =
        observeQuery(
            db.collection("users").document(uid).collection("notes")
                .orderBy("updatedAt", Query.Direction.DESCENDING),
            "users/$uid/notes"
        ) { docs ->
            docs.map { doc ->
                val entity = FsMappers.noteFromSnapshot(doc)
                noteDocIds[entity.id] = doc.id
                entity
            }
        }

    fun observeJournalEntries(uid: String): Flow<List<EmotionalJournalEntry>> =
        observeQuery(
            db.collection("users").document(uid).collection("journal_entries")
                .orderBy("dateKey", Query.Direction.DESCENDING),
            "users/$uid/journal_entries"
        ) { docs ->
            docs.map { doc ->
                val entry = FsMappers.journalEntryFromSnapshot(doc)
                journalDocIds[entry.id] = doc.id
                entry
            }.sortedWith(
                compareByDescending<EmotionalJournalEntry> { it.dateKey }
                    .thenByDescending { it.memoryTimestamp }
                    .thenByDescending { it.updatedAt }
            )
        }

    fun observeCuratedFavoriteIds(uid: String): Flow<List<String>> =
        observeQuery(
            db.collection("users").document(uid).collection("favorites")
                .whereEqualTo("kind", "CURATED"),
            "users/$uid/favorites"
        ) { docs ->
            docs.mapNotNull { it.getString("quoteId") }
        }

    fun observeSavedFavoriteQuotes(uid: String): Flow<List<SavedFavoriteQuoteEntity>> =
        observeQuery(
            db.collection("users").document(uid).collection("favorites")
                .whereEqualTo("kind", "SAVED"),
            "users/$uid/favorites"
        ) { docs ->
            docs.map { doc ->
                val entity = FsMappers.savedFavoriteFromSnapshot(doc)
                savedFavDocIds[entity.id] = doc.id
                entity
            }.sortedByDescending { it.createdAt }
        }

    fun observeReminders(uid: String): Flow<List<ReminderEntity>> =
        observeQuery(
            db.collection("users").document(uid).collection("reminders")
                .orderBy("targetTimestamp", Query.Direction.ASCENDING),
            "users/$uid/reminders"
        ) { docs ->
            val list = docs.map { doc ->
                val entity = FsMappers.reminderFromSnapshot(doc)
                reminderDocIds[entity.id] = doc.id
                entity
            }
            saveLocalAlarmCache(list)
            list
        }

    fun observeMusicTracks(uid: String): Flow<List<CustomMusicTrackEntity>> =
        observeQuery(
            db.collection("users").document(uid).collection("music_tracks")
                .orderBy("createdAt", Query.Direction.DESCENDING),
            "users/$uid/music_tracks"
        ) { docs ->
            docs.map { doc ->
                val entity = FsMappers.musicTrackFromSnapshot(doc)
                musicDocIds[entity.id] = doc.id
                entity
            }
        }

    fun observeEngagementNotifications(uid: String): Flow<List<EngagementNotificationEntity>> =
        observeQuery(
            db.collection("users").document(uid).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50),
            "users/$uid/notifications"
        ) { docs ->
            docs.map { doc ->
                val entity = FsMappers.engagementNotifFromSnapshot(doc)
                notifDocIds[entity.id] = doc.id
                entity
            }
        }

    fun observeTimeCapsules(uid: String): Flow<List<LoveTimeCapsule>> =
        observeQuery(
            db.collection("users").document(uid).collection("time_capsules")
                .orderBy("createdAt", Query.Direction.DESCENDING),
            "users/$uid/time_capsules"
        ) { docs ->
            docs.map { FsMappers.timeCapsuleFromSnapshot(it) }
        }

    fun observeCountdowns(uid: String): Flow<List<SpecialCountdownEvent>> =
        observeQuery(
            db.collection("users").document(uid).collection("countdowns")
                .orderBy("targetTimestamp", Query.Direction.ASCENDING),
            "users/$uid/countdowns"
        ) { docs ->
            docs.map { FsMappers.countdownFromSnapshot(it) }
        }

    fun observeLoveJar(uid: String): Flow<List<Pair<String, String>>> =
        observeQuery(
            db.collection("users").document(uid).collection("love_jar")
                .orderBy("createdAt", Query.Direction.DESCENDING),
            "users/$uid/love_jar"
        ) { docs ->
            docs.mapNotNull { doc ->
                val r = doc.getString("reason") ?: return@mapNotNull null
                doc.id to r
            }
        }

    fun observePurchases(uid: String): Flow<List<PurchaseRecord>> =
        observeQuery(
            db.collection("users").document(uid).collection("purchases")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(25),
            "users/$uid/purchases"
        ) { docs ->
            docs.map { FsMappers.purchaseFromSnapshot(it) }
        }

    fun observeAdminQuotes(): Flow<List<AdminQuoteEntity>> =
        observeQuery(
            db.collection("admin_quotes").orderBy("createdAt", Query.Direction.DESCENDING),
            "admin_quotes"
        ) { docs ->
            docs.map { doc ->
                val entity = FsMappers.adminQuoteFromSnapshot(doc)
                adminQuoteDocIds[entity.id] = doc.id
                entity
            }
        }

    fun observeBroadcasts(readBroadcastIdsFlow: Flow<Set<String>>): Flow<List<BroadcastNotificationEntity>> =
        combine(
            observeQuery(
                db.collection("broadcasts").orderBy("sentAt", Query.Direction.DESCENDING),
                "broadcasts"
            ) { docs -> docs },
            readBroadcastIdsFlow
        ) { docs, readIds ->
            docs.map { doc ->
                val entity = FsMappers.broadcastFromSnapshot(doc, readIds)
                broadcastDocIds[entity.id] = doc.id
                entity
            }
        }

    fun observeSupportMessages(uid: String, isAdmin: Boolean): Flow<List<AdminMessageEntity>> {
        val query = if (isAdmin) {
            db.collection("support_messages").orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            db.collection("support_messages")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }
        return observeQuery(query, "support_messages") { docs ->
            docs.map { doc ->
                val entity = FsMappers.supportMessageFromSnapshot(doc)
                supportMsgDocIds[entity.id] = doc.id
                entity
            }
        }
    }

    fun observeCommunityQuotes(uid: String, isAdmin: Boolean): Flow<List<CommunityQuoteEntity>> {
        if (isAdmin) {
            return observeQuery(
                db.collection("community_quotes").orderBy("createdAt", Query.Direction.DESCENDING),
                "community_quotes"
            ) { docs ->
                docs.map { doc ->
                    val entity = FsMappers.communityQuoteFromSnapshot(doc)
                    communityQuoteDocIds[entity.id] = doc.id
                    entity
                }
            }
        }
        val approvedFlow = observeQuery(
            db.collection("community_quotes")
                .whereEqualTo("status", "APPROVED")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(60),
            "community_quotes(approved)"
        ) { it }

        val ownFlow = observeQuery(
            db.collection("community_quotes")
                .whereEqualTo("authorUid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(40),
            "community_quotes(own)"
        ) { it }

        return combine(approvedFlow, ownFlow) { approvedDocs, ownDocs ->
            val merged = LinkedHashMap<String, DocumentSnapshot>()
            approvedDocs.forEach { merged[it.id] = it }
            ownDocs.forEach { merged[it.id] = it }
            merged.values.map { doc ->
                val entity = FsMappers.communityQuoteFromSnapshot(doc)
                communityQuoteDocIds[entity.id] = doc.id
                entity
            }.sortedByDescending { it.createdAt }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeCommunityCommentsForQuotes(quotesFlow: Flow<List<CommunityQuoteEntity>>): Flow<Map<Long, List<CommunityCommentEntity>>> {
        return quotesFlow.map { quotes ->
            quotes.filter { it.status == "APPROVED" }
                .take(20)
                .mapNotNull { q -> communityQuoteDocIds[q.id]?.let { q.id to it } }
        }.flatMapLatest { pairs ->
            if (pairs.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val perQuoteFlows = pairs.map { (quoteLongId, quoteDocId) ->
                    observeQuery(
                        db.collection("community_quotes")
                            .document(quoteDocId)
                            .collection("comments")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .limit(20),
                        "community_quotes/$quoteDocId/comments"
                    ) { docs ->
                        quoteLongId to docs.map { doc ->
                            val c = FsMappers.communityCommentFromSnapshot(quoteDocId, doc)
                            commentDocIds[c.id] = doc.id
                            commentParentQuoteDocIds[c.id] = quoteDocId
                            c
                        }
                    }
                }
                combine(perQuoteFlows) { results ->
                    results.toMap()
                }
            }
        }
    }

    fun observeLiveChatMessages(uid: String, isAdmin: Boolean): Flow<List<ChatMessageEntity>> {
        if (isAdmin) {
            return observeQuery(
                db.collection("chat_messages")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(120),
                "chat_messages"
            ) { docs ->
                val senderMap = HashMap<Long, String>()
                val list = docs.map { doc ->
                    val entity = FsMappers.chatMessageFromSnapshot(doc)
                    chatDocIds[entity.id] = doc.id
                    doc.getString("senderUid")?.takeIf { it.isNotBlank() }?.let { sUid ->
                        senderMap[entity.id] = sUid
                    }
                    entity
                }
                _chatSenderUidByMessageId.value = _chatSenderUidByMessageId.value + senderMap
                list
            }
        }
        val publishedFlow = observeQuery(
            db.collection("chat_messages")
                .whereEqualTo("status", "PUBLISHED")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(80),
            "chat_messages(published)"
        ) { it }

        val ownFlow = observeQuery(
            db.collection("chat_messages")
                .whereEqualTo("senderUid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30),
            "chat_messages(own)"
        ) { it }

        return combine(publishedFlow, ownFlow) { pubDocs, ownDocs ->
            val merged = LinkedHashMap<String, DocumentSnapshot>()
            pubDocs.forEach { merged[it.id] = it }
            ownDocs.forEach { merged[it.id] = it }
            val senderMap = HashMap<Long, String>()
            val list = merged.values.map { doc ->
                val entity = FsMappers.chatMessageFromSnapshot(doc)
                chatDocIds[entity.id] = doc.id
                doc.getString("senderUid")?.takeIf { it.isNotBlank() }?.let { sUid ->
                    senderMap[entity.id] = sUid
                }
                entity
            }.sortedByDescending { it.createdAt }
            _chatSenderUidByMessageId.value = _chatSenderUidByMessageId.value + senderMap
            list
        }
    }

    // ===============================================================
    // Paginated Chat History ("Mesaj Geçmişi")
    // ===============================================================
    suspend fun loadChatHistoryPage(
        roomId: String,
        cursor: DocumentSnapshot?,
        isAdmin: Boolean,
        pageSize: Long = 20L
    ): Result<ChatHistoryPageResult> = withContext(Dispatchers.IO) {
        try {
            var q: Query = db.collection("chat_messages")
                .whereEqualTo("roomId", roomId)
            if (!isAdmin) {
                q = q.whereEqualTo("status", "PUBLISHED")
            }
            q = q.orderBy("createdAt", Query.Direction.DESCENDING).limit(pageSize)
            if (cursor != null) {
                q = q.startAfter(cursor)
            }
            val snap = q.get().await()
            val senderMap = HashMap<Long, String>()
            val items = snap.documents.map { doc ->
                val entity = FsMappers.chatMessageFromSnapshot(doc)
                chatDocIds[entity.id] = doc.id
                doc.getString("senderUid")?.takeIf { it.isNotBlank() }?.let { sUid ->
                    senderMap[entity.id] = sUid
                }
                entity
            }
            if (senderMap.isNotEmpty()) {
                _chatSenderUidByMessageId.value = _chatSenderUidByMessageId.value + senderMap
            }
            Result.success(
                ChatHistoryPageResult(
                    messages = items,
                    nextCursor = snap.documents.lastOrNull(),
                    hasMore = snap.documents.size >= pageSize
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "chat_messages(history:$roomId)", auth)
            Result.failure(e)
        }
    }

    // ===============================================================
    // Notes CRUD
    // ===============================================================
    suspend fun saveNote(uid: String, note: NoteEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val existingDocId = if (note.id != 0L) noteDocIds[note.id] else null
            val col = db.collection("users").document(uid).collection("notes")
            val docRef = if (existingDocId != null) col.document(existingDocId) else col.document()
            val payload = hashMapOf<String, Any?>(
                "title" to note.title.trim().ifBlank { "Romantik Not" }.take(200),
                "content" to note.content.trim().ifBlank { "Not" }.take(5000),
                "author" to note.author.trim().ifBlank { "Ben" }.take(100),
                "tags" to note.tags.take(300),
                "voiceAudioPath" to note.voiceAudioPath?.take(500),
                "voiceDurationSeconds" to note.voiceDurationSeconds.coerceIn(0, 3600),
                "isFavorite" to note.isFavorite,
                "backgroundTheme" to note.backgroundTheme.ifBlank { "ruby_velvet" }.take(60),
                "filterStyle" to note.filterStyle.ifBlank { "normal" }.take(60),
                "isSyncedWithCloud" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingDocId == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                val snap = docRef.get().await()
                val existingCreatedAt = snap.getTimestamp("createdAt")
                if (existingCreatedAt != null) {
                    payload["createdAt"] = existingCreatedAt
                    docRef.set(payload).await()
                } else {
                    payload["createdAt"] = FieldValue.serverTimestamp()
                    docRef.set(payload).await()
                }
            }
            val newLongId = stableLongIdFromDocId(docRef.id)
            noteDocIds[newLongId] = docRef.id
            Result.success(newLongId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/notes", auth)
            Result.failure(e)
        }
    }

    suspend fun deleteNoteById(uid: String, id: Long) = withContext(Dispatchers.IO) {
        val docId = noteDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).collection("notes").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/notes/$docId", auth)
        }
    }

    // ===============================================================
    // Private Emotional Journal ("Duygusal Günlük") CRUD
    // ===============================================================
    suspend fun saveJournalEntry(
        uid: String,
        entry: EmotionalJournalEntry
    ): Result<EmotionalJournalEntry> = withContext(Dispatchers.IO) {
        val cleanTitle = entry.title.trim().take(120)
        val cleanContent = entry.content.trim().take(6000)
        if (cleanTitle.isBlank()) {
            return@withContext Result.failure(Exception("Lütfen anınız için kısa bir başlık yazın."))
        }
        if (cleanContent.isBlank()) {
            return@withContext Result.failure(Exception("Lütfen anı veya duygu notunuzu yazın."))
        }
        val normalizedTs = JournalDateHelper.normalizeDateMillis(entry.memoryTimestamp)
        val safeDateKey = if (entry.dateKey.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            entry.dateKey
        } else {
            JournalDateHelper.toDateKey(normalizedTs)
        }
        val safeMood = JournalMood.fromId(entry.moodId).id
        val cleanLocation = entry.locationOrMoment.trim().take(100)

        try {
            val existingDocId = entry.docId.takeIf { it.isNotBlank() }
                ?: journalDocIds[entry.id]
            val col = db.collection("users").document(uid).collection("journal_entries")
            val docRef = if (existingDocId != null) col.document(existingDocId) else col.document()

            if (existingDocId == null) {
                val createPayload = hashMapOf<String, Any>(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "content" to cleanContent,
                    "moodId" to safeMood,
                    "dateKey" to safeDateKey,
                    "memoryTimestamp" to normalizedTs,
                    "locationOrMoment" to cleanLocation,
                    "isFavorite" to entry.isFavorite,
                    "isPrivateLocked" to entry.isPrivateLocked,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = hashMapOf<String, Any>(
                    "title" to cleanTitle,
                    "content" to cleanContent,
                    "moodId" to safeMood,
                    "dateKey" to safeDateKey,
                    "memoryTimestamp" to normalizedTs,
                    "locationOrMoment" to cleanLocation,
                    "isFavorite" to entry.isFavorite,
                    "isPrivateLocked" to entry.isPrivateLocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }

            val resolvedLongId = stableLongIdFromDocId(docRef.id)
            journalDocIds[resolvedLongId] = docRef.id
            val now = System.currentTimeMillis()
            Result.success(
                entry.copy(
                    id = resolvedLongId,
                    docId = docRef.id,
                    title = cleanTitle,
                    content = cleanContent,
                    moodId = safeMood,
                    dateKey = safeDateKey,
                    memoryTimestamp = normalizedTs,
                    locationOrMoment = cleanLocation,
                    updatedAt = now,
                    createdAt = if (existingDocId == null) now else entry.createdAt
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/journal_entries", auth)
            Result.failure(e)
        }
    }

    suspend fun toggleJournalFavorite(
        uid: String,
        entry: EmotionalJournalEntry
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val docId = entry.docId.takeIf { it.isNotBlank() } ?: journalDocIds[entry.id]
            ?: return@withContext Result.failure(Exception("Günlük kaydı bulunamadı."))
        val nextVal = !entry.isFavorite
        try {
            db.collection("users").document(uid)
                .collection("journal_entries").document(docId)
                .update(
                    mapOf(
                        "isFavorite" to nextVal,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(nextVal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/journal_entries/$docId", auth)
            Result.failure(e)
        }
    }

    suspend fun toggleJournalPrivateLock(
        uid: String,
        entry: EmotionalJournalEntry
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val docId = entry.docId.takeIf { it.isNotBlank() } ?: journalDocIds[entry.id]
            ?: return@withContext Result.failure(Exception("Günlük kaydı bulunamadı."))
        val nextVal = !entry.isPrivateLocked
        try {
            db.collection("users").document(uid)
                .collection("journal_entries").document(docId)
                .update(
                    mapOf(
                        "isPrivateLocked" to nextVal,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(nextVal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/journal_entries/$docId", auth)
            Result.failure(e)
        }
    }

    suspend fun deleteJournalEntry(
        uid: String,
        entry: EmotionalJournalEntry
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val docId = entry.docId.takeIf { it.isNotBlank() } ?: journalDocIds[entry.id]
            ?: return@withContext Result.failure(Exception("Günlük kaydı bulunamadı."))
        try {
            db.collection("users").document(uid)
                .collection("journal_entries").document(docId)
                .delete().await()
            journalDocIds.remove(entry.id)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/journal_entries/$docId", auth)
            Result.failure(e)
        }
    }

    // ===============================================================
    // Favorites CRUD
    // ===============================================================
    suspend fun toggleQuoteFavorite(uid: String, quoteId: String) = withContext(Dispatchers.IO) {
        val docId = "cur_${safeDocId(quoteId)}"
        val ref = db.collection("users").document(uid).collection("favorites").document(docId)
        try {
            val snap = ref.get().await()
            if (snap.exists()) {
                ref.delete().await()
            } else {
                ref.set(
                    hashMapOf(
                        "quoteId" to quoteId.take(120),
                        "title" to "",
                        "text" to "",
                        "author" to "",
                        "category" to "",
                        "sourceLabel" to "Kürasyon",
                        "kind" to "CURATED",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/favorites/$docId", auth)
        }
    }

    suspend fun addSavedFavoriteQuote(
        uid: String,
        title: String,
        text: String,
        author: String,
        category: String,
        sourceLabel: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanText = text.trim().take(2000)
        if (cleanText.isBlank()) return@withContext Result.failure(Exception("Söz metni boş olamaz."))
        try {
            val hash = stableLongIdFromDocId(cleanText.lowercase())
            val docId = "sav_$hash"
            val ref = db.collection("users").document(uid).collection("favorites").document(docId)
            if (ref.get().await().exists()) {
                return@withContext Result.success(false)
            }
            ref.set(
                hashMapOf(
                    "quoteId" to docId,
                    "title" to title.ifBlank { "Favori Sözüm" }.take(200),
                    "text" to cleanText,
                    "author" to author.ifBlank { "Anonim" }.take(120),
                    "category" to category.ifBlank { "Romantik" }.take(80),
                    "sourceLabel" to sourceLabel.ifBlank { "Kişisel Favori" }.take(80),
                    "kind" to "SAVED",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/favorites", auth)
            Result.failure(e)
        }
    }

    suspend fun removeSavedFavoriteQuote(uid: String, id: Long) = withContext(Dispatchers.IO) {
        val docId = savedFavDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).collection("favorites").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/favorites/$docId", auth)
        }
    }

    // ===============================================================
    // Reminders CRUD + Local Boot Cache
    // ===============================================================
    private fun saveLocalAlarmCache(reminders: List<ReminderEntity>) {
        try {
            val arr = org.json.JSONArray()
            reminders.filter { !it.isCompleted && it.isNotificationEnabled }.forEach { r ->
                arr.put(
                    org.json.JSONObject().apply {
                        put("id", r.id)
                        put("title", r.title)
                        put("note", r.note)
                        put("targetTimestamp", r.targetTimestamp)
                    }
                )
            }
            context.getSharedPreferences(ALARM_CACHE_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ALARM_CACHE_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    suspend fun saveReminder(uid: String, reminder: ReminderEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val existingDocId = if (reminder.id != 0L) reminderDocIds[reminder.id] else null
            val col = db.collection("users").document(uid).collection("reminders")
            val ref = if (existingDocId != null) col.document(existingDocId) else col.document()
            val payload = hashMapOf<String, Any?>(
                "title" to reminder.title.trim().ifBlank { "Hatırlatıcı" }.take(200),
                "note" to reminder.note.take(1000),
                "targetTimestamp" to reminder.targetTimestamp,
                "reminderType" to reminder.reminderType.ifBlank { "Özel Gün" }.take(60),
                "isCompleted" to reminder.isCompleted,
                "isNotificationEnabled" to reminder.isNotificationEnabled
            )
            if (existingDocId == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                val oldTs = ref.get().await().getTimestamp("createdAt")
                payload["createdAt"] = oldTs ?: FieldValue.serverTimestamp()
            }
            ref.set(payload).await()
            val longId = stableLongIdFromDocId(ref.id)
            reminderDocIds[longId] = ref.id
            Result.success(longId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/reminders", auth)
            Result.failure(e)
        }
    }

    suspend fun setReminderCompleted(uid: String, id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        val docId = reminderDocIds[id] ?: return@withContext
        try {
            val ref = db.collection("users").document(uid).collection("reminders").document(docId)
            ref.update("isCompleted", completed).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/reminders/$docId", auth)
        }
    }

    suspend fun deleteReminderById(uid: String, id: Long) = withContext(Dispatchers.IO) {
        val docId = reminderDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).collection("reminders").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/reminders/$docId", auth)
        }
    }

    // ===============================================================
    // Music Tracks CRUD
    // ===============================================================
    suspend fun addCustomMusicTrack(
        uid: String,
        title: String,
        artist: String,
        filePath: String,
        addedBy: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val ref = db.collection("users").document(uid).collection("music_tracks").document()
            ref.set(
                hashMapOf(
                    "title" to title.trim().ifBlank { "Romantik Fon Müziği" }.take(150),
                    "artist" to artist.trim().ifBlank { "Kişisel Kütüphane" }.take(150),
                    "filePath" to filePath.take(600),
                    "presetKey" to null,
                    "addedBy" to addedBy.trim().ifBlank { "Kullanıcı" }.take(100),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val longId = stableLongIdFromDocId(ref.id)
            musicDocIds[longId] = ref.id
            Result.success(longId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/music_tracks", auth)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomMusicTrack(uid: String, id: Long, filePath: String) = withContext(Dispatchers.IO) {
        if (filePath.isNotBlank()) {
            try {
                File(filePath).delete()
            } catch (_: Exception) {
            }
        }
        val docId = musicDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).collection("music_tracks").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/music_tracks/$docId", auth)
        }
    }

    // ===============================================================
    // User Doc Field Updates (Settings, Profile, Mood, Tools, Monetization)
    // ===============================================================
    suspend fun updateUserFields(uid: String, fields: Map<String, Any?>) = withContext(Dispatchers.IO) {
        try {
            val merged = fields.toMutableMap()
            merged["updatedAt"] = FieldValue.serverTimestamp()
            db.collection("users").document(uid).update(merged).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid", auth)
        }
    }

    suspend fun updatePublicProfileStats(
        uid: String,
        displayName: String,
        partnerName: String,
        isAdmin: Boolean,
        isPremium: Boolean,
        vipPlanName: String,
        sharedQuotesCount: Int,
        receivedHeartsCount: Int
    ) = withContext(Dispatchers.IO) {
        try {
            db.collection("public_profiles").document(uid).update(
                mapOf(
                    "displayName" to displayName.trim().ifBlank { "Kullanıcı" }.take(100),
                    "partnerName" to partnerName.trim().ifBlank { "Sevgilim" }.take(100),
                    "role" to if (isAdmin) "ADMIN" else "USER",
                    "isPremium" to isPremium,
                    "vipPlanName" to vipPlanName.take(100),
                    "sharedQuotesCount" to sharedQuotesCount.coerceAtLeast(0),
                    "receivedHeartsCount" to receivedHeartsCount.coerceAtLeast(0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "public_profiles/$uid", auth)
        }
    }

    suspend fun updateMoodCheckIn(
        uid: String,
        currentHistoryJson: String,
        label: String,
        emoji: String,
        score: Int
    ) = withContext(Dispatchers.IO) {
        val today = todayKey()
        val historyObj = try {
            org.json.JSONObject(currentHistoryJson.ifBlank { "{}" })
        } catch (_: Exception) {
            org.json.JSONObject()
        }
        historyObj.put(
            today,
            org.json.JSONObject().apply {
                put("label", label)
                put("emoji", emoji)
                put("score", score)
            }
        )
        val keys = historyObj.keys().asSequence().toList().sorted()
        if (keys.size > 45) {
            keys.take(keys.size - 45).forEach { historyObj.remove(it) }
        }
        updateUserFields(
            uid,
            mapOf(
                "todayMoodLabel" to label.take(60),
                "todayMoodEmoji" to emoji.take(16),
                "todayMoodScore" to score.coerceIn(0, 100),
                "todayMoodDate" to today,
                "moodCheckInHistoryJson" to historyObj.toString().take(15000)
            )
        )
    }

    suspend fun addLoveJarReason(uid: String, reason: String) = withContext(Dispatchers.IO) {
        val clean = reason.trim().take(500)
        if (clean.isBlank()) return@withContext
        try {
            db.collection("users").document(uid).collection("love_jar").add(
                mapOf(
                    "reason" to clean,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/love_jar", auth)
        }
    }

    suspend fun deleteLoveJarReason(uid: String, docId: String) = withContext(Dispatchers.IO) {
        try {
            db.collection("users").document(uid).collection("love_jar").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/love_jar/$docId", auth)
        }
    }

    suspend fun createTimeCapsule(uid: String, title: String, message: String, author: String, unlockDelayMillis: Long) =
        withContext(Dispatchers.IO) {
            val cleanMsg = message.trim().take(3000)
            if (cleanMsg.isBlank()) return@withContext
            val unlockAt = System.currentTimeMillis() + unlockDelayMillis.coerceAtLeast(1000L)
            try {
                db.collection("users").document(uid).collection("time_capsules").add(
                    mapOf(
                        "title" to title.trim().ifBlank { "Geleceğe Mektup" }.take(150),
                        "message" to cleanMsg,
                        "author" to author.trim().ifBlank { "Ben" }.take(100),
                        "unlockAt" to unlockAt,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "users/$uid/time_capsules", auth)
            }
        }

    suspend fun deleteTimeCapsule(uid: String, docId: String) = withContext(Dispatchers.IO) {
        try {
            db.collection("users").document(uid).collection("time_capsules").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/time_capsules/$docId", auth)
        }
    }

    suspend fun addCountdown(uid: String, title: String, emoji: String, daysFromNow: Int) = withContext(Dispatchers.IO) {
        val clean = title.trim().take(150)
        if (clean.isBlank()) return@withContext
        val target = System.currentTimeMillis() + (daysFromNow.coerceAtLeast(1) * 24L * 60L * 60L * 1000L)
        try {
            db.collection("users").document(uid).collection("countdowns").add(
                mapOf(
                    "title" to clean,
                    "emoji" to emoji.trim().ifBlank { "💖" }.take(16),
                    "targetTimestamp" to target,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/countdowns", auth)
        }
    }

    suspend fun deleteCountdown(uid: String, docId: String) = withContext(Dispatchers.IO) {
        try {
            db.collection("users").document(uid).collection("countdowns").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/countdowns/$docId", auth)
        }
    }

    // ===============================================================
    // Cloud Backup & Restore (Inside Firestore /users/{uid})
    // ===============================================================
    suspend fun createBackupJson(
        notes: List<NoteEntity>,
        curatedFavIds: List<String>,
        reminders: List<ReminderEntity>
    ): String = withContext(Dispatchers.Default) {
        val payload = BackupPayload(
            exportTimestamp = System.currentTimeMillis(),
            notes = notes.map {
                BackupNoteItem(
                    id = it.id,
                    title = it.title,
                    content = it.content,
                    author = it.author,
                    tags = it.tags,
                    isFavorite = it.isFavorite,
                    backgroundTheme = it.backgroundTheme,
                    filterStyle = it.filterStyle,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            },
            favoriteQuoteIds = curatedFavIds,
            reminders = reminders.map {
                BackupReminderItem(
                    id = it.id,
                    title = it.title,
                    note = it.note,
                    targetTimestamp = it.targetTimestamp,
                    reminderType = it.reminderType,
                    isCompleted = it.isCompleted
                )
            }
        )
        backupAdapter.indent("  ").toJson(payload)
    }

    suspend fun syncWithCloud(
        uid: String,
        notes: List<NoteEntity>,
        curatedFavIds: List<String>,
        savedFavCount: Int,
        reminders: List<ReminderEntity>,
        entitlement: EntitlementState
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val totalItems = notes.size + curatedFavIds.size + savedFavCount + reminders.size
            val freeLimit = MonetizationCatalog.FREE_CLOUD_BACKUP_LIMIT
            val snapshotJson = createBackupJson(notes, curatedFavIds, reminders).take(350_000)
            val now = System.currentTimeMillis()
            val formatted = SimpleDateFormat("dd MMM HH:mm", Locale("tr")).format(Date(now))

            val backedNotes = if (entitlement.hasUnlimitedCloudBackup) notes.size else notes.size.coerceAtMost(freeLimit)
            val backedFavs = if (entitlement.hasUnlimitedCloudBackup) (curatedFavIds.size + savedFavCount)
            else (curatedFavIds.size + savedFavCount).coerceAtMost(freeLimit)
            val backedRem = if (entitlement.hasUnlimitedCloudBackup) reminders.size else reminders.size.coerceAtMost(freeLimit)

            updateUserFields(
                uid,
                mapOf(
                    "lastSyncAt" to now,
                    "syncedNotesCount" to backedNotes,
                    "syncedFavsCount" to backedFavs,
                    "syncedRemindersCount" to backedRem,
                    "lastBackupJson" to snapshotJson
                )
            )

            if (!entitlement.hasUnlimitedCloudBackup && totalItems > freeLimit) {
                Result.success(
                    "Ücretsiz $freeLimit kayıt kotasına kadar buluta yedeklendi ($formatted). Tüm $totalItems kayıt için Premium Sınırsız Yedekleme'ye geçebilirsiniz!"
                )
            } else {
                Result.success(
                    "Firebase buluta yedeklendi ($formatted) • $backedNotes not, $backedFavs favori, $backedRem hatırlatıcı"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromJson(uid: String, jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val payload = backupAdapter.fromJson(jsonString)
                ?: return@withContext Result.failure(Exception("Geçersiz yedek dosyası biçimi"))

            var count = 0
            val batch = db.batch()
            val userRef = db.collection("users").document(uid)

            payload.notes.take(150).forEach { n ->
                val ref = userRef.collection("notes").document()
                batch.set(
                    ref,
                    hashMapOf(
                        "title" to n.title.ifBlank { "Romantik Not" }.take(200),
                        "content" to n.content.ifBlank { "Not" }.take(5000),
                        "author" to n.author.ifBlank { "Ben" }.take(100),
                        "tags" to n.tags.take(300),
                        "voiceAudioPath" to null,
                        "voiceDurationSeconds" to 0,
                        "isFavorite" to n.isFavorite,
                        "backgroundTheme" to n.backgroundTheme.ifBlank { "ruby_velvet" }.take(60),
                        "filterStyle" to n.filterStyle.ifBlank { "normal" }.take(60),
                        "isSyncedWithCloud" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                count++
            }
            payload.favoriteQuoteIds.take(150).forEach { qId ->
                val docId = "cur_${safeDocId(qId)}"
                val ref = userRef.collection("favorites").document(docId)
                batch.set(
                    ref,
                    hashMapOf(
                        "quoteId" to qId.take(120),
                        "title" to "",
                        "text" to "",
                        "author" to "",
                        "category" to "",
                        "sourceLabel" to "Kürasyon",
                        "kind" to "CURATED",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                count++
            }
            payload.reminders.take(150).forEach { r ->
                val ref = userRef.collection("reminders").document()
                batch.set(
                    ref,
                    hashMapOf(
                        "title" to r.title.ifBlank { "Hatırlatıcı" }.take(200),
                        "note" to r.note.take(1000),
                        "targetTimestamp" to r.targetTimestamp,
                        "reminderType" to r.reminderType.ifBlank { "Özel Gün" }.take(60),
                        "isCompleted" to r.isCompleted,
                        "isNotificationEnabled" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                count++
            }
            batch.commit().await()
            Result.success(count)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid(restore)", auth)
            Result.failure(e)
        }
    }

    // ===============================================================
    // Monetization & In-App Economy
    // ===============================================================
    suspend fun applyStoreProductPurchase(
        uid: String,
        currentUser: FsUserDoc,
        product: StoreProduct,
        source: String
    ) = withContext(Dispatchers.IO) {
        val updatedUnlocked = currentUser.unlockedItemIds.toMutableSet()
        if (product.grantsAllFrames) updatedUnlocked.add("pack_all_frames")
        if (product.grantsDesignPack) updatedUnlocked.add("pack_design_pro")
        if (product.grantsUnlimitedBackup) updatedUnlocked.add("pack_unlimited_backup")
        if (product.grantsVipBadgeAndAi) updatedUnlocked.add("pack_vip_badge_ai")

        val nextVip = currentUser.isVip || product.grantsVip
        val nextAdFree = currentUser.isAdFree || product.grantsAdFree || product.grantsVip
        val nextUnlimitedBackup = currentUser.hasUnlimitedBackup || product.grantsUnlimitedBackup || product.grantsVip
        val nextDesignPack = currentUser.hasDesignPack || product.grantsDesignPack || product.grantsAllFrames || product.grantsVip
        val nextVipBadgeAi = currentUser.hasVipBadgeAndAi || product.grantsVipBadgeAndAi || product.grantsVip
        val nextVipPlan = if (product.grantsVip) product.title else currentUser.vipPlanName
        val nextCoins = currentUser.loveCoins + product.coinGrant
        val nextCoinPurchases = currentUser.totalCoinPurchases +
            if (product.kind == com.example.data.model.BillingProductKind.CONSUMABLE_COINS) 1 else 0
        val nextPackPurchases = currentUser.totalVipOrPackPurchases +
            if (product.kind != com.example.data.model.BillingProductKind.CONSUMABLE_COINS) 1 else 0
        val nextRev = currentUser.estimatedRevenueTry + product.priceTry

        try {
            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "isVip" to nextVip,
                    "vipPlanName" to nextVipPlan.take(100),
                    "isAdFree" to nextAdFree,
                    "hasUnlimitedBackup" to nextUnlimitedBackup,
                    "hasDesignPack" to nextDesignPack,
                    "hasVipBadgeAndAi" to nextVipBadgeAi,
                    "loveCoins" to nextCoins,
                    "unlockedItemIds" to updatedUnlocked.toList(),
                    "totalCoinPurchases" to nextCoinPurchases,
                    "totalVipOrPackPurchases" to nextPackPurchases,
                    "estimatedRevenueTry" to nextRev,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val txRef = userRef.collection("purchases").document()
            batch.set(
                txRef,
                mapOf(
                    "title" to product.title.take(150),
                    "amountDisplay" to product.priceDisplay.take(60),
                    "source" to source.take(60),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            val profileRef = db.collection("public_profiles").document(uid)
            batch.update(
                profileRef,
                mapOf(
                    "isPremium" to (nextVip || nextVipBadgeAi),
                    "vipPlanName" to nextVipPlan.take(100),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid(purchase)", auth)
        }
    }

    suspend fun restoreActiveEntitlementsFromPlay(
        uid: String,
        currentUser: FsUserDoc,
        products: List<StoreProduct>
    ) = withContext(Dispatchers.IO) {
        if (products.isEmpty()) return@withContext
        val updatedUnlocked = currentUser.unlockedItemIds.toMutableSet()
        var nextVip = currentUser.isVip
        var nextVipPlan = currentUser.vipPlanName
        var nextAdFree = currentUser.isAdFree
        var nextUnlimitedBackup = currentUser.hasUnlimitedBackup
        var nextDesignPack = currentUser.hasDesignPack
        var nextVipBadgeAi = currentUser.hasVipBadgeAndAi

        products.forEach { product ->
            if (product.grantsVip) {
                nextVip = true
                nextVipPlan = product.title
            }
            if (product.grantsAdFree || product.grantsVip) nextAdFree = true
            if (product.grantsUnlimitedBackup || product.grantsVip) {
                nextUnlimitedBackup = true
                updatedUnlocked.add("pack_unlimited_backup")
            }
            if (product.grantsDesignPack || product.grantsAllFrames || product.grantsVip) {
                nextDesignPack = true
                updatedUnlocked.add("pack_all_frames")
                updatedUnlocked.add("pack_design_pro")
            }
            if (product.grantsVipBadgeAndAi || product.grantsVip) {
                nextVipBadgeAi = true
                updatedUnlocked.add("pack_vip_badge_ai")
            }
        }

        updateUserFields(
            uid,
            mapOf(
                "isVip" to nextVip,
                "vipPlanName" to nextVipPlan.take(100),
                "isAdFree" to nextAdFree,
                "hasUnlimitedBackup" to nextUnlimitedBackup,
                "hasDesignPack" to nextDesignPack,
                "hasVipBadgeAndAi" to nextVipBadgeAi,
                "unlockedItemIds" to updatedUnlocked.toList()
            )
        )
        try {
            db.collection("public_profiles").document(uid).update(
                mapOf(
                    "isPremium" to (nextVip || nextVipBadgeAi),
                    "vipPlanName" to nextVipPlan.take(100),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    suspend fun grantRewardedCoins(uid: String, currentCoins: Int, amount: Int, reason: String) =
        withContext(Dispatchers.IO) {
            try {
                val batch = db.batch()
                val userRef = db.collection("users").document(uid)
                batch.update(
                    userRef,
                    mapOf(
                        "loveCoins" to (currentCoins + amount),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val txRef = userRef.collection("purchases").document()
                batch.set(
                    txRef,
                    mapOf(
                        "title" to reason.take(150),
                        "amountDisplay" to "+$amount 🪙",
                        "source" to "Ödüllü Reklam",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, "users/$uid(rewardCoins)", auth)
            }
        }

    suspend fun spendCoinsToUnlockItem(
        uid: String,
        currentUser: FsUserDoc,
        itemId: String,
        itemTitle: String,
        cost: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (currentUser.unlockedItemIds.contains(itemId) || currentUser.isVip) {
            return@withContext Result.success(Unit)
        }
        if (currentUser.loveCoins < cost) {
            return@withContext Result.failure(
                Exception("Yetersiz Aşk Parası! ($cost 🪙 gerekli, mevcut: ${currentUser.loveCoins} 🪙)")
            )
        }
        try {
            val updatedUnlocked = currentUser.unlockedItemIds.toMutableSet().apply { add(itemId) }
            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "loveCoins" to (currentUser.loveCoins - cost),
                    "unlockedItemIds" to updatedUnlocked.toList(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val txRef = userRef.collection("purchases").document()
            batch.set(
                txRef,
                mapOf(
                    "title" to "$itemTitle Açıldı".take(150),
                    "amountDisplay" to "-$cost 🪙",
                    "source" to "Harcama",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid(spendCoins)", auth)
            Result.failure(e)
        }
    }

    suspend fun setTestVipStatus(uid: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        val plan = if (enabled) "Yönetici Premium Test" else ""
        updateUserFields(
            uid,
            mapOf(
                "isVip" to enabled,
                "vipPlanName" to plan,
                "isAdFree" to enabled,
                "hasUnlimitedBackup" to enabled,
                "hasDesignPack" to enabled,
                "hasVipBadgeAndAi" to enabled
            )
        )
        try {
            db.collection("public_profiles").document(uid).update(
                mapOf(
                    "isPremium" to enabled,
                    "vipPlanName" to plan,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    // ===============================================================
    // Admin Settings, Quotes, Broadcasts & Support Messages
    // ===============================================================
    suspend fun saveAdminSettings(settings: AdminSettings) = withContext(Dispatchers.IO) {
        try {
            db.collection("app_config").document("admin_settings")
                .set(FsMappers.adminSettingsToMap(settings))
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "app_config/admin_settings", auth)
        }
    }

    suspend fun addAdminQuote(
        text: String,
        author: String,
        sectionId: String,
        subCategory: String,
        tagsCsv: String,
        isFeaturedDaily: Boolean
    ): Result<AdminQuoteEntity> = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.length < 5) {
            return@withContext Result.failure(Exception("Lütfen en az 5 karakterlik bir söz yazın."))
        }
        val cleanAuthor = author.trim().ifBlank { "Aşk Sözleri Editörü" }.take(100)
        val cleanSub = subCategory.trim().ifBlank { "Editör Seçimi" }.take(80)
        val safeSection = if (sectionId in listOf("ask", "romantik", "gunaydin", "gece")) sectionId else "ask"
        try {
            if (isFeaturedDaily) {
                clearFeaturedAdminFlags()
            }
            val ref = db.collection("admin_quotes").document()
            ref.set(
                hashMapOf(
                    "text" to cleanText.take(1000),
                    "author" to cleanAuthor,
                    "sectionId" to safeSection,
                    "subCategory" to cleanSub,
                    "tagsCsv" to tagsCsv.ifBlank { "Aşk, Özel" }.take(300),
                    "isFeaturedDaily" to isFeaturedDaily,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val longId = stableLongIdFromDocId(ref.id)
            adminQuoteDocIds[longId] = ref.id
            Result.success(
                AdminQuoteEntity(
                    id = longId,
                    text = cleanText,
                    author = cleanAuthor,
                    sectionId = safeSection,
                    subCategory = cleanSub,
                    tagsCsv = tagsCsv.ifBlank { "Aşk, Özel" },
                    isFeaturedDaily = isFeaturedDaily
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "admin_quotes", auth)
            Result.failure(e)
        }
    }

    private suspend fun clearFeaturedAdminFlags() {
        try {
            val featured = db.collection("admin_quotes").whereEqualTo("isFeaturedDaily", true).get().await()
            if (!featured.isEmpty) {
                val batch = db.batch()
                featured.documents.forEach { doc ->
                    batch.update(doc.reference, "isFeaturedDaily", false)
                }
                batch.commit().await()
            }
        } catch (_: Exception) {
        }
    }

    suspend fun updateAdminQuote(quote: AdminQuoteEntity) = withContext(Dispatchers.IO) {
        val docId = adminQuoteDocIds[quote.id] ?: return@withContext
        try {
            if (quote.isFeaturedDaily) {
                clearFeaturedAdminFlags()
            }
            val ref = db.collection("admin_quotes").document(docId)
            val snap = ref.get().await()
            val createdAt = snap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()
            ref.set(
                hashMapOf(
                    "text" to quote.text.take(1000),
                    "author" to quote.author.take(100),
                    "sectionId" to quote.sectionId,
                    "subCategory" to quote.subCategory.take(80),
                    "tagsCsv" to quote.tagsCsv.take(300),
                    "isFeaturedDaily" to quote.isFeaturedDaily,
                    "createdAt" to createdAt
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "admin_quotes/$docId", auth)
        }
    }

    suspend fun setFeaturedAdminQuote(id: Long) = withContext(Dispatchers.IO) {
        val docId = adminQuoteDocIds[id] ?: return@withContext
        try {
            clearFeaturedAdminFlags()
            db.collection("admin_quotes").document(docId).update("isFeaturedDaily", true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "admin_quotes/$docId", auth)
        }
    }

    suspend fun deleteAdminQuote(id: Long) = withContext(Dispatchers.IO) {
        val docId = adminQuoteDocIds[id] ?: return@withContext
        try {
            db.collection("admin_quotes").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "admin_quotes/$docId", auth)
        }
    }

    suspend fun sendBroadcastNotification(
        title: String,
        message: String,
        category: String,
        emoji: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val ref = db.collection("broadcasts").document()
            ref.set(
                hashMapOf(
                    "title" to title.trim().take(150),
                    "message" to message.trim().take(2000),
                    "category" to category.ifBlank { "Duyuru" }.take(60),
                    "emoji" to emoji.ifBlank { "📢" }.take(16),
                    "sentBy" to "Yönetici",
                    "sentAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val longId = stableLongIdFromDocId(ref.id)
            broadcastDocIds[longId] = ref.id
            Result.success(longId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "broadcasts", auth)
            Result.failure(e)
        }
    }

    suspend fun markBroadcastAsRead(uid: String, id: Long) = withContext(Dispatchers.IO) {
        val docId = broadcastDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "readBroadcastIds" to FieldValue.arrayUnion(docId),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid(readBroadcast)", auth)
        }
    }

    suspend fun markAllBroadcastsAsRead(uid: String, ids: List<Long>) = withContext(Dispatchers.IO) {
        val docIds = ids.mapNotNull { broadcastDocIds[it] }
        if (docIds.isEmpty()) return@withContext
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "readBroadcastIds" to FieldValue.arrayUnion(*docIds.toTypedArray()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid(readAllBroadcasts)", auth)
        }
    }

    suspend fun deleteBroadcast(id: Long) = withContext(Dispatchers.IO) {
        val docId = broadcastDocIds[id] ?: return@withContext
        try {
            db.collection("broadcasts").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "broadcasts/$docId", auth)
        }
    }

    suspend fun sendMessageToAdmin(
        uid: String,
        senderName: String,
        senderEmail: String,
        topic: String,
        message: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val cleanMsg = message.trim()
        if (cleanMsg.length < 5) {
            return@withContext Result.failure(Exception("Lütfen en az 5 karakterlik bir mesaj yazın."))
        }
        try {
            val ref = db.collection("support_messages").document()
            ref.set(
                hashMapOf(
                    "userId" to uid,
                    "senderName" to senderName.trim().ifBlank { "Kullanıcı" }.take(100),
                    "senderEmail" to senderEmail.trim().take(160),
                    "topic" to topic.ifBlank { "Genel Mesaj" }.take(80),
                    "message" to cleanMsg.take(2000),
                    "adminReply" to null,
                    "status" to "PENDING",
                    "isReadByAdmin" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "repliedAt" to null
                )
            ).await()
            val longId = stableLongIdFromDocId(ref.id)
            supportMsgDocIds[longId] = ref.id
            Result.success(longId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "support_messages", auth)
            Result.failure(e)
        }
    }

    suspend fun replyToAdminMessage(id: Long, reply: String) = withContext(Dispatchers.IO) {
        val docId = supportMsgDocIds[id] ?: return@withContext
        val cleanReply = reply.trim().take(2000)
        if (cleanReply.isBlank()) return@withContext
        try {
            db.collection("support_messages").document(docId).update(
                mapOf(
                    "adminReply" to cleanReply,
                    "status" to "REPLIED",
                    "isReadByAdmin" to true,
                    "repliedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "support_messages/$docId", auth)
        }
    }

    suspend fun deleteAdminMessage(id: Long) = withContext(Dispatchers.IO) {
        val docId = supportMsgDocIds[id] ?: return@withContext
        try {
            db.collection("support_messages").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "support_messages/$docId", auth)
        }
    }

    // ===============================================================
    // Community Quotes, Comments & Atomic Likes
    // ===============================================================
    suspend fun submitCommunityQuote(
        uid: String,
        userDoc: FsUserDoc?,
        settings: AdminSettings,
        text: String,
        author: String,
        category: String,
        submitterName: String
    ): Result<CommunityQuoteEntity> = withContext(Dispatchers.IO) {
        if (userDoc != null) {
            if (userDoc.isBlocked) {
                val reasonSuffix = if (userDoc.restrictionReason.isNotBlank()) " Sebep: ${userDoc.restrictionReason}" else ""
                return@withContext Result.failure(Exception("Hesabınız banlandığı için söz paylaşamazsınız.$reasonSuffix"))
            }
            val now = System.currentTimeMillis()
            val stillRestricted = userDoc.isRestricted && (userDoc.restrictedUntil == null || userDoc.restrictedUntil > now)
            if (stillRestricted && !userDoc.canSubmitQuotes) {
                val reasonSuffix = if (userDoc.restrictionReason.isNotBlank()) " (${userDoc.restrictionReason})" else ""
                return@withContext Result.failure(Exception("Söz paylaşma yetkiniz yönetici tarafından kısıtlanmıştır$reasonSuffix."))
            }
        }
        val cleanText = text.trim()
        val cleanAuthor = author.trim().ifBlank { "Anonim Âşık" }.take(100)
        val cleanSubmitter = submitterName.trim().ifBlank { "Kullanıcı" }.take(100)

        if (!settings.submissionsEnabled) {
            return@withContext Result.failure(Exception("Yönetici şu anda yeni söz gönderimlerini geçici olarak durdurdu."))
        }
        if (cleanText.length < settings.minCharCount) {
            return@withContext Result.failure(Exception("Söz çok kısa! En az ${settings.minCharCount} karakter yazmalısınız."))
        }
        if (cleanText.length > settings.maxCharCount) {
            return@withContext Result.failure(Exception("Söz çok uzun! Maksimum ${settings.maxCharCount} karakter izin verilmektedir."))
        }

        try {
            // Daily quota check on Firestore authorUid quotes created today
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val recentSnap = db.collection("community_quotes")
                .whereEqualTo("authorUid", uid)
                .get()
                .await()
            val todayCount = recentSnap.documents.count { doc ->
                val ts = doc.getTimestamp("createdAt")?.toDate()
                ts != null && !ts.before(startOfDay)
            }
            if (todayCount >= settings.dailyLimitPerUser) {
                return@withContext Result.failure(
                    Exception("Bugün için söz paylaşım sınırınıza ulaştınız (Maksimum günde ${settings.dailyLimitPerUser} söz).")
                )
            }

            val initialStatus = if (settings.autoApprove) "APPROVED" else "PENDING"
            val ref = db.collection("community_quotes").document()
            ref.set(
                hashMapOf(
                    "authorUid" to uid,
                    "text" to cleanText.take(1000),
                    "author" to cleanAuthor,
                    "category" to category.ifBlank { "Aşk Sözleri" }.take(80),
                    "submitterName" to cleanSubmitter,
                    "status" to initialStatus,
                    "likesCount" to 0,
                    "moderationNote" to null,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "approvedAt" to if (settings.autoApprove) FieldValue.serverTimestamp() else null
                )
            ).await()

            val longId = stableLongIdFromDocId(ref.id)
            communityQuoteDocIds[longId] = ref.id

            if (settings.autoApprove) {
                insertOwnNotification(
                    uid = uid,
                    type = "APPROVED",
                    quoteDocId = ref.id,
                    quotePreview = cleanText.take(68),
                    actorName = "Topluluk",
                    message = "Paylaştığın söz otomatik onaylandı ve yayına girdi! ✨"
                )
            }

            Result.success(
                CommunityQuoteEntity(
                    id = longId,
                    text = cleanText,
                    author = cleanAuthor,
                    category = category.ifBlank { "Aşk Sözleri" },
                    submitterName = cleanSubmitter,
                    status = initialStatus,
                    likesCount = 0
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "community_quotes", auth)
            Result.failure(e)
        }
    }

    suspend fun toggleCommunityQuoteLike(uid: String, quoteId: Long): Boolean = withContext(Dispatchers.IO) {
        val docId = communityQuoteDocIds[quoteId] ?: return@withContext false
        val quoteRef = db.collection("community_quotes").document(docId)
        val likeRef = quoteRef.collection("likes").document(uid)
        try {
            val likeSnap = likeRef.get().await()
            val batch = db.batch()
            if (likeSnap.exists()) {
                batch.delete(likeRef)
                batch.update(quoteRef, "likesCount", FieldValue.increment(-1))
                batch.commit().await()
                _likedCommunityQuoteDocIds.value = _likedCommunityQuoteDocIds.value - docId
                false
            } else {
                batch.set(
                    likeRef,
                    mapOf("uid" to uid, "createdAt" to FieldValue.serverTimestamp())
                )
                batch.update(quoteRef, "likesCount", FieldValue.increment(1))
                batch.commit().await()
                _likedCommunityQuoteDocIds.value = _likedCommunityQuoteDocIds.value + docId
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "community_quotes/$docId/likes/$uid", auth)
            false
        }
    }

    suspend fun refreshLikedCommunityIds(uid: String, quotes: List<CommunityQuoteEntity>) =
        withContext(Dispatchers.IO) {
            val found = mutableSetOf<String>()
            quotes.take(25).forEach { q ->
                val docId = communityQuoteDocIds[q.id] ?: return@forEach
                try {
                    if (db.collection("community_quotes").document(docId).collection("likes").document(uid).get().await().exists()) {
                        found.add(docId)
                    }
                } catch (_: Exception) {
                }
            }
            if (found.isNotEmpty()) {
                _likedCommunityQuoteDocIds.value = _likedCommunityQuoteDocIds.value + found
            }
        }

    fun getQuoteDocId(quoteLongId: Long): String? = communityQuoteDocIds[quoteLongId]

    suspend fun addCommunityComment(
        uid: String,
        userDoc: FsUserDoc?,
        quoteId: Long,
        authorName: String,
        commentText: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (userDoc != null) {
            if (userDoc.isBlocked) {
                return@withContext Result.failure(Exception("Hesabınız banlı olduğu için yorum yapamazsınız."))
            }
            val now = System.currentTimeMillis()
            val stillRestricted = userDoc.isRestricted && (userDoc.restrictedUntil == null || userDoc.restrictedUntil > now)
            if (stillRestricted && !userDoc.canComment) {
                val reasonSuffix = if (userDoc.restrictionReason.isNotBlank()) " (${userDoc.restrictionReason})" else ""
                return@withContext Result.failure(Exception("Yorum yapma yetkiniz kısıtlanmıştır$reasonSuffix."))
            }
        }
        val cleanText = commentText.trim()
        if (cleanText.length < 2) {
            return@withContext Result.failure(Exception("Yorumunuz en az 2 karakter olmalıdır."))
        }
        val quoteDocId = communityQuoteDocIds[quoteId]
            ?: return@withContext Result.failure(Exception("Söz bulunamadı."))
        val cleanAuthor = authorName.trim().ifBlank { "Gönülden Bir Okur" }.take(100)
        try {
            val ref = db.collection("community_quotes").document(quoteDocId).collection("comments").document()
            ref.set(
                hashMapOf(
                    "authorUid" to uid,
                    "authorName" to cleanAuthor,
                    "commentText" to cleanText.take(1000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val longId = stableLongIdFromDocId(ref.id)
            commentDocIds[longId] = ref.id
            commentParentQuoteDocIds[longId] = quoteDocId
            Result.success(longId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "community_quotes/$quoteDocId/comments", auth)
            Result.failure(e)
        }
    }

    suspend fun deleteCommunityComment(commentId: Long) = withContext(Dispatchers.IO) {
        val docId = commentDocIds[commentId] ?: return@withContext
        val quoteDocId = commentParentQuoteDocIds[commentId] ?: return@withContext
        try {
            db.collection("community_quotes").document(quoteDocId)
                .collection("comments").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "community_quotes/$quoteDocId/comments/$docId", auth)
        }
    }

    suspend fun approveCommunityQuote(id: Long) = withContext(Dispatchers.IO) {
        val docId = communityQuoteDocIds[id] ?: return@withContext
        try {
            db.collection("community_quotes").document(docId).update(
                mapOf(
                    "status" to "APPROVED",
                    "approvedAt" to FieldValue.serverTimestamp(),
                    "moderationNote" to null
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "community_quotes/$docId", auth)
        }
    }

    suspend fun rejectCommunityQuote(id: Long, note: String?) = withContext(Dispatchers.IO) {
        val docId = communityQuoteDocIds[id] ?: return@withContext
        try {
            db.collection("community_quotes").document(docId).update(
                mapOf(
                    "status" to "REJECTED",
                    "moderationNote" to note?.trim()?.take(500)
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "community_quotes/$docId", auth)
        }
    }

    suspend fun updateCommunityQuote(quote: CommunityQuoteEntity) = withContext(Dispatchers.IO) {
        val docId = communityQuoteDocIds[quote.id] ?: return@withContext
        try {
            db.collection("community_quotes").document(docId).update(
                mapOf(
                    "text" to quote.text.take(1000),
                    "author" to quote.author.take(100),
                    "category" to quote.category.take(80),
                    "status" to quote.status,
                    "moderationNote" to quote.moderationNote?.take(500)
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "community_quotes/$docId", auth)
        }
    }

    suspend fun deleteCommunityQuote(id: Long) = withContext(Dispatchers.IO) {
        val docId = communityQuoteDocIds[id] ?: return@withContext
        try {
            db.collection("community_quotes").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "community_quotes/$docId", auth)
        }
    }

    // ===============================================================
    // Engagement Notifications (Owner-Scoped in /users/{uid}/notifications)
    // ===============================================================
    suspend fun insertOwnNotification(
        uid: String,
        type: String,
        quoteDocId: String,
        quotePreview: String,
        actorName: String,
        message: String
    ) = withContext(Dispatchers.IO) {
        try {
            db.collection("users").document(uid).collection("notifications").add(
                hashMapOf(
                    "type" to type,
                    "quoteDocId" to quoteDocId.take(120),
                    "quotePreview" to quotePreview.take(300),
                    "actorName" to actorName.take(120),
                    "message" to message.take(500),
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    suspend fun markEngagementRead(uid: String, id: Long) = withContext(Dispatchers.IO) {
        val docId = notifDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).collection("notifications")
                .document(docId).update("isRead", true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/notifications/$docId", auth)
        }
    }

    suspend fun markAllEngagementRead(uid: String, ids: List<Long>) = withContext(Dispatchers.IO) {
        val docIds = ids.mapNotNull { notifDocIds[it] }
        if (docIds.isEmpty()) return@withContext
        try {
            val batch = db.batch()
            docIds.forEach { dId ->
                batch.update(db.collection("users").document(uid).collection("notifications").document(dId), "isRead", true)
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/notifications", auth)
        }
    }

    suspend fun deleteEngagementNotification(uid: String, id: Long) = withContext(Dispatchers.IO) {
        val docId = notifDocIds[id] ?: return@withContext
        try {
            db.collection("users").document(uid).collection("notifications").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/notifications/$docId", auth)
        }
    }

    suspend fun clearAllEngagementNotifications(uid: String, ids: List<Long>) = withContext(Dispatchers.IO) {
        val docIds = ids.mapNotNull { notifDocIds[it] }
        if (docIds.isEmpty()) return@withContext
        try {
            val batch = db.batch()
            docIds.forEach { dId ->
                batch.delete(db.collection("users").document(uid).collection("notifications").document(dId))
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/notifications", auth)
        }
    }

    // ===============================================================
    // Admin User Moderation (Instant Ban / Restrict / Mute / Delete)
    // ===============================================================
    suspend fun setUserAccountBanned(userId: Long, banned: Boolean, reason: String = "") =
        withContext(Dispatchers.IO) {
            val targetUid = userUidsByLongId[userId] ?: return@withContext
            try {
                val batch = db.batch()
                batch.update(
                    db.collection("users").document(targetUid),
                    mapOf(
                        "isBlocked" to banned,
                        "restrictionReason" to if (banned) reason.trim().take(300) else "",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    db.collection("public_profiles").document(targetUid),
                    mapOf(
                        "isBlocked" to banned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "users/$targetUid(ban)", auth)
            }
        }

    suspend fun applyUserRestrictions(
        userId: Long,
        isRestricted: Boolean,
        canSubmitQuotes: Boolean,
        canComment: Boolean,
        canUploadMusic: Boolean,
        canChat: Boolean,
        reason: String,
        restrictedUntil: Long?
    ) = withContext(Dispatchers.IO) {
        val targetUid = userUidsByLongId[userId] ?: return@withContext
        try {
            val batch = db.batch()
            batch.update(
                db.collection("users").document(targetUid),
                mapOf(
                    "isRestricted" to isRestricted,
                    "canSubmitQuotes" to canSubmitQuotes,
                    "canComment" to canComment,
                    "canUploadMusic" to canUploadMusic,
                    "canChat" to canChat,
                    "restrictionReason" to reason.trim().take(300),
                    "restrictedUntil" to restrictedUntil,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("public_profiles").document(targetUid),
                mapOf(
                    "isMutedInChat" to (isRestricted && !canChat),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$targetUid(restrict)", auth)
        }
    }

    suspend fun setUserChatMutedByUid(
        targetUid: String,
        muted: Boolean,
        reason: String,
        restrictedUntil: Long?
    ) = withContext(Dispatchers.IO) {
        if (targetUid.isBlank()) return@withContext
        try {
            val userRef = db.collection("users").document(targetUid)
            val snap = userRef.get().await()
            if (!snap.exists()) return@withContext
            val current = FsUserDoc.fromSnapshot(snap)
            val stillOther = !current.canSubmitQuotes || !current.canComment || !current.canUploadMusic
            val finalRestricted = muted || stillOther
            val finalReason = when {
                muted -> reason.trim().ifBlank { "Sohbet kuralları ihlali" }.take(300)
                stillOther -> current.restrictionReason.take(300)
                else -> ""
            }
            val finalUntil = when {
                muted -> restrictedUntil
                stillOther -> current.restrictedUntil
                else -> null
            }

            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "isRestricted" to finalRestricted,
                    "canChat" to !muted,
                    "restrictionReason" to finalReason,
                    "restrictedUntil" to finalUntil,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("public_profiles").document(targetUid),
                mapOf(
                    "isMutedInChat" to muted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$targetUid(mute)", auth)
        }
    }

    suspend fun setUserChatMuted(
        userId: Long,
        muted: Boolean,
        reason: String,
        restrictedUntil: Long?
    ) = withContext(Dispatchers.IO) {
        val targetUid = userUidsByLongId[userId] ?: return@withContext
        setUserChatMutedByUid(targetUid, muted, reason, restrictedUntil)
    }

    suspend fun deleteUserAccountByAdmin(userId: Long) = withContext(Dispatchers.IO) {
        val targetUid = userUidsByLongId[userId] ?: return@withContext
        try {
            val batch = db.batch()
            batch.delete(db.collection("users").document(targetUid))
            batch.delete(db.collection("public_profiles").document(targetUid))
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$targetUid", auth)
        }
    }

    suspend fun deleteOwnAccountAndData(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val subCollections = listOf(
                "notes", "favorites", "reminders", "music_tracks",
                "notifications", "time_capsules", "countdowns", "love_jar", "purchases"
            )
            val userRef = db.collection("users").document(uid)
            for (sub in subCollections) {
                val snap = userRef.collection(sub).get().await()
                if (!snap.isEmpty) {
                    val batch = db.batch()
                    snap.documents.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
            }
            val batch = db.batch()
            batch.delete(db.collection("public_profiles").document(uid))
            batch.delete(userRef)
            batch.commit().await()

            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) {
                // Recent login might be required for Auth deletion; Firestore PII is already deleted
            }
            GoogleAuthManager.signOut(context, auth)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid(selfDelete)", auth)
            Result.failure(e)
        }
    }

    // ===============================================================
    // Community Chat CRUD & Moderation
    // ===============================================================
    suspend fun sendChatMessage(
        uid: String,
        userDoc: FsUserDoc?,
        settings: AdminSettings,
        roomId: String,
        senderName: String,
        senderRole: String,
        isPremiumSender: Boolean,
        vipPlanName: String,
        message: String,
        replyTo: ChatMessageEntity? = null,
        pinAsAdmin: Boolean = false
    ): Result<ChatMessageEntity> = withContext(Dispatchers.IO) {
        val isAdmin = senderRole == "ADMIN"
        val cleanMessage = message.trim()
        val contentError = ChatModerationHelper.validateMessageContent(
            text = cleanMessage,
            maxChars = if (isAdmin) 500 else settings.chatMaxCharCount
        )
        if (contentError != null) {
            return@withContext Result.failure(Exception(contentError))
        }

        if (!isAdmin) {
            if (!settings.chatEnabled) {
                return@withContext Result.failure(Exception(settings.chatClosedMessage))
            }
            if (userDoc != null) {
                if (userDoc.isBlocked) {
                    val reasonPart = if (userDoc.restrictionReason.isNotBlank()) " Sebep: ${userDoc.restrictionReason}" else ""
                    return@withContext Result.failure(Exception("Hesabınız banlı olduğu için sohbete yazamazsınız.$reasonPart"))
                }
                val now = System.currentTimeMillis()
                val stillRestricted = userDoc.isRestricted && (userDoc.restrictedUntil == null || userDoc.restrictedUntil > now)
                if (stillRestricted && !userDoc.canChat) {
                    val reasonPart = if (userDoc.restrictionReason.isNotBlank()) " (${userDoc.restrictionReason})" else ""
                    return@withContext Result.failure(Exception("Sohbette yazma yetkiniz yönetici tarafından kısıtlanmıştır$reasonPart."))
                }
            }
            val blockedWord = ChatModerationHelper.containsBlockedWord(
                text = cleanMessage,
                blockedWordsCsv = settings.chatBlockedWordsCsv
            )
            if (blockedWord != null) {
                return@withContext Result.failure(Exception("Mesajınız topluluk filtresine takılan bir ifade içeriyor. Lütfen düzenleyin."))
            }
            val now = System.currentTimeMillis()
            val remainingSec = ChatModerationHelper.checkSlowModeRemainingSeconds(
                lastSentAtMillis = _lastChatSentAtMs.value,
                slowModeSeconds = settings.chatSlowModeSeconds,
                nowMillis = now
            )
            if (remainingSec > 0) {
                return@withContext Result.failure(Exception("Yavaş mod aktif: Yeni mesaj için $remainingSec saniye bekleyin ⏳"))
            }
        }

        val initialStatus = if (!isAdmin && settings.chatRequireApproval) "PENDING" else "PUBLISHED"
        val shouldPin = isAdmin && pinAsAdmin

        try {
            if (shouldPin) {
                clearRoomPins(roomId)
            }
            val ref = db.collection("chat_messages").document()
            val replyDocId = replyTo?.id?.let { chatDocIds[it] }
            val safeName = if (isAdmin) "Yönetici 👑" else senderName.trim().ifBlank { "Kullanıcı" }.take(80)
            val payload = hashMapOf<String, Any?>(
                "roomId" to roomId,
                "senderUid" to uid,
                "senderName" to safeName,
                "senderRole" to if (isAdmin) "ADMIN" else "USER",
                "isPremiumSender" to (isPremiumSender || isAdmin),
                "vipPlanName" to vipPlanName.take(80),
                "message" to cleanMessage.take(600),
                "replyToMessageId" to replyDocId?.take(120),
                "replyToSenderName" to replyTo?.senderName?.take(80),
                "replyToSnippet" to replyTo?.message?.take(120),
                "status" to initialStatus,
                "isPinned" to shouldPin,
                "likesCount" to 0,
                "moderationNote" to null,
                "createdAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            if (!isAdmin) {
                _lastChatSentAtMs.value = System.currentTimeMillis()
            }

            val longId = stableLongIdFromDocId(ref.id)
            chatDocIds[longId] = ref.id
            _chatSenderUidByMessageId.value = _chatSenderUidByMessageId.value + (longId to uid)

            Result.success(
                ChatMessageEntity(
                    id = longId,
                    roomId = roomId,
                    senderUserId = stableLongIdFromDocId(uid),
                    senderName = safeName,
                    senderRole = if (isAdmin) "ADMIN" else "USER",
                    isVipSender = isPremiumSender || isAdmin,
                    message = cleanMessage,
                    replyToMessageId = replyTo?.id,
                    replyToSenderName = replyTo?.senderName,
                    replyToSnippet = replyTo?.message?.take(64),
                    status = initialStatus,
                    isPinned = shouldPin,
                    likesCount = 0,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "chat_messages", auth)
            Result.failure(e)
        }
    }

    private suspend fun clearRoomPins(roomId: String) {
        try {
            val pinned = db.collection("chat_messages")
                .whereEqualTo("roomId", roomId)
                .whereEqualTo("isPinned", true)
                .get()
                .await()
            if (!pinned.isEmpty) {
                val batch = db.batch()
                pinned.documents.forEach { doc -> batch.update(doc.reference, "isPinned", false) }
                batch.commit().await()
            }
        } catch (_: Exception) {
        }
    }

    suspend fun toggleChatMessageLike(uid: String, messageId: Long): Boolean = withContext(Dispatchers.IO) {
        val docId = chatDocIds[messageId] ?: return@withContext false
        val msgRef = db.collection("chat_messages").document(docId)
        val likeRef = msgRef.collection("likes").document(uid)
        try {
            val likeSnap = likeRef.get().await()
            val batch = db.batch()
            if (likeSnap.exists()) {
                batch.delete(likeRef)
                batch.update(msgRef, "likesCount", FieldValue.increment(-1))
                batch.commit().await()
                _likedChatMessageDocIds.value = _likedChatMessageDocIds.value - docId
                false
            } else {
                batch.set(likeRef, mapOf("uid" to uid, "createdAt" to FieldValue.serverTimestamp()))
                batch.update(msgRef, "likesCount", FieldValue.increment(1))
                batch.commit().await()
                _likedChatMessageDocIds.value = _likedChatMessageDocIds.value + docId
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chat_messages/$docId/likes/$uid", auth)
            false
        }
    }

    fun getChatDocId(messageLongId: Long): String? = chatDocIds[messageLongId]

    suspend fun refreshLikedChatIds(uid: String, messages: List<ChatMessageEntity>) =
        withContext(Dispatchers.IO) {
            val found = mutableSetOf<String>()
            messages.take(30).forEach { msg ->
                val docId = chatDocIds[msg.id] ?: return@forEach
                try {
                    if (db.collection("chat_messages").document(docId).collection("likes").document(uid).get().await().exists()) {
                        found.add(docId)
                    }
                } catch (_: Exception) {
                }
            }
            if (found.isNotEmpty()) {
                _likedChatMessageDocIds.value = _likedChatMessageDocIds.value + found
            }
        }

    suspend fun approveChatMessage(id: Long) = withContext(Dispatchers.IO) {
        val docId = chatDocIds[id] ?: return@withContext
        try {
            db.collection("chat_messages").document(docId).update(
                mapOf("status" to "PUBLISHED", "moderationNote" to null)
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chat_messages/$docId", auth)
        }
    }

    suspend fun hideChatMessage(id: Long, note: String? = null) = withContext(Dispatchers.IO) {
        val docId = chatDocIds[id] ?: return@withContext
        try {
            db.collection("chat_messages").document(docId).update(
                mapOf(
                    "status" to "HIDDEN",
                    "moderationNote" to (note?.trim()?.takeIf { it.isNotBlank() } ?: "Yönetici tarafından gizlendi").take(300)
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chat_messages/$docId", auth)
        }
    }

    suspend fun togglePinChatMessage(id: Long, roomId: String, pin: Boolean) = withContext(Dispatchers.IO) {
        val docId = chatDocIds[id] ?: return@withContext
        try {
            if (pin) clearRoomPins(roomId)
            db.collection("chat_messages").document(docId).update("isPinned", pin).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chat_messages/$docId", auth)
        }
    }

    suspend fun deleteChatMessage(id: Long) = withContext(Dispatchers.IO) {
        val docId = chatDocIds[id] ?: return@withContext
        try {
            db.collection("chat_messages").document(docId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "chat_messages/$docId", auth)
        }
    }

    suspend fun clearChatRoom(roomId: String?) = withContext(Dispatchers.IO) {
        try {
            val query = if (roomId != null) {
                db.collection("chat_messages").whereEqualTo("roomId", roomId)
            } else {
                db.collection("chat_messages")
            }
            val snap = query.get().await()
            if (!snap.isEmpty) {
                val batch = db.batch()
                snap.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "chat_messages(clear)", auth)
        }
    }
}
