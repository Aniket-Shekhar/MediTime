package com.levi.meditime.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.levi.meditime.domain.repository.AppointmentRepository
import com.levi.meditime.manager.ReminderNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class AppointmentWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AppointmentRepository,
    private val notificationManager: ReminderNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val appointments = repository.getAllAppointments().first().data ?: emptyList()
            val now = System.currentTimeMillis()
            
            appointments.forEach { appointment ->
                val diff = appointment.date - now
                
                // 1 Day before
                if (diff in 0..(24 * 3600 * 1000) && !appointment.notifiedOneDayBefore) {
                    notificationManager.showMedicineReminder("Appointment tomorrow with Dr. ${appointment.doctorName}", appointment.appointmentId)
                    repository.updateAppointment(appointment.copy(notifiedOneDayBefore = true))
                }

                // 1 Hour before
                if (diff in 0..(3600 * 1000) && !appointment.notifiedOneHourBefore) {
                    notificationManager.showMedicineReminder("Appointment in 1 hour with Dr. ${appointment.doctorName}", appointment.appointmentId)
                    repository.updateAppointment(appointment.copy(notifiedOneHourBefore = true))
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("AppointmentWorker", "Error", e)
            Result.retry()
        }
    }
}
