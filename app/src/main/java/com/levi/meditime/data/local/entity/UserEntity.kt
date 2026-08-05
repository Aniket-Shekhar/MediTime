package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val age: Int,
    val gender: String,
    val profileImage: String,
    val emergencyContact: String,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
    val failedOtpAttempts: Int = 0,
    val otpLockedUntil: Long = 0,
    val lastOtpSentAt: Long = 0,
    val hourlyOtpCount: Int = 0
)
