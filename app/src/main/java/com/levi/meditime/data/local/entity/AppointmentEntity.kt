package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val appointmentId: String,
    val userId: String,
    val doctorName: String,
    val doctorPhone: String?,
    val hospitalName: String,
    val date: Long,
    val time: Long,
    val notes: String?,
    val notifiedOneDayBefore: Boolean,
    val notifiedOneHourBefore: Boolean,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
