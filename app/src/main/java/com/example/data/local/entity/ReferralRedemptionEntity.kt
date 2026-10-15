package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.ReferralRedemption
import java.util.UUID

@Entity(
    tableName = "referral_redemptions",
    indices = [
        Index(value = ["referralCode"]),
        Index(value = ["refereeUserId"], unique = true)
    ]
)
data class ReferralRedemptionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val referralCode: String,
    val refereeUserId: String,
    val refereeName: String,
    val refereeEmail: String,
    val bonusAwarded: Int = 150,
    val redeemedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ReferralRedemption = ReferralRedemption(
        id = id,
        referralCode = referralCode,
        refereeUserId = refereeUserId,
        refereeName = refereeName,
        refereeEmail = refereeEmail,
        bonusAwarded = bonusAwarded,
        redeemedAt = redeemedAt
    )
}
