package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.Caretaker
import com.levi.meditime.domain.repository.CaretakerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

class CaretakerRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth,
    private val auditLogManager: AuditLogManager
) : CaretakerRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllCaretakers(): Flow<Resource<List<Caretaker>>> {
        return dao.getAllCaretakers(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun addCaretaker(caretaker: Caretaker): Resource<Unit> {
        return try {
            dao.insertCaretaker(caretaker.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("caretakers").child(caretaker.caretakerId).setValue(caretaker).await()
                auditLogManager.logEvent("CARETAKER_ADD", caretaker.caretakerId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to add caretaker")
        }
    }

    override suspend fun deleteCaretaker(caretaker: Caretaker): Resource<Unit> {
        return try {
            dao.deleteCaretaker(caretaker.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("caretakers").child(caretaker.caretakerId).removeValue().await()
                auditLogManager.logEvent("CARETAKER_DELETE", caretaker.caretakerId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to delete caretaker")
        }
    }

    override suspend fun updateCaretaker(caretaker: Caretaker): Resource<Unit> {
        return try {
            dao.insertCaretaker(caretaker.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("caretakers").child(caretaker.caretakerId).setValue(caretaker).await()
                auditLogManager.logEvent("CARETAKER_UPDATE", caretaker.caretakerId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to update caretaker")
        }
    }
}
