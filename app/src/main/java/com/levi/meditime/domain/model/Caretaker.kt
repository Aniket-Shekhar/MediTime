package com.levi.meditime.domain.model

data class Caretaker(
    val caretakerId: String,
    val userId: String,
    val name: String,
    val relation: String,
    val email: String,
    val phone: String,
    val fcmToken: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
