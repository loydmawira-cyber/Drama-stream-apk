package com.example.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseSignInRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    val user: SupabaseUserDto? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
    val id: String,
    val email: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseProfileDto(
    val id: String,
    val email: String,
    val username: String? = null,
    @Json(name = "coin_balance") val coinBalance: Int = 500,
    @Json(name = "is_vip") val isVip: Boolean = false,
    @Json(name = "referral_code") val referralCode: String? = null,
    @Json(name = "referred_by") val referredBy: String? = null,
    @Json(name = "total_referrals") val totalReferrals: Int = 0,
    @Json(name = "referral_coins_earned") val referralCoinsEarned: Int = 0
)

@JsonClass(generateAdapter = true)
data class SupabaseReferralDto(
    @Json(name = "referrer_code") val referrerCode: String,
    @Json(name = "referee_id") val refereeId: String,
    @Json(name = "bonus_awarded") val bonusAwarded: Int = 150
)
