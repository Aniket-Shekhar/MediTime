package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.Symptom
import com.levi.meditime.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.core.util.SecurityUtils

class SymptomRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth
) : SymptomRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllSymptoms(): Flow<Resource<List<Symptom>>> {
        return dao.getAllSymptoms(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun addSymptom(symptom: Symptom): Resource<Unit> {
        return try {
            dao.insertSymptom(symptom.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("symptoms").child(symptom.symptomId).setValue(symptom).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to add symptom")
        }
    }

    override suspend fun deleteSymptom(symptom: Symptom): Resource<Unit> {
        return try {
            dao.deleteSymptom(symptom.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("symptoms").child(symptom.symptomId).removeValue().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to delete symptom")
        }
    }

    override fun getSymptomsForTimeline(days: Int): Flow<Resource<List<Symptom>>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = calendar.timeInMillis
        return dao.getSymptomsFromDate(userId, startDate).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }
}
