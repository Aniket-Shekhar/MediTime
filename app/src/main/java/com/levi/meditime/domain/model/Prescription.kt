package com.levi.meditime.domain.model

data class Prescription(
    val prescriptionId: String = "",
    val userId: String = "",
    val doctorName: String = "",
    val hospitalName: String = "",
    val uploadDate: Long = 0,
    val fileUrl: String = "",
    val fileType: String = "", // JPG, PNG, PDF
    val notes: String? = null
)
