package com.levi.meditime.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.manager.PdfManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class TimePeriod { WEEKLY, MONTHLY, YEARLY }

data class ChartPoint(
    val label: String,
    val value: Float // 0.0 to 1.0 (percentage)
)

data class ReportState(
    val selectedPeriod: TimePeriod = TimePeriod.WEEKLY,
    val adherence: Int = 0,
    val takenCount: Int = 0,
    val missedCount: Int = 0,
    val refillCount: Int = 0,
    val streak: Int = 0,
    val chartPoints: List<ChartPoint> = emptyList(),
    val isLoading: Boolean = false,
    val reportFile: File? = null,
    val message: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: MedicineRepository,
    private val authRepository: AuthRepository,
    private val pdfManager: PdfManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    init {
        loadReportData()
    }

    fun setPeriod(period: TimePeriod) {
        _state.update { it.copy(selectedPeriod = period) }
        loadReportData()
    }

    fun exportReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = authRepository.currentUser.first()
            val history = repository.getAllHistory().first()
            val medicines = repository.getAllMedicines().first()
            
            val file = pdfManager.generateFullReport(
                userName = user?.name ?: "User",
                adherence = _state.value.adherence,
                history = history,
                medicines = medicines,
                fileName = "MediTime_Report_${System.currentTimeMillis()}"
            )
            
            _state.update { 
                it.copy(
                    isLoading = false,
                    reportFile = file,
                    message = if (file != null) "Report exported to Downloads/MediTime" else "Failed to export report"
                )
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null, reportFile = null) }
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            repository.getAllHistory().distinctUntilChanged().collectLatest { history ->
                val taken = history.count { it.status == DoseStatus.TAKEN }
                val missed = history.count { it.status == DoseStatus.MISSED }
                val total = history.size
                
                _state.update { 
                    it.copy(
                        adherence = if (total > 0) (taken * 100) / total else 0,
                        takenCount = taken,
                        missedCount = missed,
                        refillCount = 2,
                        streak = calculateStreak(history),
                        chartPoints = calculateChartPoints(history, it.selectedPeriod),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateChartPoints(history: List<com.levi.meditime.domain.model.History>, period: TimePeriod): List<ChartPoint> {
        val calendar = java.util.Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        return when (period) {
            TimePeriod.WEEKLY -> {
                val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                (0..6).reversed().map { daysAgo ->
                    calendar.timeInMillis = now
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                    val dayStart = calendar.clone() as java.util.Calendar
                    dayStart.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    dayStart.set(java.util.Calendar.MINUTE, 0)
                    val dayEnd = calendar.clone() as java.util.Calendar
                    dayEnd.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    dayEnd.set(java.util.Calendar.MINUTE, 59)
                    
                    val dayHistory = history.filter { it.scheduledTime in dayStart.timeInMillis..dayEnd.timeInMillis }
                    val dayTaken = dayHistory.count { it.status == DoseStatus.TAKEN }
                    val dayTotal = dayHistory.size
                    
                    ChartPoint(
                        label = dayLabels[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1],
                        value = if (dayTotal > 0) dayTaken.toFloat() / dayTotal else 0f
                    )
                }
            }
            TimePeriod.MONTHLY -> {
                (0..3).reversed().map { weeksAgo ->
                    calendar.timeInMillis = now
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, -weeksAgo)
                    val weekStart = calendar.clone() as java.util.Calendar
                    weekStart.set(java.util.Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
                    val weekEnd = calendar.clone() as java.util.Calendar
                    weekEnd.add(java.util.Calendar.DAY_OF_YEAR, 6)
                    
                    val weekHistory = history.filter { it.scheduledTime in weekStart.timeInMillis..weekEnd.timeInMillis }
                    val weekTaken = weekHistory.count { it.status == DoseStatus.TAKEN }
                    val weekTotal = weekHistory.size
                    
                    ChartPoint(
                        label = "W${4 - weeksAgo}",
                        value = if (weekTotal > 0) weekTaken.toFloat() / weekTotal else 0f
                    )
                }
            }
            TimePeriod.YEARLY -> {
                val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                (0..11).reversed().map { monthsAgo ->
                    calendar.timeInMillis = now
                    calendar.add(java.util.Calendar.MONTH, -monthsAgo)
                    val monthStart = calendar.clone() as java.util.Calendar
                    monthStart.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val monthEnd = calendar.clone() as java.util.Calendar
                    monthEnd.set(java.util.Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                    
                    val monthHistory = history.filter { it.scheduledTime in monthStart.timeInMillis..monthEnd.timeInMillis }
                    val monthTaken = monthHistory.count { it.status == DoseStatus.TAKEN }
                    val monthTotal = monthHistory.size
                    
                    ChartPoint(
                        label = monthLabels[calendar.get(java.util.Calendar.MONTH)],
                        value = if (monthTotal > 0) monthTaken.toFloat() / monthTotal else 0f
                    )
                }
            }
        }
    }

    private fun calculateStreak(history: List<com.levi.meditime.domain.model.History>): Int {
        if (history.isEmpty()) return 0
        val sorted = history.sortedByDescending { it.actionTime }
        var streak = 0
        for (item in sorted) {
            if (item.status == DoseStatus.TAKEN) streak++
            else break
        }
        return streak
    }
}
