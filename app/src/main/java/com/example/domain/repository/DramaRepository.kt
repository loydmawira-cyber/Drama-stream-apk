package com.example.domain.repository

import com.example.domain.model.Episode
import com.example.domain.model.Series
import com.example.domain.model.WatchHistory
import kotlinx.coroutines.flow.Flow

interface DramaRepository {
    fun getAllSeries(): Flow<List<Series>>
    fun getSeriesById(id: String): Flow<Series?>
    fun getEpisodesForSeries(seriesId: String): Flow<List<Episode>>
    fun getEpisode(episodeId: String): Flow<Episode?>
    suspend fun unlockEpisode(episodeId: String)
    suspend fun unlockAllEpisodesForSeries(seriesId: String)
    fun getWatchHistory(): Flow<List<WatchHistory>>
    suspend fun updateWatchProgress(seriesId: String, episodeNumber: Int, positionMs: Long, durationMs: Long)
}
