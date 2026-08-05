package com.levi.meditime.data.repository

import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import com.levi.meditime.domain.model.FamilyMember
import com.levi.meditime.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

import com.levi.meditime.domain.model.Relationship
import java.util.UUID
import kotlinx.coroutines.flow.first

class FamilyRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auditLogManager: AuditLogManager
) : FamilyRepository {

    override fun getAllFamilyMembers(): Flow<Resource<List<FamilyMember>>> {
        return dao.getAllFamilyMembers().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun seedDefaultMembers(userId: String): Resource<Unit> {
        return try {
            val existing = dao.getAllFamilyMembers().first()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    FamilyMember(UUID.randomUUID().toString(), userId, "Mother", 0, "", "", Relationship.MOTHER),
                    FamilyMember(UUID.randomUUID().toString(), userId, "Father", 0, "", "", Relationship.FATHER),
                    FamilyMember(UUID.randomUUID().toString(), userId, "Brother", 0, "", "", Relationship.BROTHER),
                    FamilyMember(UUID.randomUUID().toString(), userId, "Sister", 0, "", "", Relationship.SISTER)
                )
                defaults.forEach { insertFamilyMember(it) }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Seeding failed")
        }
    }

    override suspend fun insertFamilyMember(member: FamilyMember): Resource<Unit> {
        return try {
            dao.insertFamilyMember(member.toEntity())
            dbRef.child("family").child(member.userId).child(member.id).setValue(member).await()
            auditLogManager.logEvent(AuditLogManager.ACTION_FAMILY_MEMBER_ADD, member.id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not save family member")
        }
    }

    override suspend fun deleteFamilyMember(id: String): Resource<Unit> {
        // Implementation for delete local and remote
        auditLogManager.logEvent(AuditLogManager.ACTION_FAMILY_MEMBER_REMOVE, id)
        return Resource.Success(Unit)
    }

    override suspend fun updateFamilyMember(member: FamilyMember): Resource<Unit> {
        // Implementation for update local and remote
        auditLogManager.logEvent(AuditLogManager.ACTION_FAMILY_MEMBER_UPDATE, member.id)
        return Resource.Success(Unit)
    }
}