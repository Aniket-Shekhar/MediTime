package com.levi.meditime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.work.*
import com.levi.meditime.worker.AppointmentWorker
import com.levi.meditime.worker.LiveLocationWorker
import com.levi.meditime.worker.RefillReminderWorker
import java.util.concurrent.TimeUnit

import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.levi.meditime.BuildConfig

@HiltAndroidApp
class MediTimeApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250 * 1024 * 1024) // PRD 5: Use flat 250MB instead of 2%
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            android.util.Log.d("OTP_DEBUG", "Initializing App Check Debug Provider")
            // PRD 1.3: Log debug token for Firebase Console registration
            android.util.Log.i("APP_CHECK_DEBUG", "***************************************************")
            android.util.Log.i("APP_CHECK_DEBUG", "FETCHING APP CHECK DEBUG TOKEN...")
            android.util.Log.i("APP_CHECK_DEBUG", "***************************************************")
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        // PRD 3.2: Notification channels are now managed by ReminderNotificationManager
        scheduleBackgroundWorkers()
    }

    private fun scheduleBackgroundWorkers() {
        val workManager = WorkManager.getInstance(this)

        // 1. Refill Reminder Worker (Every 6 hours)
        val refillRequest = PeriodicWorkRequestBuilder<RefillReminderWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniquePeriodicWork("refill_check", ExistingPeriodicWorkPolicy.KEEP, refillRequest)

        // 2. Appointment Worker (Every 1 hour)
        val appointmentRequest = PeriodicWorkRequestBuilder<AppointmentWorker>(1, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork("appointment_check", ExistingPeriodicWorkPolicy.KEEP, appointmentRequest)

        // 4. Sync Worker (Every 1 hour) - PRD 1.3
        val syncRequest = PeriodicWorkRequestBuilder<com.levi.meditime.worker.SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniquePeriodicWork("sync_data", ExistingPeriodicWorkPolicy.KEEP, syncRequest)
    }

}