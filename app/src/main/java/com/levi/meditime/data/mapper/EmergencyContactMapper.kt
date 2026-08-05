package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.EmergencyContactEntity
import com.levi.meditime.domain.model.EmergencyContact

fun EmergencyContactEntity.toDomain(): EmergencyContact {
    return EmergencyContact(
        id = id,
        name = name,
        relationship = relationship,
        phoneNumber = phoneNumber,
        notifyOnMissedDose = notifyOnMissedDose,
        updatedAt = updatedAt
    )
}

fun EmergencyContact.toEntity(userId: String): EmergencyContactEntity {
    return EmergencyContactEntity(
        id = id,
        userId = userId,
        name = name,
        relationship = relationship,
        phoneNumber = phoneNumber,
        notifyOnMissedDose = notifyOnMissedDose
    )
}