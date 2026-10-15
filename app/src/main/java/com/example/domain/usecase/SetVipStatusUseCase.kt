package com.example.domain.usecase

import com.example.domain.repository.UserDataRepository

class SetVipStatusUseCase(
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(isVip: Boolean, durationDays: Int = 30) {
        userDataRepository.setVipStatus(isVip, durationDays)
    }
}
