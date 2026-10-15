package com.example.domain.repository

import com.example.domain.model.UserProfile
import com.example.domain.model.UserWallet
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    fun getUserWallet(): Flow<UserWallet>
    fun getUserProfile(): Flow<UserProfile>
    suspend fun deductCoins(amount: Int): Boolean
    suspend fun addCoins(amount: Int)
    suspend fun setVipStatus(isVip: Boolean, durationDays: Int = 30)

    // Registration & Referral
    suspend fun registerWithGoogle(name: String, email: String): Result<Unit>
    suspend fun registerWithEmail(name: String, email: String, password: String): Result<Unit>
    suspend fun applyReferralCode(code: String): Pair<Boolean, String>
    suspend fun simulateFriendReferral()
    suspend fun signOut()

    // Daily Check-In
    fun getDailyCheckInStatus(): Flow<com.example.domain.model.DailyCheckInStatus>
    suspend fun claimDailyCheckIn(): Pair<Boolean, String>
}
