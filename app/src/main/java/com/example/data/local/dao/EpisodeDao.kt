package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId LIMIT 1")
    fun getEpisodeById(episodeId: String): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND episodeNumber = :episodeNumber LIMIT 1")
    suspend fun getEpisodeByNumber(seriesId: String, episodeNumber: Int): EpisodeEntity?

    @Query("UPDATE episodes SET isUnlocked = 1 WHERE id = :episodeId")
    suspend fun unlockEpisode(episodeId: String)

    @Query("UPDATE episodes SET isUnlocked = 1 WHERE seriesId = :seriesId")
    suspend fun unlockAllEpisodesForSeries(seriesId: String)

    @Query("SELECT COUNT(*) FROM episodes")
    suspend fun getEpisodeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)
}
