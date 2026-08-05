package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val dbRef: DatabaseReference,
    private val firestore: FirebaseFirestore,
    private val resendApi: com.levi.meditime.data.remote.resend.ResendApiService,
    private val auditLogManager: AuditLogManager
) : AuthRepository {

    init {
        // Use app language for Firebase Auth (e.g. for localized SMS)
        auth.useAppLanguage()
        Log.d("AUTH_AUDIT", "Firebase Initialized with Application Language")
    }

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    emailVerified = firebaseUser.isEmailVerified,
                    phoneVerified = !firebaseUser.phoneNumber.isNullOrEmpty()
                )
                trySend(user)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun register(email: String, pass: String): Resource<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: ""
            if (uid.isNotEmpty()) {
                val now = System.currentTimeMillis()
                dbRef.child("users").child(uid).child("security").updateChildren(mapOf(
                    "lastLoginTimestamp" to now,
                    "revocationTimestamp" to 0
                )).await()
            }
            auditLogManager.logEvent(AuditLogManager.ACTION_REGISTER, uid)
            Resource.Success(uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun login(email: String, pass: String): Resource<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: ""
            if (uid.isNotEmpty()) {
                dbRef.child("users").child(uid).child("security").child("lastLoginTimestamp").setValue(System.currentTimeMillis()).await()
                auditLogManager.logEvent(AuditLogManager.ACTION_LOGIN, uid)
            }
            Resource.Success(uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun logout() {
        auditLogManager.logEvent(AuditLogManager.ACTION_LOGOUT, auth.currentUser?.uid)
        auth.signOut()
    }

    override suspend fun logoutFromAllDevices(): Resource<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val revocationTime = System.currentTimeMillis()
            dbRef.child("users").child(uid).child("security").child("revocationTimestamp").setValue(revocationTime).await()
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Global logout failed")
        }
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Password reset failed")
        }
    }

    override suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not send verification email")
        }
    }

    override suspend fun updateEmail(newEmail: String): Resource<Unit> {
        return try {
            auth.currentUser?.updateEmail(newEmail)?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Email update failed. You might need to re-authenticate.")
        }
    }

    override suspend fun verifyBeforeUpdateEmail(newEmail: String): Resource<Unit> {
        return try {
            auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not send verification email")
        }
    }

    override suspend fun reloadUser(): Resource<Unit> {
        return try {
            auth.currentUser?.reload()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not reload user")
        }
    }

    override fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    override fun sendOtp(
        phoneNumber: String,
        activity: android.app.Activity,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("AUTH_AUDIT", "sendOtp event triggered")
        val uid = auth.currentUser?.uid
        
        // Whitelist known test numbers for development
        if (BuildConfig.DEBUG) {
            val testPhone = "+16505554567"
            val testCode = "123456"
            if (phoneNumber == testPhone) {
                auth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(testPhone, testCode)
                Log.d("AUTH_AUDIT", "Test number whitelisted")
            }
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.e("OTP_FLOW", ">>> onVerificationCompleted (Auto-Verify success)")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (auth.currentUser != null) {
                            auth.currentUser?.updatePhoneNumber(credential)?.await()
                        } else {
                            auth.signInWithCredential(credential).await()
                        }
                        onVerificationCompleted()
                    } catch (e: Exception) {
                        Log.e("OTP_FLOW", ">>> Verification Failed: ${e.localizedMessage}")
                        onError("Verification Failed: ${e.localizedMessage}")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("OTP_FLOW", ">>> onVerificationFailed: ${e.message}", e)
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid Phone Number"
                    is FirebaseTooManyRequestsException -> "Too Many Requests. Please try again later."
                    is FirebaseAuthMissingActivityForRecaptchaException -> "Firebase Configuration Error: Missing Activity for ReCAPTCHA"
                    else -> "Authentication Service Unavailable: ${e.message}"
                }
                onError(errorMessage)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("AUTH_AUDIT", "Phone code sent")
                // Track resend in profile
                uid?.let {
                    val profileRef = dbRef.child("users").child(it).child("profile")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val snapshot = profileRef.get().await()
                            val count = (snapshot.child("phoneResendCount").getValue(Int::class.java) ?: 0) + 1
                            profileRef.updateChildren(mapOf(
                                "phoneResendCount" to count, 
                                "lastPhoneOtpSent" to System.currentTimeMillis(),
                                "phoneOtpExpiry" to System.currentTimeMillis() + 300000 // 5 mins
                            ))
                        } catch (ex: Exception) {
                            Log.e("OTP_FLOW", ">>> DB Update Error: ${ex.message}")
                        }
                    }
                }
                onCodeSent(verificationId)
            }
        }

        try {
            Log.e("OTP_FLOW", ">>> Starting verifyPhoneNumber...")
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("OTP_FLOW", ">>> Exception in verifyPhoneNumber: ${e.message}", e)
            onError("Authentication Service Unavailable")
        }
    }

    override suspend fun verifyOtp(
        verificationId: String, 
        otp: String,
        countryCode: String?,
        countryIso: String?
    ): Resource<Unit> {
        return try {
            Log.d("AUTH_AUDIT", "Phone OTP verification started")
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")

            // Check Lockout
            when (val lockoutStatus = checkOtpLockout(uid)) {
                is Resource.Error -> return Resource.Error(lockoutStatus.message ?: "Locked")
                else -> {}
            }

            val profileRef = dbRef.child("users").child(uid).child("profile")
            val snapshot = profileRef.get().await()
            
            // Check Expiry
            val expiry = snapshot.child("phoneOtpExpiry").getValue(Long::class.java) ?: 0L
            if (System.currentTimeMillis() > expiry && !BuildConfig.DEBUG) { // Allow expired for testing/debug
                return Resource.Error("OTP Expired")
            }

            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            
            // PRD 1.1: Scrubbed verificationId from log
            Log.d("AUTH_AUDIT", "Attempting updatePhoneNumber")

            auth.currentUser?.updatePhoneNumber(credential)?.await()
            
            // Success
            Log.d("OTP", "Verification Successful")
            resetOtpAttempts(uid)
            
            val now = System.currentTimeMillis()
            val phone = auth.currentUser?.phoneNumber ?: ""
            
            // Sync across Firestore and RTDB
            val updateData = mutableMapOf<String, Any>(
                "phoneVerified" to true,
                "phone" to phone,
                "verificationTime" to now,
                "phoneResendCount" to 0,
                "failedOtpAttempts" to 0
            )
            
            countryCode?.let { updateData["countryCode"] = it }
            countryIso?.let { updateData["countryISO"] = it }
            
            profileRef.updateChildren(updateData).await()
            firestore.collection("users").document(uid).update(updateData).await()
            
            Log.d("OTP", "Firestore and RTDB Updated with Metadata")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("OTP", "Verification Failed", e)
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            
            // Increment failed attempts
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val profileRef = dbRef.child("users").child(uid).child("profile")
                val snapshot = profileRef.get().await()
                val attempts = (snapshot.child("failedOtpAttempts").getValue(Int::class.java) ?: 0) + 1
                
                if (attempts >= 5) {
                    val lockoutTime = System.currentTimeMillis() + 600000
                    profileRef.updateChildren(mapOf("failedOtpAttempts" to attempts, "otpLockedUntil" to lockoutTime))
                    Resource.Error("Too Many Requests. Locked for 10 minutes.")
                } else {
                    profileRef.updateChildren(mapOf("failedOtpAttempts" to attempts))
                    Resource.Error("Incorrect OTP. Attempt $attempts/5")
                }
            } else {
                Resource.Error("Incorrect OTP")
            }
        }
    }

    override suspend fun sendEmailOtp(email: String): Resource<String> {
        return try {
            Log.d("AUTH_AUDIT", "Email OTP requested")
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            
            val profileRef = dbRef.child("users").child(uid).child("profile")
            val snapshot = profileRef.get().await()
            
            // Check Lockout
            val lockedUntil = snapshot.child("otpLockedUntil").getValue(Long::class.java) ?: 0L
            if (System.currentTimeMillis() < lockedUntil) {
                val remaining = (lockedUntil - System.currentTimeMillis()) / 1000 / 60
                return Resource.Error("Too Many Requests. Please try again in $remaining minutes.")
            }

            // Check Max Resend
            val resendCount = snapshot.child("emailResendCount").getValue(Int::class.java) ?: 0
            if (resendCount >= 5) {
                val lockoutTime = System.currentTimeMillis() + 600000 // 10 mins
                profileRef.updateChildren(mapOf("otpLockedUntil" to lockoutTime, "emailResendCount" to 0)).await()
                return Resource.Error("Maximum resend attempts reached. Cooldown: 10 minutes.")
            }

            val otp = (100000..999999).random().toString()
            val now = System.currentTimeMillis()
            val expiry = now + 300000 // 5 minutes

            // Send via Resend API
            val emailHtml = """
                <div style="font-family: sans-serif; padding: 20px; color: #111;">
                    <h2 style="color: #4C8DFF;">MediTime Verification Code</h2>
                    <p>Your verification code is:</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 5px; padding: 10px; background: #F8F8F8; display: inline-block; border-radius: 8px;">
                        $otp
                    </div>
                    <p>This code will expire in 5 minutes.</p>
                    <p>If you didn't request this code, please ignore this email.</p>
                </div>
            """.trimIndent()

            val request = com.levi.meditime.data.remote.resend.ResendEmailRequest(
                from = "MediTime <verification@meditime-app.com>", // PRD 1.4: Placeholder for verified domain
                to = listOf(email),
                subject = "MediTime Verification Code",
                html = emailHtml
            )

            // PRD 2.1: Use BuildConfig for API key instead of hardcoded string
            val apiKey = "Bearer ${com.levi.meditime.BuildConfig.RESEND_API_KEY}"
            
            try {
                resendApi.sendEmail(apiKey, request)
                Log.d("AUTH_AUDIT", "Resend API call successful")
            } catch (e: Exception) {
                Log.e("AUTH_AUDIT", "Resend API call failed: ${e.message}")
                return Resource.Error("Couldn't send verification email right now. Please try again in a few minutes.")
            }

            val otpData = mapOf(
                "email" to email,
                "otp" to otp,
                "createdAt" to now,
                "expiryAt" to expiry,
                "verified" to false,
                "type" to "EMAIL_VERIFICATION"
            )
            
            firestore.collection("email_otps").document(uid).set(otpData).await()
            
            profileRef.updateChildren(mapOf(
                "lastEmailOtpSent" to now,
                "emailResendCount" to resendCount + 1,
                "emailOtpExpiry" to expiry
            )).await()
            
            Resource.Success(otp)
        } catch (e: Exception) {
            Log.e("OTP", "Email OTP implementation error", e)
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error("Service Unavailable")
        }
    }

    override suspend fun verifyEmailOtp(email: String, otp: String): Resource<Unit> {
        return try {
            Log.d("AUTH_AUDIT", "Email Verification Started")
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            
            // Check Lockout
            when (val lockoutStatus = checkOtpLockout(uid)) {
                is Resource.Error -> return Resource.Error(lockoutStatus.message ?: "Locked")
                else -> {}
            }

            val doc = firestore.collection("email_otps").document(uid).get().await()
            if (!doc.exists()) return Resource.Error("Incorrect OTP")

            val storedOtp = doc.getString("otp")
            val expiry = doc.getLong("expiryAt") ?: 0L

            if (System.currentTimeMillis() > expiry) {
                return Resource.Error("OTP Expired")
            }

            if (storedOtp == otp) {
                Log.d("AUTH_AUDIT", "Email verification successful")
                // Clear sensitive OTP data after success
                firestore.collection("email_otps").document(uid).delete().await()
                
                val now = System.currentTimeMillis()
                val updateData = mapOf(
                    "emailVerified" to true,
                    "email" to email,
                    "verificationTime" to now,
                    "failedOtpAttempts" to 0,
                    "emailResendCount" to 0
                )
                
                dbRef.child("users").child(uid).child("profile").updateChildren(updateData).await()
                firestore.collection("users").document(uid).update(updateData).await()
                
                Log.d("AUTH_AUDIT", "Firestore and RTDB updated after email verification")
                Resource.Success(Unit)
            } else {
                val profileRef = dbRef.child("users").child(uid).child("profile")
                val snapshot = profileRef.get().await()
                val attempts = (snapshot.child("failedOtpAttempts").getValue(Int::class.java) ?: 0) + 1
                
                if (attempts >= 5) {
                    val lockoutTime = System.currentTimeMillis() + 600000 
                    profileRef.updateChildren(mapOf("failedOtpAttempts" to attempts, "otpLockedUntil" to lockoutTime)).await()
                    Resource.Error("Too Many Requests. Locked for 10 minutes.")
                } else {
                    profileRef.updateChildren(mapOf("failedOtpAttempts" to attempts)).await()
                    Resource.Error("Incorrect OTP. Attempt $attempts/5")
                }
            }
        } catch (e: Exception) {
            Log.e("OTP", "Email Verification Failed", e)
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error("Verification Failed")
        }
    }

    override suspend fun checkOtpLockout(userId: String): Resource<Unit> {
        return try {
            val snapshot = dbRef.child("users").child(userId).child("profile").get().await()
            val lockedUntil = snapshot.child("otpLockedUntil").getValue(Long::class.java) ?: 0L
            if (System.currentTimeMillis() < lockedUntil) {
                val remaining = (lockedUntil - System.currentTimeMillis()) / 1000 / 60
                Resource.Error("Too Many Requests. Please try again in $remaining minutes.")
            } else {
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Success(Unit)
        }
    }

    override suspend fun resetOtpAttempts(userId: String) {
        dbRef.child("users").child(userId).child("profile").updateChildren(mapOf(
            "failedOtpAttempts" to 0,
            "otpLockedUntil" to 0
        ))
    }

    override suspend fun sendSensitiveActionOtp(userId: String, type: com.levi.meditime.domain.model.OtpType): Resource<String> {
        return try {
            val userDoc = dbRef.child("users").child(userId).child("profile").get().await()
            val lockedUntil = userDoc.child("otpLockedUntil").getValue(Long::class.java) ?: 0L
            if (System.currentTimeMillis() < lockedUntil) {
                return Resource.Error("Too Many Requests")
            }

            val otp = (100000..999999).random().toString()
            val sessionId = java.util.UUID.randomUUID().toString()
            
            val session = mapOf(
                "sessionId" to sessionId,
                "userId" to userId,
                "type" to type.name,
                "otp" to otp,
                "expiresAt" to System.currentTimeMillis() + 300000,
                "attempts" to 0
            )

            firestore.collection("otp_sessions").document(sessionId).set(session).await()
            dbRef.child("users").child(userId).child("profile").updateChildren(mapOf("lastOtpSent" to System.currentTimeMillis())).await()

            Log.d("AUTH_AUDIT", "Sensitive Action OTP Requested")
            Resource.Success(sessionId)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error("Authentication Service Unavailable")
        }
    }

    override suspend fun verifySensitiveActionOtp(userId: String, otp: String, type: com.levi.meditime.domain.model.OtpType): Resource<Unit> {
        return try {
            val query = firestore.collection("otp_sessions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type.name)
                .get()
                .await()

            val snapshot = query.documents.maxByOrNull { it.getLong("expiresAt") ?: 0L } ?: return Resource.Error("Incorrect OTP")
            val serverOtp = snapshot.getString("otp")
            val expiresAt = snapshot.getLong("expiresAt") ?: 0L
            var attempts = snapshot.getLong("attempts") ?: 0L

            if (System.currentTimeMillis() > expiresAt) {
                return Resource.Error("OTP Expired")
            }

            if (serverOtp == otp) {
                Log.d("AUTH_AUDIT", "Sensitive Action Verified")
                snapshot.reference.delete().await()
                Resource.Success(Unit)
            } else {
                attempts += 1
                snapshot.reference.update("attempts", attempts).await()
                
                if (attempts >= 5) {
                    dbRef.child("users").child(userId).child("profile")
                        .updateChildren(mapOf("otpLockedUntil" to System.currentTimeMillis() + 600000)).await()
                    Resource.Error("Too Many Requests")
                } else {
                    Resource.Error("Incorrect OTP")
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error("Verification Failed")
        }
    }
}
