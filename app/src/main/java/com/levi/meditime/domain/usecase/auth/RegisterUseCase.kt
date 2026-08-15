package com.levi.meditime.domain.usecase.auth

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        pass: String
    ): Resource<String> {
        if (name.isBlank()) return Resource.Error("Please enter your name")
        if (email.isBlank()) return Resource.Error("Please enter your email address")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Please enter a valid email address")
        }
        if (pass.length < 6) return Resource.Error("Password must be at least 6 characters")

        val authResult = authRepository.register(email, pass)
        
        if (authResult is Resource.Success) {
            val uid = authResult.data ?: ""
            val user = User(
                uid = uid,
                name = name,
                email = email
            )
            val dbResult = userRepository.saveUser(user)
            return if (dbResult is Resource.Success) {
                Resource.Success(uid)
            } else {
                // If profile creation fails, we still have the Auth account, 
                // but the app might be in a half-ready state.
                // We'll let the user proceed and handle profile recovery during login.
                Resource.Success(uid)
            }
        }
        
        return Resource.Error(authResult.message ?: "Registration failed")
    }
}