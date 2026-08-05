package com.levi.meditime.presentation.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Symptom
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.SymptomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SymptomState(
    val symptoms: List<Symptom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val commonSymptom: String? = null,
    val symptomsIncreasingAfterMissed: Boolean = false
)

@HiltViewModel
class SymptomViewModel @Inject constructor(
    private val repository: SymptomRepository,
    private val medicineRepository: com.levi.meditime.domain.repository.MedicineRepository,
    private val themePreferences: com.levi.meditime.datastore.ThemePreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    val userSettings = themePreferences.userSettings

    private val _state = MutableStateFlow(SymptomState())
    val state: StateFlow<SymptomState> = _state.asStateFlow()

    private var userId: String = ""

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    userId = it.uid
                    loadSymptoms()
                }
            }
        }
    }

    private fun loadSymptoms() {
        viewModelScope.launch {
            repository.getAllSymptoms().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val symptoms = resource.data ?: emptyList()
                        calculateAnalytics(symptoms)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = resource.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun calculateAnalytics(symptoms: List<Symptom>) {
        viewModelScope.launch {
            val common = symptoms.groupBy { it.name }
                .maxByOrNull { it.value.size }?.key

            // Correlate with missed medicines
            val history = medicineRepository.getAllHistory().first()
            val missedTimes = history.filter { it.status == com.levi.meditime.domain.model.DoseStatus.MISSED }
                .map { it.scheduledTime }
            
            var symptomsAfterMissed = false
            missedTimes.forEach { missedTime ->
                val symptomsCount = symptoms.count { it.date > missedTime && it.date < missedTime + (24 * 3600 * 1000) }
                if (symptomsCount > 0) symptomsAfterMissed = true
            }

            _state.value = _state.value.copy(
                symptoms = symptoms,
                commonSymptom = common,
                symptomsIncreasingAfterMissed = symptomsAfterMissed,
                isLoading = false
            )
        }
    }

    fun addSymptom(name: String, severity: Int, notes: String?) {
        viewModelScope.launch {
            val severityLevel = when {
                severity <= 3 -> "Mild"
                severity <= 7 -> "Moderate"
                else -> "Severe"
            }
            val symptom = Symptom(
                symptomId = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                severity = severity,
                severityLevel = severityLevel,
                date = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            repository.addSymptom(symptom)
        }
    }

    fun deleteSymptom(symptom: Symptom) {
        viewModelScope.launch {
            repository.deleteSymptom(symptom)
        }
    }
}
