package com.example.data.repository

import com.example.data.local.dao.EpisodeDao
import com.example.data.local.dao.SeriesDao
import com.example.data.local.dao.WatchHistoryDao
import com.example.data.local.entity.WatchHistoryEntity
import com.example.domain.model.Episode
import com.example.domain.model.Series
import com.example.domain.model.WatchHistory
import com.example.domain.repository.DramaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DramaRepositoryImpl(
    private val seriesDao: SeriesDao,
    private val episodeDao: EpisodeDao,
    private val watchHistoryDao: WatchHistoryDao
) : DramaRepository {

    override fun getAllSeries(): Flow<List<Series>> {
        return seriesDao.getAllSeries().map { list -> list.map { it.toDomain() } }
    }

    override fun getSeriesById(id: String): Flow<Series?> {
        return seriesDao.getSeriesById(id).map { it?.toDomain() }
    }

    override fun getEpisodesForSeries(seriesId: String): Flow<List<Episode>> {
        return episodeDao.getEpisodesForSeries(seriesId).map { list -> list.map { it.toDomain() } }
    }

    override fun getEpisode(episodeId: String): Flow<Episode?> {
        return episodeDao.getEpisodeById(episodeId).map { it?.toDomain() }
    }

    override suspend fun unlockEpisode(episodeId: String) {
        episodeDao.unlockEpisode(episodeId)
    }

    override suspend fun unlockAllEpisodesForSeries(seriesId: String) {
        episodeDao.unlockAllEpisodesForSeries(seriesId)
    }

    override fun getWatchHistory(): Flow<List<WatchHistory>> {
        return watchHistoryDao.getAllWatchHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateWatchProgress(
        seriesId: String,
        episodeNumber: Int,
        positionMs: Long,
        durationMs: Long
    ) {
        val series = seriesDao.getSeriesById(seriesId).firstOrNull() ?: return
        val history = WatchHistoryEntity(
            seriesId = seriesId,
            seriesTitle = series.title,
            coverResId = series.coverResId,
            lastEpisodeNumber = episodeNumber,
            totalEpisodes = series.totalEpisodes,
            lastPositionMs = positionMs,
            durationMs = durationMs,
            updatedAt = System.currentTimeMillis()
        )
        watchHistoryDao.insertOrUpdate(history)
    }
}
