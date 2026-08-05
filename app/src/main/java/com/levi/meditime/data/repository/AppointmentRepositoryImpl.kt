package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.Appointment
import com.levi.meditime.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.core.util.SecurityUtils

class AppointmentRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth
) : AppointmentRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllAppointments(): Flow<Resource<List<Appointment>>> {
        return dao.getAllAppointments(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun addAppointment(appointment: Appointment): Resource<Unit> {
        return try {
            val updated = appointment.copy(userId = userId)
            dao.insertAppointment(updated.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("appointments").child(updated.appointmentId).setValue(updated).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to add appointment")
        }
    }

    override suspend fun updateAppointment(appointment: Appointment): Resource<Unit> {
        return try {
            dao.updateAppointment(appointment.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("appointments").child(appointment.appointmentId).setValue(appointment).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Failed to update")
        }
    }

    override suspend fun deleteAppointment(appointment: Appointment): Resource<Unit> {
        return try {
            dao.deleteAppointment(appointment.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("users").child(userId).child("appointments").child(appointment.appointmentId).removeValue().await()
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
