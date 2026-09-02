package com.palash.setu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PalashLightColors = lightColorScheme(
    primary = PalashBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = PalashBackground,
    onBackground = PalashCharcoal,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = PalashCharcoal,
    error = PalashFlame
)

@Composable
fun PalashSetuTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PalashLightColors, typography = PalashTypography, content = content)
}
