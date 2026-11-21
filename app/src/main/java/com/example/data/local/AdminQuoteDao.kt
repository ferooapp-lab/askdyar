package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminQuoteDao {
    @Query("SELECT * FROM admin_quotes ORDER BY createdAt DESC")
    fun getAllAdminQuotes(): Flow<List<AdminQuoteEntity>>

    @Query("SELECT * FROM admin_quotes WHERE isFeaturedDaily = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getFeaturedDailyQuote(): AdminQuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: AdminQuoteEntity): Long

    @Update
    suspend fun updateQuote(quote: AdminQuoteEntity)

    @Query("UPDATE admin_quotes SET isFeaturedDaily = 0")
    suspend fun clearAllFeaturedFlags()

    @Query("UPDATE admin_quotes SET isFeaturedDaily = 1 WHERE id = :id")
    suspend fun setFeaturedById(id: Long)

    @Query("DELETE FROM admin_quotes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
