package es.costalceleste.senderismo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val ES: Locale = Locale("es", "ES")

// ---------- Formato ----------

fun fmtKm(km: Double): String =
    if (km % 1.0 == 0.0) km.toInt().toString() else String.format(ES, "%.1f", km)

fun fmtDur(min: Int): String =
    if (min < 60) "$min min" else if (min % 60 == 0) "${min / 60} h" else "${min / 60} h ${min % 60} min"

fun fmtHora(min: Int): String {
    val m = ((min % (24 * 60)) + 24 * 60) % (24 * 60)
    return String.format(Locale.US, "%02d:%02d", m / 60, m % 60)
}

fun fmtDia(d: LocalDate): String {
    val hoy = LocalDate.now()
    return when (d) {
        hoy -> "Hoy"
        hoy.plusDays(1) -> "Mañana"
        hoy.minusDays(1) -> "Ayer"
        else -> d.format(DateTimeFormatter.ofPattern("EEE d MMM", ES))
    }
}

fun fmtFechaLarga(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES)).replaceFirstChar { it.uppercase(ES) }

fun abrir(context: Context, uri: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    } catch (e: ActivityNotFoundException) {
        // Sin app para abrirlo.
    }
}

/** Texto blanco con sombra, para escribir encima de los paisajes. */
@Composable
fun estiloSobrePaisaje(base: TextStyle): TextStyle =
    base.copy(color = Color.White, shadow = Shadow(Color(0x99000000), Offset(0f, 2f), 8f))

// ---------- Piezas ----------

/** Pastilla pequeña con emoji y texto. */
@Composable
fun Pastilla(texto: String, fondo: Color = MaterialTheme.colorScheme.surfaceVariant, tinta: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = tinta,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

/** Bloque blanco redondeado para agrupar contenido. */
@Composable
fun Bloque(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

/** Título de sección con emoji grande. */
@Composable
fun TituloSeccion(emoji: String, texto: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(top = 22.dp, bottom = 10.dp)) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** Dato con emoji, en vertical. */
@Composable
fun DatoEmoji(emoji: String, titulo: String, valor: String, modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Cifra grande con emoji para los resúmenes. */
@Composable
fun Cifra(emoji: String, valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 22.sp)
        Text(valor, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Barra de progreso redondeada con color propio. */
@Composable
fun Progreso(fraccion: Float, color: Color, modifier: Modifier = Modifier, alto: Dp = 10.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .height(alto)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/** Estrellas de 0 a 5; si hay onChange se pueden tocar. */
@Composable
fun Estrellas(valor: Int, tam: Int = 18, onChange: ((Int) -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val llena = i <= valor
            Text(
                "⭐",
                fontSize = tam.sp,
                modifier = Modifier
                    .alpha(if (llena) 1f else 0.22f)
                    .then(if (onChange != null) Modifier.clickable { onChange(if (valor == i) 0 else i) } else Modifier)
                    .padding(2.dp),
            )
        }
    }
}

/** Medalla redonda de un logro. */
@Composable
fun Medalla(logro: Logro, conseguida: Boolean, tam: Dp = 76.dp) {
    val c = logro.categoria
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(tam)
                .shadow(if (conseguida) 6.dp else 0.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (conseguida) Brush.linearGradient(listOf(Color(c.color1), Color(c.color2)))
                    else Brush.linearGradient(listOf(Color(0xFFE6EBE3), Color(0xFFD3DBCF)))
                )
                .border(3.dp, Color.White.copy(alpha = if (conseguida) 0.9f else 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                logro.emoji,
                fontSize = (tam.value * 0.44f).sp,
                modifier = Modifier.alpha(if (conseguida) 1f else 0.3f),
            )
        }
        if (!conseguida) {
            Text(
                "🔒",
                fontSize = (tam.value * 0.2f).sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.dp),
            )
        }
    }
}
