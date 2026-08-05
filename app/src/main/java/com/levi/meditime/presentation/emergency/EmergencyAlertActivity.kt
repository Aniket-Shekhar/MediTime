package com.levi.meditime.presentation.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.levi.meditime.presentation.theme.MediTimeMedicineReminderTheme

class EmergencyAlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contactName = intent.getStringExtra("CONTACT_NAME") ?: "Emergency Contact"
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"

        setContent {
            MediTimeMedicineReminderTheme {
                EmergencyAlertScreen(
                    contactName = contactName,
                    medicineName = medicineName,
                    onCall = { makeCall(phoneNumber) },
                    onDismiss = { finish() }
                )
            }
        }
    }

    private fun makeCall(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        val intent = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun EmergencyAlertScreen(
    contactName: String,
    medicineName: String,
    onCall: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "MEDICINE MISSED!",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                "$medicineName was not taken after multiple reminders.",
                color = MaterialTheme.colorScheme.onError,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Would you like to contact your emergency person?",
                        color = MaterialTheme.colorScheme.onError,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(contactName, color = MaterialTheme.colorScheme.onError, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onCall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onError, contentColor = MaterialTheme.colorScheme.error),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("CALL NOW", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Dismiss", color = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f))
                }
            }
        }
    }
}
