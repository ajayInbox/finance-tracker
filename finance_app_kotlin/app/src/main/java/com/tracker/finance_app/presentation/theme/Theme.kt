package com.tracker.finance_app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val PrimaryGreen = Color(0xFF13EC5B)
val PrimaryGreenDark = Color(0xFF0EA842)
val SurfaceDark = Color(0xFF16261D)
val CardBackgroundDark = Color(0xFF16261D)
val TextPrimaryDark = Color(0xFFEEEEEE)
val TextSecondaryDark = Color(0xFFAAAAAA)
val BackgroundDark = Color(0xFF0C1811)

object AppColors {
    val Success = Color(0xFF10B981)
    val SuccessLight = Color(0xFFD1FAE5)
    val Error = Color(0xFFEF4444)
    val ErrorLight = Color(0xFFFEE2E2)
    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFEF3C7)
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFFDBEAFE)
    // Account type colors
    val BankBlue = Color(0xFF3B82F6)
    val CardOrange = Color(0xFFF97316)
    val CashEmerald = Color(0xFF10B981)
    val InvestmentPurple = Color(0xFF8B5CF6)
    val LoanRed = Color(0xFFEF4444)
    val WalletCyan = Color(0xFF06B6D4)
}

val Poppins = FontFamily.SansSerif
val Inter = FontFamily.SansSerif
val RobotoMono = FontFamily.Monospace

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D25),
    onPrimaryContainer = Color(0xFF99FFB3),
    secondary = Color(0xFF03DAC6),
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = AppColors.Error
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFF018786),
    background = Color(0xFFF5F5F7),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = AppColors.Error
)

@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
