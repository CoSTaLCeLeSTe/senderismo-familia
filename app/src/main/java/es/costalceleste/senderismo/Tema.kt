package es.costalceleste.senderismo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(
    primary = Color(0xFF2E6B4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE8D9),
    onPrimaryContainer = Color(0xFF0F2E20),
    secondary = Color(0xFF8A5A2B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E1CC),
    onSecondaryContainer = Color(0xFF3A230B),
    background = Color(0xFFFAF8F3),
    onBackground = Color(0xFF1C1B18),
    surface = Color(0xFFFAF8F3),
    onSurface = Color(0xFF1C1B18),
    surfaceVariant = Color(0xFFE7E3D8),
    onSurfaceVariant = Color(0xFF4A473F),
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFF8FD1AE),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF1D4F37),
    onPrimaryContainer = Color(0xFFCDE8D9),
    secondary = Color(0xFFE6C29B),
    onSecondary = Color(0xFF3A230B),
    secondaryContainer = Color(0xFF5A3D1E),
    onSecondaryContainer = Color(0xFFF3E1CC),
)

@Composable
fun SenderismoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        content = content,
    )
}
