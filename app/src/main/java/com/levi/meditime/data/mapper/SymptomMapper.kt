package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.SymptomEntity
import com.levi.meditime.domain.model.Symptom

fun SymptomEntity.toDomain(): Symptom {
    return Symptom(
        symptomId = symptomId,
        userId = userId,
        medicineId = medicineId,
        name = name,
        severity = severity,
        severityLevel = severityLevel,
        date = date,
        time = time,
        notes = notes,
        isExperienced = isExperienced,
        createdAt = createdAt
    )
}

fun Symptom.toEntity(): SymptomEntity {
    return SymptomEntity(
        symptomId = symptomId,
        userId = userId,
        medicineId = medicineId,
        name = name,
        severity = severity,
        severityLevel = severityLevel,
        date = date,
        time = time,
        notes = notes,
        isExperienced = isExperienced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
