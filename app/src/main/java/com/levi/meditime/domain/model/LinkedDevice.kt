package com.levi.meditime.domain.model

data class LinkedDevice(
    val id: String,
    val deviceName: String,
    val userId: String,
    val inviteCode: String? = null,
    val status: LinkingStatus = LinkingStatus.PENDING
)

enum class LinkingStatus {
    PENDING, ACTIVE, DISCONNECTED
}