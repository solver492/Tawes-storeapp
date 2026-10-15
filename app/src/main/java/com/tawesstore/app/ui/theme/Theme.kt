package com.tawesstore.app.ui.theme

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
    primary = TawesGreen,
    onPrimary = TawesSurfaceWhite,
    primaryContainer = TawesMint,
    onPrimaryContainer = TawesGreenDark,
    secondary = TawesOrange,
    onSecondary = TawesSurfaceWhite,
    secondaryContainer = TawesWholesaleBg,
    onSecondaryContainer = TawesWholesaleText,
    background = TawesCream,
    onBackground = TawesInk,
    surface = TawesSurfaceWhite,
    onSurface = TawesInk,
    surfaceVariant = TawesMint,
    onSurfaceVariant = TawesMuted,
    outline = TawesLine
)

private val DarkColorScheme = darkColorScheme(
    primary = TawesMint,
    onPrimary = TawesGreenDark,
    primaryContainer = TawesGreen,
    onPrimaryContainer = TawesCream,
    secondary = TawesOrangeLight,
    onSecondary = TawesInk,
    background = TawesInk,
    onBackground = TawesCream,
    surface = Color(0xFF1E2C27),
    onSurface = TawesCream
)

@Composable
fun TawesStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TawesGreen.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
