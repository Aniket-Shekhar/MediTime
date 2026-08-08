package com.levi.meditime.core.util

import com.google.firebase.database.DatabaseException
import com.levi.meditime.core.common.Resource
import com.google.firebase.crashlytics.FirebaseCrashlytics

suspend fun <T> safeRtdbCall(block: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(block())
    } catch (e: DatabaseException) {
        FirebaseCrashlytics.getInstance().recordException(e)
        android.util.Log.e("FirebaseUtils", "RTDB Error: ${e.message}")
        Resource.Error(
            if (e.message?.contains("Permission denied", ignoreCase = true) == true)
                "Access denied. Please check your login status."
            else
                "Database error. Please try again later."
        )
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Resource.Error(e.message ?: "An unexpected error occurred")
    }
}
