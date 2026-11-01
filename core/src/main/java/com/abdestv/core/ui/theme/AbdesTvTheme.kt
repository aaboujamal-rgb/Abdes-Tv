package com.abdestv.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AbdesColors {
    val DeepBlack = Color(0xFF060609)
    val ObsidianSurface = Color(0xFF0E0E15)
    val CardDark = Color(0xFF161622)
    val CardElevated = Color(0xFF1F1F30)
    val PurplePrimary = Color(0xFF8B5CF6)
    val PurpleVibrant = Color(0xFFA855F7)
    val PurpleDeep = Color(0xFF5B21B6)
    val PurpleGlow = Color(0xFFC084FC)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
    val LiveRed = Color(0xFFEF4444)
    val SuccessGreen = Color(0xFF10B981)
    val WarningAmber = Color(0xFFF59E0B)

    val HeroGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x00060609),
            Color(0xB3060609),
            Color(0xFF060609)
        )
    )

    val PurpleBannerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4C1D95),
            Color(0xFF1E1B4B),
            Color(0xFF09090F)
        )
    )
}

private val AbdesDarkColorScheme = darkColorScheme(
    primary = AbdesColors.PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = AbdesColors.PurpleDeep,
    onPrimaryContainer = Color.White,
    secondary = AbdesColors.PurpleVibrant,
    onSecondary = Color.White,
    tertiary = AbdesColors.PurpleGlow,
    background = AbdesColors.DeepBlack,
    onBackground = AbdesColors.TextPrimary,
    surface = AbdesColors.ObsidianSurface,
    onSurface = AbdesColors.TextPrimary,
    surfaceVariant = AbdesColors.CardDark,
    onSurfaceVariant = AbdesColors.TextSecondary,
    error = AbdesColors.LiveRed,
    onError = Color.White
)

private val AbdesTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun AbdesTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AbdesDarkColorScheme,
        typography = AbdesTypography,
        content = content
    )
}
