package com.levi.meditime.presentation.medicine.detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.levi.meditime.R
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicineDetailScreen(
    medicineId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val medicine by viewModel.medicine.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        medicine?.let { med ->
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // v3.0 Hero Section (Gradient background, Large image)
                MedicineDetailHero(med, onBack)

                // PRD 1.3: Quick Actions at the top
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.action_taken),
                        onClick = { viewModel.takeMedicine() },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.CheckCircle
                    )
                    SecondaryButton(
                        text = stringResource(R.string.skip),
                        onClick = { viewModel.skipMedicine() },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Block
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.details), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailInfoCard(
                            label = stringResource(R.string.frequency), 
                            value = med.repeatType.name.lowercase().replaceFirstChar { it.uppercase() }, 
                            icon = Icons.Rounded.Repeat, 
                            modifier = Modifier.weight(1f)
                        )
                        DetailInfoCard(
                            label = stringResource(R.string.next_dose), 
                            value = "08:30 AM", 
                            icon = Icons.Rounded.Schedule, 
                            modifier = Modifier.weight(1f)
                        )
                    }

                    DetailInfoCardWide(
                        label = stringResource(R.string.instructions), 
                        value = if (med.notes.isNullOrBlank()) stringResource(R.string.no_special_instructions) else med.notes!!, 
                        icon = Icons.Rounded.Description
                    )

                    DetailInfoCardWide(
                        label = stringResource(R.string.inventory), 
                        value = stringResource(R.string.units_remaining_alert, med.currentStock, med.minimumStock),
                        icon = Icons.Rounded.Inventory
                    )

                    Spacer(Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.delete), 
                            onClick = { viewModel.deleteMedicine { onBack() } },
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 140.dp),
                            icon = Icons.Rounded.Delete
                        )
                        PrimaryButton(
                            text = stringResource(R.string.edit_medication), 
                            onClick = { onEdit(medicineId) },
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 140.dp),
                            useGradient = true,
                            icon = Icons.Rounded.Edit
                        )
                    }
                    
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun MedicineDetailHero(medicine: Medicine, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Brush.verticalGradient(listOf(PrimaryTeal, PrimaryTealDark)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(140.dp),
                shape = RoundedCornerShape(Radius.large),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
            ) {
                if (medicine.imageUrl != null) {
                    AsyncImage(
                        model = medicine.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Medication, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(64.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(medicine.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            Text(medicine.dosage, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 60.dp, start = 24.dp)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun DetailInfoCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.height(100.dp),
        border = true,
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun DetailInfoCardWide(label: String, value: String, icon: ImageVector) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        border = true,
        elevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(Radius.small),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
