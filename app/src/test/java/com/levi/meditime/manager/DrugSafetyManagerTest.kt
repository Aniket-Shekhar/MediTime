package com.levi.meditime.manager

import org.junit.Assert.assertTrue
import org.junit.Test

class DrugSafetyManagerTest {

    private val drugSafetyManager = DrugSafetyManager()

    @Test
    fun `detects duplicate paracetamol conflict`() {
        val currentMeds = listOf("Tylenol", "Lisinopril")
        val newMed = "Panadol"
        
        val conflicts = drugSafetyManager.checkConflicts(newMed, currentMeds)
        
        assertTrue(conflicts.any { it.contains("Paracetamol") })
    }

    @Test
    fun `detects NSAID conflict`() {
        val currentMeds = listOf("Ibuprofen")
        val newMed = "Aspirin"
        
        val conflicts = drugSafetyManager.checkConflicts(newMed, currentMeds)
        
        assertTrue(conflicts.any { it.contains("NSAID") })
    }

    @Test
    fun `no conflict for unrelated drugs`() {
        val currentMeds = listOf("Vitamin C")
        val newMed = "Metformin"
        
        val conflicts = drugSafetyManager.checkConflicts(newMed, currentMeds)
        
        assertTrue(conflicts.isEmpty())
    }
}
