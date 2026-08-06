package com.levi.meditime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.levi.meditime.core.common.Resource
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.*
import com.levi.meditime.domain.model.History
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.domain.model.Reminder
import com.levi.meditime.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

import kotlinx.coroutines.flow.first

class MedicineRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth,
    private val auditLogManager: AuditLogManager
) : MedicineRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    override fun getAllMedicines(): Flow<List<Medicine>> {
        return dao.getAllMedicines().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMedicine(medicine: Medicine) {
        try {
            dao.insertMedicine(medicine.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("medicines").child(userId).child(medicine.id).setValue(medicine).await()
                auditLogManager.logEvent(AuditLogManager.ACTION_MEDICINE_CREATE, medicine.id)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            throw e
        }
    }

    override suspend fun updateMedicine(medicine: Medicine) {
        try {
            dao.insertMedicine(medicine.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("medicines").child(userId).child(medicine.id).setValue(medicine).await()
                auditLogManager.logEvent(AuditLogManager.ACTION_MEDICINE_UPDATE, medicine.id)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            throw e
        }
    }

    override suspend fun deleteMedicine(medicine: Medicine) {
        try {
            dao.deleteMedicine(medicine.toEntity())
            if (userId.isNotEmpty()) {
                dbRef.child("medicines").child(userId).child(medicine.id).removeValue().await()
                auditLogManager.logEvent(AuditLogManager.ACTION_MEDICINE_DELETE, medicine.id)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            throw e
        }
    }

    override suspend fun getMedicineById(id: String): Medicine? {
        return dao.getMedicineById(id)?.toDomain()
    }

    override fun getRemindersForMedicine(medicineId: String): Flow<List<Reminder>> {
        return dao.getRemindersForMedicine(medicineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addReminder(reminder: Reminder) {
        dao.insertReminder(reminder.toEntity())
        if (userId.isNotEmpty()) {
            dbRef.child("reminders").child(userId).child(reminder.id).setValue(reminder).await()
        }
    }

    override suspend fun deleteRemindersForMedicine(medicineId: String) {
        dao.deleteRemindersForMedicine(medicineId)
        if (userId.isNotEmpty()) {
            dbRef.child("reminders").child(userId).orderByChild("medicineId").equalTo(medicineId)
                .get().await().children.forEach { it.ref.removeValue() }
        }
    }

    override suspend fun getReminderById(id: String): Reminder? {
        return dao.getReminderById(id)?.toDomain()
    }

    override fun getHistoryForMedicine(medicineId: String): Flow<List<History>> {
        return dao.getHistoryForMedicine(medicineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryForMember(memberId: String): Flow<List<History>> {
        return dao.getHistoryForMember(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addHistoryEntry(history: History) {
        dao.insertHistory(history.toEntity())
        if (userId.isNotEmpty()) {
            dbRef.child("history").child(userId).child(history.id).setValue(history).await()
        }
    }

    override fun getHistoryForPeriod(start: Long, end: Long): Flow<List<History>> {
        return dao.getHistoryForPeriod(start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllHistory(): Flow<List<History>> {
        return dao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllFamilyMembers(): Flow<List<com.levi.meditime.domain.model.FamilyMember>> {
        return dao.getAllFamilyMembers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMedicinesCount(): Flow<Int> = dao.getMedicinesCount()

    override fun getMedicinesCountForMember(memberId: String): Flow<Int> = dao.getMedicinesCountForMember(memberId)

    override fun getTakenDosesCount(): Flow<Int> = dao.getTakenDosesCount()

    override fun getMissedDosesCount(): Flow<Int> = dao.getMissedDosesCount()

    override fun getTakenDosesCountForPeriod(start: Long, end: Long): Flow<Int> = dao.getTakenDosesCountForPeriod(start, end)

    override fun getMissedDosesCountForPeriod(start: Long, end: Long): Flow<Int> = dao.getMissedDosesCountForPeriod(start, end)

    override suspend fun getNextUpcomingDose(): Pair<Medicine, Reminder>? {
        val reminderEntity = dao.getNextUpcomingReminder(System.currentTimeMillis())
        return reminderEntity?.let { reminder ->
            dao.getMedicineById(reminder.medicineId)?.let { medicine ->
                medicine.toDomain() to reminder.toDomain()
            }
        }
    }

    override suspend fun backupData(): Resource<Unit> {
        return try {
            if (userId.isEmpty()) return Resource.Error("User not logged in")
            
            val medicines = dao.getAllMedicines().first().map { it.toDomain() }
            val reminders = dao.getAllReminders().first().map { it.toDomain() }
            val history = dao.getAllHistory().first().map { it.toDomain() }
            
            dbRef.child("medicines").child(userId).setValue(medicines.associateBy { it.id }).await()
            dbRef.child("reminders").child(userId).setValue(reminders.associateBy { it.id }).await()
            dbRef.child("history").child(userId).setValue(history.associateBy { it.id }).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Backup failed")
        }
    }

    override suspend fun restoreData(): Resource<Unit> {
        return try {
            if (userId.isEmpty()) return Resource.Error("User not logged in")
            
            val medicinesSnapshot = dbRef.child("medicines").child(userId).get().await()
            val remindersSnapshot = dbRef.child("reminders").child(userId).get().await()
            val historySnapshot = dbRef.child("history").child(userId).get().await()
            
            val medicines = medicinesSnapshot.children.mapNotNull { it.getValue(Medicine::class.java) }
            val reminders = remindersSnapshot.children.mapNotNull { it.getValue(Reminder::class.java) }
            val history = historySnapshot.children.mapNotNull { it.getValue(History::class.java) }
            
            dao.deleteAllMedicines()
            dao.deleteAllReminders()
            dao.deleteAllHistory()
            
            if (medicines.isNotEmpty()) dao.insertMedicines(medicines.map { it.toEntity() })
            if (reminders.isNotEmpty()) dao.insertReminders(reminders.map { it.toEntity() })
            if (history.isNotEmpty()) dao.insertHistoryItems(history.map { it.toEntity() })
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Restore failed")
        }
    }

    override suspend fun syncPendingData(): Resource<Unit> {
        return try {
            if (userId.isEmpty()) return Resource.Error("User not logged in")

            // 1. Sync Medicines
            val unsyncedMedicines = dao.getUnsyncedMedicines()
            unsyncedMedicines.forEach { entity ->
                val remoteSnapshot = dbRef.child("medicines").child(userId).child(entity.id).get().await()
                val remoteUpdatedAt = remoteSnapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                
                if (entity.updatedAt > remoteUpdatedAt) {
                    dbRef.child("medicines").child(userId).child(entity.id).setValue(entity.toDomain()).await()
                    dao.insertMedicine(entity.copy(isSynced = true))
                } else if (remoteSnapshot.exists()) {
                    // Remote is newer, update local
                    val remoteMed = remoteSnapshot.getValue(Medicine::class.java)
                    remoteMed?.let { dao.insertMedicine(it.toEntity().copy(isSynced = true)) }
                }
            }

            // 2. Sync Emergency Contacts
            val unsyncedContacts = dao.getUnsyncedEmergencyContacts()
            unsyncedContacts.forEach { entity ->
                val remoteSnapshot = dbRef.child("users").child(userId).child("emergency_contacts").child(entity.id).get().await()
                val remoteUpdatedAt = remoteSnapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                
                if (entity.updatedAt > remoteUpdatedAt) {
                    dbRef.child("users").child(userId).child("emergency_contacts").child(entity.id).setValue(entity).await()
                    dao.insertEmergencyContact(entity.copy(isSynced = true))
                } else if (remoteSnapshot.exists()) {
                    val remoteContact = remoteSnapshot.getValue(com.levi.meditime.domain.model.EmergencyContact::class.java)
                    remoteContact?.let { dao.insertEmergencyContact(it.toEntity(userId).copy(isSynced = true)) }
                }
            }

            // 3. Sync Caretakers
            val unsyncedCaretakers = dao.getUnsyncedCaretakers()
            unsyncedCaretakers.forEach { entity ->
                val remoteSnapshot = dbRef.child("users").child(userId).child("caretakers").child(entity.caretakerId).get().await()
                val remoteUpdatedAt = remoteSnapshot.child("updatedAt").getValue(Long::class.java) ?: 0L

                if (entity.updatedAt > remoteUpdatedAt) {
                    dbRef.child("users").child(userId).child("caretakers").child(entity.caretakerId).setValue(entity).await()
                    dao.insertCaretaker(entity.copy(isSynced = true))
                } else if (remoteSnapshot.exists()) {
                    val remoteCaretaker = remoteSnapshot.getValue(com.levi.meditime.domain.model.Caretaker::class.java)
                    remoteCaretaker?.let { dao.insertCaretaker(it.toEntity().copy(isSynced = true)) }
                }
            }

            // 4. Sync User Profiles
            val unsyncedProfiles = dao.getUnsyncedUserProfiles()
            unsyncedProfiles.forEach { entity ->
                val remoteSnapshot = dbRef.child("users").child(userId).child("family_profiles").child(entity.profileId).get().await()
                val remoteUpdatedAt = remoteSnapshot.child("updatedAt").getValue(Long::class.java) ?: 0L

                if (entity.updatedAt > remoteUpdatedAt) {
                    dbRef.child("users").child(userId).child("family_profiles").child(entity.profileId).setValue(entity).await()
                    dao.insertUserProfile(entity.copy(isSynced = true))
                } else if (remoteSnapshot.exists()) {
                    val remoteProfile = remoteSnapshot.getValue(com.levi.meditime.domain.model.UserProfile::class.java)
                    remoteProfile?.let { dao.insertUserProfile(it.toEntity().copy(isSynced = true)) }
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Sync failed")
        }
    }
}
