package com.levi.meditime.presentation.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.R
import com.levi.meditime.datastore.ThemeMode
import com.levi.meditime.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    notifViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val accessibilityMode = settings?.accessibilityMode ?: false

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 120.dp)
                ) {
                    // Appearance Group
                    SettingsGroupMinimal(title = "Appearance") {
                        SettingsItem(
                            icon = Icons.Rounded.Palette,
                            title = "Theme",
                            subtitle = when(settings?.themeMode) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                else -> "System Default"
                            },
                            onClick = { showThemeDialog = true }
                        )
                        SettingsItem(
                            icon = Icons.Rounded.Accessibility,
                            title = "Senior Citizen Mode",
                            subtitle = "Larger text and buttons",
                            trailing = {
                                Switch(
                                    checked = accessibilityMode,
                                    onCheckedChange = { viewModel.setAccessibilityMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )
                    }

                    // Cloud Backup Group
                    SettingsGroupMinimal(title = "Cloud Backup") {
                        SettingsItem(
                            icon = Icons.Rounded.CloudUpload,
                            title = "Backup Now",
                            subtitle = "Sync data to cloud",
                            onClick = { viewModel.backupNow() }
                        )
                        SettingsItem(
                            icon = Icons.Rounded.CloudDownload,
                            title = "Restore Backup",
                            subtitle = "Recover data from cloud",
                            onClick = { viewModel.restoreNow() }
                        )
                    }

                    // Notifications Group
                    SettingsGroupMinimal(title = "Notifications") {
                        SettingsItem(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Medicine Reminders",
                            trailing = {
                                Switch(
                                    checked = settings?.remindersEnabled ?: true,
                                    onCheckedChange = { viewModel.setRemindersEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        )
                        SettingsItem(
                            icon = Icons.Rounded.Inventory,
                            title = "Low Stock Alerts",
                            trailing = {
                                Switch(
                                    checked = settings?.lowStockNotificationsEnabled ?: true,
                                    onCheckedChange = { viewModel.setLowStockEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        )
                    }

                    // Language Group
                    SettingsGroupMinimal(title = "Language") {
                        SettingsItem(
                            icon = Icons.Rounded.Language,
                            title = "App Language",
                            subtitle = when(settings?.language) {
                                "hi" -> "Hindi"
                                "bn" -> "Bengali"
                                "ta" -> "Tamil"
                                "te" -> "Telugu"
                                "as" -> "Assamese"
                                else -> "English"
                            },
                            onClick = { showLanguageDialog = true }
                        )
                    }

                    // Security Group
                    SettingsGroupMinimal(title = "Security") {
                        SettingsItem(
                            icon = Icons.Rounded.Fingerprint,
                            title = "Biometric Lock",
                            trailing = {
                                Switch(
                                    checked = settings?.appLockEnabled ?: false,
                                    onCheckedChange = { viewModel.setAppLockEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        )
                    }

                    // Support Group
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SettingsGroupMinimal(title = "Support") {
                        SettingsItem(
                            icon = Icons.Rounded.Info,
                            title = "About MediTime",
                            onClick = onNavigateToAbout
                        )
                        SettingsItem(
                            icon = Icons.Rounded.HelpOutline,
                            title = "Help Center",
                            onClick = {
                                android.widget.Toast.makeText(context, "Help Center is coming soon.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = settings?.themeMode ?: ThemeMode.SYSTEM,
            onDismiss = { showThemeDialog = false },
            onSelect = { 
                viewModel.setThemeMode(it)
                showThemeDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLang = settings?.language ?: "en",
            onDismiss = { showLanguageDialog = false },
            onSelect = { 
                viewModel.setLanguage(it)
                showLanguageDialog = false
            }
        )
    }
}

@Composable
fun SettingsGroupMinimal(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        AppCard(modifier = Modifier.fillMaxWidth(), border = true, elevation = 0.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                ThemeOption(label = "System Default", selected = currentMode == ThemeMode.SYSTEM, onClick = { onSelect(ThemeMode.SYSTEM) })
                ThemeOption(label = "Light Mode", selected = currentMode == ThemeMode.LIGHT, onClick = { onSelect(ThemeMode.LIGHT) })
                ThemeOption(label = "Dark Mode", selected = currentMode == ThemeMode.DARK, onClick = { onSelect(ThemeMode.DARK) })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
        },
        shape = RoundedCornerShape(Radius.large),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLang: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "hi" to "Hindi",
        "bn" to "Bengali",
        "ta" to "Tamil",
        "te" to "Telugu",
        "as" to "Assamese"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Language", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(languages) { (code, name) ->
                    LanguageOption(label = name, selected = currentLang == code, onClick = { onSelect(code) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
        },
        shape = RoundedCornerShape(Radius.large),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
