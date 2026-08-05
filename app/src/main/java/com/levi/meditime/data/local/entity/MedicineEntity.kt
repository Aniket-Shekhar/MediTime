package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.levi.meditime.domain.model.MealOption
import com.levi.meditime.domain.model.MedicineType
import com.levi.meditime.domain.model.RepeatType

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey val id: String,
    val familyMemberId: String?,
    val assignedUserIds: List<String>,
    val name: String,
    val genericName: String?,
    val dosage: String,
    val instructions: String?,
    val notes: String?,
    val type: MedicineType,
    val currentStock: Int,
    val minimumStock: Int,
    val unitsPerDose: Int,
    val unitType: String,
    val imageUrl: String?,
    val colorTag: String?,
    val colorName: String?,
    val shape: String?,
    val size: String?,
    val packagingNotes: String?,
    
    // New Fields
    val hospitalName: String?,
    val doctorName: String?,
    val prescriptionDate: Long?,
    val prescriptionImageUrl: String?,
    val medicalCondition: String?,
    
    val repeatType: RepeatType,
    val selectedDays: List<Int>, // PRD 1.1: Support for Specific Days
    val mealOption: MealOption,
    val reminderOffsetMinutes: Int,
    val expiryDate: Long?,

    val emergencyEnabled: Boolean,
    val emergencyDelay: Int,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
