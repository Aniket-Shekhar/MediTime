package com.levi.meditime.presentation.auth.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Country
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.UserRepository
import com.levi.meditime.manager.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.util.Log

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Email OTP states
    private val _showEmailOtpSection = MutableStateFlow(false)
    val showEmailOtpSection = _showEmailOtpSection.asStateFlow()
    
    private val _emailOtp = MutableStateFlow("")
    val emailOtp = _emailOtp.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified = _isEmailVerified.asStateFlow()

    private val _emailTimer = MutableStateFlow(40)
    val emailTimer = _emailTimer.asStateFlow()

    private val _canResendEmailOtp = MutableStateFlow(false)
    val canResendEmailOtp = _canResendEmailOtp.asStateFlow()

    private val _isEmailOtpExpired = MutableStateFlow(false)
    val isEmailOtpExpired = _isEmailOtpExpired.asStateFlow()

    // Phone OTP states
    private val _showPhoneOtpSection = MutableStateFlow(false)
    val showPhoneOtpSection = _showPhoneOtpSection.asStateFlow()
    
    private val _phoneOtp = MutableStateFlow("")
    val phoneOtp = _phoneOtp.asStateFlow()

    private val _isPhoneVerified = MutableStateFlow(false)
    val isPhoneVerified = _isPhoneVerified.asStateFlow()

    private val _phoneTimer = MutableStateFlow(40)
    val phoneTimer = _phoneTimer.asStateFlow()

    private val _canResendPhoneOtp = MutableStateFlow(false)
    val canResendPhoneOtp = _canResendPhoneOtp.asStateFlow()

    private val _isPhoneOtpExpired = MutableStateFlow(false)
    val isPhoneOtpExpired = _isPhoneOtpExpired.asStateFlow()

    private val _selectedCountry = MutableStateFlow(Country.ALL.first()) // Default to India
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _showCountryPicker = MutableStateFlow(false)
    val showCountryPicker = _showCountryPicker.asStateFlow()

    private var phoneVerificationId: String? = null
    private var emailTimerJob: Job? = null
    private var phoneTimerJob: Job? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { currentUser ->
                currentUser?.let { firebaseUser ->
                    userRepository.getUser(firebaseUser.uid).collectLatest { resource ->
                        if (resource is Resource.Success) {
                            val firestoreUser = resource.data
                            _user.value = firestoreUser
                            
                            // Check verification status from both sources
                            val emailVerified = firebaseUser.emailVerified || firestoreUser?.emailVerified == true
                            val phoneVerified = firebaseUser.phoneVerified || firestoreUser?.phoneVerified == true
                            
                            _isEmailVerified.value = emailVerified
                            _isPhoneVerified.value = phoneVerified
                            
                            // If mismatch, sync back to Firestore
                            if (firestoreUser != null && (emailVerified != firestoreUser.emailVerified || phoneVerified != firestoreUser.phoneVerified)) {
                                userRepository.updateUser(firestoreUser.copy(
                                    emailVerified = emailVerified,
                                    phoneVerified = phoneVerified
                                ))
                            }
                        }
                    }
                }
            }
        }
    }

    fun onEmailOtpChange(otp: String) { 
        if (otp.length <= 6 && otp.all { it.isDigit() }) _emailOtp.value = otp 
    }
    fun onPhoneOtpChange(otp: String) { 
        if (otp.length <= 6 && otp.all { it.isDigit() }) _phoneOtp.value = otp 
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
        _showCountryPicker.value = false
    }

    fun setShowCountryPicker(show: Boolean) {
        _showCountryPicker.value = show
    }

    fun onChangeEmail() {
        _isEmailVerified.value = false
        _showEmailOtpSection.value = false
        // Update model in Firebase if needed, or just let user save later
    }

    fun onChangePhone() {
        _isPhoneVerified.value = false
        _showPhoneOtpSection.value = false
    }

    fun sendEmailOtp(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            _error.value = "Email cannot be empty"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _error.value = "Invalid Email Address"
            return
        }

        viewModelScope.launch {
            Log.d("AUTH_AUDIT", "Email Validation Passed")
            Log.d("AUTH_AUDIT", "OTP Requested for Email")
            _isLoading.value = true
            _isEmailOtpExpired.value = false
            try {
                when (val result = authRepository.sendEmailOtp(trimmedEmail)) {
                    is Resource.Success -> {
                        _message.value = "OTP Sent Successfully"
                        _showEmailOtpSection.value = true
                        startEmailTimer()
                        Log.d("AUTH_AUDIT", "OTP Sent to Email")
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                        Log.e("AUTH_AUDIT", "Email OTP Failed: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _error.value = "Authentication Service Unavailable"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyEmailOtp(email: String, otp: String) {
        val trimmedEmail = email.trim()
        if (_isEmailOtpExpired.value) {
            _error.value = "OTP Expired"
            return
        }
        viewModelScope.launch {
            Log.d("AUTH_AUDIT", "Verification Started (Email)")
            _isLoading.value = true
            try {
                when (val result = authRepository.verifyEmailOtp(trimmedEmail, otp)) {
                    is Resource.Success -> {
                        _isEmailVerified.value = true
                        _showEmailOtpSection.value = false
                        _message.value = "Email verified successfully"
                        Log.d("AUTH_AUDIT", "Verification Successful")
                        Log.d("AUTH_AUDIT", "Firestore Updated")
                        
                        // Update user model locally
                        _user.value?.let { 
                            val updated = it.copy(emailVerified = true, email = trimmedEmail)
                            userRepository.updateUser(updated)
                            _user.value = updated
                        }
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Incorrect OTP"
                        Log.e("AUTH_AUDIT", "Verification Failed: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _error.value = "Verification Failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendPhoneOtp(phone: String, activity: android.app.Activity) {
        val context = activity.applicationContext
        if (phone.isBlank()) {
            _error.value = "Phone number cannot be empty"
            return
        }
        
        val dialCode = _selectedCountry.value.dialCode
        val countryIso = _selectedCountry.value.code
        
        // Full E.164 format for Firebase
        val fullPhone = if (phone.startsWith("+")) phone else "$dialCode$phone"
        
        if (!com.levi.meditime.presentation.components.validatePhoneNumber(context, phone, countryIso)) {
            _error.value = "Invalid Phone Number for ${_selectedCountry.value.name}"
            return
        }

        Log.d("AUTH_AUDIT", "Phone Validation Passed")
        Log.d("AUTH_AUDIT", "OTP Requested for Phone")
        _isLoading.value = true
        _isPhoneOtpExpired.value = false
        try {
            authRepository.sendOtp(
                phoneNumber = fullPhone,
                activity = activity,
                onCodeSent = { id ->
                    phoneVerificationId = id
                    _showPhoneOtpSection.value = true
                    startPhoneTimer()
                    _isLoading.value = false
                    Log.d("AUTH_AUDIT", "OTP Sent to Phone")
                },
                onVerificationCompleted = {
                    _isPhoneVerified.value = true
                    _showPhoneOtpSection.value = false
                    _isLoading.value = false
                    Log.d("AUTH_AUDIT", "Verification Successful (Auto)")
                    
                    // Update user model locally and sync
                    _user.value?.let { 
                        viewModelScope.launch {
                            val updated = it.copy(
                                phoneVerified = true, 
                                phoneNumber = fullPhone
                            )
                            userRepository.updateUser(updated)
                            _user.value = updated
                            Log.d("AUTH_AUDIT", "Firestore Updated")
                        }
                    }
                },
                onError = { err ->
                    _error.value = err
                    _isLoading.value = false
                    Log.e("AUTH_AUDIT", "Phone OTP Failed: $err")
                }
            )
        } catch (e: Exception) {
            _error.value = "Authentication Service Unavailable"
            _isLoading.value = false
        }
    }

    fun verifyPhoneOtp(otp: String, fullPhoneNumber: String) { // PRD Fix: Pass phone number to update local state
        if (_isPhoneOtpExpired.value) {
            _error.value = "OTP Expired"
            return
        }
        val id = phoneVerificationId ?: return
        val countryCode = _selectedCountry.value.dialCode
        val countryIso = _selectedCountry.value.code
        
        viewModelScope.launch {
            Log.d("AUTH_AUDIT", "Verification Started (Phone)")
            _isLoading.value = true
            try {
                when (val result = authRepository.verifyOtp(id, otp, countryCode, countryIso)) {
                    is Resource.Success -> {
                        _isPhoneVerified.value = true
                        _showPhoneOtpSection.value = false
                        _message.value = "Phone number verified successfully"
                        Log.d("AUTH_AUDIT", "Verification Successful")

                        // Update user model locally
                        _user.value?.let { 
                            val updated = it.copy(
                                phoneVerified = true,
                                phoneNumber = fullPhoneNumber // PRD Fix: Sync verified number
                            )
                            userRepository.updateUser(updated)
                            _user.value = updated
                            Log.d("AUTH_AUDIT", "Firestore and local user updated")
                        }
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Incorrect OTP"
                        Log.e("AUTH_AUDIT", "Verification Failed: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _error.value = "Verification Failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startEmailTimer() {
        emailTimerJob?.cancel()
        _emailTimer.value = 60 // PRD 1.3: 60s timer
        _canResendEmailOtp.value = false
        emailTimerJob = viewModelScope.launch {
            while (_emailTimer.value > 0) {
                delay(1000)
                _emailTimer.value -= 1
            }
            _canResendEmailOtp.value = true
            _isEmailOtpExpired.value = true
        }
    }

    private fun startPhoneTimer() {
        phoneTimerJob?.cancel()
        _phoneTimer.value = 60 // PRD 1.3: 60s timer
        _canResendPhoneOtp.value = false
        phoneTimerJob = viewModelScope.launch {
            while (_phoneTimer.value > 0) {
                delay(1000)
                _phoneTimer.value -= 1
            }
            _canResendPhoneOtp.value = true
            _isPhoneOtpExpired.value = true
        }
    }

    fun saveProfile(name: String, email: String, phoneNumber: String, age: Int, gender: String, emergencyContact: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            Log.d("AUTH_AUDIT", "Save profile clicked")
            
            if (!_isEmailVerified.value || !_isPhoneVerified.value) {
                _error.value = "Please verify email and phone number first."
                return@launch
            }

            if (name.isBlank()) {
                _error.value = "Name cannot be empty"
                return@launch
            }
            // Add other validations...

            _isLoading.value = true
            val currentUser = _user.value ?: return@launch
            val updatedUser = currentUser.copy(
                name = name,
                email = trimmedEmail,
                phoneNumber = phoneNumber,
                age = age,
                gender = gender,
                emergencyContact = emergencyContact,
                emailVerified = _isEmailVerified.value,
                phoneVerified = _isPhoneVerified.value
            )

            when (val result = userRepository.updateUser(updatedUser)) {
                is Resource.Success -> {
                    _user.value = updatedUser
                    _message.value = "Profile updated successfully"
                    analyticsManager.logProfileUpdated()
                }
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            val currentUser = _user.value ?: return@launch
            _isUploading.value = true
            when (val result = userRepository.uploadProfileImage(currentUser.uid, uri)) {
                is Resource.Success -> {
                    val updatedUser = currentUser.copy(profileImage = result.data ?: "")
                    userRepository.updateUser(updatedUser)
                    _user.value = updatedUser
                    _message.value = "Profile image updated successfully"
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Failed to upload profile picture. Please try again."
                    analyticsManager.logPfpUploadFailed(result.message ?: "Unknown error")
                }
                else -> {}
            }
            _isUploading.value = false
        }
    }

    fun clearMessages() {
        _message.value = null
        _error.value = null
    }
}
