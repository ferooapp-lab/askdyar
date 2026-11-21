package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Query("UPDATE user_accounts SET lastLoginAt = :timestamp WHERE id = :id")
    suspend fun updateLastLogin(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_accounts SET isBlocked = :blocked, restrictionReason = :reason WHERE id = :id")
    suspend fun setUserBanned(id: Long, blocked: Boolean, reason: String)

    @Query(
        """
        UPDATE user_accounts SET 
            isRestricted = :isRestricted,
            canSubmitQuotes = :canSubmitQuotes,
            canComment = :canComment,
            canUploadMusic = :canUploadMusic,
            canChat = :canChat,
            restrictionReason = :reason,
            restrictedUntil = :restrictedUntil
        WHERE id = :id
        """
    )
    suspend fun updateUserRestrictions(
        id: Long,
        isRestricted: Boolean,
        canSubmitQuotes: Boolean,
        canComment: Boolean,
        canUploadMusic: Boolean,
        canChat: Boolean,
        reason: String,
        restrictedUntil: Long?
    )

    @Query(
        """
        UPDATE user_accounts SET
            canChat = :canChat,
            isRestricted = :isRestricted,
            restrictionReason = :reason,
            restrictedUntil = :restrictedUntil
        WHERE id = :id
        """
    )
    suspend fun updateUserChatPermission(
        id: Long,
        canChat: Boolean,
        isRestricted: Boolean,
        reason: String,
        restrictedUntil: Long?
    )

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteUserById(id: Long)
}
