package com.levi.meditime.presentation.auth.register

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

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
                .fillMaxHeight(0.3f)
                .background(
                    Brush.verticalGradient(
                        listOf(GessoPrimary.copy(alpha = 0.2f), GessoCanvas)
                    )
                )
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = GessoSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = GessoFg)
                    }
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        color = GessoFg
                    )
                }
            }
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
                Spacer(modifier = Modifier.height(GessoSpacing.s4))
                
                Text(
                    text = "Join MediTime",
                    style = MaterialTheme.typography.displayMedium,
                    color = GessoFg
                )
                Text(
                    text = "Start tracking your family's health with a premium experience.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GessoFgMuted
                )
                
                Spacer(modifier = Modifier.height(GessoSpacing.s6))
                
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(GessoSpacing.s3)) {
                        GessoInput(
                            value = name,
                            onValueChange = viewModel::onNameChange,
                            placeholder = "Full Name",
                            leadingIcon = Icons.Rounded.Person,
                            isError = nameError != null
                        )
                        if (nameError != null) {
                            Text(nameError!!, color = GessoError, style = MaterialTheme.typography.labelSmall)
                        }
                        
                        GessoInput(
                            value = email,
                            onValueChange = viewModel::onEmailChange,
                            placeholder = "Email Address",
                            leadingIcon = Icons.Rounded.Email,
                            isError = emailError != null,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        if (emailError != null) {
                            Text(emailError!!, color = GessoError, style = MaterialTheme.typography.labelSmall)
                        }
                        
                        GessoInput(
                            value = password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = "Password (min 6 chars)",
                            leadingIcon = Icons.Rounded.Lock,
                            isError = passwordError != null,
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
                        if (passwordError != null) {
                            Text(passwordError!!, color = GessoError, style = MaterialTheme.typography.labelSmall)
                        }
                        
                        Spacer(modifier = Modifier.height(GessoSpacing.s1))
                        
                        PrimaryButton(
                            text = "Create Account",
                            loading = isLoading,
                            onClick = {
                                viewModel.register(
                                    onSuccess = { onRegisterSuccess(email) },
                                    onError = { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    }
                                )
                            }
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
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GessoFgMuted
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GessoAccentText,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
                
                Spacer(modifier = Modifier.height(GessoSpacing.s8))
            }
        }
    }
}
