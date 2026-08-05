package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val notifyOnMissedDose: Boolean = false,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
