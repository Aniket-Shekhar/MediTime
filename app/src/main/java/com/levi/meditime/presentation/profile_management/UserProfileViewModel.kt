package com.levi.meditime.presentation.profile_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.UserProfile
import com.levi.meditime.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UserProfileState(
    val profiles: List<UserProfile> = emptyList(),
    val selectedProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val analyticsManager: com.levi.meditime.manager.AnalyticsManager
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    init {
        loadProfiles()
        loadSelectedProfile()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            repository.getAllUserProfiles().collectLatest { resource ->
                if (resource is Resource.Success) {
                    _state.value = _state.value.copy(profiles = resource.data ?: emptyList())
                }
            }
        }
    }

    private fun loadSelectedProfile() {
        viewModelScope.launch {
            repository.getSelectedProfile().collectLatest { resource ->
                if (resource is Resource.Success) {
                    _state.value = _state.value.copy(selectedProfile = resource.data)
                }
            }
        }
    }

    fun addProfile(name: String, age: Int, relation: String) {
        viewModelScope.launch {
            val profile = UserProfile(
                profileId = UUID.randomUUID().toString(),
                name = name,
                age = age,
                relation = relation
            )
            repository.addUserProfile(profile)
            analyticsManager.logProfileCreated(relation)
        }
    }

    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            repository.selectProfile(profileId)
        }
    }
}
