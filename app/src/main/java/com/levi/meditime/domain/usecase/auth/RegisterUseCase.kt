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
        pass: String,
        age: Int,
        gender: String,
        emergencyContact: String
    ): Resource<String> {
        if (name.isBlank()) return Resource.Error("Name cannot be empty")
        if (email.isBlank()) return Resource.Error("Email cannot be empty")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Invalid email format")
        }
        if (pass.length < 6) return Resource.Error("Password must be at least 6 characters")
        if (age <= 0) return Resource.Error("Please enter a valid age")

        val authResult = authRepository.register(email, pass)
        
        if (authResult is Resource.Success) {
            val uid = authResult.data ?: ""
            val user = User(
                uid = uid,
                name = name,
                email = email,
                age = age,
                gender = gender,
                emergencyContact = emergencyContact
            )
            val dbResult = userRepository.saveUser(user)
            return if (dbResult is Resource.Success) {
                Resource.Success(uid)
            } else {
                Resource.Error(dbResult.message ?: "Could not save user profile")
            }
        }
        
        return Resource.Error(authResult.message ?: "Registration failed")
    }
}