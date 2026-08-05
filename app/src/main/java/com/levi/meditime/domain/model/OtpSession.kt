package com.levi.meditime.domain.model

data class OtpSession(
    val sessionId: String,
    val userId: String,
    val type: OtpType,
    val otp: String,
    val expiresAt: Long,
    val attempts: Int = 0,
    val isVerified: Boolean = false
)

enum class OtpType {
    SIGNUP, EMAIL_CHANGE, PHONE_CHANGE, PASSWORD_RESET, LINK_CARETAKER, ADD_EMERGENCY, DELETE_ACCOUNT, EXPORT_REPORT, SHARE_LOCATION
}
