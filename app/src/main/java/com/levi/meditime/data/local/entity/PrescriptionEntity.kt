package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey val prescriptionId: String,
    val userId: String,
    val doctorName: String,
    val hospitalName: String,
    val uploadDate: Long,
    val fileUrl: String,
    val fileType: String,
    val notes: String?,
    val isSynced: Boolean = false
)
