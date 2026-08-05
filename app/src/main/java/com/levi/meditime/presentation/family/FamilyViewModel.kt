package com.levi.meditime.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.FamilyMember
import com.levi.meditime.domain.model.Relationship
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FamilyState(
    val members: List<FamilyMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val repository: FamilyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FamilyState())
    val state: StateFlow<FamilyState> = _state.asStateFlow()

    private var currentUserId: String = ""

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    currentUserId = it.uid
                    loadMembers()
                }
            }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            repository.getAllFamilyMembers().collect { resource ->
                when (resource) {
                    is Resource.Success -> _state.value = _state.value.copy(members = resource.data ?: emptyList(), isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun addMember(name: String, age: Int, gender: String, relationship: Relationship) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            if (!user.emailVerified || !user.phoneVerified) {
                _state.value = _state.value.copy(error = "Verification required to add family members")
                return@launch
            }

            val member = FamilyMember(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = name,
                age = age,
                gender = gender,
                relationship = relationship
            )
            repository.insertFamilyMember(member)
        }
    }

    fun addFamilyMember(name: String, relationship: Relationship) {
        viewModelScope.launch {
            val member = FamilyMember(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = name,
                age = 0,
                gender = "Other",
                relationship = relationship
            )
            repository.insertFamilyMember(member)
        }
    }
}
