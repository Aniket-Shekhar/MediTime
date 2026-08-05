package com.levi.meditime.presentation.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.domain.repository.UserRepository
import com.levi.meditime.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val medicineRepository: MedicineRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _medicinesCount = MutableStateFlow(0)
    val medicinesCount: StateFlow<Int> = _medicinesCount.asStateFlow()

    private val _takenCount = MutableStateFlow(0)
    val takenCount: StateFlow<Int> = _takenCount.asStateFlow()

    private val _missedCount = MutableStateFlow(0)
    val missedCount: StateFlow<Int> = _missedCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showOtpDialog = MutableStateFlow<OtpType?>(null)
    val showOtpDialog = _showOtpDialog.asStateFlow()

    private val _isOtpLoading = MutableStateFlow(false)
    val isOtpLoading = _isOtpLoading.asStateFlow()

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError = _otpError.asStateFlow()

    private val _requiresReauth = MutableStateFlow(false)
    val requiresReauth = _requiresReauth.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _user.value = user
            }
        }

        viewModelScope.launch {
            medicineRepository.getMedicinesCount().collectLatest { count ->
                _medicinesCount.value = count
            }
        }

        viewModelScope.launch {
            medicineRepository.getTakenDosesCount().collectLatest { count ->
                _takenCount.value = count
            }
        }

        viewModelScope.launch {
            medicineRepository.getMissedDosesCount().collectLatest { count ->
                _missedCount.value = count
            }
        }
    }

    fun startSensitiveAction(type: OtpType) {
        viewModelScope.launch {
            val user = _user.value ?: return@launch
            _isOtpLoading.value = true
            _otpError.value = null
            val result = authRepository.sendSensitiveActionOtp(user.uid, type)
            if (result is Resource.Success) {
                _showOtpDialog.value = type
                _isOtpLoading.value = false
            } else {
                _otpError.value = result.message
                _isOtpLoading.value = false
            }
        }
    }

    fun verifySensitiveAction(otp: String, type: OtpType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = _user.value ?: return@launch
            _isOtpLoading.value = true
            val result = authRepository.verifySensitiveActionOtp(user.uid, otp, type)
            if (result is Resource.Success) {
                _showOtpDialog.value = null
                _isOtpLoading.value = false
                onSuccess()
            } else {
                _otpError.value = result.message
                _isOtpLoading.value = false
            }
        }
    }

    fun dismissOtpDialog() {
        _showOtpDialog.value = null
        _otpError.value = null
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = _user.value ?: return@launch
            when (val result = userRepository.deleteUser(user.uid)) {
                is Resource.Success -> onComplete()
                is Resource.Error -> {
                    if (result.message == "REAUTH_REQUIRED") {
                        _requiresReauth.value = true
                    } else {
                        _error.value = result.message
                    }
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun dismissReauth() {
        _requiresReauth.value = false
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            val user = _user.value ?: return@launch
            _isLoading.value = true
            when (val result = userRepository.uploadProfileImage(user.uid, uri)) {
                is Resource.Success -> {
                    _message.value = "Profile image updated successfully"
                    val updatedUser = user.copy(profileImage = result.data ?: "")
                    userRepository.updateUser(updatedUser)
                }
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun removeProfileImage() {
        viewModelScope.launch {
            val user = _user.value ?: return@launch
            _isLoading.value = true
            val updatedUser = user.copy(profileImage = "")
            when (val result = userRepository.updateUser(updatedUser)) {
                is Resource.Success -> {
                    _message.value = "Profile image removed successfully"
                }
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val trimmedEmail = email.trim()
            // PRD 1.4: Use verifyBeforeUpdateEmail instead of updateEmail (deprecated)
            when (val result = authRepository.verifyBeforeUpdateEmail(trimmedEmail)) {
                is Resource.Success -> {
                    _message.value = "Verification link sent to $trimmedEmail. Please confirm to complete the change."
                    authRepository.reloadUser()
                }
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updatePhone(phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Implementation of phone update logic
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _message.value = null
        _error.value = null
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onComplete()
        }
    }
}
