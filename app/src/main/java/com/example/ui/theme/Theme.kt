package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonCyan,
    tertiary = NeonGold,
    background = CyberObsidian,
    surface = CyberDarkSlate,
    onPrimary = TextDarkGreen,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextSlateWhite,
    onSurface = TextSlateWhite,
    error = CyberCrimson,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    secondary = NeonCyan,
    tertiary = NeonGold,
    background = CyberObsidian,
    surface = CyberDarkSlate,
    onPrimary = TextDarkGreen,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextSlateWhite,
    onSurface = TextSlateWhite,
    error = CyberCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We force cyber dark theme by default as the app's intentional vibe
    dynamicColor: Boolean = false, // We use our premium cyber theme palette instead of matching device dynamic theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
