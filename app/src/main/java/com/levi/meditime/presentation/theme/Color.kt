package com.levi.meditime.presentation.theme

import androidx.compose.ui.graphics.Color

// Amber Vigil — Style Reference Tokens
val GessoCanvas = Color(0xFFF5FCFF)
val GessoSurfaceRecessed = Color(0xFFEBF2F5)
val GessoSurface = Color(0xFFEBEBEB)
val GessoSurfaceElevated = Color(0xFFE2E2E2)
val GessoDivider = Color(0x0A000000) // rgba(0,0,0,0.04)
val GessoFg = Color(0xFF1B1B1F)
val GessoFgMuted = Color(0xFF616369)
val GessoPrimary = Color(0xFF2DD4BF)
val GessoOnAccent = Color(0xFF000000)
val GessoAccentText = Color(0xFF1B7F73)
val GessoSecondary = Color(0xFFF5A623)
val GessoAccent2Text = Color(0xFF936415)

// Neutral Ramp
val GessoNeutral50 = Color(0xFFF5FCFF)
val GessoNeutral100 = Color(0xFFEBEBEB)
val GessoNeutral200 = Color(0xFFCFCECD)
val GessoNeutral300 = Color(0xFFB5B1B1)
val GessoNeutral400 = Color(0xFF9A9698)
val GessoNeutral500 = Color(0xFF7E7B80)
val GessoNeutral600 = Color(0xFF616369)
val GessoNeutral700 = Color(0xFF48494F)
val GessoNeutral800 = Color(0xFF313136)
val GessoNeutral900 = Color(0xFF1B1B1F)
val GessoNeutral950 = Color(0xFF18181B)

// Semantic
val GessoSuccess = Color(0xFF149343)
val GessoWarning = Color(0xFFB86505)
val GessoError = Color(0xFFDC2626)

// Data Viz
val GessoData1 = Color(0xFF754B00)
val GessoData2 = Color(0xFF936000)
val GessoData3 = Color(0xFFB27500)
val GessoData4 = Color(0xFFD18A00)
val GessoData5 = Color(0xFFF0A21A)
val GessoData6 = Color(0xFFFFC16D)

// Sticker Shadows
val GessoStickerShadow = Color(0x241B1B1F) // 5px 5px 0 rgba(27,27,31,0.14)
val GessoStickerShadowSm = Color(0x1F1B1B1F) // 3px 3px 0 rgba(27,27,31,0.12)

// MediTime v3.1 Production Healthcare Palette (Legacy Support)
val PrimaryTeal = GessoPrimary
val PrimaryTealDark = GessoAccentText
val PrimaryTealLight = Color(0xFFCCFBF1)

// Premium Sage Palette for Auth/Onboarding
val PrimarySage = Color(0xFF759B8E)
val PrimarySageDark = Color(0xFF5E7C72)
val PrimarySageLight = Color(0xFFD1E0DB)

// Light Mode Palette
val BackgroundLight = GessoCanvas
val SurfaceLight = Color.White
val SurfaceVariantLight = GessoSurfaceRecessed
val OnSurfaceLight = GessoFg
val OnSurfaceVariantLight = GessoFgMuted
val OutlineLight = GessoNeutral200

// Dark Mode Palette (Stay same for now or update if needed)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceVariantDark = Color(0xFF334155)
val OnSurfaceDark = Color(0xFFF8FAFC)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)
val OutlineDark = Color(0xFF475569)

// Status Colors - Dark (Better Contrast)
val SuccessDark = Color(0xFF4ADE80)
val WarningDark = Color(0xFFFBBF24)
val ErrorDark = Color(0xFFF87171)
val InfoDark = Color(0xFF60A5FA)

// Chart Colors
val ChartLine = Color(0xFF2DD4BF)
val ChartGrid = Color(0xFF475569)
val ChartLabel = Color(0xFFCBD5E1)

// Legacy / Support Colors
val SuccessGreen = Color(0xFF22C55E)
val WarningOrange = Color(0xFFF59E0B)
val DangerRed = GessoError
val InfoBlue = Color(0xFF3B82F6)

val BentoCardGradientStart = Color(0xFFEAF3FF)
val BentoCardGradientEnd = Color(0xFFBFD4E9)

// Grey shades for premium UI
val DividerGrey = Color(0xFFE2E8F0)
val InactiveGrey = Color(0xFF94A3B8)
val SocialButtonBorder = Color(0xFFCBD5E1)

val Primary = PrimaryTeal
val Secondary = GessoSecondary
val Success = SuccessGreen
val Warning = WarningOrange
val Danger = DangerRed
