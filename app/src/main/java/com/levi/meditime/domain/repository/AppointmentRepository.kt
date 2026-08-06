package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAllAppointments(): Flow<Resource<List<Appointment>>>
    fun getAppointmentsForMember(memberId: String): Flow<Resource<List<Appointment>>>
    suspend fun addAppointment(appointment: Appointment): Resource<Unit>
    suspend fun updateAppointment(appointment: Appointment): Resource<Unit>
    suspend fun deleteAppointment(appointment: Appointment): Resource<Unit>
}
