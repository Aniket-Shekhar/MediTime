package com.levi.meditime.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.manager.LocationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class LiveLocationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val locationManager: LocationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val user = authRepository.currentUser.first()
            if (user != null) {
                locationManager.shareLiveLocation(user.uid)
                
                // Reschedule itself for 30s later
                val nextWork = OneTimeWorkRequestBuilder<LiveLocationWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "live_location",
                    ExistingWorkPolicy.REPLACE,
                    nextWork
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("LiveLocationWorker", "Error: ${e.message}")
            Result.retry()
        }
    }
}
