package com.levi.meditime.domain.usecase.auth

import com.levi.meditime.data.local.database.MediTimeDatabase
import com.levi.meditime.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val database: MediTimeDatabase
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            authRepository.logout()
            database.clearAllTables()
        }
    }
}