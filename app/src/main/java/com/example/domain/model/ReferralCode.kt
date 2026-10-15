package com.example.domain.model

data class ReferralCode(
    val code: String,
    val referrerUserId: String,
    val referrerName: String,
    val totalUses: Int,
    val maxUses: Int,
    val bonusCoins: Int,
    val createdAt: Long,
    val isActive: Boolean
)
