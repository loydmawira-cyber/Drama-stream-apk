package com.example.domain.usecase

import com.example.domain.model.Episode
import com.example.domain.model.Series
import com.example.domain.model.UserWallet
import com.example.domain.model.WatchHistory
import com.example.domain.repository.DramaRepository
import com.example.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnlockEpisodeUseCaseTest {

    private lateinit var fakeDramaRepository: FakeDramaRepository
    private lateinit var fakeUserDataRepository: FakeUserDataRepository
    private lateinit var unlockEpisodeUseCase: UnlockEpisodeUseCase

    @Before
    fun setUp() {
        fakeDramaRepository = FakeDramaRepository()
        fakeUserDataRepository = FakeUserDataRepository()
        unlockEpisodeUseCase = UnlockEpisodeUseCase(fakeDramaRepository, fakeUserDataRepository)
    }

    @Test
    fun `unlocking locked episode deducts 50 coins and marks unlocked`() = runTest {
        // Given wallet with 500 coins and locked episode 4 costing 50 coins
        fakeUserDataRepository.walletFlow.value = UserWallet(coinBalance = 500, isVip = false)
        val lockedEpisode = Episode(
            id = "ep_4",
            seriesId = "series_1",
            episodeNumber = 4,
            title = "Episode 4",
            durationSeconds = 60,
            coinCost = 50,
            isUnlocked = false,
            videoUrl = "sample.mp4"
        )

        // When
        val result = unlockEpisodeUseCase(lockedEpisode)

        // Then
        assertTrue(result is UnlockResult.SuccessUnlockedWithCoins)
        assertEquals(450, fakeUserDataRepository.walletFlow.value.coinBalance)
        assertTrue(fakeDramaRepository.unlockedEpisodeIds.contains("ep_4"))
    }

    @Test
    fun `VIP user unlocks episode without coin deduction`() = runTest {
        // Given VIP user with 500 coins
        fakeUserDataRepository.walletFlow.value = UserWallet(coinBalance = 500, isVip = true)
        val lockedEpisode = Episode(
            id = "ep_5",
            seriesId = "series_1",
            episodeNumber = 5,
            title = "Episode 5",
            durationSeconds = 60,
            coinCost = 50,
            isUnlocked = false,
            videoUrl = "sample.mp4"
        )

        // When
        val result = unlockEpisodeUseCase(lockedEpisode)

        // Then
        assertTrue(result is UnlockResult.SuccessVipFreeAccess)
        assertEquals(500, fakeUserDataRepository.walletFlow.value.coinBalance)
        assertTrue(fakeDramaRepository.unlockedEpisodeIds.contains("ep_5"))
    }

    @Test
    fun `insufficient coins returns InsufficientCoins and does not unlock`() = runTest {
        // Given user with only 30 coins
        fakeUserDataRepository.walletFlow.value = UserWallet(coinBalance = 30, isVip = false)
        val lockedEpisode = Episode(
            id = "ep_6",
            seriesId = "series_1",
            episodeNumber = 6,
            title = "Episode 6",
            durationSeconds = 60,
            coinCost = 50,
            isUnlocked = false,
            videoUrl = "sample.mp4"
        )

        // When
        val result = unlockEpisodeUseCase(lockedEpisode)

        // Then
        assertTrue(result is UnlockResult.InsufficientCoins)
        val insufficient = result as UnlockResult.InsufficientCoins
        assertEquals(50, insufficient.required)
        assertEquals(30, insufficient.currentBalance)
        assertEquals(30, fakeUserDataRepository.walletFlow.value.coinBalance)
        assertTrue(!fakeDramaRepository.unlockedEpisodeIds.contains("ep_6"))
    }

    @Test
    fun `free episode 1-3 returns AlreadyUnlocked without deducting coins`() = runTest {
        fakeUserDataRepository.walletFlow.value = UserWallet(coinBalance = 500, isVip = false)
        val freeEpisode = Episode(
            id = "ep_1",
            seriesId = "series_1",
            episodeNumber = 1,
            title = "Episode 1",
            durationSeconds = 60,
            coinCost = 0,
            isUnlocked = true,
            videoUrl = "sample.mp4"
        )

        val result = unlockEpisodeUseCase(freeEpisode)

        assertTrue(result is UnlockResult.AlreadyUnlocked)
        assertEquals(500, fakeUserDataRepository.walletFlow.value.coinBalance)
    }
}

private class FakeDramaRepository : DramaRepository {
    val unlockedEpisodeIds = mutableSetOf<String>()

    override fun getAllSeries(): Flow<List<Series>> = flowOf(emptyList())
    override fun getSeriesById(id: String): Flow<Series?> = flowOf(null)
    override fun getEpisodesForSeries(seriesId: String): Flow<List<Episode>> = flowOf(emptyList())
    override fun getEpisode(episodeId: String): Flow<Episode?> = flowOf(null)

    override suspend fun unlockEpisode(episodeId: String) {
        unlockedEpisodeIds.add(episodeId)
    }

    override suspend fun unlockAllEpisodesForSeries(seriesId: String) {
        // no-op
    }

    override fun getWatchHistory(): Flow<List<WatchHistory>> = flowOf(emptyList())
    override suspend fun updateWatchProgress(seriesId: String, episodeNumber: Int, positionMs: Long, durationMs: Long) {}
}

private class FakeUserDataRepository : UserDataRepository {
    val walletFlow = MutableStateFlow(UserWallet(coinBalance = 500, isVip = false))

    override fun getUserWallet(): Flow<UserWallet> = walletFlow

    override suspend fun deductCoins(amount: Int): Boolean {
        val current = walletFlow.value.coinBalance
        return if (current >= amount) {
            walletFlow.value = walletFlow.value.copy(coinBalance = current - amount)
            true
        } else {
            false
        }
    }

    override suspend fun addCoins(amount: Int) {
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + amount)
    }

    override suspend fun setVipStatus(isVip: Boolean, durationDays: Int) {
        walletFlow.value = walletFlow.value.copy(isVip = isVip)
    }

    private val profileFlow = MutableStateFlow(com.example.domain.model.UserProfile())
    override fun getUserProfile(): Flow<com.example.domain.model.UserProfile> = profileFlow

    override suspend fun registerWithGoogle(name: String, email: String): Result<Unit> {
        profileFlow.value = profileFlow.value.copy(isRegistered = true, name = name, email = email, authProvider = "google")
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + 200)
        return Result.success(Unit)
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): Result<Unit> {
        profileFlow.value = profileFlow.value.copy(isRegistered = true, name = name, email = email, authProvider = "email")
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + 200)
        return Result.success(Unit)
    }

    override suspend fun applyReferralCode(code: String): Pair<Boolean, String> {
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + 150)
        return true to "Claimed 150 free coins"
    }

    override suspend fun simulateFriendReferral() {
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + 150)
    }

    override suspend fun signOut() {
        profileFlow.value = com.example.domain.model.UserProfile()
    }

    override fun getDailyCheckInStatus(): Flow<com.example.domain.model.DailyCheckInStatus> = flowOf(com.example.domain.model.DailyCheckInStatus())

    override suspend fun claimDailyCheckIn(): Pair<Boolean, String> = true to "Claimed"
}
