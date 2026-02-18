package com.expensemanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Brand Colors
val Primary = Color(0xFF1A6EDD)
val PrimaryDark = Color(0xFF5B9BF8)
val Secondary = Color(0xFF00BFA5)
val SecondaryDark = Color(0xFF00E5CC)
val TertiaryGreen = Color(0xFF43A047)
val ErrorRed = Color(0xFFE53935)
val WarningAmber = Color(0xFFFF8F00)
val SurfaceLight = Color(0xFFF8F9FA)
val SurfaceDark = Color(0xFF121212)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E1E1E)
val ExpenseColor = Color(0xFFE53935)
val IncomeColor = Color(0xFF43A047)
val TransferColor = Color(0xFF1A6EDD)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001C3D),
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2F0E8),
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = TertiaryGreen,
    error = ErrorRed,
    background = SurfaceLight,
    surface = CardLight,
    surfaceVariant = Color(0xFFEFF1F5),
    onSurface = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF003065),
    primaryContainer = Color(0xFF004391),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = SecondaryDark,
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFFB2F0E8),
    tertiary = Color(0xFF81C784),
    error = Color(0xFFFF6B6B),
    background = SurfaceDark,
    surface = CardDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurface = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8E9099)
)

@Composable
fun ExpenseManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
