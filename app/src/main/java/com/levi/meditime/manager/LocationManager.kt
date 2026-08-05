package com.levi.meditime.manager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class LocationManager(private val context: Context, private val firestore: FirebaseFirestore) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun shareLiveLocation(userId: String): Location? {
        return try {
            val result: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            result?.let { location ->
                val data = mapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("users").document(userId)
                    .collection("live_location").document("current")
                    .set(data).await()
            }
            result
        } catch (e: Exception) {
            null
        }
    }
}
