package com.levi.meditime.domain.model

enum class DoseStatus {
    PENDING, TAKEN, MISSED, SKIPPED
}

data class History(
    val id: String,
    val medicineId: String,
    val reminderId: String,
    val scheduledTime: Long,
    val actionTime: Long?,
    val status: DoseStatus,
    val notifiedCaretaker: Boolean = false,
    val missedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
