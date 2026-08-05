package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.MedicineEntity
import com.levi.meditime.domain.model.Medicine

fun MedicineEntity.toDomain(): Medicine {
    return Medicine(
        id = id,
        familyMemberId = familyMemberId,
        assignedUserIds = assignedUserIds,
        name = name,
        genericName = genericName,
        dosage = dosage,
        instructions = instructions,
        notes = notes,
        type = type,
        currentStock = currentStock,
        minimumStock = minimumStock,
        unitsPerDose = unitsPerDose,
        unitType = unitType,
        imageUrl = imageUrl,
        colorTag = colorTag,
        colorName = colorName,
        shape = shape,
        size = size,
        packagingNotes = packagingNotes,
        hospitalName = hospitalName,
        doctorName = doctorName,
        prescriptionDate = prescriptionDate,
        prescriptionImageUrl = prescriptionImageUrl,
        medicalCondition = medicalCondition,
        repeatType = repeatType,
        selectedDays = selectedDays,
        mealOption = mealOption,
        reminderOffsetMinutes = reminderOffsetMinutes,
        expiryDate = expiryDate,
        emergencyEnabled = emergencyEnabled,
        emergencyDelay = emergencyDelay,
        updatedAt = updatedAt
    )
}

fun Medicine.toEntity(): MedicineEntity {
    return MedicineEntity(
        id = id,
        familyMemberId = familyMemberId,
        assignedUserIds = assignedUserIds,
        name = name,
        genericName = genericName,
        dosage = dosage,
        instructions = instructions,
        notes = notes,
        type = type,
        currentStock = currentStock,
        minimumStock = minimumStock,
        unitsPerDose = unitsPerDose,
        unitType = unitType,
        imageUrl = imageUrl,
        colorTag = colorTag,
        colorName = colorName,
        shape = shape,
        size = size,
        packagingNotes = packagingNotes,
        hospitalName = hospitalName,
        doctorName = doctorName,
        prescriptionDate = prescriptionDate,
        prescriptionImageUrl = prescriptionImageUrl,
        medicalCondition = medicalCondition,
        repeatType = repeatType,
        selectedDays = selectedDays,
        mealOption = mealOption,
        reminderOffsetMinutes = reminderOffsetMinutes,
        expiryDate = expiryDate,
        emergencyEnabled = emergencyEnabled,
        emergencyDelay = emergencyDelay,
        updatedAt = updatedAt
    )
}
