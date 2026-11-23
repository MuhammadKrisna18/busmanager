package com.example.busmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TransNusantaraColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlue,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = IceBlue,
    onSecondaryContainer = PrimaryBlueDark,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = BackgroundWhite,
    onSurfaceVariant = TextMuted,
    outline = CardBorder
)

@Composable
fun TransNusantaraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TransNusantaraColorScheme,
        typography = Typography,
        content = content
    )
}
