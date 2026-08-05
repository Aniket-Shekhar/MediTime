package com.levi.meditime.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.manager.ReminderNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RefillReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicineRepository: MedicineRepository,
    private val notificationManager: ReminderNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val medicines = medicineRepository.getAllMedicines().first()
            
            medicines.forEach { medicine ->
                // 1. Check Low Stock
                if (medicine.currentStock <= medicine.minimumStock) {
                    notificationManager.showLowStockAlert(medicine.name, medicine.currentStock)
                }

                // 2. Refill Estimation
                val reminders = medicineRepository.getRemindersForMedicine(medicine.id).first()
                val activeReminders = reminders.filter { it.isActive }.size
                
                if (activeReminders > 0) {
                    val dailyConsumption = medicine.unitsPerDose * activeReminders
                    val daysRemaining = if (dailyConsumption > 0) medicine.currentStock / dailyConsumption else 0
                    
                    if (daysRemaining <= 2) {
                        notificationManager.showMedicineReminder(
                            "Refill Alert: Buy ${medicine.name} in next 2 days.",
                            medicine.id
                        )
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("RefillWorker", "Error", e)
            Result.retry()
        }
    }
}
