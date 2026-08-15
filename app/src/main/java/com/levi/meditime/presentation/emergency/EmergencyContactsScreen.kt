package com.levi.meditime.presentation.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import com.levi.meditime.R
import com.levi.meditime.domain.model.EmergencyContact
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.presentation.common.components.SensitiveActionOtpDialog
import com.levi.meditime.presentation.theme.*
import com.levi.meditime.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit,
    viewModel: EmergencyContactsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var pendingContactData by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.emergency_contacts),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.contacts.isEmpty()) {
                EmptyState(
                    illustration = { Icon(Icons.Rounded.ContactPhone, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) },
                    title = "No emergency contacts",
                    description = "Add trusted contacts to be notified in case of emergency or missed doses.",
                    actionText = "Add Contact",
                    onActionClick = { showAddSheet = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.contacts) { contact ->
                        EmergencyContactCardRedesign(
                            contact = contact,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                                context.startActivity(intent)
                            },
                            onDelete = { viewModel.deleteContact(contact) }
                        )
                    }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
        ) {
            AnimatedFAB(onClick = { showAddSheet = true })
        }
    }

    if (showAddSheet) {
        AddEmergencyContactBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, relation, phone, notify ->
                pendingContactData = mapOf(
                    "name" to name,
                    "relation" to relation,
                    "phone" to phone,
                    "notify" to notify
                )
                viewModel.startAddContactProcess()
                showAddSheet = false
            }
        )
    }

    if (state.showOtpDialog && pendingContactData != null) {
        val data = pendingContactData ?: emptyMap()
        SensitiveActionOtpDialog(
            type = OtpType.ADD_EMERGENCY,
            onDismiss = viewModel::dismissOtpDialog,
            onVerify = { otp ->
                viewModel.verifyOtpAndAddContact(
                    otp = otp,
                    name = data["name"] as? String ?: "",
                    relation = data["relation"] as? String ?: "",
                    phone = data["phone"] as? String ?: "",
                    notify = data["notify"] as? Boolean ?: false
                ) {
                    pendingContactData = null
                }
            },
            onResend = viewModel::startAddContactProcess,
            isLoading = state.isOtpLoading,
            error = state.otpError
        )
    }
}

@Composable
fun EmergencyContactCardRedesign(contact: EmergencyContact, onCall: () -> Unit, onDelete: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val successColor = if (isDark) SuccessDark else SuccessGreen
    val errorColor = if (isDark) ErrorDark else DangerRed

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("${contact.relationship} • ${contact.phoneNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (contact.notifyOnMissedDose) {
                    Surface(
                        modifier = Modifier.padding(top = 4.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = successColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "Notifies on missed doses",
                            style = MaterialTheme.typography.labelSmall,
                            color = successColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Row {
                IconButton(onClick = onCall) {
                    Icon(Icons.Rounded.Call, null, tint = successColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = errorColor.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmergencyContactBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notify by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            uri?.let {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        if (nameIndex >= 0) name = c.getString(nameIndex) ?: ""
                        
                        if (idIndex >= 0) {
                            val contactId = c.getString(idIndex)
                            val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                            val hasPhone = if (hasPhoneIndex >= 0) c.getString(hasPhoneIndex) == "1" else false
                            
                            if (hasPhone) {
                                val phonesCursor = context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(contactId),
                                    null
                                )
                                phonesCursor?.use { pc ->
                                    if (pc.moveToFirst()) {
                                        val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        if (phoneIndex >= 0) phone = pc.getString(phoneIndex) ?: ""
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_contact), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                TextButton(onClick = { 
                    val status = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                    if (status == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        contactPickerLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                    }
                }) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.from_contacts))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.full_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = relation,
                onValueChange = { relation = it },
                label = { Text(stringResource(R.string.relationship)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = notify,
                    onCheckedChange = { notify = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.notify_missed_doses_checkbox), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = stringResource(R.string.save), onClick = { onSave(name, relation, phone, notify) }, enabled = name.isNotBlank() && phone.isNotBlank())
        }
    }
}
