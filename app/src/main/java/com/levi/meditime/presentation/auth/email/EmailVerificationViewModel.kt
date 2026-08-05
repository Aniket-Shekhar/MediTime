package com.levi.meditime.presentation.auth.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _otp = MutableStateFlow("")
    val otp = _otp.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _timer = MutableStateFlow(40)
    val timer = _timer.asStateFlow()

    private val _canResend = MutableStateFlow(false)
    val canResend = _canResend.asStateFlow()

    private val _isVerified = MutableStateFlow(false)
    val isVerified = _isVerified.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

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

    fun sendEmailOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.sendEmailOtp(email)) {
                is Resource.Success -> {
                    _message.value = "OTP sent to $email"
                    startTimer()
                }
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun verifyEmailOtp(email: String) {
        if (_otp.value.length != 6) return
        
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.verifyEmailOtp(email, _otp.value)) {
                is Resource.Success -> _isVerified.value = true
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _canResend.value = false
        _timer.value = 40
        timerJob = viewModelScope.launch {
            while (_timer.value > 0) {
                delay(1000)
                _timer.value -= 1
            }
            _canResend.value = true
        }
    }

    fun clearMessages() {
        _message.value = null
        _error.value = null
    }
}
