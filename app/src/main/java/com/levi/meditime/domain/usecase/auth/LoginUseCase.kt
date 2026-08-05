package com.levi.meditime.domain.usecase.auth

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String): Resource<String> {
        if (email.isBlank() || pass.isBlank()) {
            return Resource.Error("Email and password cannot be empty")
        }
        return authRepository.login(email, pass)
    }
}