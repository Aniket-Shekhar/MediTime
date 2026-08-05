package com.levi.meditime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.manager.AlarmManagerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles device reboot recovery for Alarms.
 * Declared with ACTION_BOOT_COMPLETED in Manifest.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: MedicineRepository

    @Inject
    lateinit var alarmHelper: AlarmManagerHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && 
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        Log.d("BootReceiver", "Event received: ${intent.action}. Restoring alarms...")
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // PRD 1.2: Use Flow.first() to get a snapshot of medicines for recovery
                val medicines = repository.getAllMedicines().first()
                medicines.forEach { medicine ->
                    val reminders = repository.getRemindersForMedicine(medicine.id).first()
                    reminders.filter { it.isActive }.forEach { reminder ->
                        // Only schedule if the time is in the future
                        if (reminder.time > System.currentTimeMillis()) {
                            alarmHelper.schedule(medicine, reminder)
                        }
                    }
                }
                Log.d("BootReceiver", "Alarms successfully restored.")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to restore alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
