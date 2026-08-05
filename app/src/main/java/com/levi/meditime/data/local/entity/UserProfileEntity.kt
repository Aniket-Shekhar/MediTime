package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val profileId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val relation: String,
    val photo: String?,
    val isSelected: Boolean,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
