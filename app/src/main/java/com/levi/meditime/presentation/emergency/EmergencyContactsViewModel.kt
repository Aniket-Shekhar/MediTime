package com.levi.meditime.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.EmergencyContact
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.EmergencyContactRepository
import com.levi.meditime.manager.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EmergencyContactsState(
    val contacts: List<EmergencyContact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showOtpDialog: Boolean = false,
    val isOtpLoading: Boolean = false,
    val otpError: String? = null
)

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val repository: EmergencyContactRepository,
    private val authRepository: AuthRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _state = MutableStateFlow(EmergencyContactsState())
    val state: StateFlow<EmergencyContactsState> = _state.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            repository.getAllEmergencyContacts().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            contacts = resource.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = resource.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun startAddContactProcess() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            
            if (!user.emailVerified || !user.phoneVerified) {
                _state.value = _state.value.copy(error = "Verification required to manage emergency contacts")
                return@launch
            }

            _state.value = _state.value.copy(isOtpLoading = true, otpError = null)
            val result = authRepository.sendSensitiveActionOtp(user.uid, OtpType.ADD_EMERGENCY)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = true, isOtpLoading = false)
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun verifyOtpAndAddContact(otp: String, name: String, relation: String, phone: String, notify: Boolean, onVerified: () -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            _state.value = _state.value.copy(isOtpLoading = true)
            val result = authRepository.verifySensitiveActionOtp(user.uid, otp, OtpType.ADD_EMERGENCY)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = false, isOtpLoading = false)
                addContact(name, relation, phone, notify)
                onVerified()
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun dismissOtpDialog() {
        _state.value = _state.value.copy(showOtpDialog = false, otpError = null)
    }

    private fun addContact(name: String, relation: String, phone: String, notify: Boolean) {
        viewModelScope.launch {
            val contact = EmergencyContact(
                id = UUID.randomUUID().toString(),
                name = name,
                relationship = relation,
                phoneNumber = phone,
                notifyOnMissedDose = notify
            )
            repository.insertEmergencyContact(contact)
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteEmergencyContact(contact)
        }
    }
}
