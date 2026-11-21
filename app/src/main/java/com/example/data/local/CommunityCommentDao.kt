package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityCommentDao {
    @Query("SELECT * FROM community_comments ORDER BY createdAt DESC")
    fun getAllComments(): Flow<List<CommunityCommentEntity>>

    @Query("SELECT * FROM community_comments WHERE quoteId = :quoteId ORDER BY createdAt DESC")
    fun getCommentsForQuote(quoteId: Long): Flow<List<CommunityCommentEntity>>

    @Query("SELECT COUNT(*) FROM community_comments")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommunityCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommunityCommentEntity>)

    @Query("DELETE FROM community_comments WHERE id = :id")
    suspend fun deleteCommentById(id: Long)
}
