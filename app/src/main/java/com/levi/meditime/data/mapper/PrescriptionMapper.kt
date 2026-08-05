package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.PrescriptionEntity
import com.levi.meditime.domain.model.Prescription

fun PrescriptionEntity.toDomain(): Prescription {
    return Prescription(
        prescriptionId = prescriptionId,
        userId = userId,
        doctorName = doctorName,
        hospitalName = hospitalName,
        uploadDate = uploadDate,
        fileUrl = fileUrl,
        fileType = fileType,
        notes = notes
    )
}

fun Prescription.toEntity(): PrescriptionEntity {
    return PrescriptionEntity(
        prescriptionId = prescriptionId,
        userId = userId,
        doctorName = doctorName,
        hospitalName = hospitalName,
        uploadDate = uploadDate,
        fileUrl = fileUrl,
        fileType = fileType,
        notes = notes
    )
}
