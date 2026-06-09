package com.miquido.stravapoc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = StravaRed,
    onPrimary = OnBlack,
    primaryContainer = StravaRedDark,
    onPrimaryContainer = OnBlack,
    secondary = StravaRedLight,
    onSecondary = OnBlack,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnBlack,
    tertiary = StravaRedLight,
    onTertiary = OnBlack,
    background = Black,
    onBackground = OnBlack,
    surface = SurfaceDark,
    onSurface = OnBlack,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnBlackSecondary,
    outline = Color(0xFF3A3A3A),
    error = StravaRedLight,
    onError = OnBlack,
)

@Composable
fun StravaPocTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
