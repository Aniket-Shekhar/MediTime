package com.levi.meditime.domain.model

data class Appointment(
    val appointmentId: String = "",
    val userId: String = "",
    val doctorName: String = "",
    val doctorPhone: String? = null,
    val hospitalName: String = "",
    val date: Long = 0,
    val time: Long = 0,
    val notes: String? = null,
    val notifiedOneDayBefore: Boolean = false,
    val notifiedOneHourBefore: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
