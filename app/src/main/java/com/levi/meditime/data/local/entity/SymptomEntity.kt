package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey val symptomId: String,
    val userId: String,
    val familyMemberId: String? = null,
    val medicineId: String?,
    val name: String,
    val severity: Int, // 1-10
    val severityLevel: String, // Mild, Moderate, Severe
    val date: Long,
    val time: Long,
    val notes: String?,
    val isExperienced: Boolean,
    val createdAt: Long,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
