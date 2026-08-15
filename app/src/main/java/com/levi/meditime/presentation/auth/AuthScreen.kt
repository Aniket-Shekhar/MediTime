package com.levi.meditime.presentation.auth

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.auth.login.LoginViewModel
import com.levi.meditime.presentation.auth.register.RegisterViewModel
import com.levi.meditime.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onNavigateToEmailVerify: (String) -> Unit,
    onBack: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Go ahead and set up\nyour account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                        fontSize = 28.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in-up to enjoy the best managing experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Main Auth Card (Refined to look like a tall rounded sheet)
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Modern Segmented Control (Toggle)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(27.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                TabButton(
                                    text = "Login",
                                    isSelected = isLoginMode,
                                    onClick = { isLoginMode = true },
                                    modifier = Modifier.weight(1f)
                                )
                                TabButton(
                                    text = "Register",
                                    isSelected = !isLoginMode,
                                    onClick = { isLoginMode = false },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoginMode) {
                        LoginForm(
                            viewModel = loginViewModel,
                            onSuccess = onAuthSuccess,
                            onPhoneOtp = onNavigateToOtp,
                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                        )
                    } else {
                        RegisterForm(
                            viewModel = registerViewModel,
                            onSuccess = onNavigateToEmailVerify,
                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(), 
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface, 
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Or login with",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SocialButton(
                            text = "Google",
                            icon = Icons.Rounded.AccountCircle, // Generic icon
                            modifier = Modifier.weight(1f)
                        )
                        SocialButton(
                            text = "Facebook",
                            icon = Icons.Rounded.Facebook,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoginForm(
    viewModel: LoginViewModel,
    onSuccess: () -> Unit,
    onPhoneOtp: (String) -> Unit,
    onError: (String) -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isPhoneMode by viewModel.isPhoneLoginMode.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()
    
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!isPhoneMode) {
            PremiumTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                icon = Icons.Rounded.Email,
                placeholder = "example@domain.com",
                error = emailError
            )

            PremiumTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                error = passwordError
            )
        } else {
            PremiumTextField(
                value = phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                label = "Phone Number",
                icon = Icons.Rounded.Phone,
                placeholder = "+1 234 567 8900",
                error = phoneError
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe, 
                    onCheckedChange = viewModel::onRememberMeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "Remember me", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(onClick = { viewModel.forgotPassword(onSuccess = onError, onError = onError) }) {
                Text(
                    text = "Forgot Password?", 
                    color = MaterialTheme.colorScheme.primary, 
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.login(
                    onSuccess = {
                        if (isPhoneMode) onPhoneOtp(phoneNumber) else onSuccess()
                    },
                    onError = onError
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimarySage),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isPhoneMode) "Send OTP" else "Login", fontWeight = FontWeight.Bold)
            }
        }
        
        TextButton(
            onClick = viewModel::toggleLoginMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isPhoneMode) "Use Email Instead" else "Login with Phone",
                color = PrimarySage,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RegisterForm(
    viewModel: RegisterViewModel,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PremiumTextField(
            value = name,
            onValueChange = viewModel::onNameChange,
            label = "Full Name",
            icon = Icons.Rounded.Person,
            placeholder = "Enter your full name",
            error = nameError
        )

        PremiumTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            label = "Email Address",
            icon = Icons.Rounded.Email,
            placeholder = "example@domain.com",
            error = emailError
        )

        PremiumTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            icon = Icons.Rounded.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            placeholder = "••••••••",
            error = passwordError
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.register(
                    onSuccess = { onSuccess(email) },
                    onError = onError
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimarySage),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Register", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    error: String? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            leadingIcon = { 
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (error != null) MaterialTheme.colorScheme.error else PrimarySage, 
                    modifier = Modifier.size(20.dp)
                ) 
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (error != null) {
                { Icon(Icons.Rounded.Error, null, tint = MaterialTheme.colorScheme.error) }
            } else null,
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                errorTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            singleLine = true,
            keyboardOptions = if (isPassword) {
                androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                )
            } else if (label.contains("Email", ignoreCase = true)) {
                androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                )
            } else {
                androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                )
            }
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun SocialButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text, 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
