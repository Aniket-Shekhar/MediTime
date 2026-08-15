package com.levi.meditime.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.usecase.auth.LoginUseCase
import com.levi.meditime.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
    private val themePreferences: com.levi.meditime.datastore.ThemePreferences
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

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError = _phoneError.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe = _rememberMe.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.userSettings.collect { settings ->
                _rememberMe.value = settings.rememberMe
            }
        }
    }

    fun onEmailChange(newEmail: String) { 
        _email.value = newEmail 
        if (newEmail.isNotBlank()) _emailError.value = null
    }
    fun onPasswordChange(newPassword: String) { 
        _password.value = newPassword 
        if (newPassword.isNotBlank()) _passwordError.value = null
    }
    fun onPhoneNumberChange(newPhone: String) { 
        _phoneNumber.value = newPhone 
        if (newPhone.isNotBlank()) _phoneError.value = null
    }
    fun onRememberMeChange(value: Boolean) {
        _rememberMe.value = value
        viewModelScope.launch {
            themePreferences.setRememberMe(value)
        }
    }
    fun toggleLoginMode() { 
        _isPhoneLoginMode.value = !_isPhoneLoginMode.value 
        _emailError.value = null
        _passwordError.value = null
        _phoneError.value = null
    }

    fun forgotPassword(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (_email.value.isBlank()) {
            _emailError.value = "Please enter your email address"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.resetPassword(_email.value)
            _isLoading.value = false
            if (result is Resource.Success<*>) {
                onSuccess("Password reset email sent to ${_email.value}")
            } else {
                onError(result.message ?: "Failed to send reset email")
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        if (_isPhoneLoginMode.value) {
            if (_phoneNumber.value.isBlank()) {
                _phoneError.value = "Please enter a phone number"
                isValid = false
            }
        } else {
            if (_email.value.isBlank()) {
                _emailError.value = "Please enter your email address"
                isValid = false
            }
            if (_password.value.isBlank()) {
                _passwordError.value = "Please enter your password"
                isValid = false
            }
        }
        return isValid
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!validate()) return

        if (_isPhoneLoginMode.value) {
            onSuccess() // This will navigate to OtpScreen via NavGraph
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = loginUseCase(_email.value, _password.value)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> {
                    val message = result.message ?: "Login failed"
                    if (message.contains("email", ignoreCase = true)) {
                        _emailError.value = message
                    } else if (message.contains("password", ignoreCase = true)) {
                        _passwordError.value = message
                    } else {
                        onError(message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}