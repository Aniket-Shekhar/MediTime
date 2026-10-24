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
    val fs1 = 48 * scale
    val fs2 = 36 * scale
    val fs3 = 28 * scale
    val fs4 = 24 * scale
    val fs5 = 20 * scale
    val fs6 = 16 * scale
    val fs7 = 14 * scale
    val fs8 = 12 * scale

    return Typography(
        displayLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fs1.sp,
            lineHeight = (fs1 * 1.2f).sp,
            letterSpacing = (-0.02f * fs1).sp
        ),
        displayMedium = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fs2.sp,
            lineHeight = (fs2 * 1.2f).sp,
            letterSpacing = (-0.02f * fs2).sp
        ),
        displaySmall = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fs3.sp,
            lineHeight = (fs3 * 1.2f).sp,
            letterSpacing = (-0.02f * fs3).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fs4.sp,
            lineHeight = (fs4 * 1.3f).sp
        ),
        titleLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fs5.sp,
            lineHeight = (fs5 * 1.3f).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Normal,
            fontSize = fs6.sp,
            lineHeight = (fs6 * 1.4f).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Normal,
            fontSize = fs7.sp,
            lineHeight = (fs7 * 1.4f).sp
        ),
        labelLarge = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fs7.sp,
            lineHeight = (fs7 * 1.4f).sp
        ),
        labelSmall = TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Medium,
            fontSize = fs8.sp,
            lineHeight = (fs8 * 1.4f).sp
        )
    )
}

val Typography = getTypography()
