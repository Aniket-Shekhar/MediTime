package com.levi.meditime.presentation.prescription

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.levi.meditime.R
import com.levi.meditime.domain.model.Prescription
import com.levi.meditime.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionListScreen(
    onBack: () -> Unit,
    viewModel: PrescriptionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileType by remember { mutableStateOf("JPG") }
    var showPickerOptions by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            selectedFileType = "JPG"
            showAddDialog = true
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            selectedFileType = "PDF"
            showAddDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Medical Records",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your prescriptions & documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.prescriptions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PrescriptionEmptyState()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.prescriptions) { prescription ->
                        PremiumPrescriptionCard(
                            prescription = prescription,
                            onDelete = { viewModel.deletePrescription(prescription) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            PremiumFloatingActionButton(
                icon = Icons.Default.CloudUpload,
                onClick = { showPickerOptions = true },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showPickerOptions) {
        ModalBottomSheet(
            onDismissRequest = { showPickerOptions = false },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Upload Document",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                DocumentTypeItem(
                    title = "Image Gallery",
                    subtitle = "JPG, PNG formats",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = {
                        imagePicker.launch("image/*")
                        showPickerOptions = false
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                DocumentTypeItem(
                    title = "PDF Document",
                    subtitle = "Reports & scans",
                    icon = Icons.Default.Description,
                    onClick = {
                        pdfPicker.launch("application/pdf")
                        showPickerOptions = false
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddDialog && selectedUri != null) {
        val uri = selectedUri
        AddPrescriptionBottomSheet(
            onDismiss = { showAddDialog = false },
            onSave = { doctor, hospital ->
                if (uri != null) {
                    viewModel.uploadPrescription(uri, doctor, hospital, selectedFileType)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PremiumPrescriptionCard(prescription: Prescription, onDelete: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val errorColor = if (isDark) ErrorDark else DangerRed

    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Open viewer */ }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (prescription.fileType == "PDF") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = errorColor
                        )
                        Text("PDF Report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    AsyncImage(
                        model = prescription.fileUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = prescription.doctorName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = prescription.hospitalName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM", LocalLocale.current.platformLocale).format(Date(prescription.uploadDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun PrescriptionEmptyState() {
    Column(
        modifier = Modifier.padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Description,
            null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No records yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Keep all your medical documents organized in one place.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun DocumentTypeItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrescriptionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var doctor by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text(text = "Document Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(24.dp))
            
            PremiumPrescriptionInput(
                value = doctor,
                onValueChange = { doctor = it },
                label = "Doctor Name",
                placeholder = "e.g. Dr. Smith",
                icon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(16.dp))
            PremiumPrescriptionInput(
                value = hospital,
                onValueChange = { hospital = it },
                label = "Hospital / Clinic",
                placeholder = "e.g. City Hospital",
                icon = Icons.Default.HomeWork
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            PremiumButton(
                text = "Upload Document",
                onClick = { onSave(doctor, hospital) },
                enabled = doctor.isNotBlank()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PremiumPrescriptionInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
