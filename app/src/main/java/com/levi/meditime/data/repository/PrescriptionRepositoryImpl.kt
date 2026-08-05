package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.Prescription
import com.levi.meditime.domain.repository.PrescriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

class PrescriptionRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val auditLogManager: AuditLogManager
) : PrescriptionRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllPrescriptions(): Flow<Resource<List<Prescription>>> {
        return dao.getAllPrescriptions(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun uploadPrescription(prescription: Prescription, uri: android.net.Uri): Resource<Unit> {
        return try {
            val fileName = "${prescription.prescriptionId}.${prescription.fileType.lowercase()}"
            val ref = storage.reference.child("prescriptions/$userId/$fileName")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            
            val updatedPrescription = prescription.copy(fileUrl = url, userId = userId)
            
            // Local
            dao.insertPrescription(updatedPrescription.toEntity())
            
            // Remote
            firestore.collection("users").document(userId)
                .collection("prescriptions").document(updatedPrescription.prescriptionId)
                .set(updatedPrescription).await()
                
            auditLogManager.logEvent(AuditLogManager.ACTION_PRESCRIPTION_UPLOAD, updatedPrescription.prescriptionId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to upload")
        }
    }

    override suspend fun deletePrescription(prescription: Prescription): Resource<Unit> {
        return try {
            dao.deletePrescription(prescription.toEntity())
            firestore.collection("users").document(userId)
                .collection("prescriptions").document(prescription.prescriptionId)
                .delete().await()
            auditLogManager.logEvent(AuditLogManager.ACTION_PRESCRIPTION_DELETE, prescription.prescriptionId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to delete")
        }
    }
}
