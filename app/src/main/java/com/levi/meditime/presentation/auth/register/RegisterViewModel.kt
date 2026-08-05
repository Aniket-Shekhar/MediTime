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

    private val _age = MutableStateFlow("")
    val age = _age.asStateFlow()

    private val _gender = MutableStateFlow("Male")
    val gender = _gender.asStateFlow()

    private val _emergencyContact = MutableStateFlow("")
    val emergencyContact = _emergencyContact.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onNameChange(value: String) { _name.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onAgeChange(value: String) { _age.value = value }
    fun onGenderChange(value: String) { _gender.value = value }
    fun onEmergencyContactChange(value: String) { _emergencyContact.value = value }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = registerUseCase(
                name = _name.value,
                email = _email.value,
                pass = _password.value,
                age = _age.value.toIntOrNull() ?: 0,
                gender = _gender.value,
                emergencyContact = _emergencyContact.value
            )
            _isLoading.value = false
            when (result) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> onError(result.message ?: "Registration failed")
                is Resource.Loading -> {}
            }
        }
    }
}