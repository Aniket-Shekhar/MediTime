package com.levi.meditime.data.mapper

import com.levi.meditime.data.local.entity.FamilyMemberEntity
import com.levi.meditime.domain.model.FamilyMember

fun FamilyMemberEntity.toDomain(): FamilyMember {
    return FamilyMember(
        id = id,
        userId = userId,
        name = name,
        age = age,
        gender = gender,
        phoneNumber = phoneNumber,
        relationship = relationship,
        profileImage = profileImage
    )
}

fun FamilyMember.toEntity(): FamilyMemberEntity {
    return FamilyMemberEntity(
        id = id,
        userId = userId,
        name = name,
        age = age,
        gender = gender,
        phoneNumber = phoneNumber,
        relationship = relationship,
        profileImage = profileImage
    )
}