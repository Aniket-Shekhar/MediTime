package com.levi.meditime.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.LinkedDevice
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LinkedDevicesState(
    val linkedDevices: List<LinkedDevice> = emptyList(),
    val inviteCode: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showOtpDialog: OtpType? = null,
    val isOtpLoading: Boolean = false,
    val otpError: String? = null
)

@HiltViewModel
class LinkedDevicesViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val dbRef: DatabaseReference,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LinkedDevicesState())
    val state: StateFlow<LinkedDevicesState> = _state.asStateFlow()

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadLinkedDevices()
    }

    private fun loadLinkedDevices() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val snapshot = dbRef.child("users").child(userId).child("linked_devices").get().await()
                val devices = snapshot.children.mapNotNull { it.getValue(LinkedDevice::class.java) }
                val codeSnapshot = dbRef.child("users").child(userId).child("invite_code").get().await()
                val code = codeSnapshot.getValue(String::class.java)
                _state.value = _state.value.copy(linkedDevices = devices, inviteCode = code, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun startSensitiveAction(type: OtpType) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            if (!user.emailVerified || !user.phoneVerified) {
                _state.value = _state.value.copy(otpError = "Verification required for sensitive actions")
                return@launch
            }

            _state.value = _state.value.copy(isOtpLoading = true, otpError = null)
            val result = authRepository.sendSensitiveActionOtp(userId, type)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = type, isOtpLoading = false)
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun verifySensitiveAction(otp: String, type: OtpType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isOtpLoading = true)
            val result = authRepository.verifySensitiveActionOtp(userId, otp, type)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = null, isOtpLoading = false)
                onSuccess()
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun dismissOtpDialog() {
        _state.value = _state.value.copy(showOtpDialog = null, otpError = null)
    }

    fun generateInviteCode() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            if (!user.emailVerified || !user.phoneVerified) {
                _state.value = _state.value.copy(error = "Verification required to generate invite codes")
                return@launch
            }

            try {
                val code = (100000..999999).random().toString()
                dbRef.child("users").child(userId).child("invite_code").setValue(code).await()
                _state.value = _state.value.copy(inviteCode = code)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to generate code: ${e.message}")
            }
        }
    }

    fun joinWithCode(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Simplified join logic
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }
}
