package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Prescription
import kotlinx.coroutines.flow.Flow

interface PrescriptionRepository {
    fun getAllPrescriptions(): Flow<Resource<List<Prescription>>>
    suspend fun uploadPrescription(prescription: Prescription, uri: android.net.Uri): Resource<Unit>
    suspend fun deletePrescription(prescription: Prescription): Resource<Unit>
}
