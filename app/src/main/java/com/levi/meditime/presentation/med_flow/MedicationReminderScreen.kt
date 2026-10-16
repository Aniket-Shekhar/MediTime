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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.dashboard.DashboardViewModel
import com.levi.meditime.presentation.theme.*

@Composable
fun MedicationReminderScreen(
    medicineId: String,
    onTakeNow: (String) -> Unit,
    onSkip: () -> Unit,
    onInstructions: () -> Unit,
    onBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Find the medicine in the state
    val medicine = state.medicines.find { it.id == medicineId }
    
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = GessoSpacing.s1)
            ) {
                Box(modifier = Modifier.size(8.dp).background(GessoSecondary, CircleShape))
                Text(
                    text = "Time for your medicine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GessoFgMuted,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(GessoSpacing.s2))
            
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                // Hero Top
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s2)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medicine?.name ?: "Metformin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GessoFg
                        )
                        Text(
                            text = "${medicine?.dosage ?: "500mg"} tablet · 1 pill",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GessoFgMuted
                        )
                    }
                    Surface(
                        color = GessoSecondary,
                        shape = RoundedCornerShape(GessoRadius.full)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                            Text("Due now", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                    }
                }
                
                Spacer(Modifier.height(GessoSpacing.s4))
                
                // Ring Wrap
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier.fillMaxSize(),
                            color = GessoSecondary,
                            strokeWidth = 12.dp,
                            trackColor = GessoSurfaceRecessed,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "6:00",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GessoFg
                            )
                            Text(
                                text = "PM · TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GessoFgMuted
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(GessoSpacing.s4))
                
                // Instruction Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GessoSurfaceRecessed, RoundedCornerShape(GessoRadius.md))
                        .padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Info, null, tint = GessoFgMuted, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Take with dinner. Avoid on an empty stomach — may cause nausea if taken alone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GessoFgMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(Modifier.height(GessoSpacing.s4))
                
                // Action Stack
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(
                        text = "Take now",
                        icon = Icons.Rounded.Check,
                        onClick = { onTakeNow(medicineId) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton(
                            text = "Skip dose",
                            icon = Icons.Rounded.Close,
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = "Instructions",
                            icon = Icons.Rounded.Description,
                            onClick = onInstructions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(GessoSpacing.s4))
            
            // Snooze Options
            Text(
                text = "NOT READY YET?",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GessoFgMuted,
                letterSpacing = 0.06.sp
            )
            
            Spacer(Modifier.height(GessoSpacing.s2))
            
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                contentPadding = PaddingValues(0.dp)
            ) {
                SnoozeItem("Remind in 10 minutes", "Next nudge at 6:10 PM", Icons.Rounded.NotificationsActive)
                HorizontalDivider(color = GessoDivider)
                SnoozeItem("Remind in 30 minutes", "Next nudge at 6:30 PM", Icons.Rounded.NotificationsActive)
                HorizontalDivider(color = GessoDivider)
                SnoozeItem("Later tonight", "Ask again at 9:00 PM", Icons.Rounded.Nightlight)
            }
            
            Spacer(Modifier.height(120.dp))
        }
        
        // Custom Close Button if needed
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            Icon(Icons.Rounded.Close, null, tint = GessoFg)
        }
    }
}

@Composable
fun SnoozeItem(title: String, subtitle: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* snooze */ }
            .padding(GessoSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s3)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(GessoSurfaceRecessed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GessoFg, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = GessoFg)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = GessoFgMuted)
        }
    }
}
