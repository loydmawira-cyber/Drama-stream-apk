package com.example.domain.usecase

import com.example.domain.model.Episode
import com.example.domain.repository.DramaRepository
import com.example.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first

sealed class UnlockResult {
    data object SuccessUnlockedWithCoins : UnlockResult()
    data object SuccessVipFreeAccess : UnlockResult()
    data object AlreadyUnlocked : UnlockResult()
    data class InsufficientCoins(val required: Int, val currentBalance: Int) : UnlockResult()
    data class Error(val message: String) : UnlockResult()
}

class UnlockEpisodeUseCase(
    private val dramaRepository: DramaRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(episode: Episode): UnlockResult {
        if (episode.isUnlocked || episode.isFree) {
            return UnlockResult.AlreadyUnlocked
        }

        val wallet = userDataRepository.getUserWallet().first()
        if (wallet.isVip) {
            dramaRepository.unlockEpisode(episode.id)
            return UnlockResult.SuccessVipFreeAccess
        }

        val cost = episode.coinCost
        if (wallet.coinBalance < cost) {
            return UnlockResult.InsufficientCoins(required = cost, currentBalance = wallet.coinBalance)
        }

        val deducted = userDataRepository.deductCoins(cost)
        if (deducted) {
            dramaRepository.unlockEpisode(episode.id)
            return UnlockResult.SuccessUnlockedWithCoins
        } else {
            return UnlockResult.InsufficientCoins(required = cost, currentBalance = wallet.coinBalance)
        }
    }
}
