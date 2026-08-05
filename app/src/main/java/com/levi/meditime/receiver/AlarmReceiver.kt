package com.levi.meditime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.model.History
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.manager.AlarmManagerHelper
import com.levi.meditime.manager.ReminderNotificationManager
import com.levi.meditime.worker.MissedDoseWorker
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.levi.meditime.datastore.ThemePreferences
import com.levi.meditime.R
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: ReminderNotificationManager
    
    @Inject
    lateinit var repository: MedicineRepository

    @Inject
    lateinit var alarmHelper: AlarmManagerHelper

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var ttsManager: com.levi.meditime.manager.TtsManager

    @Inject
    lateinit var analyticsManager: com.levi.meditime.manager.AnalyticsManager

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val medicineId = intent.getStringExtra("MEDICINE_ID") ?: ""
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    "ACTION_TAKEN" -> {
                        handleTaken(reminderId, medicineName)
                    }
                    "ACTION_SNOOZE" -> {
                        handleSnooze(reminderId, medicineName)
                    }
                    "ACTION_SKIP" -> {
                        handleSkip(reminderId, medicineName)
                    }
                    else -> {
                        // PRD 1.1: Standard trigger branch
                        notificationManager.showMedicineReminder(medicineName, reminderId)
                        
                        val settings = themePreferences.userSettings.first()
                        if (settings.voiceAlarmEnabled) {
                            val text = context.getString(R.string.reminder_tts_text, medicineName)
                            ttsManager.speak(text)
                        }

                        val delay = settings.missedReminderDelayMinutes
                        scheduleMissedDoseCheck(context, reminderId, medicineId, 0, delay)

                        // PRD 1.1: Reschedule for next occurrence
                        rescheduleRecurringAlarm(reminderId)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleMissedDoseCheck(context: Context, reminderId: String, medicineId: String, retryCount: Int, delayMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        val data = workDataOf(
            "REMINDER_ID" to reminderId,
            "MEDICINE_ID" to medicineId,
            "RETRY_COUNT" to retryCount
        )

        val checkRequest = OneTimeWorkRequestBuilder<MissedDoseWorker>()
            .setInputData(data)
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .addTag("MISSED_DOSE_$reminderId")
            .build()

        workManager.enqueueUniqueWork(
            "MISSED_DOSE_$reminderId",
            ExistingWorkPolicy.REPLACE,
            checkRequest
        )
    }

    private suspend fun handleTaken(reminderId: String, medicineName: String) {
        try {
            val reminder = repository.getReminderById(reminderId) ?: return
            val medicine = repository.getMedicineById(reminder.medicineId) ?: return

            // 1. Add History Entry
            val history = History(
                id = UUID.randomUUID().toString(),
                medicineId = medicine.id,
                reminderId = reminder.id,
                scheduledTime = reminder.time,
                actionTime = System.currentTimeMillis(),
                status = DoseStatus.TAKEN
            )
            repository.addHistoryEntry(history)
            analyticsManager.logReminderCompleted(medicineName)

            // 2. Decrease Stock
            val newStock = (medicine.currentStock - medicine.unitsPerDose).coerceAtLeast(0)
            val updatedMedicine = medicine.copy(currentStock = newStock)
            repository.updateMedicine(updatedMedicine)

            // 3. Show Low Stock Alert if needed
            if (newStock <= medicine.minimumStock) {
                notificationManager.showLowStockAlert(medicine.name, newStock)
            }

            Log.d("AlarmReceiver", "Marked $medicineName as taken, new stock: $newStock")
            notificationManager.cancelNotification(reminderId.hashCode())
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error handling taken action", e)
        }
    }

    private suspend fun handleSnooze(reminderId: String, medicineName: String) {
        val snoozeMinutes = themePreferences.userSettings.first().snoozeDurationMinutes
        alarmHelper.scheduleSnooze(medicineName, reminderId, snoozeMinutes)
        notificationManager.cancelNotification(reminderId.hashCode())
        Log.d("AlarmReceiver", "Snoozed $medicineName for $snoozeMinutes minutes")
    }

    private suspend fun handleSkip(reminderId: String, medicineName: String) {
        try {
            Log.d("AlarmReceiver", "Skipping reminder for $medicineName")
            val reminder = repository.getReminderById(reminderId) ?: return
            val history = History(
                id = UUID.randomUUID().toString(),
                medicineId = reminder.medicineId,
                reminderId = reminder.id,
                scheduledTime = reminder.time,
                actionTime = System.currentTimeMillis(),
                status = DoseStatus.SKIPPED
            )
            repository.addHistoryEntry(history)
            analyticsManager.logReminderSkipped(medicineName)
            notificationManager.cancelNotification(reminderId.hashCode())
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error handling skip action", e)
        }
    }

    private suspend fun rescheduleRecurringAlarm(reminderId: String) {
        try {
            val reminder = repository.getReminderById(reminderId) ?: return
            val medicine = repository.getMedicineById(reminder.medicineId) ?: return
            
            // PRD 1.1: Calculate next time
            val nextTime = com.levi.meditime.core.util.NextReminderCalculator.nextTriggerTime(
                currentTriggerTime = reminder.time,
                repeatType = reminder.repeatType,
                selectedDays = reminder.selectedDays
            )
            
            val nextReminder = reminder.copy(time = nextTime)
            repository.addReminder(nextReminder)
            alarmHelper.schedule(medicine, nextReminder)
            
            Log.d("AlarmReceiver", "Rescheduled ${medicine.name} for next occurrence at $nextTime")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to reschedule recurring alarm", e)
        }
    }
}
