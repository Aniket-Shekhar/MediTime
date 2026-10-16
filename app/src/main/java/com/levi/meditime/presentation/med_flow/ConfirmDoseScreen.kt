package com.levi.meditime.presentation.med_flow

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.dashboard.DashboardViewModel
import com.levi.meditime.presentation.theme.*

@Composable
fun ConfirmDoseScreen(
    medicineId: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val medicine = state.medicines.find { it.id == medicineId }
    
    var note by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GessoCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GessoSpacing.s4)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(48.dp))
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GessoSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confirm Dose",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = GessoFg
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, GessoDivider, CircleShape)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, null, tint = GessoFg)
                }
            }
            
            Spacer(Modifier.height(GessoSpacing.s2))
            
            // Hero Confirmation Card
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                contentPadding = PaddingValues(GessoSpacing.s4),
                shape = RoundedCornerShape(GessoRadius.lg)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(GessoPrimary, CircleShape)
                            .border(3.dp, GessoDivider, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Medication, null, tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = medicine?.name ?: "Metformin",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = GessoFg
                    )
                    Text(
                        text = "Scheduled for Mom",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GessoFgMuted
                    )
                    
                    // Progress Ring
                    Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier.fillMaxSize(),
                            color = GessoSecondary,
                            strokeWidth = 14.dp,
                            trackColor = GessoSurfaceRecessed,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "6:00",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = GessoFg
                            )
                            Text(
                                text = "PM · PENDING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GessoFgMuted,
                                letterSpacing = 0.06.sp
                            )
                        }
                    }
                    
                    // Dosage Row
                    Surface(
                        color = GessoSurfaceRecessed,
                        shape = RoundedCornerShape(GessoRadius.full)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Medication, null, tint = GessoFg, modifier = Modifier.size(20.dp))
                            Text(
                                text = "${medicine?.dosage ?: "500mg"} · after dinner",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = GessoFg
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(GessoSpacing.s4))
            
            // Confirm Actions
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PrimaryButton(
                    text = "Confirm Taken",
                    icon = Icons.Rounded.CheckCircle,
                    onClick = { onConfirm(medicineId) }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = "Add Note",
                        icon = Icons.Rounded.EditNote,
                        onClick = { /* toggle note */ },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = "Cancel",
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(Modifier.height(GessoSpacing.s4))
            
            // Note Field
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Text(
                    text = "OPTIONAL NOTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GessoFgMuted,
                    letterSpacing = 0.06.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Felt fine after taking it, no dizziness…", color = GessoFgMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(GessoRadius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GessoPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = GessoSurfaceRecessed,
                        unfocusedContainerColor = GessoSurfaceRecessed,
                        focusedTextColor = GessoFg,
                        unfocusedTextColor = GessoFg
                    )
                )
            }
            
            Spacer(Modifier.height(GessoSpacing.s4))
            
            // Schedule Context
            Text(
                text = "REST OF MOM'S DAY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GessoFgMuted,
                letterSpacing = 0.06.sp
            )
            Spacer(Modifier.height(12.dp))
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                contentPadding = PaddingValues(0.dp)
            ) {
                ScheduleContextItem("Metoprolol · 50mg", "8:00 AM · with breakfast", "Taken", true)
                HorizontalDivider(color = GessoDivider)
                ScheduleContextItem("Vitamin D3", "1:00 PM · after lunch", "Taken", true)
                HorizontalDivider(color = GessoDivider)
                ScheduleContextItem("Atorvastatin · 20mg", "9:00 PM · before bed", "9:00 PM", false)
            }
            
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun ScheduleContextItem(title: String, meta: String, status: String, isDone: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (isDone) GessoPrimary else GessoSurfaceRecessed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Medication, null, tint = Color.Black, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = GessoFg)
            Text(meta, style = MaterialTheme.typography.bodyMedium, color = GessoFgMuted)
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDone) GessoPrimary else GessoFgMuted
        )
    }
}
