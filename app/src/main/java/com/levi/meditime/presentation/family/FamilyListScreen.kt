package com.levi.meditime.presentation.family

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.levi.meditime.domain.model.FamilyMember
import com.levi.meditime.presentation.theme.*

@Composable
fun FamilyListScreen(
    onBack: () -> Unit,
    onMemberClick: (FamilyMember) -> Unit,
    onAddMember: () -> Unit,
    viewModel: FamilyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GessoCanvas)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 120.dp,
                top = 24.dp,
                start = GessoSpacing.s4,
                end = GessoSpacing.s4
            ),
            verticalArrangement = Arrangement.spacedBy(GessoSpacing.s4)
        ) {
            // Header
            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Family overview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GessoFgMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Everyone's Meds",
                            style = MaterialTheme.typography.headlineLarge,
                            color = GessoFg,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, GessoDivider, CircleShape)
                            .clickable { /* notifications */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Notifications, null, tint = GessoFg)
                    }
                }
            }

            // Status Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TODAY'S STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GessoFgMuted,
                        letterSpacing = 0.06.sp
                    )
                    Text(
                        text = "${state.members.size} people",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GessoFgMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Family List Container
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // Mocked data for design alignment if real list is empty
                    val displayMembers = if (state.members.isEmpty()) {
                        listOf("Mom", "Dad", "Aanya", "You")
                    } else {
                        state.members.map { it.name }
                    }

                    displayMembers.forEachIndexed { index, name ->
                        FamilyMemberRow(
                            name = name,
                            status = when(name) {
                                "Mom" -> "2 of 3 taken · 7-day streak"
                                "Dad" -> "2 of 2 taken · 12-day streak"
                                "Aanya" -> "1 of 1 taken · allergy syrup"
                                else -> "0 of 2 taken · missed morning dose"
                            },
                            statusTag = when(name) {
                                "Mom" -> "1 LEFT"
                                "Dad" -> "DONE"
                                "Aanya" -> "DONE"
                                else -> "MISSED"
                            },
                            time = when(name) {
                                "Mom" -> "6:00 PM"
                                "Dad" -> "All clear"
                                "Aanya" -> "All clear"
                                else -> "8:00 AM"
                            },
                            progress = when(name) {
                                "Mom" -> 0.67f
                                "Dad" -> 1f
                                "Aanya" -> 1f
                                else -> 0f
                            },
                            onClick = { 
                                if (state.members.isNotEmpty()) {
                                    onMemberClick(state.members[index % state.members.size])
                                }
                            }
                        )
                        if (index < displayMembers.size - 1) {
                            HorizontalDivider(color = GessoDivider)
                        }
                    }
                    
                    HorizontalDivider(color = GessoDivider)
                    
                    // Add Member Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddMember() }
                            .padding(GessoSpacing.s4),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s4)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GessoSurfaceRecessed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = GessoFgMuted)
                        }
                        Text(
                            text = "Add family member",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GessoFgMuted
                        )
                    }
                }
            }

            // Caregiver Actions
            item {
                Text(
                    text = "CAREGIVER ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GessoFgMuted,
                    letterSpacing = 0.06.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CaregiverActionTile("Call doctor", Icons.Rounded.Phone, GessoSurfaceRecessed, Modifier.weight(1f))
                    CaregiverActionTile("SOS", Icons.Rounded.Emergency, GessoError.copy(alpha = 0.9f), Modifier.weight(1f), Color.White)
                }
            }
        }
    }
}

@Composable
fun FamilyMemberRow(
    name: String,
    status: String,
    statusTag: String,
    time: String,
    progress: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(GessoSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar with Progress Ring
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = if (statusTag == "MISSED") GessoSurfaceRecessed else if (statusTag == "DONE") GessoPrimary else GessoSecondary,
                strokeWidth = 3.dp,
                trackColor = GessoSurfaceRecessed,
                strokeCap = StrokeCap.Round
            )
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(2.dp, GessoDivider)
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=$name&background=ebebeb&color=000",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = GessoFg)
            Text(status, style = MaterialTheme.typography.bodyMedium, color = GessoFgMuted)
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
                color = when(statusTag) {
                    "DONE" -> GessoPrimary
                    "1 LEFT" -> GessoSecondary
                    else -> GessoSurfaceRecessed
                },
                shape = RoundedCornerShape(GessoRadius.full)
            ) {
                Text(
                    text = statusTag,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (statusTag == "MISSED") GessoFgMuted else Color.Black
                )
            }
            Text(time, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (statusTag == "MISSED") GessoError else GessoFg)
        }
    }
}

@Composable
fun CaregiverActionTile(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, iconTint: Color = Color.Black) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(GessoRadius.full))
            .border(2.dp, GessoDivider, RoundedCornerShape(GessoRadius.full))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GessoFg, textAlign = TextAlign.Center)
    }
}
