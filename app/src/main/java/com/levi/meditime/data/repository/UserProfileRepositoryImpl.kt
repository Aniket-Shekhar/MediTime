package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.UserProfile
import com.levi.meditime.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.core.util.SecurityUtils

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth
) : UserProfileRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllUserProfiles(): Flow<Resource<List<UserProfile>>> {
        return dao.getAllUserProfiles().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun addUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            dao.insertUserProfile(profile.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("userProfiles").child(profile.profileId).setValue(profile).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to add profile")
        }
    }

    override suspend fun selectProfile(profileId: String): Resource<Unit> {
        return try {
            dao.deselectAllProfiles()
            dao.selectProfile(profileId)
            // Realtime Sync could be added here if needed to track selected profile across devices
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to select profile")
        }
    }

    override suspend fun getSelectedProfile(): Flow<Resource<UserProfile?>> {
        return dao.getAllUserProfiles().map { entities ->
            Resource.Success(entities.find { it.isSelected }?.toDomain())
        }
    }

    override suspend fun deleteUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            dao.deleteUserProfile(profile.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("userProfiles").child(profile.profileId).removeValue().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to delete")
        }
    }
}
