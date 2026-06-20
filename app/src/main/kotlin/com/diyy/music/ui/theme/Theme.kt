package com.diyy.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DiyyRed = Color(0xFFF94C57)
val DiyyRedStrong = Color(0xFFFC3C44)
val DiyySoftRed = Color(0xFFFFEEF0)
val DiyyCanvas = Color(0xFFFFFFFF)
val DiyySurface = Color(0xFFF5F5F5)
val DiyySurfaceStrong = Color(0xFFD9D9D9)
val DiyyDivider = Color(0xFFC2CAD7)
val DiyyInk = Color(0xFF1C1B1F)
val DiyyMuted = Color(0xFF6D7077)
val DiyyDarkCanvas = Color(0xFF101014)
val DiyyDarkSurface = Color(0xFF1B1B21)
val DiyyDarkDivider = Color(0xFF34343C)

val DefaultThemeColor: Color = DiyyRed

val ColorSaver = Saver<Color, Int>(
    save = { it.toArgb() },
    restore = { Color(it) },
)

private val LightColors = lightColorScheme(
    primary = DiyyRed,
    onPrimary = Color.White,
    primaryContainer = DiyySoftRed,
    onPrimaryContainer = DiyyInk,
    secondary = DiyyRedStrong,
    onSecondary = Color.White,
    background = DiyyCanvas,
    onBackground = DiyyInk,
    surface = DiyyCanvas,
    onSurface = DiyyInk,
    surfaceVariant = DiyySurface,
    onSurfaceVariant = DiyyMuted,
    outline = DiyyDivider,
    outlineVariant = DiyySurfaceStrong,
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6570),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A171D),
    onPrimaryContainer = Color(0xFFFFDADD),
    secondary = Color(0xFFFF8B93),
    onSecondary = Color.Black,
    background = DiyyDarkCanvas,
    onBackground = Color(0xFFF4F2F4),
    surface = DiyyDarkCanvas,
    onSurface = Color(0xFFF4F2F4),
    surfaceVariant = DiyyDarkSurface,
    onSurfaceVariant = Color(0xFFBDBBC2),
    outline = DiyyDarkDivider,
    outlineVariant = Color(0xFF2A2A31),
    error = Color(0xFFFFB4AB),
)

private val DiyyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        lineHeight = 46.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
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
        fontWeight = FontWeight.Medium,
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
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(18.dp)
    val Player = RoundedCornerShape(0.dp)
}

@Composable
fun DiyyMusicTheme(
    darkTheme: Boolean = false,
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val base: ColorScheme = if (darkTheme) DarkColors else LightColors
    val colors = base.copy(
        primary = themeColor,
        background = if (darkTheme && pureBlack) Color.Black else base.background,
        surface = if (darkTheme && pureBlack) Color.Black else base.surface,
    )
    MaterialTheme(
        colorScheme = colors,
        typography = DiyyTypography,
        content = content,
    )
}

@Composable
@ReadOnlyComposable
fun isDiyyDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

fun android.graphics.Bitmap.extractThemeColor(): Color = DefaultThemeColor
