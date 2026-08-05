package com.levi.meditime.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let { viewModel.setReminderSound(it.toString()) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, start = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(44.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Reminder Settings",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(bottom = 120.dp)
            ) {
                // Global Toggle
                SettingsGroupMinimal(title = "Core Configuration") {
                    SettingsItem(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Medication Reminders",
                        subtitle = "Enable or disable all notifications",
                        trailing = {
                            Switch(
                                checked = settings?.remindersEnabled ?: true,
                                onCheckedChange = { viewModel.setRemindersEnabled(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                }

                // Sound & Vibration
                var showSoundDialog by remember { mutableStateOf(false) }
                SettingsGroupMinimal(title = "Alert Style") {
                    SettingsItem(
                        icon = Icons.Rounded.VolumeUp,
                        title = "Notification Sound",
                        subtitle = settings?.reminderSound ?: "Default",
                        onClick = { showSoundDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Vibration,
                        title = "Vibration",
                        trailing = {
                            Switch(
                                checked = settings?.vibrationEnabled ?: true,
                                onCheckedChange = { viewModel.setVibrationEnabled(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                }

                if (showSoundDialog) {
                    SoundSelectionDialog(
                        current = settings?.reminderSound ?: "Default",
                        onDismiss = { showSoundDialog = false },
                        onSelect = {
                            viewModel.setReminderSound(it)
                            showSoundDialog = false
                        },
                        onPickFromDevice = {
                            val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                val currentUri = try { Uri.parse(settings?.reminderSound) } catch (e: Exception) { null }
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                            }
                            ringtonePickerLauncher.launch(intent)
                            showSoundDialog = false
                        }
                    )
                }

                // Snooze
                var showSnoozeDialog by remember { mutableStateOf(false) }
                SettingsGroupMinimal(title = "Postponement") {
                    SettingsItem(
                        icon = Icons.Rounded.Snooze,
                        title = "Snooze Duration",
                        subtitle = "${settings?.snoozeDurationMinutes ?: 10} minutes",
                        onClick = { showSnoozeDialog = true }
                    )
                }

                if (showSnoozeDialog) {
                    SnoozeDurationDialog(
                        current = settings?.snoozeDurationMinutes ?: 10,
                        onDismiss = { showSnoozeDialog = false },
                        onSelect = {
                            viewModel.setSnoozeDuration(it)
                            showSnoozeDialog = false
                        }
                    )
                }

                // Missed Alerts
                SettingsGroupMinimal(title = "Reliability") {
                    SettingsItem(
                        icon = Icons.Rounded.NotificationImportant,
                        title = "Missed Reminder Alerts",
                        subtitle = "Urgent alerts if dose not taken",
                        trailing = {
                            Switch(
                                checked = settings?.missedReminderAlertEnabled ?: true,
                                onCheckedChange = { viewModel.setMissedReminderAlertEnabled(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                }

                // Testing
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Send Test Notification",
                    onClick = { viewModel.sendTestNotification() },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    icon = Icons.Rounded.PlayArrow
                )
            }
        }
    }
}

@Composable
fun SoundSelectionDialog(
    current: String, 
    onDismiss: () -> Unit, 
    onSelect: (String) -> Unit,
    onPickFromDevice: () -> Unit
) {
    val sounds = listOf("Default", "Bell", "Soft Chime")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Sound", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                sounds.forEach { sound ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(sound) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == sound, onClick = { onSelect(sound) })
                        Spacer(Modifier.width(12.dp))
                        Text(sound)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onPickFromDevice() }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.LibraryMusic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Choose from device", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun SnoozeDurationDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val durations = listOf(5, 10, 15)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Snooze Duration", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                durations.forEach { min ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(min) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == min, onClick = { onSelect(min) })
                        Spacer(Modifier.width(12.dp))
                        Text("$min minutes")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
