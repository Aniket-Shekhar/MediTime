package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.UserProfileEntity
import com.levi.meditime.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        profileId = profileId,
        name = name,
        age = age,
        gender = gender,
        relation = relation,
        photo = photo,
        isSelected = isSelected,
        updatedAt = updatedAt
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        profileId = profileId,
        name = name,
        age = age,
        gender = gender,
        relation = relation,
        photo = photo,
        isSelected = isSelected,
        updatedAt = updatedAt
    )
}
