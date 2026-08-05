package com.levi.meditime.presentation.medicine.add

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.levi.meditime.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.levi.meditime.domain.model.MealOption
import com.levi.meditime.domain.model.MedicineType
import com.levi.meditime.domain.model.RepeatType
import com.levi.meditime.manager.rememberImagePicker
import com.levi.meditime.presentation.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddMedicineScreen(
    onBack: () -> Unit,
    onNavigateToFamily: () -> Unit,
    viewModel: AddMedicineViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val dosage by viewModel.dosage.collectAsStateWithLifecycle()
    val medicineImage by viewModel.medicineImage.collectAsStateWithLifecycle()
    val medicineType by viewModel.medicineType.collectAsStateWithLifecycle()
    
    val colorTag by viewModel.colorTag.collectAsStateWithLifecycle()
    val shape by viewModel.shape.collectAsStateWithLifecycle()
    val size by viewModel.size.collectAsStateWithLifecycle()

    val reminderTimes by viewModel.reminderTimes.collectAsStateWithLifecycle()
    val repeatType by viewModel.repeatType.collectAsStateWithLifecycle()
    val selectedDays by viewModel.selectedDays.collectAsStateWithLifecycle()
    val mealOption by viewModel.mealOption.collectAsStateWithLifecycle()

    val currentStock by viewModel.currentStock.collectAsStateWithLifecycle()
    val minimumStock by viewModel.minimumStock.collectAsStateWithLifecycle()
    val expiryDate by viewModel.expiryDate.collectAsStateWithLifecycle()

    val doctorName by viewModel.doctorName.collectAsStateWithLifecycle()
    val hospitalName by viewModel.hospitalName.collectAsStateWithLifecycle()
    val medicalCondition by viewModel.medicalCondition.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var currentStep by remember { mutableIntStateOf(1) }
    var isSuccess by remember { mutableStateOf(false) }
    val totalSteps = 7

    val context = LocalContext.current
    val imagePicker = rememberImagePicker { uri -> viewModel.onImageSelected(uri) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (isSuccess) {
        SuccessAnimation(onDismiss = onBack)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // v3.0 Header
                AddMedicineHeader(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    onBack = {
                        if (currentStep > 1) currentStep--
                        else onBack()
                    }
                )

                // Step Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        label = "StepTransition"
                    ) { step ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))
                            when (step) {
                                1 -> Step1Info(
                                    name = name,
                                    onNameChange = viewModel::onNameChange,
                                    imageUri = medicineImage,
                                    onAddPhoto = {
                                        if (cameraPermissionState.status.isGranted) imagePicker.takePhoto()
                                        else cameraPermissionState.launchPermissionRequest()
                                    },
                                    onPickGallery = { imagePicker.pickFromGallery() },
                                    selectedType = medicineType,
                                    onTypeChange = viewModel::onTypeChange
                                )
                                2 -> Step2Dosage(
                                    dosage = dosage,
                                    onDosageChange = viewModel::onDosageChange
                                )
                                3 -> Step3Appearance(
                                    selectedColorTag = colorTag,
                                    onColorSelected = viewModel::onAppearanceChange,
                                    selectedShape = shape,
                                    onShapeChange = viewModel::onShapeChange
                                )
                                4 -> Step4Schedule(
                                    reminderTimes = reminderTimes,
                                    onAddTime = viewModel::addReminderTime,
                                    onRemoveTime = viewModel::removeReminderTime,
                                    repeatType = repeatType,
                                    onRepeatChange = viewModel::onRepeatTypeChange,
                                    selectedDays = selectedDays,
                                    onToggleDay = viewModel::toggleSelectedDay,
                                    mealOption = mealOption,
                                    onMealChange = viewModel::onMealOptionChange
                                )
                                5 -> Step5Inventory(
                                    current = currentStock,
                                    onCurrentChange = viewModel::onCurrentStockChange,
                                    min = minimumStock,
                                    onMinChange = viewModel::onMinimumStockChange,
                                    expiry = expiryDate,
                                    onExpiryChange = viewModel::onExpiryDateChange
                                )
                                6 -> Step6Doctor(
                                    doctor = doctorName,
                                    onDoctorChange = viewModel::onDoctorNameChange,
                                    hospital = hospitalName,
                                    onHospitalChange = viewModel::onHospitalNameChange,
                                    condition = medicalCondition,
                                    onConditionChange = viewModel::onMedicalConditionChange,
                                    notes = notes,
                                    onNotesChange = viewModel::onNotesChange
                                )
                                7 -> Step7Review(
                                    name = name,
                                    type = medicineType,
                                    colorTag = colorTag,
                                    schedule = reminderTimes,
                                    isSaving = viewModel.isSaving.collectAsStateWithLifecycle().value,
                                    safetyWarnings = viewModel.safetyWarnings.collectAsStateWithLifecycle().value,
                                    onSave = { 
                                        viewModel.saveMedicine { 
                                            isSuccess = true
                                        } 
                                    }
                                )
                            }
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }

                // v3.0 Bottom Navigation with Gradient Button
                val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    PrimaryButton(
                        text = if (currentStep == totalSteps) stringResource(R.string.save_medicine) else stringResource(R.string.continue_label),
                        onClick = {
                            if (currentStep < totalSteps) {
                                if (currentStep == 6) viewModel.runSafetyCheck()
                                currentStep++
                            }
                            else viewModel.saveMedicine { isSuccess = true }
                        },
                        useGradient = true, // v3.0: Premium Gradient
                        enabled = when (currentStep) {
                            1 -> name.isNotBlank()
                            4 -> reminderTimes.isNotEmpty()
                            7 -> !isSaving
                            else -> true
                        },
                        loading = isSaving
                    )
                }
            }
        }
    }
}

