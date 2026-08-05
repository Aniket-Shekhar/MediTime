package com.levi.meditime.domain.model

data class Symptom(
    val symptomId: String,
    val userId: String,
    val medicineId: String? = null,
    val name: String,
    val severity: Int,
    val severityLevel: String,
    val date: Long,
    val time: Long,
    val notes: String?,
    val isExperienced: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
