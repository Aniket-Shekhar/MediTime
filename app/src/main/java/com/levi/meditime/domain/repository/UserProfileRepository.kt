package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getAllUserProfiles(): Flow<Resource<List<UserProfile>>>
    suspend fun addUserProfile(profile: UserProfile): Resource<Unit>
    suspend fun selectProfile(profileId: String): Resource<Unit>
    suspend fun getSelectedProfile(): Flow<Resource<UserProfile?>>
    suspend fun deleteUserProfile(profile: UserProfile): Resource<Unit>
}
