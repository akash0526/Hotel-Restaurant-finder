package com.placefinder.directory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = PineContainer,
    onPrimaryContainer = PineOnContainer,
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = Mist,
    onSecondaryContainer = Ink,
    tertiary = Pine,
    background = Paper,
    onBackground = Ink,
    surface = Cloud,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Stone,
    outline = Color(0xFFC9C4BA),
    outlineVariant = Color(0xFFE3DED4),
    error = Color(0xFFB42318),
    inverseSurface = Ink,
    inverseOnSurface = Paper,
)

private val DarkColorScheme = darkColorScheme(
    primary = PineDark,
    onPrimary = Color(0xFF06281E),
    primaryContainer = Color(0xFF164536),
    onPrimaryContainer = PineDark,
    secondary = InkDark,
    onSecondary = Night,
    secondaryContainer = NightMist,
    onSecondaryContainer = InkDark,
    tertiary = PineDark,
    background = Night,
    onBackground = InkDark,
    surface = NightSurface,
    onSurface = InkDark,
    surfaceVariant = NightMist,
    onSurfaceVariant = Color(0xFFB3B8B4),
    outline = Color(0xFF4A524C),
    outlineVariant = Color(0xFF2F3632),
    error = Color(0xFFFFB4AB),
    inverseSurface = Paper,
    inverseOnSurface = Ink,
)

private val PlacesShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun LocalPlaceFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = PlacesTypography,
        shapes = PlacesShapes,
        content = content,
    )
}
