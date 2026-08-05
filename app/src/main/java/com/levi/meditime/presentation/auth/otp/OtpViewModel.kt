package com.levi.meditime.presentation.auth.otp


import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.UserRepository
import com.levi.meditime.manager.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _otp = MutableStateFlow("")
    val otp = _otp.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _timer = MutableStateFlow(40)
    val timer = _timer.asStateFlow()

    private val _resendAttempts = MutableStateFlow(0)
    val resendAttempts = _resendAttempts.asStateFlow()

    private val _isLockedOut = MutableStateFlow(false)
    val isLockedOut = _isLockedOut.asStateFlow()

    private val _canResend = MutableStateFlow(false)
    val canResend = _canResend.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isVerified = MutableStateFlow(false)
    val isVerified = _isVerified.asStateFlow()

    private var timerJob: Job? = null
    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                currentUser = user
            }
        }
    }

    fun onOtpChange(newOtp: String) {
        if (newOtp.length <= 6) {
            _otp.value = newOtp
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        android.util.Log.d("AUTH_AUDIT", "sendOtp event triggered")
        _isLoading.value = true
        authRepository.sendOtp(
            phoneNumber = phoneNumber,
            activity = activity,
            onCodeSent = { id ->
                _verificationId.value = id
                _isLoading.value = false
                startTimer()
            },
            onVerificationCompleted = {
                // Auto-verification handled by Firebase on some devices
                verifyPhoneNumber(phoneNumber)
            },
            onError = { err ->
                _error.value = err
                _isLoading.value = false
            }
        )
    }

    fun resendOtp(phoneNumber: String, activity: Activity) {
        if (_canResend.value) {
            _resendAttempts.value += 1
            sendOtp(phoneNumber, activity)
        }
    }

    fun verifyOtp(phoneNumber: String) {
        val id = _verificationId.value ?: return
        val code = _otp.value
        if (code.length != 6) return

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.verifyOtp(id, code, null, null)) {
                is Resource.Success -> verifyPhoneNumber(phoneNumber)
                is Resource.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                    analyticsManager.logOtpFailed("PHONE", result.message ?: "Unknown error")
                    
                    // Simple Lockout Track (in view model for UI response)
                    if (result.message?.contains("lock", ignoreCase = true) == true) {
                        _isLockedOut.value = true
                    }
                }
                else -> {}
            }
        }
    }

    private fun verifyPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            currentUser?.let { user ->
                val updatedUser = user.copy(
                    phoneNumber = phoneNumber,
                    phoneVerified = true
                )
                when (val result = userRepository.updateUser(updatedUser)) {
                    is Resource.Success -> _isVerified.value = true
                    is Resource.Error -> _error.value = result.message
                    else -> {}
                }
            }
            _isLoading.value = false
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _canResend.value = false
        _timer.value = 60 // PRD 1.2: 60s timer
        timerJob = viewModelScope.launch {
            while (_timer.value > 0) {
                delay(1000)
                _timer.value -= 1
            }
            _canResend.value = true
        }
    }

    fun clearError() {
        _error.value = null
    }
}
