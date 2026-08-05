package com.levi.meditime.presentation.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Appointment
import com.levi.meditime.domain.repository.AppointmentRepository
import com.levi.meditime.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AppointmentState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val themePreferences: com.levi.meditime.datastore.ThemePreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    val userSettings = themePreferences.userSettings

    private val _state = MutableStateFlow(AppointmentState())
    val state: StateFlow<AppointmentState> = _state.asStateFlow()

    private var userId: String = ""

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    userId = it.uid
                    loadAppointments()
                }
            }
        }
    }

    private fun loadAppointments() {
        viewModelScope.launch {
            repository.getAllAppointments().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            appointments = resource.data ?: emptyList(),
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

    fun addAppointment(doctor: String, phone: String, hospital: String, date: Long, time: Long) {
        viewModelScope.launch {
            val appointment = Appointment(
                appointmentId = UUID.randomUUID().toString(),
                userId = userId,
                doctorName = doctor,
                doctorPhone = phone,
                hospitalName = hospital,
                date = date,
                time = time
            )
            repository.addAppointment(appointment)
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }
}
