package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUser(user: User): Resource<Unit>
    fun getUser(uid: String): Flow<Resource<User>>
    suspend fun updateUser(user: User): Resource<Unit>
    suspend fun deleteUser(uid: String): Resource<Unit>
    suspend fun uploadProfileImage(uid: String, imageUri: android.net.Uri): Resource<String>
}