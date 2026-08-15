package com.levi.meditime.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.manager.ScreenshotSecurityManager
import com.levi.meditime.presentation.settings.SettingsViewModel
import com.levi.meditime.presentation.theme.*

@Composable
fun AppLockScreen(
    onUnlockSuccess: () -> Unit,
    securityManager: ScreenshotSecurityManager,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val accessibilityMode = LocalAccessibilityMode.current
    val settings by viewModel.settings.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (securityManager.isBiometricAvailable(activity)) {
            securityManager.showBiometricPrompt(
                activity = activity,
                onSuccess = onUnlockSuccess,
                onError = { /* Fallback to PIN if implemented */ }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Security Icon
            Surface(
                modifier = Modifier
                    .size(if (accessibilityMode) 140.dp else 120.dp)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier.size(if (accessibilityMode) 72.dp else 56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Text(
                text = "MediTime is Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            Text(
                text = "Please authenticate to access your personal medication reminders and health data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.card)
            )

            Spacer(modifier = Modifier.height(64.dp))

            PrimaryButton(
                text = "Unlock with Biometrics",
                onClick = {
                    securityManager.showBiometricPrompt(
                        activity = activity,
                        onSuccess = onUnlockSuccess,
                        onError = { }
                    )
                },
                icon = Icons.Rounded.Fingerprint,
                modifier = Modifier.width(if (accessibilityMode) 300.dp else 260.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.card))
            
            TextButton(onClick = { 
                if (settings?.appPin != null) {
                    showPinDialog = true 
                } else {
                    pinError = "No PIN set. Please use biometrics."
                }
            }) {
                Text(
                    text = "Use App PIN", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (pinError != null && !showPinDialog) {
                pinError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (showPinDialog) {
            PinEntryDialog(
                correctPin = settings?.appPin ?: "",
                onDismiss = { showPinDialog = false },
                onSuccess = {
                    showPinDialog = false
                    onUnlockSuccess()
                }
            )
        }
        
        // Bottom Branding
        Text(
            text = "SECURED BY MEDITIME",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun PinEntryDialog(
    correctPin: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter App PIN", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) enteredPin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.width(150.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
                if (error != null) {
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (enteredPin == correctPin) {
                    onSuccess()
                } else {
                    error = "Incorrect PIN"
                    enteredPin = ""
                }
            }) {
                Text("Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
