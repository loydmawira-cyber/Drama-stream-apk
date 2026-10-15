package com.example.domain.usecase

import com.example.domain.model.Series
import com.example.domain.repository.DramaRepository
import kotlinx.coroutines.flow.Flow

class GetSeriesUseCase(
    private val dramaRepository: DramaRepository
) {
    operator fun invoke(): Flow<List<Series>> {
        return dramaRepository.getAllSeries()
    }
}
