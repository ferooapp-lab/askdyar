package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityQuoteDao {
    @Query("SELECT * FROM community_quotes WHERE status = 'APPROVED' ORDER BY likesCount DESC, createdAt DESC")
    fun getApprovedQuotes(): Flow<List<CommunityQuoteEntity>>

    @Query("SELECT * FROM community_quotes WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingQuotes(): Flow<List<CommunityQuoteEntity>>

    @Query("SELECT * FROM community_quotes WHERE status = 'REJECTED' ORDER BY createdAt DESC")
    fun getRejectedQuotes(): Flow<List<CommunityQuoteEntity>>

    @Query("SELECT * FROM community_quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<CommunityQuoteEntity>>

    @Query("SELECT * FROM community_quotes WHERE id = :id LIMIT 1")
    suspend fun getQuoteById(id: Long): CommunityQuoteEntity?

    @Query("SELECT COUNT(*) FROM community_quotes WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM community_quotes")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM community_quotes WHERE submitterName = :submitterName AND createdAt >= :dayStart")
    suspend fun getSubmissionsCountToday(submitterName: String, dayStart: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: CommunityQuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<CommunityQuoteEntity>)

    @Update
    suspend fun updateQuote(quote: CommunityQuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: CommunityQuoteEntity)

    @Query("DELETE FROM community_quotes WHERE id = :id")
    suspend fun deleteQuoteById(id: Long)

    @Query("UPDATE community_quotes SET status = 'APPROVED', approvedAt = :approvedAt WHERE id = :id")
    suspend fun approveQuote(id: Long, approvedAt: Long = System.currentTimeMillis())

    @Query("UPDATE community_quotes SET status = 'REJECTED', moderationNote = :note WHERE id = :id")
    suspend fun rejectQuote(id: Long, note: String?)

    @Query("UPDATE community_quotes SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementLikes(id: Long)

    @Query("UPDATE community_quotes SET likesCount = CASE WHEN likesCount > 0 THEN likesCount - 1 ELSE 0 END WHERE id = :id")
    suspend fun decrementLikes(id: Long)
}
