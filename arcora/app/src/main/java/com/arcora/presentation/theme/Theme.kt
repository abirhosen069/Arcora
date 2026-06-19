package com.arcora.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArcOraColorScheme = darkColorScheme(
    primary = ArcoraGreen,
    secondary = ArcoraBlue,
    background = ArcoraBlack,
    surface = ArcoraSurface,
    onPrimary = ArcoraBlack,
    onSecondary = ArcoraBlack,
    onBackground = ArcoraText,
    onSurface = ArcoraText,
    surfaceVariant = ArcoraCard,
    onSurfaceVariant = ArcoraMuted
)

@Composable
fun ArcOraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcOraColorScheme,
        typography = ArcOraTypography,
        content = content
    )
}
