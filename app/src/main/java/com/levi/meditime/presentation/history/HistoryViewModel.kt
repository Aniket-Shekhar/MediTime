package com.levi.meditime.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.model.History
import com.levi.meditime.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryState(
    val historyItems: List<History> = emptyList(),
    val adherencePercentage: Int = 0,
    val totalTaken: Int = 0,
    val totalMissed: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MedicineRepository
) : ViewModel() {

    val state: StateFlow<HistoryState> = repository.getAllHistory()
        .map { history ->
            val taken = history.count { it.status == DoseStatus.TAKEN }
            val total = history.size
            val percentage = if (total > 0) (taken * 100) / total else 0
            
            HistoryState(
                historyItems = history.sortedByDescending { it.actionTime },
                adherencePercentage = percentage,
                totalTaken = taken,
                totalMissed = history.count { it.status == DoseStatus.MISSED }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryState()
        )
}