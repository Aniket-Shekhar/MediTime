package com.levi.meditime.manager

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    fun logMedicineAdded(name: String, type: String) {
        logEvent("medicine_added", mapOf("name" to name, "type" to type))
    }

    fun logReminderCompleted(medicineName: String) {
        logEvent("reminder_completed", mapOf("medicine" to medicineName))
    }

    fun logReminderMissed(medicineName: String) {
        logEvent("reminder_missed", mapOf("medicine" to medicineName))
    }

    fun logReminderSkipped(medicineName: String) {
        logEvent("reminder_skipped", mapOf("medicine" to medicineName))
    }

    fun logSosTriggered(method: String) {
        logEvent("sos_triggered", mapOf("method" to method))
    }

    fun logPrescriptionUploaded(type: String) {
        logEvent("prescription_uploaded", mapOf("file_type" to type))
    }

    fun logReportExported() {
        logEvent("report_exported")
    }

    fun logProfileCreated(relation: String) {
        logEvent("profile_created", mapOf("relation" to relation))
    }

    fun logProfileUpdated() {
        logEvent("profile_updated")
    }

    fun logEmergencyContactAdded(relation: String) {
        logEvent("emergency_contact_added", mapOf("relation" to relation))
    }

    fun logLanguageChanged(language: String) {
        logEvent("language_changed", mapOf("language" to language))
    }

    fun logPfpUploadFailed(error: String) {
        logEvent("pfp_upload_failed", mapOf("error" to error))
    }

    fun logOtpFailed(type: String, error: String) {
        logEvent("otp_failed", mapOf("type" to type, "error" to error))
    }
}
