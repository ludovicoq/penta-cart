package com.pentacart.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green800,
    onPrimary = Paper,
    primaryContainer = Green100,
    onPrimaryContainer = Ink,
    secondary = Orange900,
    onSecondary = Paper,
    secondaryContainer = Orange100,
    onSecondaryContainer = Ink,
    error = Red800,
    onError = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = InkMuted,
    outline = Outline,
)

private val DarkColors = darkColorScheme(
    primary = Green300,
    onPrimary = NightPaper,
    primaryContainer = Green800,
    onPrimaryContainer = NightInk,
    secondary = Orange300,
    onSecondary = NightPaper,
    error = Red300,
    onError = NightPaper,
    background = NightPaper,
    onBackground = NightInk,
    surface = NightPaper,
    onSurface = NightInk,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightInkMuted,
    outline = NightInkMuted,
)

@Composable
fun PentaCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
