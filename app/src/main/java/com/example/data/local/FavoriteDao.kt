package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT quoteId FROM quote_favorites")
    fun getAllFavoriteQuoteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM quote_favorites WHERE quoteId = :quoteId")
    suspend fun removeFavorite(quoteId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM quote_favorites WHERE quoteId = :quoteId)")
    suspend fun isFavorite(quoteId: String): Boolean

    @Query("SELECT * FROM quote_favorites")
    suspend fun getAllFavoritesSync(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)
}
