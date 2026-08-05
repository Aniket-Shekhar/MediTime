package com.levi.meditime.presentation.medicine.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    private val repository: MedicineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]

    private val _medicine = MutableStateFlow<Medicine?>(null)
    val medicine = _medicine.asStateFlow()

    init {
        loadMedicine()
    }

    private fun loadMedicine() {
        medicineId?.let { id ->
            viewModelScope.launch {
                _medicine.value = repository.getMedicineById(id)
            }
        }
    }

    fun deleteMedicine(onComplete: () -> Unit) {
        _medicine.value?.let { medicine ->
            viewModelScope.launch {
                repository.deleteMedicine(medicine)
                repository.deleteRemindersForMedicine(medicine.id)
                onComplete()
            }
        }
    }

    fun takeMedicine() {
        _medicine.value?.let { medicine ->
            viewModelScope.launch {
                // 1. Add History
                val history = com.levi.meditime.domain.model.History(
                    id = java.util.UUID.randomUUID().toString(),
                    medicineId = medicine.id,
                    reminderId = "detail_manual",
                    scheduledTime = System.currentTimeMillis(),
                    actionTime = System.currentTimeMillis(),
                    status = com.levi.meditime.domain.model.DoseStatus.TAKEN
                )
                repository.addHistoryEntry(history)

                // 2. Decrease Stock
                val updated = medicine.copy(currentStock = (medicine.currentStock - medicine.unitsPerDose).coerceAtLeast(0))
                repository.updateMedicine(updated)
                _medicine.value = updated
            }
        }
    }

    fun skipMedicine() {
        _medicine.value?.let { medicine ->
            viewModelScope.launch {
                val history = com.levi.meditime.domain.model.History(
                    id = java.util.UUID.randomUUID().toString(),
                    medicineId = medicine.id,
                    reminderId = "detail_manual",
                    scheduledTime = System.currentTimeMillis(),
                    actionTime = System.currentTimeMillis(),
                    status = com.levi.meditime.domain.model.DoseStatus.SKIPPED
                )
                repository.addHistoryEntry(history)
            }
        }
    }
}
