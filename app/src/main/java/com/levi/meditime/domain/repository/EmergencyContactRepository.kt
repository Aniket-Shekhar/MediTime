package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {
    fun getAllEmergencyContacts(): Flow<Resource<List<EmergencyContact>>>
    suspend fun insertEmergencyContact(contact: EmergencyContact): Resource<Unit>
    suspend fun deleteEmergencyContact(contact: EmergencyContact): Resource<Unit>
    suspend fun getEmergencyContactById(id: String): EmergencyContact?
}