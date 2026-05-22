package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Dynamic Customizable Color Schemes Mapping ---

fun getSscColorScheme(preset: String, isDark: Boolean): ColorScheme {
    return when (preset) {
        "PROFESSIONAL_POLISH" -> {
            if (isDark) {
                darkColorScheme(
                    primary = ProfessionalPolishPrimary,
                    primaryContainer = ProfessionalPolishPrimaryContainer,
                    secondary = ProfessionalPolishSecondary,
                    background = ProfessionalPolishBackgroundDark,
                    surface = ProfessionalPolishSurfaceDark,
                    onPrimary = ProfessionalPolishOnPrimary,
                    onSecondary = Color.White,
                    onBackground = ProfessionalPolishOnBackgroundDark,
                    onSurface = ProfessionalPolishOnSurfaceDark,
                    surfaceVariant = Color(0xFF333537),
                    onSurfaceVariant = Color(0xFFC4C6CF)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF6750A4),
                    primaryContainer = Color(0xFFEADDFF),
                    secondary = Color(0xFF625B71),
                    background = ProfessionalPolishBackgroundLight,
                    surface = ProfessionalPolishSurfaceLight,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFF1D1B20),
                    onSurface = Color(0xFF1D1B20),
                    surfaceVariant = Color(0xFFE7E0EC),
                    onSurfaceVariant = Color(0xFF49454F)
                )
            }
        }
        "COSMIC_MIDNIGHT" -> {
            if (isDark) {
                darkColorScheme(
                    primary = CosmicMidnightPrimary,
                    primaryContainer = CosmicMidnightPrimaryContainer,
                    secondary = CosmicMidnightSecondary,
                    background = CosmicMidnightBackgroundDark,
                    surface = CosmicMidnightSurfaceDark,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFFF3E8FF),
                    onSurface = Color(0xFFF3E8FF)
                )
            } else {
                lightColorScheme(
                    primary = CosmicMidnightPrimary,
                    primaryContainer = CosmicMidnightBackgroundLight,
                    secondary = CosmicMidnightSecondary,
                    background = CosmicMidnightBackgroundLight,
                    surface = CosmicMidnightSurfaceLight,
                    onPrimary = Color.White,
                    onSecondary = Color.DarkGray,
                    onBackground = Color(0xFF3B0764),
                    onSurface = Color(0xFF3B0764)
                )
            }
        }
        "WARM_SEPIA" -> {
            if (isDark) {
                darkColorScheme(
                    primary = WarmSepiaPrimary,
                    primaryContainer = WarmSepiaPrimaryContainer,
                    secondary = WarmSepiaSecondary,
                    background = WarmSepiaBackgroundDark,
                    surface = WarmSepiaSurfaceDark,
                    onPrimary = Color.Black,
                    onSecondary = Color.White,
                    onBackground = Color(0xFFFDFBF7),
                    onSurface = Color(0xFFFDFBF7)
                )
            } else {
                lightColorScheme(
                    primary = WarmSepiaPrimary,
                    primaryContainer = WarmSepiaPrimaryContainer,
                    secondary = WarmSepiaSecondary,
                    background = WarmSepiaBackgroundLight,
                    surface = WarmSepiaSurfaceLight,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFF451A03),
                    onSurface = Color(0xFF451A03)
                )
            }
        }
        "FOREST_ZEN" -> {
            if (isDark) {
                darkColorScheme(
                    primary = ForestZenPrimary,
                    primaryContainer = ForestZenPrimaryContainer,
                    secondary = ForestZenSecondary,
                    background = ForestZenBackgroundDark,
                    surface = ForestZenSurfaceDark,
                    onPrimary = Color.Black,
                    onSecondary = Color.White,
                    onBackground = Color(0xFFECFDF5),
                    onSurface = Color(0xFFECFDF5)
                )
            } else {
                lightColorScheme(
                    primary = ForestZenPrimary,
                    primaryContainer = ForestZenPrimaryContainer,
                    secondary = ForestZenSecondary,
                    background = ForestZenBackgroundLight,
                    surface = ForestZenSurfaceLight,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFF064E3B),
                    onSurface = Color(0xFF064E3B)
                )
            }
        }
        else -> { // "CLASSIC_BLUE"
            if (isDark) {
                darkColorScheme(
                    primary = ClassicBluePrimary,
                    primaryContainer = ClassicBluePrimaryContainer,
                    secondary = ClassicBlueSecondary,
                    background = ClassicBlueBackgroundDark,
                    surface = ClassicBlueSurfaceDark,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFFF1F5F9),
                    onSurface = Color(0xFFF1F5F9)
                )
            } else {
                lightColorScheme(
                    primary = ClassicBluePrimary,
                    primaryContainer = ClassicBluePrimaryContainer,
                    secondary = ClassicBlueSecondary,
                    background = ClassicBlueBackgroundLight,
                    surface = ClassicBlueSurfaceLight,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFF0F172A),
                    onSurface = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun MyApplicationTheme(
    themePreset: String = "PROFESSIONAL_POLISH",
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = getSscColorScheme(themePreset, isDarkMode)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
