package com.levi.meditime.presentation.caretaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import androidx.compose.ui.platform.LocalContext
import com.levi.meditime.R
import com.levi.meditime.domain.model.Caretaker
import com.levi.meditime.domain.model.OtpType
import com.levi.meditime.presentation.common.components.SensitiveActionOtpDialog
import com.levi.meditime.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerListScreen(
    onBack: () -> Unit,
    viewModel: CaretakerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var pendingCaretakerData by remember { mutableStateOf<Map<String, String>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                CaretakerHeader(onBack = onBack)
            }

            if (state.caretakers.isEmpty()) {
                item {
                    CaretakerEmptyState()
                }
            } else {
                items(state.caretakers) { caretaker ->
                    PremiumCaretakerCard(
                        caretaker = caretaker,
                        onDelete = { viewModel.deleteCaretaker(caretaker) }
                    )
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            PremiumFloatingActionButton(
                icon = Icons.Default.PersonAdd,
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showAddSheet) {
        AddCaretakerBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, relation, email, phone ->
                pendingCaretakerData = mapOf(
                    "name" to name,
                    "relation" to relation,
                    "email" to email,
                    "phone" to phone
                )
                viewModel.startLinkProcess()
                showAddSheet = false
            }
        )
    }

    if (state.showOtpDialog && pendingCaretakerData != null) {
        val data = pendingCaretakerData ?: emptyMap()
        SensitiveActionOtpDialog(
            type = OtpType.LINK_CARETAKER,
            onDismiss = viewModel::dismissOtpDialog,
            onVerify = { otp ->
                viewModel.verifyOtpAndAddCaretaker(
                    otp = otp,
                    name = data["name"] ?: "",
                    relation = data["relation"] ?: "",
                    email = data["email"] ?: "",
                    phone = data["phone"] ?: ""
                ) {
                    pendingCaretakerData = null
                }
            },
            onResend = viewModel::startLinkProcess,
            isLoading = state.isOtpLoading,
            error = state.otpError
        )
    }
}

@Composable
fun CaretakerHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 24.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.my_caretakers),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PremiumCaretakerCard(caretaker: Caretaker, onDelete: () -> Unit) {
    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(caretaker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${caretaker.relation} • ${caretaker.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun CaretakerEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Group,
            null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_caretakers_yet),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.add_caretaker_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaretakerBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_caretaker), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
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

            CaretakerInput(value = name, onValueChange = { name = it }, label = stringResource(R.string.full_name), icon = Icons.Default.Badge)
            Spacer(modifier = Modifier.height(16.dp))
            CaretakerInput(value = relation, onValueChange = { relation = it }, label = stringResource(R.string.relationship), icon = Icons.Default.FamilyRestroom)
            Spacer(modifier = Modifier.height(16.dp))
            CaretakerInput(value = email, onValueChange = { email = it }, label = stringResource(R.string.email_address), icon = Icons.Default.Email)
            Spacer(modifier = Modifier.height(16.dp))
            CaretakerInput(value = phone, onValueChange = { phone = it }, label = stringResource(R.string.phone_number), icon = Icons.Default.Phone)

            Spacer(modifier = Modifier.height(32.dp))
            PremiumButton(
                text = stringResource(R.string.add_caretaker),
                onClick = { onSave(name, relation, email, phone) },
                enabled = name.isNotBlank() && phone.isNotBlank()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CaretakerInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
