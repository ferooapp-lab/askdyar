package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        NoteEntity::class,
        FavoriteEntity::class,
        ReminderEntity::class,
        CommunityQuoteEntity::class,
        SavedFavoriteQuoteEntity::class,
        AdminMessageEntity::class,
        BroadcastNotificationEntity::class,
        CommunityCommentEntity::class,
        AdminQuoteEntity::class,
        UserAccountEntity::class,
        CustomMusicTrackEntity::class,
        EngagementNotificationEntity::class,
        ChatMessageEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun communityQuoteDao(): CommunityQuoteDao
    abstract fun savedFavoriteQuoteDao(): SavedFavoriteQuoteDao
    abstract fun adminMessageDao(): AdminMessageDao
    abstract fun broadcastNotificationDao(): BroadcastNotificationDao
    abstract fun communityCommentDao(): CommunityCommentDao
    abstract fun adminQuoteDao(): AdminQuoteDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun customMusicTrackDao(): CustomMusicTrackDao
    abstract fun engagementNotificationDao(): EngagementNotificationDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_favorite_quotes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        text TEXT NOT NULL,
                        author TEXT NOT NULL,
                        category TEXT NOT NULL,
                        sourceLabel TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS admin_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        senderName TEXT NOT NULL,
                        senderEmail TEXT NOT NULL,
                        topic TEXT NOT NULL,
                        message TEXT NOT NULL,
                        adminReply TEXT,
                        status TEXT NOT NULL,
                        isReadByAdmin INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        repliedAt INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS broadcast_notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        category TEXT NOT NULL,
                        emoji TEXT NOT NULL,
                        sentBy TEXT NOT NULL,
                        sentAt INTEGER NOT NULL,
                        isRead INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS community_comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        quoteId INTEGER NOT NULL,
                        authorName TEXT NOT NULL,
                        commentText TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS admin_quotes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        text TEXT NOT NULL,
                        author TEXT NOT NULL,
                        sectionId TEXT NOT NULL,
                        subCategory TEXT NOT NULL,
                        tagsCsv TEXT NOT NULL,
                        isFeaturedDaily INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fullName TEXT NOT NULL,
                        email TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        partnerName TEXT NOT NULL,
                        isBlocked INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        lastLoginAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_music_tracks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        presetKey TEXT,
                        addedBy TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN isRestricted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN canSubmitQuotes INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN canComment INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN canUploadMusic INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN restrictionReason TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN restrictedUntil INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS engagement_notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        quoteId INTEGER NOT NULL,
                        quotePreview TEXT NOT NULL,
                        actorName TEXT NOT NULL,
                        message TEXT NOT NULL,
                        isRead INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN canChat INTEGER NOT NULL DEFAULT 1")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        roomId TEXT NOT NULL,
                        senderUserId INTEGER NOT NULL,
                        senderName TEXT NOT NULL,
                        senderRole TEXT NOT NULL,
                        isVipSender INTEGER NOT NULL,
                        message TEXT NOT NULL,
                        replyToMessageId INTEGER,
                        replyToSenderName TEXT,
                        replyToSnippet TEXT,
                        status TEXT NOT NULL,
                        isPinned INTEGER NOT NULL,
                        likesCount INTEGER NOT NULL,
                        moderationNote TEXT,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "love_quotes_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
