@file:OptIn(ExperimentalTextApi::class)

package es.costalceleste.senderismo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta: pino, girasol y cielo sobre una pradera clara.
val Pino = Color(0xFF2F6B3A)
val PinoOscuro = Color(0xFF173A25)
val Girasol = Color(0xFFF5B700)
val Cielo = Color(0xFF2E8BC0)
val Pradera = Color(0xFFF3F9EA)

private fun fuente(res: Int, peso: Int) = Font(
    res,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso)),
)

/** Fredoka: redondeada y amable, para títulos. */
val Fredoka = FontFamily(
    fuente(R.font.fredoka, 500),
    fuente(R.font.fredoka, 600),
    fuente(R.font.fredoka, 700),
)

/** Nunito: cálida y muy legible, para el texto. */
val Nunito = FontFamily(
    fuente(R.font.nunito, 400),
    fuente(R.font.nunito, 600),
    fuente(R.font.nunito, 700),
    fuente(R.font.nunito, 800),
    Font(
        R.font.nunito_italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
)

private val Colores = lightColorScheme(
    primary = Pino,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EED2),
    onPrimaryContainer = PinoOscuro,
    secondary = Color(0xFFB98600),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0C2),
    onSecondaryContainer = Color(0xFF4A3500),
    tertiary = Cielo,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6EEFB),
    onTertiaryContainer = Color(0xFF0B3A57),
    background = Pradera,
    onBackground = PinoOscuro,
    surface = Color.White,
    onSurface = PinoOscuro,
    surfaceVariant = Color(0xFFE8F1E2),
    onSurfaceVariant = Color(0xFF4D6355),
    outline = Color(0xFFB9CDB4),
    error = Color(0xFFC0392B),
    errorContainer = Color(0xFFFDE3DE),
    onErrorContainer = Color(0xFF5C1209),
)

private fun t(familia: FontFamily, peso: Int, tam: Int, linea: Int) =
    TextStyle(fontFamily = familia, fontWeight = FontWeight(peso), fontSize = tam.sp, lineHeight = linea.sp)

private val Tipos = Typography(
    displaySmall = t(Fredoka, 700, 34, 40),
    headlineLarge = t(Fredoka, 700, 30, 36),
    headlineMedium = t(Fredoka, 700, 26, 32),
    headlineSmall = t(Fredoka, 600, 22, 28),
    titleLarge = t(Fredoka, 600, 20, 26),
    titleMedium = t(Fredoka, 600, 17, 22),
    titleSmall = t(Fredoka, 500, 15, 20),
    bodyLarge = t(Nunito, 400, 16, 24),
    bodyMedium = t(Nunito, 400, 14, 21),
    bodySmall = t(Nunito, 400, 13, 18),
    labelLarge = t(Nunito, 800, 15, 20),
    labelMedium = t(Nunito, 700, 13, 17),
    labelSmall = t(Nunito, 700, 11, 14),
)

@Composable
fun SenderismoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, typography = Tipos, content = content)
}
