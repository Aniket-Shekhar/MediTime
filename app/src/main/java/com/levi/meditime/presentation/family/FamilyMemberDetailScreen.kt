package com.levi.meditime.presentation.family

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.levi.meditime.domain.model.FamilyMember
import com.levi.meditime.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMemberDetailScreen(
    memberId: String, // Kept for HiltViewModel scope
    onBack: () -> Unit,
    onNavigateToMedicines: (String) -> Unit,
    viewModel: FamilyMemberDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // PRD 1.3: Verification of memberId usage
    Log.d("FamilyDetail", "Viewing member: $memberId")
    
    val member = state.member
    val accessibilityMode = LocalAccessibilityMode.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var showRemoveDialog by remember { mutableStateOf(false) }

    fun showComingSoon() {
        android.widget.Toast.makeText(context, "This feature is coming soon for family members.", android.widget.Toast.LENGTH_SHORT).show()
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Member", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${member?.name}? This will also delete their medicine assignments.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.removeMember { onBack() }
                        showRemoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        member?.let { mem ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Profile Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (accessibilityMode) 320.dp else 280.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    IconButton(
                        onClick = onBack, 
                        modifier = Modifier.padding(top = 60.dp, start = Spacing.section).border(1.5.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(if (accessibilityMode) 140.dp else 120.dp).border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        ) {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=${mem.name}&background=DCC6F5&color=000",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(Spacing.card))
                        Text(mem.name, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                        Text(mem.relationship.name, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.padding(Spacing.section), verticalArrangement = Arrangement.spacedBy(Spacing.section)) {
                    // Today's Progress
                    SectionTitle(title = "Health Status")
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Medicines Today", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${state.takenToday} of ${state.totalToday} doses completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            CircularProgressIndicator(
                                progress = { if (state.totalToday > 0) state.takenToday.toFloat() / state.totalToday else 0f },
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                strokeWidth = 6.dp
                            )
                        }
                    }

                    // Options Grid
                    SectionTitle(title = "Management")
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        ManagementRow(
                            icon = Icons.Rounded.Medication, 
                            title = "Assigned Medicines", 
                            count = "${state.medicineCount} Active",
                            onClick = { onNavigateToMedicines(mem.id) }
                        )
                        ManagementRow(
                            icon = Icons.Rounded.Assessment, 
                            title = "Health Reports", 
                            count = "2 New", 
                            onClick = { showComingSoon() }
                        )
                        ManagementRow(
                            icon = Icons.Rounded.Favorite, 
                            title = "Symptom Log", 
                            count = "Today", 
                            onClick = { showComingSoon() }
                        )
                        ManagementRow(
                            icon = Icons.Rounded.Event, 
                            title = "Appointments", 
                            count = "1 Upcoming", 
                            onClick = { showComingSoon() }
                        )
                        ManagementRow(
                            icon = Icons.Rounded.History, 
                            title = "Activity History", 
                            count = "", 
                            onClick = { showComingSoon() }
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.card))
                    
                    SecondaryButton(text = "Manage Permissions", onClick = { /* Deferred */ }, icon = Icons.Rounded.VpnKey)
                    
                    PrimaryButton(
                        text = "Remove Member", 
                        onClick = { showRemoveDialog = true }, 
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), 
                        contentColor = MaterialTheme.colorScheme.error,
                        icon = Icons.Rounded.PersonRemove
                    )
                    
                    Spacer(modifier = Modifier.height(140.dp))
                }
            }
        }
    }
}

@Composable
fun ManagementRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    count: String,
    onClick: () -> Unit
) {
    val accessibilityMode = LocalAccessibilityMode.current
    AppCard(
        modifier = Modifier.fillMaxWidth().height(if (accessibilityMode) 80.dp else 64.dp), 
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.size(40.dp).border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.small)),
                shape = RoundedCornerShape(Radius.small),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(Spacing.card))
            Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (count.isNotEmpty()) {
                Text(count, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(Spacing.small))
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }
    }
}
