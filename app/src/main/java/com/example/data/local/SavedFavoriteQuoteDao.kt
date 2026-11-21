package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFavoriteQuoteDao {
    @Query("SELECT * FROM saved_favorite_quotes ORDER BY createdAt DESC")
    fun getAllSavedFavorites(): Flow<List<SavedFavoriteQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedFavorite(item: SavedFavoriteQuoteEntity): Long

    @Query("DELETE FROM saved_favorite_quotes WHERE id = :id")
    suspend fun deleteSavedFavoriteById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_favorite_quotes WHERE text = :text)")
    suspend fun existsByText(text: String): Boolean

    @Query("SELECT COUNT(*) FROM saved_favorite_quotes")
    suspend fun getAllSavedFavoritesSyncCount(): Int
}
