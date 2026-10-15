package com.example.domain.model

data class Episode(
    val id: String,
    val seriesId: String,
    val episodeNumber: Int,
    val title: String,
    val durationSeconds: Int,
    val coinCost: Int,
    val isUnlocked: Boolean,
    val videoUrl: String
) {
    val isFree: Boolean
        get() = episodeNumber <= 3 || coinCost == 0
}
