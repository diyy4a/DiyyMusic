package com.diyy.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DiyyRed = Color(0xFFFF2768)
val DiyyRedStrong = Color(0xFFFF0D5E)
val DiyyPinkLight = Color(0xFFFFD9E5)
val DiyySoftRed = Color(0xFFFFEEF4)
val DiyyCanvas = Color(0xFFFCFBFD)
val DiyySurface = Color(0xFFF4F2F6)
val DiyySurfaceStrong = Color(0xFFE9E5EC)
val DiyyDivider = Color(0xFFD9D3DD)
val DiyyInk = Color(0xFF19171C)
val DiyyMuted = Color(0xFF77717D)
val DiyyGlass = Color(0xD9FFFFFF)
val DiyyGlassDark = Color(0xCC24232A)
val DiyyDarkCanvas = Color(0xFF0F0E12)
val DiyyDarkSurface = Color(0xFF1A181E)
val DiyyDarkDivider = Color(0xFF37333D)

val DefaultThemeColor: Color = DiyyRed

val ColorSaver = Saver<Color, Int>(
    save = { it.toArgb() },
    restore = { Color(it) },
)

enum class DiyyMotionPreset {
    GENTLE,
    SMOOTH,
    SNAPPY,
}

@Immutable
data class DiyyUiConfig(
    val accentStrength: Float = 0.74f,
    val roundedArtwork: Boolean = true,
    val motionPreset: DiyyMotionPreset = DiyyMotionPreset.SMOOTH,
    val reduceMotion: Boolean = false,
    val glassIntensity: Float = 0.60f,
    val glassSoftness: Float = 0.45f,
    val backgroundGlow: Boolean = true,
)

val LocalDiyyUiConfig = staticCompositionLocalOf { DiyyUiConfig() }

private val LightColors = lightColorScheme(
    primary = DiyyRed,
    onPrimary = Color.White,
    primaryContainer = DiyySoftRed,
    onPrimaryContainer = DiyyInk,
    secondary = DiyyRedStrong,
    onSecondary = Color.White,
    background = DiyyCanvas,
    onBackground = DiyyInk,
    surface = Color.White,
    onSurface = DiyyInk,
    surfaceVariant = DiyySurface,
    onSurfaceVariant = DiyyMuted,
    outline = DiyyDivider,
    outlineVariant = DiyySurfaceStrong,
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF5B8E),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4B1629),
    onPrimaryContainer = Color(0xFFFFD9E5),
    secondary = Color(0xFFFF8EB1),
    onSecondary = Color.Black,
    background = DiyyDarkCanvas,
    onBackground = Color(0xFFF8F3F7),
    surface = DiyyDarkCanvas,
    onSurface = Color(0xFFF8F3F7),
    surfaceVariant = DiyyDarkSurface,
    onSurfaceVariant = Color(0xFFC8C0CA),
    outline = DiyyDarkDivider,
    outlineVariant = Color(0xFF2A2730),
    error = Color(0xFFFFB4AB),
)

private val DiyyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.45).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.25).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.15).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

object DiyyShapes {
    val Small = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(18.dp)
    val Large = RoundedCornerShape(28.dp)
    val Player = RoundedCornerShape(0.dp)
}

@Composable
fun DiyyMusicTheme(
    darkTheme: Boolean = false,
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    uiConfig: DiyyUiConfig = DiyyUiConfig(),
    content: @Composable () -> Unit,
) {
    val base: ColorScheme = if (darkTheme) DarkColors else LightColors
    val colors = base.copy(
        primary = themeColor,
        background = if (darkTheme && pureBlack) Color.Black else base.background,
        surface = if (darkTheme && pureBlack) Color.Black else base.surface,
    )
    CompositionLocalProvider(LocalDiyyUiConfig provides uiConfig) {
        MaterialTheme(
            colorScheme = colors,
            typography = DiyyTypography,
            content = content,
        )
    }
}

@Composable
@ReadOnlyComposable
fun isDiyyDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

fun android.graphics.Bitmap.extractThemeColor(): Color = DefaultThemeColor
