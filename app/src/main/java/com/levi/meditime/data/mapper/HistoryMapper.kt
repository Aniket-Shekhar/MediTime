package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.HistoryEntity
import com.levi.meditime.domain.model.History

fun HistoryEntity.toDomain(): History {
    return History(
        id = id,
        medicineId = medicineId,
        reminderId = reminderId,
        scheduledTime = scheduledTime,
        actionTime = actionTime,
        status = status,
        notifiedCaretaker = notifiedCaretaker,
        missedAt = missedAt,
        updatedAt = updatedAt
    )
}

fun History.toEntity(): HistoryEntity {
    return HistoryEntity(
        id = id,
        medicineId = medicineId,
        reminderId = reminderId,
        scheduledTime = scheduledTime,
        actionTime = actionTime,
        status = status,
        notifiedCaretaker = notifiedCaretaker,
        missedAt = missedAt
    )
}