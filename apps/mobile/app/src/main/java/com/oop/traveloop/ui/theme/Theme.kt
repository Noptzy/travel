package com.oop.traveloop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SenjaTeal,
    onPrimary = Color.White,
    primaryContainer = SenjaTealDeep,
    onPrimaryContainer = Color.White,
    secondary = SenjaSunset,
    onSecondary = Color.White,
    tertiary = SenjaSand,
    onTertiary = SenjaInk,
    background = SenjaCanvas,
    onBackground = SenjaInk,
    surface = SenjaSurfaceCard,
    onSurface = SenjaInk,
    surfaceVariant = SenjaCanvas,
    onSurfaceVariant = SenjaMist,
    error = SenjaError,
    onError = Color.White,
    outline = SenjaMist,
)

@Composable
fun TraveloopTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
