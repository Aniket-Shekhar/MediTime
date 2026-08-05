package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.CaretakerEntity
import com.levi.meditime.domain.model.Caretaker

fun CaretakerEntity.toDomain(): Caretaker {
    return Caretaker(
        caretakerId = caretakerId,
        userId = userId,
        name = name,
        relation = relation,
        email = email,
        phone = phone,
        fcmToken = fcmToken,
        updatedAt = updatedAt
    )
}

fun Caretaker.toEntity(): CaretakerEntity {
    return CaretakerEntity(
        caretakerId = caretakerId,
        userId = userId,
        name = name,
        relation = relation,
        email = email,
        phone = phone,
        fcmToken = fcmToken,
        updatedAt = updatedAt
    )
}
