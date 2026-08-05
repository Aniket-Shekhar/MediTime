package com.levi.meditime.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.levi.meditime.MainActivity
import com.levi.meditime.R
import com.levi.meditime.presentation.reminder.AlarmActivity
import com.levi.meditime.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "medicine_reminders"
        const val STOCK_CHANNEL_ID = "stock_alerts"
        const val MISSED_CHANNEL_ID = "missed_medicines"
        const val CARETAKER_CHANNEL_ID = "caretaker_alerts"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.reminder_channel_desc)
                enableVibration(true)
            }

            val stockChannel = NotificationChannel(
                STOCK_CHANNEL_ID,
                context.getString(R.string.stock_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.stock_channel_desc)
            }

            val missedChannel = NotificationChannel(
                MISSED_CHANNEL_ID,
                "Missed Medicine Alerts", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts when a dose is missed"
            }

            val caretakerChannel = NotificationChannel(
                CARETAKER_CHANNEL_ID,
                "Caretaker Alerts", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for caregivers"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(stockChannel)
            notificationManager.createNotificationChannel(missedChannel)
            notificationManager.createNotificationChannel(caretakerChannel)
        }
    }

    fun showMedicineReminder(medicineName: String, reminderId: String) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("REMINDER_ID", reminderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("REMINDER_ID", reminderId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_SKIP"
            putExtra("REMINDER_ID", reminderId)
            putExtra("MEDICINE_NAME", medicineName)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("REMINDER_ID", reminderId)
            putExtra("MEDICINE_NAME", medicineName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.reminder_notif_title))
            .setContentText(context.getString(R.string.reminder_notif_body, medicineName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.action_taken), takenPendingIntent)
            .addAction(0, context.getString(R.string.skip), skipPendingIntent)
            .addAction(0, context.getString(R.string.snooze), snoozePendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun showLowStockAlert(medicineName: String, remaining: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, STOCK_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.low_stock_notif_title))
            .setContentText(context.getString(R.string.low_stock_notif_body, remaining, medicineName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medicineName.hashCode(), notification)
    }
}