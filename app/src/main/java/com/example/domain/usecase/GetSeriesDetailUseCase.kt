package com.example.domain.usecase

import com.example.domain.model.Episode
import com.example.domain.model.Series
import com.example.domain.repository.DramaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SeriesDetail(
    val series: Series,
    val episodes: List<Episode>
)

class GetSeriesDetailUseCase(
    private val dramaRepository: DramaRepository
) {
    operator fun invoke(seriesId: String): Flow<SeriesDetail?> {
        return combine(
            dramaRepository.getSeriesById(seriesId),
            dramaRepository.getEpisodesForSeries(seriesId)
        ) { series, episodes ->
            if (series != null) {
                SeriesDetail(series = series, episodes = episodes)
            } else {
                null
            }
        }
    }
}
