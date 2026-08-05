package com.levi.meditime.presentation.auth.profile

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.levi.meditime.R
import com.levi.meditime.domain.model.Country
import com.levi.meditime.manager.rememberImagePicker
import com.levi.meditime.presentation.components.CountryPickerDialog
import com.levi.meditime.presentation.components.InternationalPhoneInput
import com.levi.meditime.presentation.components.OtpInputField
import com.levi.meditime.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun VerificationCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVerified: Boolean,
    showOtpField: Boolean,
    otpValue: String,
    onOtpChange: (String) -> Unit,
    timerValue: Int,
    canResend: Boolean,
    isExpired: Boolean,
    onSendClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onChangeClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean,
    isPhone: Boolean = false,
    selectedCountry: Country? = null,
    onCountryClick: () -> Unit = {}
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        border = true,
        elevation = 0.dp // PRD 2.5: No shadow
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                if (isVerified) {
                    val statusColor = if (androidx.compose.foundation.isSystemInDarkTheme()) SuccessDark else SuccessGreen
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Verified, null, tint = statusColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = isVerified,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "verification_state"
            ) { verified ->
                if (!verified) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (isPhone) {
                            selectedCountry?.let {
                                InternationalPhoneInput(
                                    value = value,
                                    onValueChange = onValueChange,
                                    selectedCountry = it,
                                    onCountryClick = onCountryClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = true
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = value,
                                onValueChange = onValueChange,
                                label = { Text("Enter $title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }

                        AnimatedContent(
                            targetState = showOtpField,
                            transitionSpec = { fadeIn(tween(300)) + expandVertically() togetherWith fadeOut(tween(200)) + shrinkVertically() },
                            label = "otp_field_reveal"
                        ) { showOtp ->
                            if (!showOtp) {
                                PrimaryButton(
                                    text = "Verify $title",
                                    onClick = onSendClick,
                                    enabled = !isLoading && value.isNotBlank(),
                                    loading = isLoading,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        "Enter 6-digit verification code", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OtpInputField(
                                        otpText = otpValue,
                                        onOtpChange = onOtpChange,
                                        isError = isExpired
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isExpired) {
                                            Text("Code expired", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                        } else if (timerValue > 0) {
                                            Text(
                                                text = "00:${timerValue.toString().padStart(2, '0')}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Spacer(Modifier.width(1.dp))
                                        }

                                        TextButton(
                                            onClick = onSendClick,
                                            enabled = canResend && !isLoading
                                        ) {
                                            Text(
                                                if (canResend) "Resend Code" else "Resend in ${timerValue}s",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (canResend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    PrimaryButton(
                                        text = "Verify Code",
                                        onClick = onVerifyClick,
                                        enabled = !isLoading && otpValue.length == 6 && !isExpired,
                                        loading = isLoading
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {},
                        label = { Text("Verified $title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        enabled = false,
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()

    // Email States
    val showEmailOtpSection by viewModel.showEmailOtpSection.collectAsState()
    val emailOtp by viewModel.emailOtp.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()
    val emailTimer by viewModel.emailTimer.collectAsState()
    val canResendEmailOtp by viewModel.canResendEmailOtp.collectAsState()
    val isEmailOtpExpired by viewModel.isEmailOtpExpired.collectAsState()

    // Phone States
    val showPhoneOtpSection by viewModel.showPhoneOtpSection.collectAsState()
    val phoneOtp by viewModel.phoneOtp.collectAsState()
    val isPhoneVerified by viewModel.isPhoneVerified.collectAsState()
    val phoneTimer by viewModel.phoneTimer.collectAsState()
    val canResendPhoneOtp by viewModel.canResendPhoneOtp.collectAsState()
    val isPhoneOtpExpired by viewModel.isPhoneOtpExpired.collectAsState()

    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val showCountryPicker by viewModel.showCountryPicker.collectAsState()

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    var age by remember(user) { mutableStateOf(user?.age?.toString() ?: "") }
    var gender by remember(user) { mutableStateOf(user?.gender ?: "") }
    var emergencyContact by remember(user) { mutableStateOf(user?.emergencyContact ?: "") }

    val context = LocalContext.current
    val activity = context as android.app.Activity
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberImagePicker { uri ->
        viewModel.uploadProfileImage(uri)
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile Image Section
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                        shadowElevation = 8.dp
                    ) {
                        Crossfade(
                            targetState = if (isUploading) "loading" else if (user?.profileImage.isNullOrEmpty()) "placeholder" else "image",
                            animationSpec = tween(500),
                            label = "pfp_state"
                        ) { state ->
                            when (state) {
                                "loading" -> {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }
                                "placeholder" -> {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            Icons.Rounded.Person, 
                                            null, 
                                            modifier = Modifier.size(80.dp), 
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                                "image" -> {
                                    AsyncImage(
                                        model = user?.profileImage,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        error = androidx.compose.ui.res.painterResource(R.drawable.ic_launcher_foreground)
                                    )
                                }
                            }
                        }
                    }
                    
                    Surface(
                        onClick = { imagePicker.pickFromGallery() },
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd)
                            .offset((-4).dp, (-4).dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(20.dp), tint = Color.White)
                        }
                    }
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = true,
                    elevation = 0.dp // PRD 2.5
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Basic Information", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp), // PRD 2.5
                            leadingIcon = { Icon(Icons.Rounded.Badge, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp), // PRD 2.5
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Rounded.CalendarToday, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                // Verification Cards
                VerificationCard(
                    title = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    isVerified = isEmailVerified,
                    showOtpField = showEmailOtpSection,
                    otpValue = emailOtp,
                    onOtpChange = viewModel::onEmailOtpChange,
                    timerValue = emailTimer,
                    canResend = canResendEmailOtp,
                    isExpired = isEmailOtpExpired,
                    onSendClick = { viewModel.sendEmailOtp(email) },
                    onVerifyClick = { viewModel.verifyEmailOtp(email, emailOtp) },
                    onChangeClick = viewModel::onChangeEmail,
                    icon = Icons.Rounded.Email,
                    isLoading = isLoading
                )

                VerificationCard(
                    title = "Phone Number",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    isVerified = isPhoneVerified,
                    showOtpField = showPhoneOtpSection,
                    otpValue = phoneOtp,
                    onOtpChange = viewModel::onPhoneOtpChange,
                    timerValue = phoneTimer,
                    canResend = canResendPhoneOtp,
                    isExpired = isPhoneOtpExpired,
                    onSendClick = { viewModel.sendPhoneOtp("${selectedCountry.dialCode}$phoneNumber", activity) },
                    onVerifyClick = { viewModel.verifyPhoneOtp(phoneOtp, "${selectedCountry.dialCode}$phoneNumber") }, // Updated: Pass full phone
                    onChangeClick = viewModel::onChangePhone,
                    icon = Icons.Rounded.Phone,
                    isLoading = isLoading,
                    isPhone = true,
                    selectedCountry = selectedCountry,
                    onCountryClick = { viewModel.setShowCountryPicker(true) }
                )

                if (showCountryPicker) {
                    CountryPickerDialog(
                        onDismissRequest = { viewModel.setShowCountryPicker(false) },
                        onCountrySelected = viewModel::onCountrySelected
                    )
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = true,
                    elevation = 0.dp // PRD 2.5
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Personalization", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                        Text("Gender", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                FilterChip(
                                    selected = gender == g,
                                    onClick = { gender = g },
                                    label = { 
                                        Text(
                                            text = g,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.labelSmall
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            label = { Text("Emergency Contact Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp), // PRD 2.5
                            leadingIcon = { Icon(Icons.Rounded.ContactPhone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Save Profile Changes",
                    onClick = {
                        if (isEmailVerified && isPhoneVerified) {
                            viewModel.saveProfile(name, email, phoneNumber, age.toIntOrNull() ?: 0, gender, emergencyContact)
                        }
                    },
                    enabled = !isLoading && isEmailVerified && isPhoneVerified
                )
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
