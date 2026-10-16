package com.levi.meditime.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.levi.meditime.R
import com.levi.meditime.domain.model.Medicine
import com.levi.meditime.presentation.theme.*

@Composable
fun DashboardScreen(
    onAddMedicine: () -> Unit,
    onNavigateToSymptoms: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMedicineDetail: (String) -> Unit,
    onViewAllMedicines: () -> Unit,
    onViewAllFamily: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
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
                DashboardHeader(
                    onNotificationClick = onNavigateToNotifications
                )
            }

            // Family Switcher
            item {
                FamilySwitcher()
            }

            // Family Medicine Hero Card
            item {
                FamilyMedicineHero(
                    state = state,
                    onClick = onNavigateToReports
                )
            }

            // Quick Actions
            item {
                QuickActionsGrid(
                    onAddMedicine = onAddMedicine,
                    onScan = onNavigateToPrescriptions,
                    onDoctor = onNavigateToAppointments,
                    onSOS = onNavigateToSOS
                )
            }

            // Medicine Schedule
            item {
                SectionTitle(title = "Mom's schedule today", actionText = "View all", onActionClick = onViewAllMedicines)
                MedicineScheduleToday(
                    medicines = state.medicines,
                    onMedicineClick = onNavigateToMedicineDetail
                )
            }
        }
    }
}

@Composable
fun DashboardHeader(onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GessoSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Good morning",
                style = MaterialTheme.typography.bodyMedium,
                color = GessoFgMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Priya's Family",
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
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = GessoFg,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FamilySwitcher() {
    val members = listOf(
        Pair("Mom", "https://ui-avatars.com/api/?name=Mom&background=2dd4bf&color=000"),
        Pair("Dad", "https://ui-avatars.com/api/?name=Dad&background=ebebeb&color=000"),
        Pair("Aanya", "https://ui-avatars.com/api/?name=Aanya&background=ebebeb&color=000"),
        Pair("You", "https://ui-avatars.com/api/?name=You&background=ebebeb&color=000")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s2),
        contentPadding = PaddingValues(vertical = GessoSpacing.s1)
    ) {
        items(members) { (name, url) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GessoSpacing.s1)
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .then(
                            if (name == "Mom") Modifier.border(3.dp, GessoPrimary, CircleShape)
                            else Modifier
                        ),
                    shape = CircleShape,
                    color = GessoSurfaceRecessed
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (name == "Mom") GessoFg else GessoFgMuted,
                    fontWeight = if (name == "Mom") FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FamilyMedicineHero(state: DashboardState, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s2)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(3.dp, GessoDivider)
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=Mom&background=ebebeb&color=000",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mom",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = GessoFg
                )
                Text(
                    text = "${state.completedDosesToday} of ${state.totalDosesToday} medicines taken today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GessoFgMuted
                )
            }
            Surface(
                color = GessoSecondary,
                shape = RoundedCornerShape(GessoRadius.full),
                modifier = Modifier.align(Alignment.Top)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.EmojiEvents, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                    Text("7-day streak", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }
        }

        Spacer(Modifier.height(GessoSpacing.s3))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s4)
        ) {
            // Circular Progress
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (state.totalDosesToday > 0) state.completedDosesToday.toFloat() / state.totalDosesToday else 0f },
                    modifier = Modifier.fillMaxSize(),
                    color = GessoPrimary,
                    strokeWidth = 12.dp,
                    trackColor = GessoSurfaceRecessed,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.completedDosesToday}/${state.totalDosesToday}",
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NEXT DOSE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GessoFgMuted,
                    letterSpacing = 0.06.sp
                )
                Text(
                    text = state.nextDoseTime ?: "6:00 PM",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = GessoFg
                )
                Text(
                    text = "Metformin · 500mg after dinner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GessoFgMuted
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(onAddMedicine: () -> Unit, onScan: () -> Unit, onDoctor: () -> Unit, onSOS: () -> Unit) {
    Column {
        Text(
            text = "QUICK ACTIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GessoFgMuted,
            letterSpacing = 0.06.sp,
            modifier = Modifier.padding(bottom = GessoSpacing.s2)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s2)) {
            QuickActionTile("Add Med", Icons.Rounded.Add, GessoPrimary, Modifier.weight(1f), onAddMedicine)
            QuickActionTile("Scan Rx", Icons.Rounded.QrCodeScanner, GessoSecondary, Modifier.weight(1f), onScan)
            QuickActionTile("Doctor", Icons.Rounded.Phone, GessoSurfaceRecessed, Modifier.weight(1f), onDoctor)
            QuickActionTile("SOS", Icons.Rounded.Emergency, GessoError.copy(alpha = 0.9f), Modifier.weight(1f), onSOS, Color.White)
        }
    }
}

@Composable
fun QuickActionTile(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit, iconTint: Color = Color.Black) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(GessoRadius.md))
            .border(3.dp, GessoDivider, RoundedCornerShape(GessoRadius.md))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = color
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GessoFg,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MedicineScheduleToday(medicines: List<Medicine>, onMedicineClick: (String) -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Mocking some items for the reference design if real list is empty
        val displayItems = if (medicines.isEmpty()) {
            listOf(
                Triple("Metoprolol · 50mg", "8:00 AM · with breakfast", true),
                Triple("Vitamin D3", "1:00 PM · after lunch", true),
                Triple("Metformin · 500mg", "6:00 PM · after dinner", false)
            )
        } else {
            medicines.map { Triple(it.name + " · " + it.dosage, "8:00 AM", false) }
        }

        displayItems.forEachIndexed { index, (title, meta, isDone) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (medicines.isNotEmpty()) onMedicineClick(medicines[index].id)
                    }
                    .padding(GessoSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GessoSpacing.s2)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (title.contains("Metformin")) GessoSecondary else GessoPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Medication, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = GessoFg)
                    Text(meta, style = MaterialTheme.typography.bodyMedium, color = GessoFgMuted)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isDone) GessoPrimary else GessoSurfaceRecessed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDone) Icons.Rounded.Check else Icons.Rounded.Schedule,
                        null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (index < displayItems.size - 1) {
                HorizontalDivider(color = GessoDivider, thickness = 1.dp)
            }
        }
    }
}
