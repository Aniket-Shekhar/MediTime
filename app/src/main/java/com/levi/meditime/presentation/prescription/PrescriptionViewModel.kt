package com.levi.meditime.presentation.prescription

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Prescription
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PrescriptionState(
    val prescriptions: List<Prescription> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadProgress: Boolean = false
)

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val repository: PrescriptionRepository,
    private val themePreferences: com.levi.meditime.datastore.ThemePreferences,
    private val authRepository: AuthRepository,
    private val analyticsManager: com.levi.meditime.manager.AnalyticsManager,
    val ocrManager: com.levi.meditime.manager.OcrManager
) : ViewModel() {

    val userSettings = themePreferences.userSettings

    private val _state = MutableStateFlow(PrescriptionState())
    val state: StateFlow<PrescriptionState> = _state.asStateFlow()

    private var userId: String = ""

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    userId = it.uid
                    loadPrescriptions()
                }
            }
        }
    }

    private fun loadPrescriptions() {
        viewModelScope.launch {
            repository.getAllPrescriptions().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            prescriptions = resource.data ?: emptyList(),
                            isLoading = false
                        )
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

    fun uploadPrescription(uri: Uri, doctor: String, hospital: String, type: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            if (!user.emailVerified || !user.phoneVerified) {
                _state.value = _state.value.copy(error = "Verification required to upload prescriptions")
                return@launch
            }

            _state.value = _state.value.copy(uploadProgress = true)
            val prescription = Prescription(
                prescriptionId = UUID.randomUUID().toString(),
                userId = userId,
                doctorName = doctor,
                hospitalName = hospital,
                uploadDate = System.currentTimeMillis(),
                fileType = type
            )
            val result = repository.uploadPrescription(prescription, uri)
            if (result is Resource.Success) {
                analyticsManager.logPrescriptionUploaded(type)
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(error = result.message)
            }
            _state.value = _state.value.copy(uploadProgress = false)
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch {
            repository.deletePrescription(prescription)
        }
    }
}
