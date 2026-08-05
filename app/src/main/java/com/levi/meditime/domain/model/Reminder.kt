package com.levi.meditime.domain.model

data class Reminder(
    val id: String,
    val medicineId: String,
    val time: Long,
    val date: Long? = null,
    val repeatType: RepeatType = RepeatType.DAILY,
    val selectedDays: List<Int> = emptyList(), // PRD 1.1: Support for Specific Days
    val mealOption: MealOption = MealOption.NONE,
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis() // PRD 3.1: Conflict Resolution
)
