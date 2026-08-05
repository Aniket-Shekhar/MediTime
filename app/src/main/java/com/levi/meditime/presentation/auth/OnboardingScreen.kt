package com.levi.meditime.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.levi.meditime.R
import com.levi.meditime.datastore.ThemePreferences
import com.levi.meditime.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    // Reset state on each visit to ensure button is never stuck "disabled"
    var navigationInProgress by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Hero Image Placeholder (Dark tinted dashboard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BackgroundDark),
                        startY = 0f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // Safely within interactive safe zone
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "MediTime",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Effortlessly Manage\nYour Medications and\nHealth Routines",
                style = MaterialTheme.typography.displaySmall.copy(
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "The ultimate medicine reminder app designed to streamline your health routines and ensure you never miss a dose.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp
                ),
                color = Color.White.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            PrimaryButton(
                text = "Get Started",
                onClick = {
                    if (!navigationInProgress) {
                        navigationInProgress = true
                        onGetStarted()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = PrimarySage,
                loading = navigationInProgress,
                enabled = !navigationInProgress
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
