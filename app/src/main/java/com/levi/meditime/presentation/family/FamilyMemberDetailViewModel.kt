package com.levi.meditime.presentation.family

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.model.FamilyMember
import com.levi.meditime.domain.repository.FamilyRepository
import com.levi.meditime.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyMemberDetailState(
    val member: FamilyMember? = null,
    val medicineCount: Int = 0,
    val takenToday: Int = 0,
    val totalToday: Int = 0,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class FamilyMemberDetailViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val medicineRepository: MedicineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memberId: String? = savedStateHandle["memberId"]

    private val _state = MutableStateFlow(FamilyMemberDetailState())
    val state = _state.asStateFlow()

    init {
        loadMemberData()
    }

    private fun loadMemberData() {
        val id = memberId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 1. Get Member
            familyRepository.getAllFamilyMembers().collectLatest { resource ->
                if (resource is Resource.Success) {
                    val member = resource.data?.find { it.id == id }
                    _state.update { it.copy(member = member) }
                }
            }
        }

        viewModelScope.launch {
            medicineRepository.getMedicinesCountForMember(id).collectLatest { count ->
                _state.update { it.copy(medicineCount = count) }
            }
        }

        viewModelScope.launch {
            medicineRepository.getHistoryForMember(id).collectLatest { history ->
                val today = System.currentTimeMillis()
                val startOfDay = today - (today % (24 * 60 * 60 * 1000))
                val todayHistory = history.filter { it.scheduledTime >= startOfDay }
                
                _state.update { 
                    it.copy(
                        takenToday = todayHistory.count { it.status == DoseStatus.TAKEN },
                        totalToday = todayHistory.size
                    )
                }
            }
        }
    }

    fun removeMember(onSuccess: () -> Unit) {
        val id = memberId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = familyRepository.deleteFamilyMember(id)) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> _state.update { it.copy(message = result.message, isLoading = false) }
                else -> {}
            }
        }
    }
}
