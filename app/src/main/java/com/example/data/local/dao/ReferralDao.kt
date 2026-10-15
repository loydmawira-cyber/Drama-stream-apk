package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReferralEntity
import com.example.data.local.entity.ReferralRedemptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referral_codes WHERE code = :code LIMIT 1")
    suspend fun getReferralCode(code: String): ReferralEntity?

    @Query("SELECT * FROM referral_codes WHERE referrerUserId = :userId LIMIT 1")
    suspend fun getReferralCodeForUser(userId: String): ReferralEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferralCode(referral: ReferralEntity)

    @Query("UPDATE referral_codes SET totalUses = totalUses + 1 WHERE code = :code")
    suspend fun incrementReferralUses(code: String)

    @Query("SELECT * FROM referral_redemptions WHERE refereeUserId = :userId LIMIT 1")
    suspend fun getRedemptionByReferee(userId: String): ReferralRedemptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(redemption: ReferralRedemptionEntity)

    @Query("SELECT * FROM referral_redemptions WHERE referralCode = :code ORDER BY redeemedAt DESC")
    fun getRedemptionsForCode(code: String): Flow<List<ReferralRedemptionEntity>>
}
