package com.levi.meditime.presentation.auth.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.R
import com.levi.meditime.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToPhoneOtp: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val isPhoneLoginMode by viewModel.isPhoneLoginMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GessoCanvas)
    ) {
        // Subtle top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(
                    Brush.verticalGradient(
                        listOf(GessoPrimary.copy(alpha = 0.2f), GessoCanvas)
                    )
                )
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = GessoSpacing.s4)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(GessoSpacing.s8))
                
                // Branding
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(GessoPrimary, RoundedCornerShape(GessoRadius.md))
                        .border(2.dp, GessoDivider, RoundedCornerShape(GessoRadius.md)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Medication,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(GessoSpacing.s4))
                
                Text(
                    text = "Welcome to MediTime",
                    style = MaterialTheme.typography.displayMedium,
                    color = GessoFg
                )
                Text(
                    text = "Sign in to manage your family's health.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GessoFgMuted
                )
                
                Spacer(modifier = Modifier.height(GessoSpacing.s6))
                
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(GessoSpacing.s3)) {
                        Text(
                            text = if (isPhoneLoginMode) "Use Phone Number" else "Use Email Address",
                            style = MaterialTheme.typography.titleLarge,
                            color = GessoFg
                        )

                        if (!isPhoneLoginMode) {
                            GessoInput(
                                value = email,
                                onValueChange = viewModel::onEmailChange,
                                placeholder = "Email Address",
                                leadingIcon = Icons.Rounded.Email,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            
                            GessoInput(
                                value = password,
                                onValueChange = viewModel::onPasswordChange,
                                placeholder = "Password",
                                leadingIcon = Icons.Rounded.Lock,
                                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = null,
                                            tint = GessoFgMuted
                                        )
                                    }
                                }
                            )
                        } else {
                            GessoInput(
                                value = phoneNumber,
                                onValueChange = viewModel::onPhoneNumberChange,
                                placeholder = "Phone Number (+1...)",
                                leadingIcon = Icons.Rounded.Phone,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(GessoSpacing.s1))
                        
                        PrimaryButton(
                            text = if (isPhoneLoginMode) "Get Verification Code" else "Sign In",
                            loading = isLoading,
                            onClick = {
                                viewModel.login(
                                    onSuccess = {
                                        if (isPhoneLoginMode) {
                                            onNavigateToPhoneOtp(phoneNumber)
                                        } else {
                                            onLoginSuccess()
                                        }
                                    },
                                    onError = { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    }
                                )
                            }
                        )
                        
                        SecondaryButton(
                            text = if (isPhoneLoginMode) "Sign in with Email" else "Sign in with Phone",
                            icon = Icons.Rounded.SwapHoriz,
                            onClick = viewModel::toggleLoginMode
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(GessoSpacing.s4))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New to MediTime? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GessoFgMuted
                    )
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GessoAccentText,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }
                
                Spacer(modifier = Modifier.height(GessoSpacing.s8))
            }
        }
    }
}
