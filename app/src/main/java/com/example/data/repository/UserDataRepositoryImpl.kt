package com.example.data.repository

import com.example.data.local.dao.ReferralDao
import com.example.data.local.entity.ReferralEntity
import com.example.data.local.entity.ReferralRedemptionEntity
import com.example.data.preferences.UserPreferencesManager
import com.example.data.remote.supabase.SupabaseClient
import com.example.data.remote.supabase.SupabaseSignUpRequest
import com.example.domain.model.UserProfile
import com.example.domain.model.UserWallet
import com.example.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserDataRepositoryImpl(
    private val preferencesManager: UserPreferencesManager,
    private val referralDao: ReferralDao
) : UserDataRepository {

    override fun getUserWallet(): Flow<UserWallet> {
        return preferencesManager.userWallet
    }

    override fun getUserProfile(): Flow<UserProfile> {
        return preferencesManager.userProfile
    }

    override suspend fun deductCoins(amount: Int): Boolean {
        return preferencesManager.deductCoins(amount)
    }

    override suspend fun addCoins(amount: Int) {
        preferencesManager.addCoins(amount)
    }

    override suspend fun setVipStatus(isVip: Boolean, durationDays: Int) {
        preferencesManager.setVipStatus(isVip, durationDays)
    }

    override suspend fun registerWithGoogle(name: String, email: String): Result<Unit> {
        return try {
            if (SupabaseClient.isConfigured) {
                try {
                    SupabaseClient.authApi.signUp(
                        apiKey = SupabaseClient.anonKey,
                        request = SupabaseSignUpRequest(
                            email = email,
                            password = "GoogleUserPassword_${email.hashCode()}",
                            data = mapOf("full_name" to name, "provider" to "google")
                        )
                    )
                } catch (_: Exception) {}
            }

            preferencesManager.registerUser(
                name = name,
                email = email,
                provider = "google",
                welcomeBonusCoins = 200
            )

            // Ensure referral code is registered in database
            val profile = preferencesManager.userProfile.first()
            val existingCode = referralDao.getReferralCodeForUser(profile.id)
            if (existingCode == null) {
                referralDao.insertReferralCode(
                    ReferralEntity(
                        code = profile.referralCode,
                        referrerUserId = profile.id,
                        referrerName = profile.name
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): Result<Unit> {
        return try {
            if (SupabaseClient.isConfigured) {
                try {
                    SupabaseClient.authApi.signUp(
                        apiKey = SupabaseClient.anonKey,
                        request = SupabaseSignUpRequest(
                            email = email,
                            password = password,
                            data = mapOf("full_name" to name, "provider" to "email")
                        )
                    )
                } catch (_: Exception) {}
            }

            preferencesManager.registerUser(
                name = name,
                email = email,
                provider = "email",
                welcomeBonusCoins = 200
            )

            val profile = preferencesManager.userProfile.first()
            val existingCode = referralDao.getReferralCodeForUser(profile.id)
            if (existingCode == null) {
                referralDao.insertReferralCode(
                    ReferralEntity(
                        code = profile.referralCode,
                        referrerUserId = profile.id,
                        referrerName = profile.name
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyReferralCode(code: String): Pair<Boolean, String> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return false to "Please enter a valid referral code."
        }

        val profile = preferencesManager.userProfile.first()
        if (cleanCode == profile.referralCode.uppercase()) {
            return false to "You cannot use your own referral code."
        }

        // Check if already redeemed
        val existingRedemption = referralDao.getRedemptionByReferee(profile.id)
        if (existingRedemption != null) {
            return false to "You have already claimed a referral bonus using code ${existingRedemption.referralCode}."
        }

        // Validate code in database (or create a fallback valid entity if default demo code)
        var refEntity = referralDao.getReferralCode(cleanCode)
        if (refEntity == null && cleanCode.startsWith("DRAMA-")) {
            // Auto-register demo code if valid format
            refEntity = ReferralEntity(
                code = cleanCode,
                referrerUserId = "user_demo_${cleanCode.hashCode()}",
                referrerName = "Drama Friend"
            )
            referralDao.insertReferralCode(refEntity)
        }

        if (refEntity == null || !refEntity.isActive) {
            return false to "Invalid or expired referral code ($cleanCode)."
        }

        // Insert redemption and increment use count
        referralDao.insertRedemption(
            ReferralRedemptionEntity(
                referralCode = refEntity.code,
                refereeUserId = profile.id,
                refereeName = profile.name,
                refereeEmail = profile.email,
                bonusAwarded = refEntity.bonusCoins
            )
        )
        referralDao.incrementReferralUses(refEntity.code)

        // Apply bonus to local preferences
        val (success, msg) = preferencesManager.applyReferralCode(cleanCode, rewardCoins = refEntity.bonusCoins)
        return success to msg
    }

    override suspend fun simulateFriendReferral() {
        val profile = preferencesManager.userProfile.first()
        // Ensure user has a referral code in DB
        var refEntity = referralDao.getReferralCodeForUser(profile.id)
        if (refEntity == null) {
            refEntity = ReferralEntity(
                code = profile.referralCode,
                referrerUserId = profile.id,
                referrerName = profile.name
            )
            referralDao.insertReferralCode(refEntity)
        }

        referralDao.incrementReferralUses(refEntity.code)
        referralDao.insertRedemption(
            ReferralRedemptionEntity(
                referralCode = refEntity.code,
                refereeUserId = "simulated_friend_${System.currentTimeMillis()}",
                refereeName = "Simulated Friend",
                refereeEmail = "friend@dramastream.app",
                bonusAwarded = 150
            )
        )

        preferencesManager.simulateFriendReferral(rewardCoins = 150)
    }

    override suspend fun signOut() {
        preferencesManager.signOut()
    }

    override fun getDailyCheckInStatus(): Flow<com.example.domain.model.DailyCheckInStatus> {
        return preferencesManager.dailyCheckInStatus
    }

    override suspend fun claimDailyCheckIn(): Pair<Boolean, String> {
        return preferencesManager.claimDailyCheckIn()
    }
}
