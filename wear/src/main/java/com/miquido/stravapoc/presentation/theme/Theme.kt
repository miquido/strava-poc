package com.miquido.stravapoc.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val WearColorPalette = Colors(
    primary = StravaRed,
    primaryVariant = StravaRedDark,
    secondary = StravaRedLight,
    secondaryVariant = StravaRedDark,
    background = Black,
    surface = SurfaceDark,
    error = StravaRedLight,
    onPrimary = OnBlack,
    onSecondary = OnBlack,
    onBackground = OnBlack,
    onSurface = OnBlack,
    onSurfaceVariant = OnBlackSecondary,
    onError = OnBlack,
)

@Composable
fun StravaPocTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorPalette,
        content = content
    )
}
