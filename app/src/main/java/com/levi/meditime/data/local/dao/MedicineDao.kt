package com.levi.meditime.data.local.dao

import androidx.room.*
import com.levi.meditime.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: String): MedicineEntity?

    @Query("SELECT * FROM reminders WHERE medicineId = :medicineId")
    fun getRemindersForMedicine(medicineId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE medicineId = :medicineId")
    suspend fun deleteRemindersForMedicine(medicineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Query("DELETE FROM medicines")
    suspend fun deleteAllMedicines()

    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("SELECT * FROM history")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE medicineId = :medicineId")
    fun getHistoryForMedicine(medicineId: String): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM medicines WHERE familyMemberId = :memberId")
    fun getMedicinesCountForMember(memberId: String): Flow<Int>

    @Query("SELECT * FROM history h INNER JOIN medicines m ON h.medicineId = m.id WHERE m.familyMemberId = :memberId")
    fun getHistoryForMember(memberId: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(history: List<HistoryEntity>)

    @Query("SELECT * FROM history WHERE scheduledTime BETWEEN :start AND :end")
    fun getHistoryForPeriod(start: Long, end: Long): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()

    @Query("SELECT * FROM family_members")
    fun getAllFamilyMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMembers(members: List<FamilyMemberEntity>)

    @Query("DELETE FROM family_members")
    suspend fun deleteAllFamilyMembers()

    // Statistics
    @Query("SELECT COUNT(*) FROM medicines")
    fun getMedicinesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM history WHERE status = 'TAKEN'")
    fun getTakenDosesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM history WHERE status = 'MISSED'")
    fun getMissedDosesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM history WHERE status = 'TAKEN' AND scheduledTime BETWEEN :start AND :end")
    fun getTakenDosesCountForPeriod(start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM history WHERE status = 'MISSED' AND scheduledTime BETWEEN :start AND :end")
    fun getMissedDosesCountForPeriod(start: Long, end: Long): Flow<Int>

    // Emergency Contacts
    @Query("SELECT * FROM emergency_contacts")
    fun getAllEmergencyContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteEmergencyContact(contact: EmergencyContactEntity)

    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getEmergencyContactById(id: String): EmergencyContactEntity?

    // User Profile
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Caretakers
    @Query("SELECT * FROM caretakers WHERE userId = :userId")
    fun getAllCaretakers(userId: String): Flow<List<CaretakerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaretaker(caretaker: CaretakerEntity)

    @Delete
    suspend fun deleteCaretaker(caretaker: CaretakerEntity)

    // Symptoms
    @Query("SELECT * FROM symptoms WHERE userId = :userId ORDER BY date DESC, time DESC")
    fun getAllSymptoms(userId: String): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE familyMemberId = :memberId ORDER BY date DESC, time DESC")
    fun getSymptomsForMember(memberId: String): Flow<List<SymptomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntity)

    @Query("SELECT * FROM symptoms WHERE userId = :userId AND date >= :startDate")
    fun getSymptomsFromDate(userId: String, startDate: Long): Flow<List<SymptomEntity>>

    // Prescriptions
    @Query("SELECT * FROM prescriptions WHERE userId = :userId ORDER BY uploadDate DESC")
    fun getAllPrescriptions(userId: String): Flow<List<PrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Delete
    suspend fun deletePrescription(prescription: PrescriptionEntity)

    // Appointments
    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY date ASC, time ASC")
    fun getAllAppointments(userId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE familyMemberId = :memberId ORDER BY date ASC, time ASC")
    fun getAppointmentsForMember(memberId: String): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    // User Profiles (Multiple)
    @Query("SELECT * FROM user_profiles")
    fun getAllUserProfiles(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET isSelected = 0")
    suspend fun deselectAllProfiles()

    @Query("UPDATE user_profiles SET isSelected = 1 WHERE profileId = :profileId")
    suspend fun selectProfile(profileId: String)

    @Query("SELECT * FROM user_profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedProfile(): UserProfileEntity?

    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)

    // Sync Queries
    @Query("SELECT * FROM medicines WHERE isSynced = 0")
    suspend fun getUnsyncedMedicines(): List<MedicineEntity>

    @Query("SELECT * FROM emergency_contacts WHERE isSynced = 0")
    suspend fun getUnsyncedEmergencyContacts(): List<EmergencyContactEntity>

    @Query("SELECT * FROM user_profiles WHERE isSynced = 0")
    suspend fun getUnsyncedUserProfiles(): List<UserProfileEntity>

    @Query("SELECT * FROM caretakers WHERE isSynced = 0")
    suspend fun getUnsyncedCaretakers(): List<CaretakerEntity>

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND time > :currentTime ORDER BY time ASC LIMIT 1")
    suspend fun getNextUpcomingReminder(currentTime: Long): ReminderEntity?
}
