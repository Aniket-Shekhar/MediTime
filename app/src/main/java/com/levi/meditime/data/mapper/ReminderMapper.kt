package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.ReminderEntity
import com.levi.meditime.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        medicineId = medicineId,
        time = time,
        date = date,
        repeatType = repeatType,
        selectedDays = selectedDays,
        mealOption = mealOption,
        isActive = isActive,
        updatedAt = updatedAt
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        medicineId = medicineId,
        time = time,
        date = date,
        repeatType = repeatType,
        selectedDays = selectedDays,
        mealOption = mealOption,
        isActive = isActive
    )
}