package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun register(email: String, pass: String): Resource<String>
    suspend fun login(email: String, pass: String): Resource<String>
    suspend fun logout()
    suspend fun logoutFromAllDevices(): Resource<Unit>
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun sendEmailVerification(): Resource<Unit>
    suspend fun updateEmail(newEmail: String): Resource<Unit>
    suspend fun verifyBeforeUpdateEmail(newEmail: String): Resource<Unit>
    suspend fun reloadUser(): Resource<Unit>
    fun isEmailVerified(): Boolean
    suspend fun ensureTokenPropagation() // Part of PRD 8/6 fix
    
    // Phone Verification
    fun sendOtp(
        phoneNumber: String, 
        activity: android.app.Activity,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun verifyOtp(
        verificationId: String, 
        otp: String,
        countryCode: String? = null,
        countryIso: String? = null
    ): Resource<Unit>

    // Email OTP Verification
    suspend fun sendEmailOtp(email: String): Resource<String>
    suspend fun verifyEmailOtp(email: String, otp: String): Resource<Unit>

    // Lockout & Limits
    suspend fun checkOtpLockout(userId: String): Resource<Unit>
    suspend fun resetOtpAttempts(userId: String)

    // Sensitive Action OTP
    suspend fun sendSensitiveActionOtp(userId: String, type: com.levi.meditime.domain.model.OtpType): Resource<String>
    suspend fun verifySensitiveActionOtp(userId: String, otp: String, type: com.levi.meditime.domain.model.OtpType): Resource<Unit>
}