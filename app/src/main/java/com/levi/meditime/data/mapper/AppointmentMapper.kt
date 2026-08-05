package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.AppointmentEntity
import com.levi.meditime.domain.model.Appointment

fun AppointmentEntity.toDomain(): Appointment {
    return Appointment(
        appointmentId = appointmentId,
        userId = userId,
        doctorName = doctorName,
        doctorPhone = doctorPhone,
        hospitalName = hospitalName,
        date = date,
        time = time,
        notes = notes,
        notifiedOneDayBefore = notifiedOneDayBefore,
        notifiedOneHourBefore = notifiedOneHourBefore
    )
}

fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        appointmentId = appointmentId,
        userId = userId,
        doctorName = doctorName,
        doctorPhone = doctorPhone,
        hospitalName = hospitalName,
        date = date,
        time = time,
        notes = notes,
        notifiedOneDayBefore = notifiedOneDayBefore,
        notifiedOneHourBefore = notifiedOneHourBefore,
        updatedAt = updatedAt
    )
}
