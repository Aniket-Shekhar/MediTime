package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getAllFamilyMembers(): Flow<Resource<List<FamilyMember>>>
    suspend fun insertFamilyMember(member: FamilyMember): Resource<Unit>
    suspend fun deleteFamilyMember(id: String): Resource<Unit>
    suspend fun updateFamilyMember(member: FamilyMember): Resource<Unit>
    suspend fun seedDefaultMembers(userId: String): Resource<Unit>
}