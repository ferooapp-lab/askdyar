package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomMusicTrackDao {
    @Query("SELECT * FROM custom_music_tracks ORDER BY createdAt DESC")
    fun getAllTracks(): Flow<List<CustomMusicTrackEntity>>

    @Query("SELECT * FROM custom_music_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): CustomMusicTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: CustomMusicTrackEntity): Long

    @Query("DELETE FROM custom_music_tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Long)
}
