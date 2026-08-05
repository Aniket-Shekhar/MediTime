package com.levi.meditime.presentation.auth.otp

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.levi.meditime.R
import com.levi.meditime.presentation.components.OtpInputField
import com.levi.meditime.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val otp by viewModel.otp.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val canResend by viewModel.canResend.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isVerified by viewModel.isVerified.collectAsStateWithLifecycle()
    val isLockedOut by viewModel.isLockedOut.collectAsStateWithLifecycle()
    val resendAttempts by viewModel.resendAttempts.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.sendOtp(phoneNumber, it)
        }
    }

    LaunchedEffect(isVerified) {
        if (isVerified) {
            onVerificationSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        // Transparent Overlay to handle back click
        Box(modifier = Modifier.fillMaxSize().clickable { onBack() })

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color(0xFF4ADE80) // Brighter green for check
                        ) {
                            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verify your account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter 6 digits verification code we have sent to",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLight,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimarySage
                )

                Spacer(modifier = Modifier.height(32.dp))

                OtpInputField(
                    otpText = otp,
                    onOtpChange = viewModel::onOtpChange,
                    isError = error != null || isLockedOut
                )

                if (isLockedOut) {
                    Text(
                        text = "Too many attempts. Locked for 10 min.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.verifyOtp(phoneNumber) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySage),
                    shape = RoundedCornerShape(16.dp),
                    enabled = otp.length == 6 && !isLockedOut && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, SocialButtonBorder, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { /* Placeholder */ },
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.Fingerprint, null, tint = OnSurfaceLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Biometric Verification", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(
                    onClick = { 
                        activity?.let { viewModel.resendOtp(phoneNumber, it) }
                    },
                    enabled = canResend && !isLockedOut && !isLoading
                ) {
                    Text(
                        if (canResend) "Resend Code" else "Resend in ${timer}s",
                        fontWeight = FontWeight.Bold,
                        color = if (canResend) PrimarySage else OnSurfaceVariantLight
                    )
                }
            }
        }
    }
}
