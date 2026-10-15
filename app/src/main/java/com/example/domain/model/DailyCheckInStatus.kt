package com.example.domain.model

data class DailyCheckInStatus(
    val currentStreak: Int = 0,
    val lastCheckInTimestamp: Long = 0L,
    val canCheckInToday: Boolean = true,
    val nextRewardCoins: Int = 50
)
