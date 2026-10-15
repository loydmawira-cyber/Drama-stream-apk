package com.example.domain.model

data class UserWallet(
    val coinBalance: Int = 500,
    val isVip: Boolean = false,
    val vipExpiresAt: Long? = null
)
