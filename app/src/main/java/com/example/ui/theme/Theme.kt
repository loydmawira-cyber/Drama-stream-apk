package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DramaColorScheme = darkColorScheme(
    primary = PinkAccent,
    onPrimary = DramaBlack,
    primaryContainer = PurpleDark,
    onPrimaryContainer = TextPrimary,
    secondary = PurplePrimary,
    onSecondary = TextPrimary,
    secondaryContainer = DramaSurfaceVariant,
    onSecondaryContainer = TextSecondary,
    tertiary = CoinGold,
    onTertiary = DramaBlack,
    background = DramaBlack,
    onBackground = TextPrimary,
    surface = DramaSurface,
    onSurface = TextPrimary,
    surfaceVariant = DramaSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DividerDark
)

@Composable
fun DramaStreamTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DramaColorScheme,
        typography = Typography,
        content = content
    )
}
