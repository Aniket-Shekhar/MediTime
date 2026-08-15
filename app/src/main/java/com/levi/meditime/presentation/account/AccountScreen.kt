package com.levi.meditime.presentation.account

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.levi.meditime.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.levi.meditime.presentation.theme.*

@Composable
fun AccountScreen(
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFamily: () -> Unit,
    onNavigateToCaretakers: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToProfileSelection: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLinkedDevices: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToReminderSettings: () -> Unit,
    onNavigateToOtpVerification: (String) -> Unit,
    onNavigateToEmailVerification: (String) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val requiresReauth by viewModel.requiresReauth.collectAsStateWithLifecycle()

    if (requiresReauth) {
        AlertDialog(
            onDismissRequest = viewModel::dismissReauth,
            title = { Text("Re-authentication Required", fontWeight = FontWeight.Bold) },
            text = { Text("For security, you must have logged in recently to delete your account. Please log out and log in again, or use biometrics to continue.") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.dismissReauth()
                    // Real implementation: Trigger Biometric Prompt or Navigate to Re-auth
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissReauth) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    LaunchedEffect(message, error) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
        ) {
            // v3.0 Screen Title
            item {
                Text(
                    text = stringResource(R.string.profile),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // v3.0 User Card (90dp height)
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    elevation = 2.dp,
                    onClick = onNavigateToEditProfile
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            val avatarUrl = remember(user?.name) {
                                "https://ui-avatars.com/api/?name=${android.net.Uri.encode(user?.name ?: "User")}&background=14B8A6&color=fff"
                            }
                            AsyncImage(
                                model = user?.profileImage ?: avatarUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.name ?: stringResource(R.string.guest_user), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user?.email ?: stringResource(R.string.sign_in_to_sync), 
                                style = MaterialTheme.typography.labelSmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            Icons.Rounded.ChevronRight, 
                            null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // v3.0 Settings List (64dp items)
            item {
                SectionTitle(title = stringResource(R.string.account_details))
            }
            
            item {
                SettingsCard {
                    SettingsItem(icon = Icons.Rounded.Person, title = stringResource(R.string.personal_information), onClick = onNavigateToEditProfile)
                    SettingsItem(icon = Icons.Rounded.NotificationsActive, title = stringResource(R.string.reminders), onClick = onNavigateToReminderSettings)
                    SettingsItem(icon = Icons.Rounded.Group, title = stringResource(R.string.family_caregivers), onClick = onNavigateToFamily)
                    SettingsItem(icon = Icons.Rounded.Emergency, title = stringResource(R.string.emergency_contacts), onClick = onNavigateToEmergencyContacts)
                }
            }

            item {
                SectionTitle(title = stringResource(R.string.general))
            }

            item {
                SettingsCard {
                    SettingsItem(icon = Icons.Rounded.Settings, title = stringResource(R.string.app_settings), onClick = onNavigateToSettings)
                    SettingsItem(icon = Icons.Rounded.Info, title = stringResource(R.string.about_meditime), onClick = onNavigateToAbout)
                    SettingsItem(
                        icon = Icons.Rounded.Logout, 
                        title = stringResource(R.string.logout), 
                        onClick = { viewModel.logout(onLogout) },
                        trailing = { 
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) 
                        }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        border = true,
        elevation = 1.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            content()
        }
    }
}
