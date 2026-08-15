package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.UserEntity
import com.levi.meditime.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        uid = uid,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        age = age,
        gender = gender,
        profileImage = profileImage,
        emergencyContact = emergencyContact,
        emailVerified = emailVerified,
        phoneVerified = phoneVerified,
        failedOtpAttempts = failedOtpAttempts,
        otpLockedUntil = otpLockedUntil,
        lastOtpSentAt = lastOtpSentAt,
        hourlyOtpCount = hourlyOtpCount
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        uid = uid,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        age = age ?: 0,
        gender = gender ?: "",
        profileImage = profileImage ?: "",
        emergencyContact = emergencyContact ?: "",
        emailVerified = emailVerified,
        phoneVerified = phoneVerified,
        failedOtpAttempts = failedOtpAttempts,
        otpLockedUntil = otpLockedUntil,
        lastOtpSentAt = lastOtpSentAt,
        hourlyOtpCount = hourlyOtpCount
    )
}
