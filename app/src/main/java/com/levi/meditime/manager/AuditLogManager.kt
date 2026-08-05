package com.levi.meditime.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun logEvent(action: String, targetId: String? = null) {
        val uid = auth.currentUser?.uid ?: "anonymous"
        val event = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "userId" to uid,
            "action" to action,
            "targetId" to targetId
        )

        firestore.collection("audit_logs").document(uid)
            .collection("events").add(event)
            .addOnFailureListener {
                // Silently fail to avoid crashing the app, but in real production
                // we might want to log this failure to Crashlytics
            }
    }

    companion object {
        const val ACTION_LOGIN = "LOGIN"
        const val ACTION_REGISTER = "REGISTER"
        const val ACTION_LOGOUT = "LOGOUT"
        const val ACTION_DELETE_ACCOUNT = "DELETE_ACCOUNT"
        const val ACTION_MEDICINE_CREATE = "MEDICINE_CREATE"
        const val ACTION_MEDICINE_UPDATE = "MEDICINE_UPDATE"
        const val ACTION_MEDICINE_DELETE = "MEDICINE_DELETE"
        const val ACTION_PRESCRIPTION_UPLOAD = "PRESCRIPTION_UPLOAD"
        const val ACTION_PRESCRIPTION_DELETE = "PRESCRIPTION_DELETE"
        const val ACTION_FAMILY_MEMBER_ADD = "FAMILY_MEMBER_ADD"
        const val ACTION_FAMILY_MEMBER_REMOVE = "FAMILY_MEMBER_REMOVE"
        const val ACTION_FAMILY_MEMBER_UPDATE = "FAMILY_MEMBER_UPDATE"
        const val ACTION_EMERGENCY_CONTACT_ADD = "EMERGENCY_CONTACT_ADD"
        const val ACTION_EMERGENCY_CONTACT_REMOVE = "EMERGENCY_CONTACT_REMOVE"
    }
}
