package com.levi.meditime.presentation.reminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levi.meditime.presentation.theme.MediTimeMedicineReminderTheme
import com.levi.meditime.receiver.AlarmReceiver

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: ""
        
        setContent {
            MediTimeMedicineReminderTheme {
                AlarmScreen(
                    medicineName = medicineName,
                    onTake = { 
                        sendBroadcastAction("ACTION_TAKEN", reminderId, medicineName)
                        finish() 
                    },
                    onSnooze = { 
                        sendBroadcastAction("ACTION_SNOOZE", reminderId, medicineName)
                        finish() 
                    },
                    onSkip = { 
                        sendBroadcastAction("ACTION_SKIP", reminderId, medicineName)
                        finish() 
                    }
                )
            }
        }
    }

    private fun sendBroadcastAction(action: String, reminderId: String, medicineName: String) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("REMINDER_ID", reminderId)
            putExtra("MEDICINE_NAME", medicineName)
        }
        sendBroadcast(intent)
    }
}

@Composable
fun AlarmScreen(
    medicineName: String,
    onTake: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
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
                    Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Time for your medicine!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                modifier = Modifier.size(200.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        medicineName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AlarmButton(Icons.Default.Close, "Skip", MaterialTheme.colorScheme.error, onSkip)
                AlarmButton(Icons.Default.Check, "Take", Color(0xFF4ADE80), onTake) // SuccessGreen equivalent
                AlarmButton(Icons.Default.Snooze, "Snooze", Color(0xFFFBBF24), onSnooze) // Warning equivalent
            }
        }
    }
}

@Composable
fun AlarmButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = color,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(icon, label, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
    }
}
