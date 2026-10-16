package com.levi.meditime.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.levi.meditime.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val ManropeFont = GoogleFont("Manrope")

val ManropeFamily = FontFamily(
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

fun getTypography(scale: Float = 1f): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (48 * scale).sp,
            lineHeight = 1.1.sp,
            letterSpacing = (-0.02 * 48).sp
        ),
        displayMedium = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * scale).sp,
            lineHeight = 1.1.sp,
            letterSpacing = (-0.02 * 36).sp
        ),
        displaySmall = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28 * scale).sp,
            lineHeight = 1.1.sp,
            letterSpacing = (-0.02 * 28).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * scale).sp,
            lineHeight = 1.2.sp
        ),
        titleLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            lineHeight = 1.2.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = 1.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = 1.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * scale).sp,
            lineHeight = 1.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = 1.5.sp
        )
    )
}

val Typography = getTypography()
