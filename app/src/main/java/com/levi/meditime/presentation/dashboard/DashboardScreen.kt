package com.levi.meditime.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 140.dp,
                top = 60.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // v3.0 Header
            item {
                DashboardHeader(
                    userName = state.userName,
                    onProfileClick = onNavigateToProfile,
                    onNotificationClick = onNavigateToNotifications
                )
            }

            // v3.0 Today's Progress Card
            item {
                TodayProgressCard(
                    completed = state.completedDosesToday,
                    total = state.totalDosesToday,
                    onClick = onNavigateToReports
                )
            }

            // v3.0 Next Medicine Card
            item {
                if (!state.isLoading && state.medicines.isNotEmpty()) {
                    NextMedicineCard(
                        medicine = state.medicines.first(),
                        onTaken = { viewModel.takeMedicine(state.medicines.first()) },
                        onSkip = { viewModel.skipMedicine(state.medicines.first()) },
                        onClick = { onNavigateToMedicineDetail(state.medicines.first().id) }
                    )
                }
            }

            // v3.0 Quick Actions Grid
            item {
                QuickActionsGrid(
                    onAddMedicine = onAddMedicine,
                    onScan = onNavigateToPrescriptions,
                    onDoctor = onNavigateToAppointments,
                    onSOS = onNavigateToSOS
                )
            }

            // v3.0 Medicine Stock Card
            item {
                SectionTitle(title = stringResource(R.string.medicine_stock), actionText = stringResource(R.string.view_all), onActionClick = onViewAllMedicines)
                MedicineStockCard(state.medicines.take(2))
            }

            // v3.0 Family Status Card
            item {
                SectionTitle(title = stringResource(R.string.family_status), actionText = stringResource(R.string.view_all), onActionClick = onViewAllFamily)
                FamilyStatusCard()
            }

            // v3.0 Recent Activity
            item {
                SectionTitle(title = stringResource(R.string.recent_activity))
                RecentActivityCard()
            }
        }
    }
}

@Composable
fun DashboardHeader(userName: String, onProfileClick: () -> Unit, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.good_morning, userName) + " \uD83D\uDC4B",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.take_care_health),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(44.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.onSurface)
        }
        
        Spacer(Modifier.width(12.dp))
        
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() },
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=$userName&background=14B8A6&color=fff",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TodayProgressCard(completed: Int, total: Int, onClick: () -> Unit) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "")

    AppCard(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        elevation = Shadow.card,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(Modifier.width(24.dp))
            
            Column {
                Text(text = stringResource(R.string.todays_progress), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                ProgressBadge(text = stringResource(R.string.taken_badge, completed), color = SuccessGreen, icon = Icons.Rounded.CheckCircle)
                Spacer(Modifier.height(4.dp))
                ProgressBadge(text = stringResource(R.string.upcoming_badge, total - completed), color = InfoBlue, icon = Icons.Rounded.Schedule)
                Spacer(Modifier.height(4.dp))
                ProgressBadge(text = stringResource(R.string.missed_badge, 0), color = DangerRed, icon = Icons.Rounded.Cancel)
            }
        }
    }
}

@Composable
fun ProgressBadge(text: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun NextMedicineCard(medicine: Medicine, onTaken: () -> Unit, onSkip: () -> Unit, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = Shadow.card
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(Radius.medium),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (medicine.imageUrl != null) {
                        AsyncImage(model = medicine.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Medication, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("08:30 AM • ${medicine.dosage}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onTaken,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(Radius.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.taken), fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(Radius.medium),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(stringResource(R.string.skip), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(onAddMedicine: () -> Unit, onScan: () -> Unit, onDoctor: () -> Unit, onSOS: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionItem(stringResource(R.string.add_med), Icons.Rounded.Add, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, Modifier.weight(1f), onAddMedicine)
        QuickActionItem(stringResource(R.string.scan_rx), Icons.Rounded.QrCodeScanner, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary, Modifier.weight(1f), onScan)
        QuickActionItem(stringResource(R.string.doctor), Icons.Rounded.MedicalServices, WarningDark.copy(alpha = 0.1f), WarningDark, Modifier.weight(1f), onDoctor)
        QuickActionItem(stringResource(R.string.sos), Icons.Rounded.Sos, ErrorDark.copy(alpha = 0.1f), ErrorDark, Modifier.weight(1f), onSOS)
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, bgColor: Color, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 1.03f else 1f, label = "")

    Column(
        modifier = modifier.scale(scale).clickable(interactionSource = interactionSource, indication = null) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape( Radius.medium),
            color = bgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun MedicineStockCard(medicines: List<Medicine>) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            medicines.forEach { medicine ->
                val isLow = medicine.currentStock <= medicine.minimumStock
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isLow) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                        null,
                        tint = if (isLow) WarningDark else SuccessDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(medicine.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "${medicine.currentStock} units left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyStatusCard() {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FamilyStatusItem(
                name = "Mom",
                status = "All medicines taken",
                statusColor = SuccessDark,
                avatarUrl = "https://ui-avatars.com/api/?name=Mom&background=22C55E&color=fff"
            )
            FamilyStatusItem(
                name = "Dad",
                status = "Missed evening dose",
                statusColor = MaterialTheme.colorScheme.error,
                avatarUrl = "https://ui-avatars.com/api/?name=Dad&background=EF4444&color=fff"
            )
        }
    }
}

@Composable
fun FamilyStatusItem(name: String, status: String, statusColor: Color, avatarUrl: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primaryContainer) {
            AsyncImage(model = avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RecentActivityCard() {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityItem("Aspirin taken", SuccessDark, Icons.Rounded.CheckCircle)
            ActivityItem("Vitamin D skipped", MaterialTheme.colorScheme.onSurfaceVariant, Icons.Rounded.Cancel)
            ActivityItem("Paracetamol reminder completed", SuccessDark, Icons.Rounded.CheckCircle)
        }
    }
}

@Composable
fun ActivityItem(text: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = if (color == MaterialTheme.colorScheme.onSurfaceVariant) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
    }
}
