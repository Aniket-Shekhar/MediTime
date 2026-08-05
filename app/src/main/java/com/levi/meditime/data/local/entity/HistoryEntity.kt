package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.levi.meditime.domain.model.DoseStatus

@Entity(
    tableName = "history",
    indices = [
        androidx.room.Index(value = ["medicineId"]),
        androidx.room.Index(value = ["scheduledTime"]),
        androidx.room.Index(value = ["status"])
    ]
)
data class HistoryEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val reminderId: String,
    val scheduledTime: Long,
    val actionTime: Long?,
    val status: DoseStatus,
    val notifiedCaretaker: Boolean = false,
    val missedAt: Long? = null,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
