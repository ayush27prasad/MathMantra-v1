package com.zendalona.mathmantra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.nunito
import org.jetbrains.compose.resources.Font

// Brand: the navy (#012D74) and sky blue (#0096E7) of the original app.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1756C4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E5FF),
    onPrimaryContainer = Color(0xFF012D74),
    secondary = Color(0xFF0079BC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEBFF),
    onSecondaryContainer = Color(0xFF001E30),
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDE9E),
    onTertiaryContainer = Color(0xFF2B1A00),
    background = Color(0xFFF6F8FE),
    onBackground = Color(0xFF181C23),
    surface = Color(0xFFF6F8FE),
    onSurface = Color(0xFF181C23),
    surfaceVariant = Color(0xFFDFE3EE),
    onSurfaceVariant = Color(0xFF434854),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F3FA),
    surfaceContainer = Color(0xFFEAEEF6),
    surfaceContainerHigh = Color(0xFFE4E8F1),
    surfaceContainerHighest = Color(0xFFDEE2EC),
    outline = Color(0xFF737885),
    outlineVariant = Color(0xFFC3C7D3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFAEC6FF),
    onPrimary = Color(0xFF002E6B),
    primaryContainer = Color(0xFF0C4199),
    onPrimaryContainer = Color(0xFFD9E5FF),
    secondary = Color(0xFF8DCDFF),
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF004C71),
    onSecondaryContainer = Color(0xFFCDEBFF),
    tertiary = Color(0xFFFFBA3B),
    onTertiary = Color(0xFF482F00),
    tertiaryContainer = Color(0xFF684500),
    onTertiaryContainer = Color(0xFFFFDE9E),
    background = Color(0xFF10131A),
    onBackground = Color(0xFFE0E2EB),
    surface = Color(0xFF10131A),
    onSurface = Color(0xFFE0E2EB),
    surfaceVariant = Color(0xFF434854),
    onSurfaceVariant = Color(0xFFC3C7D3),
    surfaceContainerLowest = Color(0xFF0B0E14),
    surfaceContainerLow = Color(0xFF181C23),
    surfaceContainer = Color(0xFF1C2027),
    surfaceContainerHigh = Color(0xFF262A32),
    surfaceContainerHighest = Color(0xFF31353D),
    outline = Color(0xFF8D919E),
    outlineVariant = Color(0xFF434854),
)

/** Colours outside the Material scheme: right/wrong feedback and one accent per activity. */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val accents: List<Color>,
)

private val LightExtended = ExtendedColors(
    success = Color(0xFF1B7F3B),
    onSuccess = Color.White,
    successContainer = Color(0xFFC6F0D1),
    accents = listOf(
        Color(0xFFFFE7A6), // amber
        Color(0xFFFFD9CF), // coral
        Color(0xFFCBF0DB), // mint
        Color(0xFFE4DCFF), // lavender
        Color(0xFFCDEBFF), // sky
        Color(0xFFFFDAEC), // pink
        Color(0xFFE5F2B8), // lime
    ),
)

private val DarkExtended = ExtendedColors(
    success = Color(0xFF7DDA97),
    onSuccess = Color(0xFF00391A),
    successContainer = Color(0xFF0F5228),
    accents = listOf(
        Color(0xFF473800),
        Color(0xFF552A1F),
        Color(0xFF173D2A),
        Color(0xFF322859),
        Color(0xFF0B354F),
        Color(0xFF4B1D35),
        Color(0xFF323F10),
    ),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
private fun appTypography(): Typography {
    val nunito = FontFamily(
        Font(Res.font.nunito, FontWeight.Normal),
        Font(Res.font.nunito, FontWeight.Medium),
        Font(Res.font.nunito, FontWeight.SemiBold),
        Font(Res.font.nunito, FontWeight.Bold),
        Font(Res.font.nunito, FontWeight.ExtraBold),
    )
    fun TextStyle.nunito(weight: FontWeight) = copy(fontFamily = nunito, fontWeight = weight)

    return with(Typography()) {
        Typography(
            displayLarge = displayLarge.nunito(FontWeight.ExtraBold),
            displayMedium = displayMedium.nunito(FontWeight.ExtraBold),
            displaySmall = displaySmall.nunito(FontWeight.ExtraBold),
            headlineLarge = headlineLarge.nunito(FontWeight.ExtraBold),
            headlineMedium = headlineMedium.nunito(FontWeight.Bold),
            headlineSmall = headlineSmall.nunito(FontWeight.Bold),
            titleLarge = titleLarge.nunito(FontWeight.Bold),
            titleMedium = titleMedium.nunito(FontWeight.Bold),
            titleSmall = titleSmall.nunito(FontWeight.Bold),
            bodyLarge = bodyLarge.nunito(FontWeight.Medium),
            bodyMedium = bodyMedium.nunito(FontWeight.Medium),
            bodySmall = bodySmall.nunito(FontWeight.Medium),
            labelLarge = labelLarge.nunito(FontWeight.Bold),
            labelMedium = labelMedium.nunito(FontWeight.Bold),
            labelSmall = labelSmall.nunito(FontWeight.Bold),
        )
    }
}

@Composable
fun MathMantraTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(colorScheme = colors, typography = appTypography(), shapes = AppShapes, content = content)
    }
}

object AppTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
