package com.levi.meditime.domain.model

data class FamilyMember(
    val id: String,
    val userId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val phoneNumber: String = "",
    val relationship: Relationship,
    val profileImage: String? = null
)

enum class Relationship {
    FATHER, MOTHER, BROTHER, SISTER, CHILD, GRANDPARENT, SPOUSE, OTHER
}