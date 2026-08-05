package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caretakers")
data class CaretakerEntity(
    @PrimaryKey val caretakerId: String,
    val userId: String,
    val name: String,
    val relation: String,
    val email: String,
    val phone: String,
    val fcmToken: String?,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