@Composable
fun AddMedicineHeader(currentStep: Int, totalSteps: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(44.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = stringResource(R.string.step_format, currentStep, totalSteps),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (currentStep) {
                    1 -> stringResource(R.string.medicine_info)
                    2 -> stringResource(R.string.dosage)
                    3 -> stringResource(R.string.appearance_step)
                    4 -> stringResource(R.string.schedule_step)
                    5 -> stringResource(R.string.inventory)
                    6 -> stringResource(R.string.doctor_info)
                    7 -> stringResource(R.string.review)
                    else -> stringResource(R.string.add_medicine)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "$currentStep",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun Step1Info(
    name: String,
    onNameChange: (String) -> Unit,
    imageUri: Any?,
    onAddPhoto: () -> Unit,
    onPickGallery: () -> Unit,
    selectedType: MedicineType,
    onTypeChange: (MedicineType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // v3.0 Upload Image Card (120dp height, dashed border)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(Radius.medium))
                .background(MaterialTheme.colorScheme.surface)
                .dashedBorder(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                .clickable { onAddPhoto() },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.upload_med_photo), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.medicine_name), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text(stringResource(R.string.paracetamol_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(Radius.input), // v3.0: 14dp
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                singleLine = true
            )
        }

        Column {
            SectionTitle(title = stringResource(R.string.medicine_type))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MedicineType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        shape = RoundedCornerShape(Radius.small),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedType == type,
                            borderColor = if (selectedType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            borderWidth = 1.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun Step2Dosage(
    dosage: String,
    onDosageChange: (String) -> Unit
) {
    val dosageInt = dosage.filter { it.isDigit() }.toIntOrNull() ?: 0
    val dosageUnit = dosage.filter { !it.isDigit() }.trim().ifBlank { stringResource(R.string.tablet) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.dosage_per_take), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onDosageChange("${(dosageInt - 1).coerceAtLeast(0)} $dosageUnit") },
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Rounded.Remove, null, tint = MaterialTheme.colorScheme.onSurface)
                }
                
                OutlinedTextField(
                    value = dosage,
                    onValueChange = onDosageChange,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(Radius.input),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                IconButton(
                    onClick = { onDosageChange("${dosageInt + 1} $dosageUnit") },
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun Step3Appearance(
    selectedColorTag: String,
    onColorSelected: (String, String) -> Unit,
    selectedShape: String,
    onShapeChange: (String) -> Unit
) {
    val colors = listOf(
        "#2FB6AD" to "Teal", 
        "#DCC6F5" to "Lavender", 
        "#F6D48A" to "Orange", 
        "#F7D1E4" to "Pink", 
        "#4CAF50" to "Green", 
        "#FFFFFF" to "White"
    )
    val shapes = listOf("Round", "Oval", "Capsule", "Square")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.tablet_appearance), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = when(selectedShape) {
                        "Round" -> CircleShape
                        "Oval" -> RoundedCornerShape(32.dp)
                        "Capsule" -> RoundedCornerShape(50.dp)
                        else -> RoundedCornerShape(Radius.medium)
                    },
                    color = Color(android.graphics.Color.parseColor(selectedColorTag)),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        Icons.Rounded.Medication, 
                        null, 
                        tint = if (selectedColorTag == "#FFFFFF") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.2f),
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Column {
            SectionTitle(title = stringResource(R.string.pick_color))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                colors.forEach { (tag, name) ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(tag)))
                            .border(
                                if (selectedColorTag == tag) 3.dp else 1.dp,
                                if (selectedColorTag == tag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                            .clickable { onColorSelected(tag, name) }
                    )
                }
            }
        }

        Column {
            SectionTitle(title = stringResource(R.string.pick_shape))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shapes) { shape ->
                    FilterChip(
                        selected = selectedShape == shape,
                        onClick = { onShapeChange(shape) },
                        label = { Text(shape) },
                        shape = RoundedCornerShape(Radius.small),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun Step4Schedule(
    reminderTimes: List<Long>,
    onAddTime: (Long) -> Unit,
    onRemoveTime: (Long) -> Unit,
    repeatType: RepeatType,
    onRepeatChange: (RepeatType) -> Unit,
    selectedDays: List<Int>,
    onToggleDay: (Int) -> Unit,
    mealOption: MealOption,
    onMealChange: (MealOption) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.reminder_times_label), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            
            TimeWheelPicker(onTimeSelected = { h, m ->
                selectedHour = h
                selectedMinute = m
            })
            
            Spacer(Modifier.height(16.dp))
            
            PrimaryButton(
                text = stringResource(R.string.add_reminder_time),
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                    }
                    onAddTime(calendar.timeInMillis)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                icon = Icons.Rounded.AddAlarm
            )
            
            if (reminderTimes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminderTimes.forEach { time ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.small)),
                            shape = RoundedCornerShape(Radius.small),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(formatTime(time), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { onRemoveTime(time) }) {
                                    Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        Column {
            SectionTitle(title = stringResource(R.string.repeat))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatType.entries.forEach { type ->
                    FilterChip(
                        selected = repeatType == type,
                        onClick = { onRepeatChange(type) },
                        label = { 
                            Text(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Radius.small),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (repeatType == RepeatType.SPECIFIC_DAYS) {
                Spacer(Modifier.height(16.dp))
                val weekdays = listOf(
                    Calendar.MONDAY to "M",
                    Calendar.TUESDAY to "T",
                    Calendar.WEDNESDAY to "W",
                    Calendar.THURSDAY to "T",
                    Calendar.FRIDAY to "F",
                    Calendar.SATURDAY to "S",
                    Calendar.SUNDAY to "S"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { (day, label) ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onToggleDay(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step5Inventory(
    current: String,
    onCurrentChange: (String) -> Unit,
    min: String,
    onMinChange: (String) -> Unit,
    expiry: Long?,
    onExpiryChange: (Long?) -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val warningColor = if (isDark) WarningDark else WarningOrange
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiry ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onExpiryChange(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.stock_management), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.total_stock), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = current,
                        onValueChange = onCurrentChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(Radius.small),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.low_alert_at), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = min,
                        onValueChange = onMinChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(Radius.small),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = warningColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(stringResource(R.string.expiry_date), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.small))
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(Radius.small),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Event, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = if (expiry != null) formatDate(expiry) else stringResource(R.string.select_date),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (expiry != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun Step6Doctor(
    doctor: String,
    onDoctorChange: (String) -> Unit,
    hospital: String,
    onHospitalChange: (String) -> Unit,
    condition: String,
    onConditionChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.doctor_notes), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = doctor,
                onValueChange = onDoctorChange,
                label = { Text(stringResource(R.string.doctor_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = hospital,
                onValueChange = onHospitalChange,
                label = { Text(stringResource(R.string.hospital_clinic_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(R.string.special_notes)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(Radius.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun Step7Review(
    name: String,
    type: MedicineType,
    colorTag: String,
    schedule: List<Long>,
    isSaving: Boolean,
    safetyWarnings: List<String>,
    onSave: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val successColor = if (isDark) SuccessDark else SuccessGreen

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (safetyWarnings.isNotEmpty()) {
            AppCard(
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                border = true,
                elevation = 0.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Safety Warning", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        safetyWarnings.forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primary
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(Radius.medium),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Medication, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(type.name, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.review_schedule), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            schedule.forEach { time ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = successColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(formatTime(time), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(Radius.medium)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.verify_details_msg), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun SuccessAnimation(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.medicine_saved), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(
            stringResource(R.string.med_saved_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(Modifier.height(48.dp))
        PrimaryButton(text = stringResource(R.string.go_to_dashboard), onClick = onDismiss, useGradient = true)
    }
}

private fun formatTime(time: Long): String {
    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(time))
}

private fun formatDate(time: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(time))
}
