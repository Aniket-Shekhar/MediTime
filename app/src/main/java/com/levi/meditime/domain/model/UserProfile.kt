package com.levi.meditime.domain.model

data class UserProfile(
    val profileId: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val relation: String = "Myself", // Myself, Father, Mother, Grandfather, Grandmother, Child
    val photo: String? = null,
    val isSelected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
