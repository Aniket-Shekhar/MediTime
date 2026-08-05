package com.levi.meditime.presentation.sos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.levi.meditime.R
import com.levi.meditime.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SosScreen(
    onBack: () -> Unit,
    viewModel: SosViewModel = hiltViewModel()
) {
    val isSending by viewModel.isSending.collectAsState()
    val message by viewModel.message.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val accessibilityMode = LocalAccessibilityMode.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing), // PRD 2.5 - 1.5 sec
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    var longPressActive by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(3) }
    
    LaunchedEffect(longPressActive) {
        if (longPressActive) {
            countdown = 3
            while (countdown > 0) {
                kotlinx.coroutines.delay(1000)
                countdown--
            }
            if (longPressActive) {
                if (permissionState.allPermissionsGranted) {
                    viewModel.triggerSos()
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
                longPressActive = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = Spacing.card, end = Spacing.section),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(Spacing.card))
                    Text(
                        text = "Emergency SOS",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(Spacing.large))
                
                var showAssistanceDialog by remember { mutableStateOf(false) }

                AppCard(
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    border = true,
                    elevation = 0.dp,
                    onClick = { showAssistanceDialog = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(Spacing.card))
                        Text(
                            text = "Immediate assistance required?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (showAssistanceDialog) {
                    AssistanceOptionsDialog(onDismiss = { showAssistanceDialog = false })
                }

                Spacer(modifier = Modifier.weight(1f))

                // SOS Core Action
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    // Animated Background Rings
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scale)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    )
                    
                    // The actual button
                    var showConfirm by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    Surface(
                        modifier = Modifier
                            .size(220.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        if (permissionState.allPermissionsGranted) showConfirm = true 
                                        else permissionState.launchMultiplePermissionRequest()
                                    },
                                    onPress = {
                                        longPressActive = true
                                        try {
                                            awaitRelease()
                                        } finally {
                                            longPressActive = false
                                        }
                                    }
                                )
                            },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                            } else if (longPressActive) {
                                Text(
                                    text = countdown.toString(),
                                    color = Color.White,
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.Sos, null, tint = Color.White, modifier = Modifier.size(64.dp))
                                    Text("HELP", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                }
                            }
                        }
                    }

                    if (showConfirm) {
                        AlertDialog(
                            onDismissRequest = { showConfirm = false },
                            title = { Text("Confirm Emergency SOS", fontWeight = FontWeight.ExtraBold) },
                            text = { Text("This will alert your primary emergency contacts, share your live location, and initiate an emergency call.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.triggerSos()
                                        showConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(Radius.medium)
                                ) {
                                    Text("Alert Now", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirm = false }) {
                                    Text("Cancel")
                                }
                            },
                            shape = RoundedCornerShape(Radius.extraLarge)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.card))

                // PRD 1.3: Stop SOS Button
                TextButton(
                    onClick = { viewModel.stopSos() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.Rounded.Stop, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Stop SOS Sharing", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Feature Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SosFeatureIcon(Icons.Rounded.GpsFixed, "Location")
                    SosFeatureIcon(Icons.Rounded.Sms, "SMS Alert")
                    SosFeatureIcon(Icons.Rounded.Call, "Auto Call")
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Text(
                    text = "Holding the button will automatically trigger in 3 seconds",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = Spacing.card)
                )
            }
        }
    }
}

@Composable
fun AssistanceOptionsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Emergency Assistance", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                AssistanceItem(
                    icon = Icons.Rounded.LocalHospital,
                    title = "Nearby Hospitals",
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=hospitals")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                        onDismiss()
                    }
                )
                AssistanceItem(
                    icon = Icons.Rounded.Phone,
                    title = "Emergency Numbers",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                        context.startActivity(intent)
                        onDismiss()
                    }
                )
                AssistanceItem(
                    icon = Icons.Rounded.MedicalServices,
                    title = "First Aid Guide",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.redcross.org/get-help/how-to-prepare-for-emergencies/types-of-emergencies/first-aid-and-cpr.html"))
                        context.startActivity(intent)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        shape = RoundedCornerShape(Radius.large)
    )
}

@Composable
fun AssistanceItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.medium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.card),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Spacing.card))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SosFeatureIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}
