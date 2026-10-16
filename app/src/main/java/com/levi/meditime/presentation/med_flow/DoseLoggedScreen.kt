package com.levi.meditime.presentation.med_flow

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.levi.meditime.presentation.dashboard.DashboardViewModel
import com.levi.meditime.presentation.theme.*

@Composable
fun DoseLoggedScreen(
    medicineId: String,
    onBackHome: () -> Unit,
    onViewNext: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
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
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GessoSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dose logged",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = GessoFg
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, GessoDivider, CircleShape)
                        .clickable { onBackHome() },
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Success Check
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(GessoPrimary, CircleShape)
                            .border(3.dp, GessoDivider, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = Color.Black, modifier = Modifier.size(36.dp))
                    }
                    
                    Text(
                        text = "Nicely done, Mom!",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = GessoFg,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${medicine?.name ?: "Metformin"} is logged. That’s one more step toward a steady week.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GessoFgMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    // Med Chip
                    Surface(
                        color = GessoSurfaceRecessed,
                        shape = RoundedCornerShape(GessoRadius.full),
                        border = androidx.compose.foundation.BorderStroke(2.dp, GessoDivider)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Medication, null, tint = GessoFg, modifier = Modifier.size(20.dp))
                            Text(
                                text = "${medicine?.name ?: "Metformin"} · ${medicine?.dosage ?: "500mg"} · after dinner",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = GessoFg
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Moment Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s4)
                    ) {
                        // Circular Progress
                        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { 0.67f },
                                modifier = Modifier.fillMaxSize(),
                                color = GessoPrimary,
                                strokeWidth = 12.dp,
                                trackColor = GessoSurfaceRecessed,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "2/3",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GessoFg
                                )
                                Text(
                                    text = "TAKEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GessoFgMuted,
                                    letterSpacing = 0.03.sp
                                )
                            }
                        }

                        // Next Dose Info
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "NEXT DOSE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GessoFgMuted,
                                letterSpacing = 0.06.sp
                            )
                            Text(
                                text = "6:00 PM",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = GessoFg
                            )
                            Text(
                                text = "Metformin · 500mg after dinner",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GessoFgMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.EmojiEvents, null, tint = GessoSecondary, modifier = Modifier.size(16.dp))
                                Text("7-day streak", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GessoFg)
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(GessoSpacing.s4))
            
            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    text = "Back to home",
                    icon = Icons.Rounded.Home,
                    onClick = onBackHome
                )
                SecondaryButton(
                    text = "View next reminder",
                    icon = Icons.Rounded.Schedule,
                    onClick = onViewNext
                )
            }
            
            Spacer(Modifier.height(GessoSpacing.s4))
            
            // Notify Caregiver
            Text(
                text = "LET FAMILY KNOW",
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
                NotifyRow("Priya", "Daughter · usually replies within minutes", "https://ui-avatars.com/api/?name=Priya&background=ebebeb&color=000")
                HorizontalDivider(color = GessoDivider)
                NotifyRow("Dr. Reddy's office", "Care team · logs shared weekly", null)
            }
            
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun NotifyRow(name: String, meta: String, avatarUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = GessoSurfaceRecessed
        ) {
            if (avatarUrl != null) {
                AsyncImage(model = avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MedicalServices, null, tint = GessoFg, modifier = Modifier.size(24.dp))
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = GessoFg)
            Text(meta, style = MaterialTheme.typography.bodyMedium, color = GessoFgMuted)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(GessoSurfaceRecessed, CircleShape)
                .clickable { /* notify */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.Send, null, tint = GessoFg, modifier = Modifier.size(18.dp))
        }
    }
}
