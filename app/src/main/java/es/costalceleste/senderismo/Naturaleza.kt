package es.costalceleste.senderismo

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Permite desactivar las animaciones (capturas de pantalla). */
val AnimacionesActivas = compositionLocalOf { true }

private class ColoresPaisaje(
    val cielo1: Color,
    val cielo2: Color,
    val lejos: Color,
    val medio: Color,
    val cerca: Color,
    val arbol: Color,
    val agua: Color,
)

private fun colores(e: Entorno): ColoresPaisaje = when (e) {
    Entorno.RIO -> ColoresPaisaje(Color(0xFF9ED8F7), Color(0xFFE9F7EF), Color(0xFFA9C9B6), Color(0xFF78B57F), Color(0xFF3F7F4C), Color(0xFF2C6A3A), Color(0xFF63BCE5))
    Entorno.COSTA -> ColoresPaisaje(Color(0xFF8ED0F5), Color(0xFFFFF5DA), Color(0xFFB9D6C6), Color(0xFF3FA2D6), Color(0xFFEFD49E), Color(0xFF3D7A4A), Color(0xFF3FA2D6))
    Entorno.SIERRA -> ColoresPaisaje(Color(0xFFB9DEFB), Color(0xFFFDF1DE), Color(0xFF9EB0C6), Color(0xFF8AA67A), Color(0xFF557E41), Color(0xFF2F5E2E), Color(0xFF6CB8E0))
    Entorno.HUMEDAL -> ColoresPaisaje(Color(0xFFFFC98F), Color(0xFFF3F9EF), Color(0xFFB4C8A8), Color(0xFF86C7D3), Color(0xFF6E9A47), Color(0xFF3F6E2E), Color(0xFF86C7D3))
    Entorno.BOSQUE -> ColoresPaisaje(Color(0xFFBFE8D0), Color(0xFFF2FAEE), Color(0xFF7BAA80), Color(0xFF4F8D5B), Color(0xFF2E673C), Color(0xFF1F5130), Color(0xFF63BCE5))
    Entorno.VIA_VERDE -> ColoresPaisaje(Color(0xFFC8E6FB), Color(0xFFFAF5E6), Color(0xFFB0C59A), Color(0xFF93B46B), Color(0xFF6A904A), Color(0xFF365F2A), Color(0xFF63BCE5))
}

/** Paisaje dibujado: cielo, sol, nubes, montañas, árboles, agua y pájaros. */
@Composable
fun Paisaje(entorno: Entorno, modifier: Modifier = Modifier, animado: Boolean = true) {
    val t = if (animado && AnimacionesActivas.current) {
        val transicion = rememberInfiniteTransition(label = "nubes")
        transicion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(70_000, easing = LinearEasing), RepeatMode.Restart),
            label = "desplazamiento",
        ).value
    } else {
        0.35f
    }
    Canvas(modifier) { dibujarPaisaje(entorno, t) }
}

/** Cabecera con paisaje y contenido encima (título, botones...). */
@Composable
fun CabeceraPaisaje(
    entorno: Entorno,
    alto: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(alto)
    ) {
        Paisaje(entorno, Modifier.fillMaxSize())
        content()
    }
}

/** Fondo general de la app: cielo claro que baja hasta una pradera con pinos suaves. */
@Composable
fun FondoNaturaleza(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5FD), Color(0xFFF3F9EA), Color(0xFFE6F2D6))))
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height
            val colina = Path().apply {
                moveTo(0f, h * 0.55f)
                cubicTo(w * 0.3f, h * 0.30f, w * 0.6f, h * 0.70f, w, h * 0.45f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(colina, Color(0x33A7CF8A))
            pino(w * 0.08f, h * 0.62f, h * 0.45f, Color(0x2E2F6B3A))
            pino(w * 0.15f, h * 0.60f, h * 0.30f, Color(0x2E2F6B3A))
            pino(w * 0.88f, h * 0.52f, h * 0.50f, Color(0x2E2F6B3A))
            pino(w * 0.95f, h * 0.55f, h * 0.34f, Color(0x2E2F6B3A))
        }
        content()
    }
}

