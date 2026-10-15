package com.example.domain.usecase

import com.example.domain.repository.UserDataRepository

class PurchaseCoinsUseCase(
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(coinsToAdd: Int) {
        userDataRepository.addCoins(coinsToAdd)
    }
}
