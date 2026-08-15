package com.levi.meditime.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val age: Int? = null,
    val gender: String? = null,
    val profileImage: String? = null,
    val emergencyContact: String? = null,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val failedOtpAttempts: Int = 0,
    val otpLockedUntil: Long = 0,
    val lastOtpSentAt: Long = 0,
    val hourlyOtpCount: Int = 0
)
