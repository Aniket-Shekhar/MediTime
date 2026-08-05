package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.levi.meditime.domain.model.MealOption
import com.levi.meditime.domain.model.RepeatType

@Entity(
    tableName = "reminders",
    indices = [androidx.room.Index(value = ["medicineId"])],
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val time: Long,
    val date: Long?,
    val repeatType: RepeatType,
    val selectedDays: List<Int>, // PRD 1.1: Support for Specific Days
    val mealOption: MealOption,
    val isActive: Boolean,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
