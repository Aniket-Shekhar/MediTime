package com.levi.meditime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.levi.meditime.domain.model.Relationship

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val phoneNumber: String = "",
    val relationship: Relationship,
    val profileImage: String? = null,
    val isSynced: Boolean = false
)