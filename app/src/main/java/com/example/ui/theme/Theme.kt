package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.dubstage.model.AppThemeMode
import com.example.dubstage.ui.theme.DubStageDarkPalette
import com.example.dubstage.ui.theme.DubStageLightPalette
import com.example.dubstage.ui.theme.LocalDubStageColors

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> darkTheme
    }

    val colors = if (isDark) DubStageDarkPalette else DubStageLightPalette

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = colors.acc,
            onPrimary = colors.accDarkest,
            primaryContainer = colors.accDark,
            onPrimaryContainer = colors.accHi,
            secondary = colors.teal,
            onSecondary = Color(0xFF003822),
            secondaryContainer = colors.panelHi,
            onSecondaryContainer = colors.tealHi,
            tertiary = colors.gold,
            onTertiary = Color(0xFF3F2E00),
            background = colors.bgBot,
            onBackground = colors.txt,
            surface = colors.panel,
            onSurface = colors.txt,
            surfaceVariant = colors.panelHi,
            onSurfaceVariant = colors.dim,
            outline = colors.edge,
            outlineVariant = colors.edgeHi,
            error = colors.red,
            onError = Color(0xFF690005)
        )
    } else {
        lightColorScheme(
            primary = colors.acc,
            onPrimary = Color.White,
            primaryContainer = colors.accDark,
            onPrimaryContainer = colors.accHi,
            secondary = colors.teal,
            onSecondary = Color.White,
            secondaryContainer = colors.panelHi,
            onSecondaryContainer = colors.tealHi,
            tertiary = colors.gold,
            onTertiary = Color.White,
            background = colors.bgBot,
            onBackground = colors.txt,
            surface = colors.panel,
            onSurface = colors.txt,
            surfaceVariant = colors.panelHi,
            onSurfaceVariant = colors.dim,
            outline = colors.edge,
            outlineVariant = colors.edgeHi,
            error = colors.red,
            onError = Color.White
        )
    }

    CompositionLocalProvider(
        LocalDubStageColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
