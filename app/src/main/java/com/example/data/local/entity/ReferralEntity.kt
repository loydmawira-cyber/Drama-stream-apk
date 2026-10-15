package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ReferralCode

@Entity(tableName = "referral_codes")
data class ReferralEntity(
    @PrimaryKey
    val code: String,
    val referrerUserId: String,
    val referrerName: String,
    val totalUses: Int = 0,
    val maxUses: Int = 1000,
    val bonusCoins: Int = 150,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    fun toDomain(): ReferralCode = ReferralCode(
        code = code,
        referrerUserId = referrerUserId,
        referrerName = referrerName,
        totalUses = totalUses,
        maxUses = maxUses,
        bonusCoins = bonusCoins,
        createdAt = createdAt,
        isActive = isActive
    )
}
