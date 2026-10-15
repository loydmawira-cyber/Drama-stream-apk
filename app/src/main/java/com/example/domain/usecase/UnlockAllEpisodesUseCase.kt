package com.example.domain.usecase

import com.example.domain.repository.DramaRepository
import com.example.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first

class UnlockAllEpisodesUseCase(
    private val dramaRepository: DramaRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(seriesId: String): UnlockResult {
        val wallet = userDataRepository.getUserWallet().first()
        if (wallet.isVip) {
            dramaRepository.unlockAllEpisodesForSeries(seriesId)
            return UnlockResult.SuccessVipFreeAccess
        }

        val episodes = dramaRepository.getEpisodesForSeries(seriesId).first()
        val lockedEpisodes = episodes.filter { !it.isUnlocked && !it.isFree }
        if (lockedEpisodes.isEmpty()) {
            return UnlockResult.AlreadyUnlocked
        }

        val totalCost = lockedEpisodes.sumOf { it.coinCost }
        if (wallet.coinBalance < totalCost) {
            return UnlockResult.InsufficientCoins(required = totalCost, currentBalance = wallet.coinBalance)
        }

        val deducted = userDataRepository.deductCoins(totalCost)
        if (deducted) {
            dramaRepository.unlockAllEpisodesForSeries(seriesId)
            return UnlockResult.SuccessUnlockedWithCoins
        } else {
            return UnlockResult.InsufficientCoins(required = totalCost, currentBalance = wallet.coinBalance)
        }
    }
}
