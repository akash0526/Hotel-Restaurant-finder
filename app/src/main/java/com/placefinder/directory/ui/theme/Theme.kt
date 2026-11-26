package com.placefinder.directory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFEF7FF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF4F378B),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B1F),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    inverseSurface = Color(0xFF31302E),
    inverseOnSurface = Color(0xFFF5F0E4),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF382E50),
    tertiary = Color(0xFFEFB3C4),
    onTertiary = Color(0xFF4F2638),
    error = Color(0xFFFFB4A9),
    background = Color(0xFF1D1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF1D1B1F),
)

@Composable
fun LocalPlaceFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
