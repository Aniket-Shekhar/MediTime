package com.levi.meditime.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.domain.repository.EmergencyContactRepository
import com.levi.meditime.manager.ReminderNotificationManager
import com.levi.meditime.presentation.emergency.EmergencyAlertActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

import com.levi.meditime.domain.repository.CaretakerRepository
import com.levi.meditime.datastore.ThemePreferences
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

@HiltWorker
class MissedDoseWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicineRepository: MedicineRepository,
    private val emergencyRepository: EmergencyContactRepository,
    private val caretakerRepository: CaretakerRepository,
    private val themePreferences: ThemePreferences,
    private val notificationManager: ReminderNotificationManager,
    private val analyticsManager: com.levi.meditime.manager.AnalyticsManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString("REMINDER_ID") ?: return Result.failure()
        val medicineId = inputData.getString("MEDICINE_ID") ?: return Result.failure()
        val retryCount = inputData.getInt("RETRY_COUNT", 0)

        // Check if medicine was taken
        val history = medicineRepository.getHistoryForMedicine(medicineId).first()
        val isTaken = history.any { it.reminderId == reminderId && it.status == DoseStatus.TAKEN }

        if (isTaken) return Result.success()

        val medicine = medicineRepository.getMedicineById(medicineId) ?: return Result.failure()
        
        if (retryCount == 0) {
            analyticsManager.logReminderMissed(medicine.name)
            // 1. Notify Patient
            notificationManager.showMedicineReminder("MISSED: ${medicine.name}", reminderId)
            
            // 2. Notify Caretakers
            val caretakers = caretakerRepository.getAllCaretakers().first().data ?: emptyList()
            caretakers.forEach { caretaker ->
                // PRD 1.1: Scrubbed PII from log
            Log.d("MissedDoseWorker", "Notifying caretakers for missed medicine")
                // In a real app, send FCM to caretaker.fcmToken here.
                // For simulation, we can show a special notification if we want, or just log.
            }

            // 3. Schedule Escalation in 15 minutes
            val workManager = WorkManager.getInstance(context)
            val data = workDataOf(
                "REMINDER_ID" to reminderId,
                "MEDICINE_ID" to medicineId,
                "RETRY_COUNT" to 1
            )
            val escalationRequest = OneTimeWorkRequestBuilder<MissedDoseWorker>()
                .setInputData(data)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .addTag("ESCALATION_$reminderId")
                .build()

            workManager.enqueueUniqueWork(
                "ESCALATION_$reminderId",
                ExistingWorkPolicy.REPLACE,
                escalationRequest
            )
        } else {
            // Urgent Escalation
            Log.d("MissedDoseWorker", "URGENT ESCALATION: Medicine still not taken.")
            
            val settings = themePreferences.userSettings.first()
            if (settings.autoCallEmergencyEnabled) {
                val emergencyContacts = emergencyRepository.getAllEmergencyContacts().first().data ?: emptyList()
                val primaryContact = emergencyContacts.find { it.notifyOnMissedDose }
                
                if (primaryContact != null) {
                    val intent = Intent(context, EmergencyAlertActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("CONTACT_NAME", primaryContact.name)
                        putExtra("PHONE_NUMBER", primaryContact.phoneNumber)
                        putExtra("MEDICINE_NAME", medicine.name)
                        putExtra("IS_URGENT", true)
                    }
                    context.startActivity(intent)
                }
            }
        }

        return Result.success()
    }
}
