package com.levi.meditime.presentation.caretaker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Caretaker
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.CaretakerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CaretakerState(
    val caretakers: List<Caretaker> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showOtpDialog: Boolean = false,
    val isOtpLoading: Boolean = false,
    val otpError: String? = null
)

@HiltViewModel
class CaretakerViewModel @Inject constructor(
    private val repository: CaretakerRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CaretakerState())
    val state: StateFlow<CaretakerState> = _state.asStateFlow()

    private var userId: String = ""

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    userId = it.uid
                    loadCaretakers()
                }
            }
        }
    }

    private fun loadCaretakers() {
        viewModelScope.launch {
            repository.getAllCaretakers().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            caretakers = resource.data ?: emptyList(),
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

    fun startLinkProcess() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            if (!user.emailVerified || !user.phoneVerified) {
                _state.value = _state.value.copy(error = "Verification required to link caretakers")
                return@launch
            }

            _state.value = _state.value.copy(isOtpLoading = true, otpError = null)
            val result = authRepository.sendSensitiveActionOtp(userId, OtpType.LINK_CARETAKER)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = true, isOtpLoading = false)
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun verifyOtpAndAddCaretaker(otp: String, name: String, relation: String, email: String, phone: String, onVerified: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isOtpLoading = true)
            val result = authRepository.verifySensitiveActionOtp(userId, otp, OtpType.LINK_CARETAKER)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = false, isOtpLoading = false)
                addCaretaker(name, relation, email, phone)
                onVerified()
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun dismissOtpDialog() {
        _state.value = _state.value.copy(showOtpDialog = false, otpError = null)
    }

    private fun addCaretaker(name: String, relation: String, email: String, phone: String) {
        viewModelScope.launch {
            val caretaker = Caretaker(
                caretakerId = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                relation = relation,
                email = email,
                phone = phone,
                fcmToken = null
            )
            repository.addCaretaker(caretaker)
        }
    }

    fun deleteCaretaker(caretaker: Caretaker) {
        viewModelScope.launch {
            repository.deleteCaretaker(caretaker)
        }
    }
}
