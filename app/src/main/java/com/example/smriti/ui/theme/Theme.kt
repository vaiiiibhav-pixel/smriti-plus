package com.example.smriti.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SmritiPrimary,
    onPrimary = Color.White,
    primaryContainer = SmritiPrimaryContainer,
    onPrimaryContainer = SmritiOnPrimaryContainer,
    secondary = SmritiSecondary,
    onSecondary = Color.White,
    secondaryContainer = SmritiSecondaryLight,
    onSecondaryContainer = SmritiOnSecondaryContainer,
    tertiary = SmritiAccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = SmritiAccentAmberLight,
    onTertiaryContainer = SmritiOnAmber,
    background = SmritiCanvasBg,
    onBackground = SmritiTextPrimary,
    surface = SmritiCardSurface,
    onSurface = SmritiTextPrimary,
    surfaceVariant = SmritiSurfaceVariant,
    onSurfaceVariant = SmritiTextSecondary,
    outline = SmritiBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF021226),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF64748B)
)

@Composable
fun SmritiTheme(
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
        typography = Typography,
        content = content
    )
}
