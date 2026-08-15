package com.levi.meditime.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.domain.usecase.auth.LogoutUseCase
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.datastore.ThemePreferences
import com.levi.meditime.datastore.UserSettings
import com.levi.meditime.manager.AnalyticsManager
import com.levi.meditime.manager.PdfManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.model.History
import java.util.UUID
import java.util.Calendar

data class DashboardState(
    val medicines: List<Medicine> = emptyList(),
    val isLoading: Boolean = false,
    val lastSyncTime: String = "Never",
    val totalDosesToday: Int = 0,
    val completedDosesToday: Int = 0,
    val nextDoseTime: String? = null,
    val userName: String = "User",
    val showOtpDialog: OtpType? = null,
    val otpError: String? = null,
    val isOtpLoading: Boolean = false,
    val missedDoseAlert: Medicine? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val themePreferences: ThemePreferences,
    private val pdfManager: PdfManager,
    private val logoutUseCase: LogoutUseCase,
    private val analyticsManager: AnalyticsManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        android.util.Log.d("DashboardVM", "Initializing DashboardViewModel (v3.1)")
        observeUser()
        loadDashboardData()
        android.util.Log.d("DashboardVM", "Initialization commands sent.")
    }

    private fun observeUser() {
        viewModelScope.launch {
            try {
                authRepository.currentUser.collectLatest { user ->
                    if (user != null) {
                        android.util.Log.d("DashboardVM", "User received: ${user.uid}")
                        _state.update { it.copy(userName = user.name.split(" ").firstOrNull() ?: "User") }
                    } else {
                        android.util.Log.d("DashboardVM", "User is null, resetting name")
                        _state.update { it.copy(userName = "User") }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error in observeUser: ${e.message}")
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            android.util.Log.d("DashboardVM", "Loading dashboard data...")
            
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endOfDay = calendar.timeInMillis

                combine(
                    medicineRepository.getAllMedicines().distinctUntilChanged(),
                    medicineRepository.getHistoryForPeriod(startOfDay, endOfDay).distinctUntilChanged()
                ) { medicines, history ->
                    android.util.Log.d("DashboardVM", "Data combined. Meds: ${medicines.size}, History: ${history.size}")
                    val takenCount = history.count { it.status == DoseStatus.TAKEN }
                    val skippedCount = history.count { it.status == DoseStatus.SKIPPED }
                    
                    val upcomingCount = medicines.size
                    val total = takenCount + skippedCount + upcomingCount
                    
                    _state.update { 
                        it.copy(
                            medicines = medicines,
                            totalDosesToday = total,
                            completedDosesToday = takenCount,
                            nextDoseTime = calculateNextDoseTime(medicines),
                            isLoading = false
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error in loadDashboardData: ${e.message}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateNextDoseTime(medicines: List<Medicine>): String {
        // Mock logic: find the next hour
        val now = java.util.Calendar.getInstance()
        now.add(java.util.Calendar.HOUR_OF_DAY, 1)
        now.set(java.util.Calendar.MINUTE, 30)
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf.format(now.time)
    }

    fun takeMedicine(medicine: Medicine) {
        viewModelScope.launch {
            val history = History(
                id = UUID.randomUUID().toString(),
                medicineId = medicine.id,
                reminderId = "mock_reminder_id", // In real app, get current reminder id
                scheduledTime = System.currentTimeMillis(),
                actionTime = System.currentTimeMillis(),
                status = DoseStatus.TAKEN
            )
            medicineRepository.addHistoryEntry(history)
            
            // PRD 1.3: Update stock using unitsPerDose
            val updatedMedicine = medicine.copy(currentStock = (medicine.currentStock - medicine.unitsPerDose).coerceAtLeast(0))
            medicineRepository.updateMedicine(updatedMedicine)
            
            analyticsManager.logEvent("medicine_taken", mapOf("name" to medicine.name))
            
            // Re-load data to update progress
            loadDashboardData()
        }
    }

    fun skipMedicine(medicine: Medicine) {
        viewModelScope.launch {
            val history = History(
                id = UUID.randomUUID().toString(),
                medicineId = medicine.id,
                reminderId = "mock_reminder_id",
                scheduledTime = System.currentTimeMillis(),
                actionTime = System.currentTimeMillis(),
                status = DoseStatus.SKIPPED
            )
            medicineRepository.addHistoryEntry(history)
            analyticsManager.logEvent("medicine_skipped", mapOf("name" to medicine.name))
            loadDashboardData()
        }
    }

    fun snoozeMedicine(medicine: Medicine, minutes: Int) {
        viewModelScope.launch {
            // In real app, reschedule the alarm for this medicine
            analyticsManager.logEvent("medicine_snoozed", mapOf("name" to medicine.name, "duration" to minutes.toString()))
            // Logic to snooze via AlarmManager/WorkManager
        }
    }

    val userSettings: StateFlow<UserSettings?> = themePreferences.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun startSensitiveAction(type: OtpType) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            _state.value = _state.value.copy(isOtpLoading = true, otpError = null)
            val result = authRepository.sendSensitiveActionOtp(user.uid, type)
            if (result is com.levi.meditime.core.common.Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = type, isOtpLoading = false)
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun verifySensitiveAction(otp: String, type: OtpType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            _state.value = _state.value.copy(isOtpLoading = true)
            val result = authRepository.verifySensitiveActionOtp(user.uid, otp, type)
            if (result is com.levi.meditime.core.common.Resource.Success) {
                _state.value = _state.value.copy(showOtpDialog = null, isOtpLoading = false)
                onSuccess()
            } else {
                _state.value = _state.value.copy(otpError = result.message, isOtpLoading = false)
            }
        }
    }

    fun dismissOtpDialog() {
        _state.value = _state.value.copy(showOtpDialog = null, otpError = null)
    }

    fun backupNow() {
        viewModelScope.launch {
            analyticsManager.logEvent("cloud_backup_started")
            val result = medicineRepository.backupData()
            if (result is com.levi.meditime.core.common.Resource.Success) {
                analyticsManager.logEvent("cloud_backup_success")
            }
        }
    }

    fun restoreNow() {
        viewModelScope.launch {
            analyticsManager.logEvent("cloud_restore_started")
            val result = medicineRepository.restoreData()
            if (result is com.levi.meditime.core.common.Resource.Success) {
                analyticsManager.logEvent("cloud_restore_success")
            }
        }
    }

    fun generateHealthReport(onComplete: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            val medicines = medicineRepository.getAllMedicines().first()
            val file = pdfManager.generateReport(medicines, "MediTime_Report_${System.currentTimeMillis()}")
            if (file != null) {
                analyticsManager.logReportExported()
            }
            onComplete(file)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onComplete()
        }
    }
}
