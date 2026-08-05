package com.levi.meditime.presentation.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Spacing {
    val tiny = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val card = 16.dp
    val gap = 20.dp
    val section = 24.dp
    val large = 32.dp
    val extraLarge = 40.dp
}

object Radius {
    val small = 16.dp // v3.0
    val medium = 20.dp // v3.0
    val input = 14.dp // v3.0 Add Medication
    val large = 24.dp // v3.0
    val extraLarge = 28.dp // v3.0
    val bottomNav = 32.dp 
    val searchBar = 16.dp // v3.0 Medications Screen
}

object AppTypography {
    val headingXL = 32.sp // v3.0 Screen Title
    val headingL = 28.sp
    val headingM = 24.sp // v3.0 Heading
    val title = 22.sp // v3.0 Card Title - Updated to 22sp
    val subtitle = 16.sp // v3.0
    val body = 14.sp // v3.0
    val caption = 12.sp // v3.0
    val navLabel = 11.sp
}

object Shadow {
    val card = 1.dp // PRD: Minimal shadows
    val button = 2.dp
    val fab = 6.dp // v3.0
}

object Motion {
    const val fast = 150 // v3.0 Card Press
    const val normal = 250 // v3.0 Navigation / FAB
    const val page = 300 // v3.0 Page Transition
}
