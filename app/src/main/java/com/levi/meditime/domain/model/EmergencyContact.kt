package com.levi.meditime.domain.model

data class EmergencyContact(
    val id: String,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val notifyOnMissedDose: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