fun DrawScope.dibujarPaisaje(e: Entorno, t: Float) {
    val c = colores(e)
    val w = size.width
    val h = size.height

    // Cielo
    drawRect(Brush.verticalGradient(listOf(c.cielo1, c.cielo2), startY = 0f, endY = h * 0.75f))

    // Sol con halo
    val sol = Offset(w * 0.80f, h * 0.27f)
    val r = h * 0.10f
    drawCircle(
        Brush.radialGradient(listOf(Color(0x88FFE08A), Color(0x00FFE08A)), center = sol, radius = r * 3f),
        radius = r * 3f,
        center = sol,
    )
    drawCircle(Color(0xFFFFCF4A), radius = r, center = sol)

    // Nubes que se desplazan
    nube((((t * 1.4f) + 0.05f) % 1.4f - 0.2f) * w, h * 0.17f, h * 0.06f)
    nube((((t * 1.4f) + 0.80f) % 1.4f - 0.2f) * w, h * 0.33f, h * 0.045f)

    // Pájaros
    pajaro(w * 0.24f, h * 0.24f, h * 0.035f)
    pajaro(w * 0.31f, h * 0.29f, h * 0.026f)
    pajaro(w * 0.52f, h * 0.15f, h * 0.022f)

    // Montañas lejanas
    val lejos = Path().apply {
        moveTo(0f, h * 0.60f)
        lineTo(w * 0.12f, h * 0.44f)
        lineTo(w * 0.24f, h * 0.55f)
        lineTo(w * 0.40f, h * 0.35f)
        lineTo(w * 0.56f, h * 0.53f)
        lineTo(w * 0.70f, h * 0.41f)
        lineTo(w * 0.86f, h * 0.57f)
        lineTo(w, h * 0.47f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(lejos, c.lejos)
    if (e == Entorno.SIERRA) {
        // Cumbres claras
        drawPath(Path().apply {
            moveTo(w * 0.40f, h * 0.35f); lineTo(w * 0.36f, h * 0.40f); lineTo(w * 0.40f, h * 0.39f); lineTo(w * 0.44f, h * 0.41f); close()
        }, Color(0xCCFFFFFF))
        drawPath(Path().apply {
            moveTo(w * 0.70f, h * 0.41f); lineTo(w * 0.67f, h * 0.45f); lineTo(w * 0.70f, h * 0.44f); lineTo(w * 0.73f, h * 0.455f); close()
        }, Color(0xCCFFFFFF))
    }

    // Plano medio: agua (costa, marisma) o colinas con pinos
    val aguaEnMedio = e == Entorno.COSTA || e == Entorno.HUMEDAL
    if (aguaEnMedio) {
        drawRect(c.medio, topLeft = Offset(0f, h * 0.62f), size = Size(w, h * 0.38f))
        for (i in 0 until 4) {
            val y = h * (0.66f + i * 0.045f)
            val x0 = w * ((0.10f + i * 0.23f + t * 0.3f) % 1f)
            drawLine(Color(0x99FFFFFF), Offset(x0, y), Offset(x0 + w * 0.12f, y), strokeWidth = h * 0.008f, cap = StrokeCap.Round)
        }
    } else {
        val medio = Path().apply {
            moveTo(0f, h * 0.70f)
            cubicTo(w * 0.25f, h * 0.58f, w * 0.45f, h * 0.74f, w * 0.65f, h * 0.64f)
            cubicTo(w * 0.80f, h * 0.57f, w * 0.92f, h * 0.66f, w, h * 0.62f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(medio, c.medio)
        val oscuro = c.arbol.copy(alpha = 0.55f)
        pino(w * 0.30f, h * 0.66f, h * 0.12f, oscuro)
        pino(w * 0.34f, h * 0.67f, h * 0.09f, oscuro)
        pino(w * 0.60f, h * 0.66f, h * 0.11f, oscuro)
        pino(w * 0.64f, h * 0.645f, h * 0.13f, oscuro)
    }

    // Primer plano
    val cerca = Path().apply {
        moveTo(0f, h * 0.84f)
        cubicTo(w * 0.30f, h * 0.76f, w * 0.60f, h * 0.92f, w, h * 0.80f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(cerca, c.cerca)

    when (e) {
        Entorno.RIO -> {
            val rio = Path().apply {
                moveTo(w * 0.46f, h * 0.83f)
                cubicTo(w * 0.40f, h * 0.90f, w * 0.62f, h * 0.93f, w * 0.52f, h * 1.0f)
                lineTo(w * 0.70f, h * 1.0f)
                cubicTo(w * 0.78f, h * 0.93f, w * 0.52f, h * 0.89f, w * 0.52f, h * 0.83f)
                close()
            }
            drawPath(rio, c.agua)
            arbolRedondo(w * 0.10f, h * 0.86f, h * 0.22f, c.arbol)
            arbolRedondo(w * 0.20f, h * 0.88f, h * 0.16f, Color(0xFF4E8E4F))
            pino(w * 0.86f, h * 0.84f, h * 0.30f, c.arbol)
            pino(w * 0.94f, h * 0.86f, h * 0.22f, c.arbol)
        }
        Entorno.COSTA -> {
            for (i in 0 until 5) {
                val x = w * (0.08f + i * 0.05f)
                drawLine(Color(0xFF8DA65A), Offset(x, h * 0.92f), Offset(x - w * 0.012f, h * 0.84f), strokeWidth = h * 0.012f, cap = StrokeCap.Round)
            }
            pino(w * 0.88f, h * 0.83f, h * 0.26f, c.arbol)
        }
        Entorno.HUMEDAL -> {
            for (i in 0 until 9) {
                val x = w * (0.04f + i * 0.028f)
                val alto = h * (0.10f + (i % 3) * 0.03f)
                drawLine(Color(0xFF557A33), Offset(x, h * 0.90f), Offset(x + w * 0.006f, h * 0.90f - alto), strokeWidth = h * 0.010f, cap = StrokeCap.Round)
                if (i % 2 == 0) drawCircle(Color(0xFF7A5230), radius = h * 0.012f, center = Offset(x + w * 0.006f, h * 0.90f - alto))
            }
            arbolRedondo(w * 0.90f, h * 0.84f, h * 0.20f, c.arbol)
        }
        Entorno.VIA_VERDE -> {
            val camino = Path().apply {
                moveTo(w * 0.42f, h * 0.84f)
                cubicTo(w * 0.40f, h * 0.90f, w * 0.30f, h * 0.95f, w * 0.20f, h * 1.0f)
                lineTo(w * 0.40f, h * 1.0f)
                cubicTo(w * 0.46f, h * 0.95f, w * 0.48f, h * 0.90f, w * 0.47f, h * 0.84f)
                close()
            }
            drawPath(camino, Color(0xFFE2C994))
            pino(w * 0.10f, h * 0.86f, h * 0.28f, c.arbol)
            pino(w * 0.86f, h * 0.84f, h * 0.30f, c.arbol)
            pino(w * 0.94f, h * 0.85f, h * 0.22f, c.arbol)
        }
        Entorno.SIERRA, Entorno.BOSQUE -> {
            pino(w * 0.06f, h * 0.87f, h * 0.32f, c.arbol)
            pino(w * 0.14f, h * 0.86f, h * 0.24f, c.arbol)
            pino(w * 0.84f, h * 0.83f, h * 0.30f, c.arbol)
            pino(w * 0.92f, h * 0.84f, h * 0.36f, c.arbol)
            if (e == Entorno.BOSQUE) {
                pino(w * 0.22f, h * 0.85f, h * 0.20f, c.arbol)
                arbolRedondo(w * 0.76f, h * 0.84f, h * 0.18f, Color(0xFF3F7A45))
            }
        }
    }
}

fun DrawScope.pino(x: Float, base: Float, alto: Float, color: Color) {
    val ancho = alto * 0.48f
    drawRect(
        Color(0xFF6B4A2B).copy(alpha = color.alpha),
        topLeft = Offset(x - ancho * 0.07f, base - alto * 0.16f),
        size = Size(ancho * 0.14f, alto * 0.16f),
    )
    for (i in 0 until 3) {
        val arriba = base - alto + i * alto * 0.22f
        val abajo = arriba + alto * 0.40f
        val medio = ancho * (0.24f + i * 0.13f)
        drawPath(Path().apply {
            moveTo(x, arriba)
            lineTo(x - medio, abajo)
            lineTo(x + medio, abajo)
            close()
        }, color)
    }
}

fun DrawScope.arbolRedondo(x: Float, base: Float, alto: Float, color: Color) {
    drawRect(Color(0xFF6B4A2B), topLeft = Offset(x - alto * 0.05f, base - alto * 0.45f), size = Size(alto * 0.10f, alto * 0.45f))
    drawCircle(color, radius = alto * 0.30f, center = Offset(x, base - alto * 0.62f))
    drawCircle(color, radius = alto * 0.22f, center = Offset(x - alto * 0.22f, base - alto * 0.50f))
    drawCircle(color, radius = alto * 0.22f, center = Offset(x + alto * 0.22f, base - alto * 0.52f))
}

fun DrawScope.nube(x: Float, y: Float, r: Float) {
    val blanco = Color(0xEEFFFFFF)
    drawCircle(blanco, radius = r, center = Offset(x, y))
    drawCircle(blanco, radius = r * 1.3f, center = Offset(x + r * 1.2f, y - r * 0.4f))
    drawCircle(blanco, radius = r * 0.95f, center = Offset(x + r * 2.4f, y))
    drawRect(blanco, topLeft = Offset(x, y), size = Size(r * 2.4f, r * 0.95f))
}

fun DrawScope.pajaro(x: Float, y: Float, s: Float) {
    val p = Path().apply {
        moveTo(x - s, y - s * 0.3f)
        quadraticBezierTo(x - s * 0.5f, y - s * 0.6f, x, y)
        quadraticBezierTo(x + s * 0.5f, y - s * 0.6f, x + s, y - s * 0.3f)
    }
    drawPath(p, Color(0xFF3B4A55), style = Stroke(width = s * 0.18f, cap = StrokeCap.Round))
}
