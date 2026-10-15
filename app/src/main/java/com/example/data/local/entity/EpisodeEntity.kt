package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Episode

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId", "episodeNumber"])]
)
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val episodeNumber: Int,
    val title: String,
    val durationSeconds: Int,
    val coinCost: Int,
    val isUnlocked: Boolean,
    val videoUrl: String
) {
    fun toDomain(): Episode = Episode(
        id = id,
        seriesId = seriesId,
        episodeNumber = episodeNumber,
        title = title,
        durationSeconds = durationSeconds,
        coinCost = coinCost,
        isUnlocked = isUnlocked,
        videoUrl = videoUrl
    )
}
