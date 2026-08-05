package com.levi.meditime.presentation.auth.email

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.theme.*

@Composable
fun EmailVerificationScreen(
    email: String,
    onNavigateToLogin: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val otp by viewModel.otp.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timer by viewModel.timer.collectAsState()
    val canResend by viewModel.canResend.collectAsState()
    val isVerified by viewModel.isVerified.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

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
        // Overlay back click
        Box(modifier = Modifier.fillMaxSize().clickable { onNavigateToLogin() })

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
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color(0xFF4ADE80)
                        ) {
                            Icon(Icons.Rounded.MarkEmailRead, null, tint = Color.White, modifier = Modifier.padding(10.dp))
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
                    text = "Enter the 6-digit code sent to:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLight,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimarySage
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Using centered text style for OTP
                OutlinedTextField(
                    value = otp,
                    onValueChange = viewModel::onOtpChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimarySage,
                        unfocusedBorderColor = OutlineLight,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.verifyEmailOtp(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySage),
                    shape = RoundedCornerShape(16.dp),
                    enabled = otp.length == 6 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { viewModel.sendEmailOtp(email) },
                    enabled = canResend
                ) {
                    Text(
                        if (canResend) "Resend OTP" else "Resend in ${timer}s",
                        fontWeight = FontWeight.Bold,
                        color = if (canResend) PrimarySage else OnSurfaceVariantLight
                    )
                }
            }
        }
    }
}

