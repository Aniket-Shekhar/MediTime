package com.levi.meditime.domain.model

import java.util.UUID

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val familyMemberId: String? = null,
    val assignedUserIds: List<String> = emptyList(),
    val name: String = "",
    val genericName: String? = null,
    val dosage: String = "",
    val instructions: String? = null,
    val notes: String? = null,
    val type: MedicineType = MedicineType.TABLET,
    val currentStock: Int = 0,
    val minimumStock: Int = 5,
    val unitsPerDose: Int = 1,
    val unitType: String = "Tablet",
    val imageUrl: String? = null,
    val colorTag: String? = null,
    val colorName: String? = null,
    val shape: String? = null,
    val size: String? = null,
    val packagingNotes: String? = null,
    
    // Doctor & Prescription Info
    val doctorName: String? = null,
    val hospitalName: String? = null,
    val prescriptionDate: Long? = null,
    val prescriptionImageUrl: String? = null,
    val medicalCondition: String? = null,
    
    // Schedule details
    val repeatType: RepeatType = RepeatType.DAILY,
    val selectedDays: List<Int> = emptyList(), // PRD 1.1: Support for Specific Days
    val mealOption: MealOption = MealOption.NONE,
    val reminderOffsetMinutes: Int = 0,
    val expiryDate: Long? = null,
    
    val emergencyEnabled: Boolean = false,
    val emergencyDelay: Int = 30, // in minutes
    val updatedAt: Long = System.currentTimeMillis() // PRD 3.1: Conflict Resolution
)

enum class MedicineType {
    TABLET, CAPSULE, SYRUP, INJECTION, DROPS, CREAM, INHALER, OTHER
}

enum class MealOption {
    BEFORE_FOOD, AFTER_FOOD, WITH_FOOD, EMPTY_STOMACH, NONE
}

enum class RepeatType {
    DAILY, WEEKDAYS, WEEKENDS, SPECIFIC_DAYS
}
