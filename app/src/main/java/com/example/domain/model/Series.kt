package com.example.domain.model

data class Series(
    val id: String,
    val title: String,
    val synopsis: String,
    val coverResId: Int,
    val category: String,
    val tags: List<String>,
    val isTrending: Boolean = false,
    val isNew: Boolean = false,
    val totalEpisodes: Int = 10
)
