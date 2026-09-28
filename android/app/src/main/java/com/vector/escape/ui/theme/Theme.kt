package com.vector.escape.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VectorCyan,
    secondary = VectorViolet,
    tertiary = VectorMint,
    background = VectorBg,
    surface = VectorSurface,
    onPrimary = Color.Black,
    onBackground = VectorTextPrimary,
    onSurface = VectorTextPrimary
)

@Composable
fun VectorEscapeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
