package com.levi.meditime.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    fun onNameChange(value: String) { 
        _name.value = value 
        if (value.isNotBlank()) _nameError.value = null
    }
    fun onEmailChange(value: String) { 
        _email.value = value 
        if (value.isNotBlank()) _emailError.value = null
    }
    fun onPasswordChange(value: String) { 
        _password.value = value 
        if (value.length >= 6) _passwordError.value = null
    }

    private fun validate(): Boolean {
        var isValid = true
        if (_name.value.isBlank()) {
            _nameError.value = "Please enter your name"
            isValid = false
        }
        if (_email.value.isBlank()) {
            _emailError.value = "Please enter your email address"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Please enter a valid email address"
            isValid = false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Please enter a password"
            isValid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "Password must be at least 6 characters"
            isValid = false
        }
        return isValid
    }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                kotlinx.coroutines.withTimeout(15000L) {
                    val result = registerUseCase(
                        name = _name.value,
                        email = _email.value,
                        pass = _password.value
                    )
                    _isLoading.value = false
                    when (result) {
                        is Resource.Success -> onSuccess()
                        is Resource.Error -> {
                            val message = result.message ?: "Registration failed"
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
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _isLoading.value = false
                onError("Registration timed out. Please check your connection.")
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "An unexpected error occurred")
            }
        }
    }
}