package com.levi.meditime.presentation.symptoms

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.R
import com.levi.meditime.domain.model.Symptom
import com.levi.meditime.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDashboardScreen(
    onBack: () -> Unit = {},
    viewModel: SymptomViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 24.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.symptom_tracker),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Insights Section
            item {
                SymptomInsightsSectionRedesign(state)
            }

            // History Title
            item {
                SectionTitle(title = stringResource(R.string.recent_logs))
            }

            if (state.symptoms.isEmpty()) {
                item {
                    EmptyState(
                        illustration = { Icon(Icons.Rounded.MonitorHeart, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) },
                        title = stringResource(R.string.no_symptoms_logged),
                        description = stringResource(R.string.track_feelings_desc),
                        actionText = stringResource(R.string.log_symptom),
                        onActionClick = { showAddSheet = true }
                    )
                }
            } else {
                items(state.symptoms) { symptom ->
                    PremiumSymptomCardRedesign(
                        symptom = symptom,
                        onDelete = { viewModel.deleteSymptom(symptom) }
                    )
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 24.dp)
        ) {
            AnimatedFAB(onClick = { showAddSheet = true })
        }
    }

    if (showAddSheet) {
        AddSymptomBottomSheetRedesign(
            onDismiss = { showAddSheet = false },
            onSave = { name, severity, notes ->
                viewModel.addSymptom(name, severity, notes)
                showAddSheet = false
            }
        )
    }
}

@Composable
fun SymptomInsightsSectionRedesign(state: SymptomState) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val infoColor = if (isDark) InfoDark else InfoBlue
    val errorColor = if (isDark) ErrorDark else DangerRed

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.commonSymptom != null) {
            AppCard(
                backgroundColor = infoColor.copy(alpha = 0.1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Timeline, null, tint = infoColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.frequent_pattern),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = infoColor
                        )
                        Text(
                            text = stringResource(R.string.reported_frequently_msg, state.commonSymptom),
                            style = MaterialTheme.typography.bodySmall,
                            color = infoColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (state.symptomsIncreasingAfterMissed) {
            AppCard(
                backgroundColor = errorColor.copy(alpha = 0.1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, null, tint = errorColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.critical_insight),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = errorColor
                        )
                        Text(
                            text = stringResource(R.string.symptoms_increased_msg),
                            style = MaterialTheme.typography.bodySmall,
                            color = errorColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumSymptomCardRedesign(symptom: Symptom, onDelete: () -> Unit) {
    val severityColor = getSeverityColorRedesign(symptom.severity)
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        onClick = { /* Details */ }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = severityColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Favorite,
                        null,
                        tint = severityColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = symptom.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${symptom.severityLevel} • ${formatDateRedesign(symptom.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSymptomBottomSheetRedesign(
    onDismiss: () -> Unit,
    onSave: (String, Int, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var severity by remember { mutableFloatStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.log_symptom),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.symptom_name)) },
                placeholder = { Text(stringResource(R.string.headache_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.severity_format, severity.toInt()),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = severity,
                onValueChange = { severity = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                placeholder = { Text(stringResource(R.string.describe_feelings)) },
                leadingIcon = { Icon(Icons.Rounded.Notes, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            PrimaryButton(
                text = stringResource(R.string.save_log),
                onClick = { onSave(name, severity.toInt(), notes.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun getSeverityColorRedesign(severity: Int): Color {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    return when {
        severity <= 3 -> if (isDark) SuccessDark else SuccessGreen
        severity <= 7 -> if (isDark) WarningDark else WarningOrange
        else -> if (isDark) ErrorDark else DangerRed
    }
}

private fun formatDateRedesign(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
