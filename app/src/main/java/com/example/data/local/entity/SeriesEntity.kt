package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Series

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val synopsis: String,
    val coverResId: Int,
    val category: String,
    val tagsString: String, // comma separated tags
    val isTrending: Boolean,
    val isNew: Boolean,
    val totalEpisodes: Int
) {
    fun toDomain(): Series = Series(
        id = id,
        title = title,
        synopsis = synopsis,
        coverResId = coverResId,
        category = category,
        tags = tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        isTrending = isTrending,
        isNew = isNew,
        totalEpisodes = totalEpisodes
    )
}
