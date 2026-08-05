package com.levi.meditime.manager

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrugSafetyManager @Inject constructor() {

    private val drugCategories = mapOf(
        "NSAID" to listOf("ibuprofen", "aspirin", "naproxen", "diclofenac"),
        "Paracetamol" to listOf("paracetamol", "acetaminophen", "panadol", "tylenol"),
        "Blood Thinner" to listOf("warfarin", "clopidogrel", "rivaroxaban", "apixaban")
    )

    fun checkConflicts(newMedicineName: String, currentMedicines: List<String>): List<String> {
        val conflicts = mutableListOf<String>()
        val newNameLower = newMedicineName.lowercase()

        drugCategories.forEach { (category, drugs) ->
            val isNewInStore = drugs.any { newNameLower.contains(it) }
            if (isNewInStore) {
                val existingInSameCategory = currentMedicines.filter { existing ->
                    drugs.any { existing.lowercase().contains(it) }
                }
                if (existingInSameCategory.isNotEmpty()) {
                    conflicts.add("Multiple medications from the $category group: ${existingInSameCategory.joinToString(", ")}")
                }
            }
        }
        return conflicts
    }
}
