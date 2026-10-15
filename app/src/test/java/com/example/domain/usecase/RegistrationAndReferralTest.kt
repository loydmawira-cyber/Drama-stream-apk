package com.example.domain.usecase

import com.example.domain.model.UserProfile
import com.example.domain.model.UserWallet
import com.example.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegistrationAndReferralTest {

    private lateinit var fakeRepo: TestUserDataRepository

    @Before
    fun setup() {
        fakeRepo = TestUserDataRepository()
    }

    @Test
    fun `registering with Google awards 200 welcome coins`() = runTest {
        // Initial balance: 500 coins
        assertEquals(500, fakeRepo.getUserWallet().first().coinBalance)
        assertFalse(fakeRepo.getUserProfile().first().isRegistered)

        // Register
        val result = fakeRepo.registerWithGoogle("Loyd Mawira", "loydmawira@gmail.com")

        assertTrue(result.isSuccess)
        val updatedProfile = fakeRepo.getUserProfile().first()
        assertTrue(updatedProfile.isRegistered)
        assertEquals("Loyd Mawira", updatedProfile.name)
        assertEquals("google", updatedProfile.authProvider)

        // Balance should now have +200 coins -> 700 coins
        assertEquals(700, fakeRepo.getUserWallet().first().coinBalance)
    }

    @Test
    fun `entering valid referral code awards 150 coins`() = runTest {
        val initialCoins = fakeRepo.getUserWallet().first().coinBalance

        val (success, _) = fakeRepo.applyReferralCode("DRAMA-FRIEND-123")

        assertTrue(success)
        assertEquals(initialCoins + 150, fakeRepo.getUserWallet().first().coinBalance)
        assertEquals("DRAMA-FRIEND-123", fakeRepo.getUserProfile().first().referredBy)
    }

    @Test
    fun `cannot use own referral code`() = runTest {
        val ownCode = fakeRepo.getUserProfile().first().referralCode
        val (success, message) = fakeRepo.applyReferralCode(ownCode)

        assertFalse(success)
        assertTrue(message.contains("cannot use your own", ignoreCase = true))
    }

    @Test
    fun `simulating friend referral increments count and adds 150 coins`() = runTest {
        val initialCoins = fakeRepo.getUserWallet().first().coinBalance
        val initialRefs = fakeRepo.getUserProfile().first().totalReferrals

        fakeRepo.simulateFriendReferral()

        assertEquals(initialCoins + 150, fakeRepo.getUserWallet().first().coinBalance)
        assertEquals(initialRefs + 1, fakeRepo.getUserProfile().first().totalReferrals)
    }
}

private class TestUserDataRepository : UserDataRepository {
    val walletFlow = MutableStateFlow(UserWallet(coinBalance = 500, isVip = false))
    val profileFlow = MutableStateFlow(UserProfile(referralCode = "DRAMA-LOYD-77"))

    override fun getUserWallet(): Flow<UserWallet> = walletFlow
    override fun getUserProfile(): Flow<UserProfile> = profileFlow

    override suspend fun deductCoins(amount: Int): Boolean {
        val cur = walletFlow.value.coinBalance
        return if (cur >= amount) {
            walletFlow.value = walletFlow.value.copy(coinBalance = cur - amount)
            true
        } else false
    }

    override suspend fun addCoins(amount: Int) {
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + amount)
    }

    override suspend fun setVipStatus(isVip: Boolean, durationDays: Int) {
        walletFlow.value = walletFlow.value.copy(isVip = isVip)
    }

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
        if (code.equals(profileFlow.value.referralCode, ignoreCase = true)) {
            return false to "You cannot use your own referral code."
        }
        profileFlow.value = profileFlow.value.copy(referredBy = code)
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + 150)
        return true to "Claimed 150 free coins"
    }

    override suspend fun simulateFriendReferral() {
        val curRefs = profileFlow.value.totalReferrals
        val curRefCoins = profileFlow.value.referralCoinsEarned
        profileFlow.value = profileFlow.value.copy(
            totalReferrals = curRefs + 1,
            referralCoinsEarned = curRefCoins + 150
        )
        walletFlow.value = walletFlow.value.copy(coinBalance = walletFlow.value.coinBalance + 150)
    }

    override suspend fun signOut() {
        profileFlow.value = UserProfile()
    }

    override fun getDailyCheckInStatus(): Flow<com.example.domain.model.DailyCheckInStatus> = flowOf(com.example.domain.model.DailyCheckInStatus())

    override suspend fun claimDailyCheckIn(): Pair<Boolean, String> = true to "Claimed"
}
