package com.levi.meditime.domain.repository

import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.Caretaker
import kotlinx.coroutines.flow.Flow

interface CaretakerRepository {
    fun getAllCaretakers(): Flow<Resource<List<Caretaker>>>
    suspend fun addCaretaker(caretaker: Caretaker): Resource<Unit>
    suspend fun deleteCaretaker(caretaker: Caretaker): Resource<Unit>
    suspend fun updateCaretaker(caretaker: Caretaker): Resource<Unit>
}
