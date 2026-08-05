package com.levi.meditime.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.levi.meditime.R
import com.levi.meditime.manager.LocationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SosLocationService : Service() {

    @Inject
    lateinit var locationManager: LocationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getStringExtra("USER_ID") ?: return START_NOT_STICKY
        
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates(userId)
        
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(userId: String) {
        locationJob?.cancel()
        locationJob = serviceScope.launch {
            while (isActive) {
                locationManager.shareLiveLocation(userId)
                delay(30000) // 30 seconds
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "sos_location_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SOS Live Location",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOS Active")
            .setContentText("Sharing your live location with emergency contacts.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationJob?.cancel()
        serviceScope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 999
    }
}
