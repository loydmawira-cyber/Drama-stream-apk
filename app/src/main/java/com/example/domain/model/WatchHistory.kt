package com.example.domain.model

data class WatchHistory(
    val seriesId: String,
    val seriesTitle: String,
    val coverResId: Int,
    val lastEpisodeNumber: Int,
    val totalEpisodes: Int,
    val lastPositionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0.5f
}
