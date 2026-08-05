package com.levi.meditime.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.levi.meditime.domain.repository.MedicineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MedicineRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val trace = FirebasePerformance.getInstance().newTrace("sync_worker_do_work")
        trace.start()
        return try {
            val result = repository.syncPendingData()
            if (result is com.levi.meditime.core.common.Resource.Success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.retry()
        } finally {
            trace.stop()
        }
    }
}
