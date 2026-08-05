package com.levi.meditime.presentation.medicine.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.*
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.FamilyRepository
import com.levi.meditime.domain.repository.MedicineRepository
import com.levi.meditime.manager.AlarmManagerHelper
import com.levi.meditime.manager.FileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddMedicineViewModel @Inject constructor(
    private val repository: MedicineRepository,
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository,
    private val alarmManagerHelper: AlarmManagerHelper,
    private val fileManager: FileManager,
    private val analyticsManager: com.levi.meditime.manager.AnalyticsManager,
    private val drugSafetyManager: com.levi.meditime.manager.DrugSafetyManager
) : ViewModel() {

    // Step 1: Info
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()
    private val _dosage = MutableStateFlow("")
    val dosage = _dosage.asStateFlow()
    private val _medicineImage = MutableStateFlow<Uri?>(null)
    val medicineImage = _medicineImage.asStateFlow()
    private val _medicineType = MutableStateFlow(MedicineType.TABLET)
    val medicineType = _medicineType.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // Step 2: Appearance
    private val _colorTag = MutableStateFlow("#3B82F6")
    val colorTag = _colorTag.asStateFlow()
    private val _colorName = MutableStateFlow("Blue")
    val colorName = _colorName.asStateFlow()
    private val _shape = MutableStateFlow("Round")
    val shape = _shape.asStateFlow()
    private val _size = MutableStateFlow("Medium")
    val size = _size.asStateFlow()

    // Step 3: Assign
    private val _assignedUserIds = MutableStateFlow<List<String>>(emptyList())
    val assignedUserIds = _assignedUserIds.asStateFlow()
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()

    // Step 4: Schedule
    private val _reminderTimes = MutableStateFlow<List<Long>>(listOf())
    val reminderTimes = _reminderTimes.asStateFlow()
    private val _repeatType = MutableStateFlow(RepeatType.DAILY)
    val repeatType = _repeatType.asStateFlow()
    private val _selectedDays = MutableStateFlow<List<Int>>(emptyList())
    val selectedDays = _selectedDays.asStateFlow()
    private val _mealOption = MutableStateFlow(MealOption.NONE)
    val mealOption = _mealOption.asStateFlow()
    private val _reminderOffsetMinutes = MutableStateFlow(0)
    val reminderOffsetMinutes = _reminderOffsetMinutes.asStateFlow()

    // Step 5: Stock
    private val _currentStock = MutableStateFlow("0")
    val currentStock = _currentStock.asStateFlow()
    private val _minimumStock = MutableStateFlow("5")
    val minimumStock = _minimumStock.asStateFlow()
    private val _expiryDate = MutableStateFlow<Long?>(null)
    val expiryDate = _expiryDate.asStateFlow()

    // Step 6: Doctor
    private val _doctorName = MutableStateFlow("")
    val doctorName = _doctorName.asStateFlow()
    private val _hospitalName = MutableStateFlow("")
    val hospitalName = _hospitalName.asStateFlow()
    private val _prescriptionDate = MutableStateFlow<Long?>(null)
    val prescriptionDate = _prescriptionDate.asStateFlow()
    private val _prescriptionImage = MutableStateFlow<Uri?>(null)
    val prescriptionImage = _prescriptionImage.asStateFlow()
    private val _medicalCondition = MutableStateFlow("")
    val medicalCondition = _medicalCondition.asStateFlow()
    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _safetyWarnings = MutableStateFlow<List<String>>(emptyList())
    val safetyWarnings = _safetyWarnings.asStateFlow()

    private var currentUserId: String = ""

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let { 
                    currentUserId = it.uid 
                    familyRepository.seedDefaultMembers(currentUserId)
                }
            }
        }
        loadFamilyMembers()
    }

    private fun loadFamilyMembers() {
        viewModelScope.launch {
            familyRepository.getAllFamilyMembers().collectLatest { resource ->
                if (resource is Resource.Success) {
                    _familyMembers.value = resource.data ?: emptyList()
                }
            }
        }
    }

    fun onNameChange(newName: String) { _name.value = newName }
    fun onDosageChange(newDosage: String) { _dosage.value = newDosage }
    fun onImageSelected(uri: Uri) { _medicineImage.value = uri }
    fun onTypeChange(newType: MedicineType) { _medicineType.value = newType }
    
    fun onAppearanceChange(tag: String, name: String) { _colorTag.value = tag; _colorName.value = name }
    fun onShapeChange(newShape: String) { _shape.value = newShape }
    fun onSizeChange(newSize: String) { _size.value = newSize }

    fun toggleUserAssignment(id: String) {
        _assignedUserIds.value = if (_assignedUserIds.value.contains(id)) _assignedUserIds.value - id else _assignedUserIds.value + id
    }
    fun selectMyself() { if (currentUserId.isNotEmpty()) toggleUserAssignment(currentUserId) }
    fun isAssignedToMyself(): Boolean = _assignedUserIds.value.contains(currentUserId)

    fun addReminderTime(time: Long) { _reminderTimes.value = _reminderTimes.value + time }
    fun removeReminderTime(time: Long) { _reminderTimes.value = _reminderTimes.value - time }
    fun onRepeatTypeChange(type: RepeatType) { _repeatType.value = type }
    fun toggleSelectedDay(day: Int) {
        val current = _selectedDays.value
        _selectedDays.value = if (current.contains(day)) current - day else current + day
    }
    fun onMealOptionChange(option: MealOption) { _mealOption.value = option }
    fun onReminderOffsetChange(offset: Int) { _reminderOffsetMinutes.value = offset }

    fun onCurrentStockChange(newStock: String) { _currentStock.value = newStock }
    fun onMinimumStockChange(newStock: String) { _minimumStock.value = newStock }
    fun onExpiryDateChange(date: Long?) { _expiryDate.value = date }

    fun onDoctorNameChange(name: String) { _doctorName.value = name }
    fun onHospitalNameChange(name: String) { _hospitalName.value = name }
    fun onPrescriptionDateChange(date: Long?) { _prescriptionDate.value = date }
    fun onPrescriptionImageSelected(uri: Uri) { _prescriptionImage.value = uri }
    fun onMedicalConditionChange(condition: String) { _medicalCondition.value = condition }
    fun onNotesChange(newNotes: String) { _notes.value = newNotes }

    fun parseExtractedText(text: String) {
        val lines = text.lines()
        
        // 1. Try to find Medicine Name (often first line or near "Rx")
        val medicineName = lines.firstOrNull { line ->
            line.length > 3 && !line.any { it.isDigit() } && !line.contains("Dr.", true)
        }?.trim()
        if (!medicineName.isNullOrBlank()) _name.value = medicineName

        // 2. Try to find Dosage (looking for mg, ml, mcg)
        val dosageRegex = "(\\d+\\s*(mg|ml|mcg|g))".toRegex(RegexOption.IGNORE_CASE)
        val dosageMatch = dosageRegex.find(text)
        if (dosageMatch != null) _dosage.value = dosageMatch.value

        // 3. Try to find Frequency (1 daily, twice a day, etc)
        if (text.contains("twice", true) || text.contains("2 times", true)) {
            // Logic to add 2 reminder times if we had a clear starting point
            Log.d("OCR_PARSE", "Detected twice daily")
        }
        
        Log.d("OCR_PARSE", "Parsed: Name=$medicineName, Dosage=${dosageMatch?.value}")
    }

    fun runSafetyCheck() {
        viewModelScope.launch {
            repository.getAllMedicines().first().let { medicines ->
                val names = medicines.map { it.name }
                _safetyWarnings.value = drugSafetyManager.checkConflicts(_name.value, names)
            }
        }
    }

    fun saveMedicine(onComplete: () -> Unit) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val medicineId = UUID.randomUUID().toString()
                val localImagePath = _medicineImage.value?.let { fileManager.saveImageLocally(it) }
                val localPrescriptionPath = _prescriptionImage.value?.let { fileManager.saveImageLocally(it) }
            
            val medicine = Medicine(
                id = medicineId,
                assignedUserIds = _assignedUserIds.value,
                name = _name.value,
                dosage = _dosage.value, // PRD 1.3: Wired dosage
                type = _medicineType.value,
                currentStock = _currentStock.value.toIntOrNull() ?: 0,
                minimumStock = _minimumStock.value.toIntOrNull() ?: 5,
                imageUrl = localImagePath,
                colorTag = _colorTag.value,
                colorName = _colorName.value,
                shape = _shape.value,
                size = _size.value,
                doctorName = _doctorName.value,
                hospitalName = _hospitalName.value,
                prescriptionDate = _prescriptionDate.value,
                prescriptionImageUrl = localPrescriptionPath,
                medicalCondition = _medicalCondition.value,
                repeatType = _repeatType.value,
                selectedDays = _selectedDays.value, // PRD 1.1: Wired selectedDays
                mealOption = _mealOption.value,
                reminderOffsetMinutes = _reminderOffsetMinutes.value,
                expiryDate = _expiryDate.value,
                notes = _notes.value
            )
            repository.insertMedicine(medicine)
            analyticsManager.logMedicineAdded(medicine.name, medicine.type.name)

            _reminderTimes.value.forEach { time ->
                val reminder = Reminder(
                    id = UUID.randomUUID().toString(),
                    medicineId = medicineId,
                    time = time,
                    repeatType = _repeatType.value, // PRD 1.4: Wired repeatType
                    selectedDays = _selectedDays.value, // PRD 1.1: Support for Specific Days
                    mealOption = _mealOption.value
                )
                repository.addReminder(reminder)
                alarmManagerHelper.schedule(medicine, reminder)
            }
            onComplete()
            } catch (e: Exception) {
                Log.e("AddMedicine", "Failed to save medicine", e)
            } finally {
                _isSaving.value = false
            }
        }
    }
}
