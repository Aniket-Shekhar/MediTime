package com.levi.meditime.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.presentation.theme.*

@Composable
fun SimpleDashboardScreen(
    onNavigateToSOS: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = "Hello, ${state.userName}",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(32.dp))

        // Giant SOS Button
        Button(
            onClick = onNavigateToSOS,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Sos, null, modifier = Modifier.size(48.dp), tint = Color.White)
                Spacer(Modifier.width(16.dp))
                Text("EMERGENCY HELP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Giant Next Medicine Card
        if (state.medicines.isNotEmpty()) {
            val med = state.medicines.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("NEXT MEDICINE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Dose: ${med.dosage}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = { viewModel.takeMedicine(med) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("I TOOK IT", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        } else {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No more medicines for today!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(100.dp)) // Floating Bottom Nav space
    }
}
