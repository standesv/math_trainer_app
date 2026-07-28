package com.standesv.mathtrainer.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Palette volontairement vive et contrastee : lisible par un enfant,
// y compris sur un ecran de telephone en plein jour.
val BlueMain = Color(0xFF4C6FFF)
val BlueDark = Color(0xFF2F4FD8)
val GreenOk = Color(0xFF2FBF71)
val RedErr = Color(0xFFEF4C58)
val Amber = Color(0xFFFFB020)
val Purple = Color(0xFF8B5CF6)
val SurfaceSoft = Color(0xFFF3F6FF)
val TextDark = Color(0xFF1B2559)

private val colors = lightColorScheme(
    primary = BlueMain,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE4FF),
    onPrimaryContainer = BlueDark,
    secondary = Purple,
    onSecondary = Color.White,
    tertiary = Amber,
    onTertiary = Color.White,
    error = RedErr,
    onError = Color.White,
    background = SurfaceSoft,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE8EDFF),
    onSurfaceVariant = Color(0xFF44508A)
)

private val shapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

private val typography = Typography(
    displayLarge = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.ExtraBold),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 18.sp),
    bodyMedium = TextStyle(fontSize = 16.sp),
    labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun MathsTrainerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        shapes = shapes,
        typography = typography,
        content = content
    )
}
