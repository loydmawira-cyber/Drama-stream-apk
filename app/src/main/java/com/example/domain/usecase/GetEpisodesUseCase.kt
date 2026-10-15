package com.example.domain.usecase

import com.example.domain.model.Episode
import com.example.domain.repository.DramaRepository
import kotlinx.coroutines.flow.Flow

class GetEpisodesUseCase(
    private val dramaRepository: DramaRepository
) {
    operator fun invoke(seriesId: String): Flow<List<Episode>> {
        return dramaRepository.getEpisodesForSeries(seriesId)
    }
}
