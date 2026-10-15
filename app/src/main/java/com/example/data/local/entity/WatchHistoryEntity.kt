package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.WatchHistory

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val seriesId: String,
    val seriesTitle: String,
    val coverResId: Int,
    val lastEpisodeNumber: Int,
    val totalEpisodes: Int,
    val lastPositionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): WatchHistory = WatchHistory(
        seriesId = seriesId,
        seriesTitle = seriesTitle,
        coverResId = coverResId,
        lastEpisodeNumber = lastEpisodeNumber,
        totalEpisodes = totalEpisodes,
        lastPositionMs = lastPositionMs,
        durationMs = durationMs,
        updatedAt = updatedAt
    )
}
