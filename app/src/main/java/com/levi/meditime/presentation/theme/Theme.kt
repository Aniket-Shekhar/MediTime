package com.levi.meditime.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalAccessibilityMode = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = GessoPrimary,
    onPrimary = GessoOnAccent,
    secondary = GessoSecondary,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = GessoError,
    onError = Color.White,
    outline = OutlineDark,
    primaryContainer = GessoPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = GessoPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = GessoPrimary,
    onPrimary = Color.Black,
    secondary = GessoSecondary,
    onSecondary = Color.Black,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = GessoError,
    onError = Color.White,
    outline = OutlineLight,
    primaryContainer = GessoPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = GessoAccentText
)

@Composable
fun MediTimeMedicineReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilityMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val typography = if (accessibilityMode) getTypography(1.2f) else getTypography(1f)

    CompositionLocalProvider(LocalAccessibilityMode provides accessibilityMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = Shapes,
            content = content
        )
    }
}
