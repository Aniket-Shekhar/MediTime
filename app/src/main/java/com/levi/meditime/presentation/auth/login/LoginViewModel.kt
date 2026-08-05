package com.levi.meditime.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isPhoneLoginMode = MutableStateFlow(false)
    val isPhoneLoginMode = _isPhoneLoginMode.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun onPhoneNumberChange(newPhone: String) { _phoneNumber.value = newPhone }
    fun toggleLoginMode() { _isPhoneLoginMode.value = !_isPhoneLoginMode.value }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isPhoneLoginMode.value) {
            if (_phoneNumber.value.isBlank()) {
                onError("Please enter a phone number")
                return
            }
            onSuccess() // This will navigate to OtpScreen via NavGraph
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = loginUseCase(_email.value, _password.value)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> onError(result.message ?: "Login failed")
                is Resource.Loading -> {}
            }
        }
    }
}