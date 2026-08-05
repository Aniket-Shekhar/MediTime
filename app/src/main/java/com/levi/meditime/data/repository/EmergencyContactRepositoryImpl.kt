package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.EmergencyContact
import com.levi.meditime.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

class EmergencyContactRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth,
    private val auditLogManager: AuditLogManager
) : EmergencyContactRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllEmergencyContacts(): Flow<Resource<List<EmergencyContact>>> {
        return dao.getAllEmergencyContacts().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun insertEmergencyContact(contact: EmergencyContact): Resource<Unit> {
        return try {
            if (userId.isEmpty()) return Resource.Error("User not logged in")
            dao.insertEmergencyContact(contact.toEntity(userId))
            dbRef.child("users").child(userId).child("emergencyContacts").child(contact.id).setValue(contact).await()
            auditLogManager.logEvent(AuditLogManager.ACTION_EMERGENCY_CONTACT_ADD, contact.id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not save emergency contact")
        }
    }

    override suspend fun deleteEmergencyContact(contact: EmergencyContact): Resource<Unit> {
        return try {
            if (userId.isEmpty()) return Resource.Error("User not logged in")
            dao.deleteEmergencyContact(contact.toEntity(userId))
            dbRef.child("users").child(userId).child("emergencyContacts").child(contact.id).removeValue().await()
            auditLogManager.logEvent(AuditLogManager.ACTION_EMERGENCY_CONTACT_REMOVE, contact.id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not delete emergency contact")
        }
    }

    override suspend fun getEmergencyContactById(id: String): EmergencyContact? {
        return dao.getEmergencyContactById(id)?.toDomain()
    }
}