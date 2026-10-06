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
    primary = KuwaitEmeraldDark,
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF134237),
    onPrimaryContainer = Color(0xFF98F7DD),
    secondary = KuwaitGoldDark,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = Color(0xFF5E4000),
    onSecondaryContainer = Color(0xFFFFDEA1),
    tertiary = KuwaitAmberWarm,
    background = KuwaitBgDark,
    surface = KuwaitSurfaceDark,
    surfaceVariant = KuwaitSurfaceVariantDark,
    onBackground = KuwaitTextPrimaryDark,
    onSurface = KuwaitTextPrimaryDark,
    onSurfaceVariant = KuwaitTextSecondaryDark,
    outline = KuwaitCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = KuwaitEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7EFE4),
    onPrimaryContainer = Color(0xFF00382B),
    secondary = KuwaitGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1D6),
    onSecondaryContainer = Color(0xFF4C3300),
    tertiary = KuwaitAmberWarm,
    background = KuwaitBgLight,
    surface = KuwaitSurfaceLight,
    surfaceVariant = KuwaitSurfaceVariantLight,
    onBackground = KuwaitTextPrimaryLight,
    onSurface = KuwaitTextPrimaryLight,
    onSurfaceVariant = KuwaitTextSecondaryLight,
    outline = KuwaitCardBorderLight
)

@Composable
fun KuwaitSalatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
