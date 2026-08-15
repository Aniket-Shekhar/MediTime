package com.levi.meditime.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.levi.meditime.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.model.History
import com.levi.meditime.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicineHistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // v3.0 Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(44.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.timeline),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (state.historyItems.isEmpty()) {
                    item {
                        EmptyState(
                            illustration = { Icon(Icons.Rounded.History, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) },
                            title = stringResource(R.string.no_activity_yet),
                            description = stringResource(R.string.history_empty_desc),
                            actionText = stringResource(R.string.go_home),
                            onActionClick = onBack
                        )
                    }
                } else {
                    items(state.historyItems) { history ->
                        TimelineItemRow(history)
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun TimelineItemRow(history: History) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val statusColor = when (history.status) {
        DoseStatus.TAKEN -> if (isDark) SuccessDark else SuccessGreen
        DoseStatus.MISSED -> if (isDark) ErrorDark else DangerRed
        DoseStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        DoseStatus.PENDING -> if (isDark) WarningDark else WarningOrange
    }

    val statusIcon = when (history.status) {
        DoseStatus.TAKEN -> Icons.Rounded.CheckCircle
        DoseStatus.MISSED -> Icons.Rounded.Cancel
        DoseStatus.SKIPPED -> Icons.Rounded.SkipNext
        DoseStatus.PENDING -> Icons.Rounded.Schedule
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Node and Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape)
                    .border(2.dp, statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(14.dp))
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
        }

        Spacer(Modifier.width(16.dp))

        // Medicine Card
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 1.dp,
                border = true
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (history.status == DoseStatus.TAKEN) stringResource(R.string.dose_taken) else history.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatDateTime(history.scheduledTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (history.actionTime != null) {
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = formatTimeOnly(history.actionTime),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateTime(time: Long): String {
    val date = Date(time)
    return SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault()).format(date)
}

private fun formatTimeOnly(time: Long): String {
    val date = Date(time)
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
}
