package com.levi.meditime.di

import com.levi.meditime.data.repository.AppointmentRepositoryImpl
import com.levi.meditime.data.repository.AuthRepositoryImpl
import com.levi.meditime.data.repository.CaretakerRepositoryImpl
import com.levi.meditime.data.repository.EmergencyContactRepositoryImpl
import com.levi.meditime.data.repository.FamilyRepositoryImpl
import com.levi.meditime.data.repository.MedicineRepositoryImpl
import com.levi.meditime.data.repository.PrescriptionRepositoryImpl
import com.levi.meditime.data.repository.SymptomRepositoryImpl
import com.levi.meditime.data.repository.UserProfileRepositoryImpl
import com.levi.meditime.data.repository.UserRepositoryImpl
import com.levi.meditime.domain.repository.AppointmentRepository
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.CaretakerRepository
import com.levi.meditime.domain.repository.EmergencyContactRepository
import com.levi.meditime.domain.repository.FamilyRepository
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.domain.repository.PrescriptionRepository
import com.levi.meditime.domain.repository.SymptomRepository
import com.levi.meditime.domain.repository.UserProfileRepository
import com.levi.meditime.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        medicineRepositoryImpl: MedicineRepositoryImpl
    ): MedicineRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindFamilyRepository(
        familyRepositoryImpl: FamilyRepositoryImpl
    ): FamilyRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyContactRepository(
        emergencyContactRepositoryImpl: EmergencyContactRepositoryImpl
    ): EmergencyContactRepository

    @Binds
    @Singleton
    abstract fun bindCaretakerRepository(
        caretakerRepositoryImpl: CaretakerRepositoryImpl
    ): CaretakerRepository

    @Binds
    @Singleton
    abstract fun bindSymptomRepository(
        symptomRepositoryImpl: SymptomRepositoryImpl
    ): SymptomRepository

    @Binds
    @Singleton
    abstract fun bindPrescriptionRepository(
        prescriptionRepositoryImpl: PrescriptionRepositoryImpl
    ): PrescriptionRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        appointmentRepositoryImpl: AppointmentRepositoryImpl
    ): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository
}