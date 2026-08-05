package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Symptom
import kotlinx.coroutines.flow.Flow

interface SymptomRepository {
    fun getAllSymptoms(): Flow<Resource<List<Symptom>>>
    suspend fun addSymptom(symptom: Symptom): Resource<Unit>
    suspend fun deleteSymptom(symptom: Symptom): Resource<Unit>
    fun getSymptomsForTimeline(days: Int): Flow<Resource<List<Symptom>>>
}
