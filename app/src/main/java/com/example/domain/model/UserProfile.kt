package com.example.domain.model

data class UserProfile(
    val id: String = "guest_user",
    val email: String = "",
    val name: String = "Guest Drama Lover",
    val avatarUrl: String? = null,
    val isRegistered: Boolean = false,
    val authProvider: String = "guest", // "google", "email", "guest"
    val referralCode: String = "DRAMA-VIP-77",
    val referredBy: String? = null,
    val totalReferrals: Int = 0,
    val referralCoinsEarned: Int = 0,
    val registeredAt: Long = 0L
)
