package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.History
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    // Medicine
    fun getAllMedicines(): Flow<List<Medicine>>
    suspend fun insertMedicine(medicine: Medicine)
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(medicine: Medicine)
    suspend fun getMedicineById(id: String): Medicine?

    // Reminders
    fun getRemindersForMedicine(medicineId: String): Flow<List<Reminder>>
    suspend fun addReminder(reminder: Reminder)
    suspend fun deleteRemindersForMedicine(medicineId: String)
    suspend fun getReminderById(id: String): Reminder?

    // History
    fun getHistoryForMedicine(medicineId: String): Flow<List<History>>
    fun getHistoryForMember(memberId: String): Flow<List<History>>
    suspend fun addHistoryEntry(history: History)
    fun getHistoryForPeriod(start: Long, end: Long): Flow<List<History>>
    fun getAllHistory(): Flow<List<History>>
    
    // Family
    fun getAllFamilyMembers(): Flow<List<com.levi.meditime.domain.model.FamilyMember>>
    
    // Statistics
    fun getMedicinesCount(): Flow<Int>
    fun getMedicinesCountForMember(memberId: String): Flow<Int>
    fun getTakenDosesCount(): Flow<Int>
    fun getMissedDosesCount(): Flow<Int>
    fun getTakenDosesCountForPeriod(start: Long, end: Long): Flow<Int>
    fun getMissedDosesCountForPeriod(start: Long, end: Long): Flow<Int>
    
    // Widget
    suspend fun getNextUpcomingDose(): Pair<Medicine, Reminder>?

    // Backup & Restore
    suspend fun backupData(): Resource<Unit>
    suspend fun restoreData(): Resource<Unit>
    suspend fun syncPendingData(): Resource<Unit>
}
