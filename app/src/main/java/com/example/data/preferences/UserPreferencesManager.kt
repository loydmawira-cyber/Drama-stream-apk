package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.UserProfile
import com.example.domain.model.UserWallet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val COIN_BALANCE = intPreferencesKey("user_coin_balance")
        val IS_VIP = booleanPreferencesKey("user_is_vip")
        val VIP_EXPIRY = longPreferencesKey("user_vip_expiry")

        // Registration & Profile Keys
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val AUTH_PROVIDER = stringPreferencesKey("auth_provider")
        val REFERRAL_CODE = stringPreferencesKey("user_referral_code")
        val REFERRED_BY = stringPreferencesKey("user_referred_by")
        val TOTAL_REFERRALS = intPreferencesKey("total_referrals")
        val REFERRAL_COINS_EARNED = intPreferencesKey("referral_coins_earned")
        val REGISTERED_AT = longPreferencesKey("registered_at")

        // Daily Check-In Keys
        val CHECKIN_STREAK = intPreferencesKey("checkin_streak")
        val LAST_CHECKIN_TIME = longPreferencesKey("last_checkin_time")
    }

    val dailyCheckInStatus: Flow<com.example.domain.model.DailyCheckInStatus> = context.dataStore.data.map { preferences ->
        val streak = preferences[PreferencesKeys.CHECKIN_STREAK] ?: 0
        val lastTime = preferences[PreferencesKeys.LAST_CHECKIN_TIME] ?: 0L
        val now = System.currentTimeMillis()
        val twentyFourHoursMs = 24L * 60 * 60 * 1000
        val canCheckIn = (now - lastTime) >= twentyFourHoursMs || lastTime == 0L
        val reward = if (streak >= 7) 100 else 50

        com.example.domain.model.DailyCheckInStatus(
            currentStreak = streak,
            lastCheckInTimestamp = lastTime,
            canCheckInToday = canCheckIn,
            nextRewardCoins = reward
        )
    }

    val userWallet: Flow<UserWallet> = context.dataStore.data.map { preferences ->
        val coins = preferences[PreferencesKeys.COIN_BALANCE] ?: 500
        val isVip = preferences[PreferencesKeys.IS_VIP] ?: false
        val expiry = preferences[PreferencesKeys.VIP_EXPIRY]
        UserWallet(coinBalance = coins, isVip = isVip, vipExpiresAt = expiry)
    }

    val userProfile: Flow<UserProfile> = context.dataStore.data.map { preferences ->
        val isRegistered = preferences[PreferencesKeys.IS_REGISTERED] ?: false
        val email = preferences[PreferencesKeys.USER_EMAIL] ?: ""
        val name = preferences[PreferencesKeys.USER_NAME] ?: if (isRegistered) "Drama Lover" else "Guest"
        val id = preferences[PreferencesKeys.USER_ID] ?: "guest_${System.currentTimeMillis() % 10000}"
        val provider = preferences[PreferencesKeys.AUTH_PROVIDER] ?: "guest"
        val refCode = preferences[PreferencesKeys.REFERRAL_CODE] ?: "DRAMA-VIP-77"
        val referredBy = preferences[PreferencesKeys.REFERRED_BY]
        val totalRefs = preferences[PreferencesKeys.TOTAL_REFERRALS] ?: 0
        val refCoins = preferences[PreferencesKeys.REFERRAL_COINS_EARNED] ?: 0
        val registeredAt = preferences[PreferencesKeys.REGISTERED_AT] ?: 0L

        UserProfile(
            id = id,
            email = email,
            name = name,
            avatarUrl = null,
            isRegistered = isRegistered,
            authProvider = provider,
            referralCode = refCode,
            referredBy = referredBy,
            totalReferrals = totalRefs,
            referralCoinsEarned = refCoins,
            registeredAt = registeredAt
        )
    }

    suspend fun registerUser(
        name: String,
        email: String,
        provider: String = "google",
        welcomeBonusCoins: Int = 200
    ) {
        val generatedRefCode = "DRAMA-" + email.substringBefore("@").uppercase().take(4) + "-" + (100..999).random()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_REGISTERED] = true
            preferences[PreferencesKeys.USER_ID] = UUID.randomUUID().toString()
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.AUTH_PROVIDER] = provider
            preferences[PreferencesKeys.REFERRAL_CODE] = generatedRefCode
            preferences[PreferencesKeys.REGISTERED_AT] = System.currentTimeMillis()

            // Credit +200 free registration welcome coins
            val currentCoins = preferences[PreferencesKeys.COIN_BALANCE] ?: 500
            preferences[PreferencesKeys.COIN_BALANCE] = currentCoins + welcomeBonusCoins
        }
    }

    suspend fun applyReferralCode(code: String, rewardCoins: Int = 150): Pair<Boolean, String> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return false to "Please enter a valid referral code."
        }

        var success = false
        var message = ""

        context.dataStore.edit { preferences ->
            val userCode = preferences[PreferencesKeys.REFERRAL_CODE] ?: "DRAMA-VIP-77"
            val alreadyReferredBy = preferences[PreferencesKeys.REFERRED_BY]

            if (alreadyReferredBy != null) {
                success = false
                message = "You have already claimed a referral bonus code ($alreadyReferredBy)."
            } else if (cleanCode == userCode.uppercase()) {
                success = false
                message = "You cannot use your own referral code."
            } else {
                preferences[PreferencesKeys.REFERRED_BY] = cleanCode
                val current = preferences[PreferencesKeys.COIN_BALANCE] ?: 500
                preferences[PreferencesKeys.COIN_BALANCE] = current + rewardCoins
                success = true
                message = "Success! Claimed $rewardCoins Free Coins from referral code $cleanCode."
            }
        }

        return success to message
    }

    suspend fun simulateFriendReferral(rewardCoins: Int = 150) {
        context.dataStore.edit { preferences ->
            val totalRefs = preferences[PreferencesKeys.TOTAL_REFERRALS] ?: 0
            val refCoins = preferences[PreferencesKeys.REFERRAL_COINS_EARNED] ?: 0
            val currentBalance = preferences[PreferencesKeys.COIN_BALANCE] ?: 500

            preferences[PreferencesKeys.TOTAL_REFERRALS] = totalRefs + 1
            preferences[PreferencesKeys.REFERRAL_COINS_EARNED] = refCoins + rewardCoins
            preferences[PreferencesKeys.COIN_BALANCE] = currentBalance + rewardCoins
        }
    }

    suspend fun signOut() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_REGISTERED] = false
            preferences[PreferencesKeys.USER_EMAIL] = ""
            preferences[PreferencesKeys.USER_NAME] = "Guest"
            preferences[PreferencesKeys.AUTH_PROVIDER] = "guest"
        }
    }

    suspend fun deductCoins(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.COIN_BALANCE] ?: 500
            if (current >= amount) {
                preferences[PreferencesKeys.COIN_BALANCE] = current - amount
                success = true
            } else {
                success = false
            }
        }
        return success
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.COIN_BALANCE] ?: 500
            preferences[PreferencesKeys.COIN_BALANCE] = current + amount
        }
    }

    suspend fun setVipStatus(isVip: Boolean, durationDays: Int = 30) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_VIP] = isVip
            if (isVip) {
                val expiry = System.currentTimeMillis() + (durationDays.toLong() * 24 * 60 * 60 * 1000)
                preferences[PreferencesKeys.VIP_EXPIRY] = expiry
            } else {
                preferences.remove(PreferencesKeys.VIP_EXPIRY)
            }
        }
    }

    suspend fun claimDailyCheckIn(): Pair<Boolean, String> {
        var success = false
        var message = ""

        context.dataStore.edit { preferences ->
            val lastTime = preferences[PreferencesKeys.LAST_CHECKIN_TIME] ?: 0L
            val now = System.currentTimeMillis()
            val twentyFourHoursMs = 24L * 60 * 60 * 1000
            val canCheckIn = (now - lastTime) >= twentyFourHoursMs || lastTime == 0L

            if (!canCheckIn && lastTime != 0L) {
                val hoursLeft = ((twentyFourHoursMs - (now - lastTime)) / (60 * 60 * 1000)).coerceAtLeast(1L)
                success = false
                message = "Already checked in today! Next check-in available in ~$hoursLeft hours."
            } else {
                val currentStreak = preferences[PreferencesKeys.CHECKIN_STREAK] ?: 0
                val fortyEightHoursMs = 48L * 60 * 60 * 1000
                // If more than 48 hours passed, reset streak to 1. Otherwise increment streak.
                val newStreak = if (lastTime != 0L && (now - lastTime) > fortyEightHoursMs) {
                    1
                } else {
                    currentStreak + 1
                }

                val reward = if (newStreak >= 7) 100 else 50
                val currentCoins = preferences[PreferencesKeys.COIN_BALANCE] ?: 500

                preferences[PreferencesKeys.CHECKIN_STREAK] = newStreak
                preferences[PreferencesKeys.LAST_CHECKIN_TIME] = now
                preferences[PreferencesKeys.COIN_BALANCE] = currentCoins + reward

                success = true
                message = "🎉 Daily Check-In Success! Streak: Day $newStreak. Earned +$reward Free Coins!"
            }
        }

        return success to message
    }
}
