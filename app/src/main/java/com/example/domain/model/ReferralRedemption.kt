package com.example.domain.model

data class ReferralRedemption(
    val id: String,
    val referralCode: String,
    val refereeUserId: String,
    val refereeName: String,
    val refereeEmail: String,
    val bonusAwarded: Int,
    val redeemedAt: Long
)
