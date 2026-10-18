@file:OptIn(ExperimentalMaterial3Api::class)

package es.costalceleste.senderismo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

sealed interface EstadoTiempo {
    data object Cargando : EstadoTiempo
    data class Listo(val t: Tiempo) : EstadoTiempo
    data object NoDisponible : EstadoTiempo
}

fun emojiTiempo(t: Tiempo): String = when {
    t.lluviaPct >= 50 -> "🌧️"
    t.lluviaPct >= 25 -> "🌦️"
    t.maxC >= 30 -> "🥵"
    t.maxC >= 20 -> "☀️"
    t.minC <= 5 -> "🥶"
    else -> "⛅"
}

@Composable
fun PlanScreen(
    plan: Plan?,
    onChange: (Plan?) -> Unit,
    onIrARutas: () -> Unit,
    onHecha: (Plan) -> Unit,
) {
    if (plan == null) {
        SinPlan(onIrARutas)
        return
    }
    val ruta = RUTAS.firstOrNull { it.id == plan.rutaId }
    if (ruta == null) {
        SinPlan(onIrARutas)
        return
    }

    val fecha = try { LocalDate.parse(plan.fecha) } catch (e: Exception) { LocalDate.now() }
    val duracion = ruta.duracionMin()
    val llegada = plan.salidaMin + plan.cocheMin
    val fin = llegada + duracion
    val casa = fin + plan.cocheMin
    val salidaMaxima = SIESTA_MIN - duracion - plan.cocheMin

    var tiempo by remember { mutableStateOf<EstadoTiempo>(EstadoTiempo.Cargando) }
    LaunchedEffect(ruta.id, plan.fecha) {
        tiempo = EstadoTiempo.Cargando
        tiempo = try {
            EstadoTiempo.Listo(cargarTiempo(ruta.lat, ruta.lon, plan.fecha))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            EstadoTiempo.NoDisponible
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CabeceraPaisaje(ruta.entorno, alto = 230.dp) {
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
            ) {
                Text("Próxima aventura ${ruta.emoji}", style = estiloSobrePaisaje(MaterialTheme.typography.titleMedium))
                Text(ruta.nombre, style = estiloSobrePaisaje(MaterialTheme.typography.headlineMedium))
                Text(fmtFechaLarga(fecha), style = estiloSobrePaisaje(MaterialTheme.typography.bodyLarge))
            }
        }

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-24).dp)
        ) {
            Bloque {
                Text("📅 ¿Qué día?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                val hoy = LocalDate.now()
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((0..13).toList()) { i ->
                        val d = hoy.plusDays(i.toLong())
                        FilterChip(
                            selected = d == fecha,
                            onClick = { onChange(plan.copy(fecha = d.toString())) },
                            label = { Text(fmtDia(d), style = MaterialTheme.typography.labelLarge) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("🏠 Salimos desde", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Base.entries.forEach { b ->
                        FilterChip(
                            selected = plan.base == b,
                            onClick = { onChange(plan.copy(base = b, cocheMin = ruta.minutosCoche[b] ?: plan.cocheMin)) },
                            label = { Text(if (b == Base.SEVILLA) "🏙️ ${b.label}" else "🏖️ ${b.label}", style = MaterialTheme.typography.labelLarge) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Ajuste(
                    titulo = "⏰ Salida de casa",
                    valor = fmtHora(plan.salidaMin),
                    onMenos = { onChange(plan.copy(salidaMin = (plan.salidaMin - 15).coerceIn(5 * 60, 14 * 60))) },
                    onMas = { onChange(plan.copy(salidaMin = (plan.salidaMin + 15).coerceIn(5 * 60, 14 * 60))) },
                )
                Ajuste(
                    titulo = "🚗 Coche",
                    valor = "${plan.cocheMin} min",
                    onMenos = { onChange(plan.copy(cocheMin = (plan.cocheMin - 5).coerceIn(5, 300))) },
                    onMas = { onChange(plan.copy(cocheMin = (plan.cocheMin + 5).coerceIn(5, 300))) },
                )
            }

            TituloSeccion("🕐", "El día, hora a hora")
            Bloque {
                PasoHora("🏠", "Salimos de casa", plan.salidaMin, ultimo = false)
                PasoHora("🚗", "Llegamos al inicio", llegada, ultimo = false)
                PasoHora("🏁", "Terminamos la ruta", fin, ultimo = false)
                PasoHora("🛋️", "En casa", casa, ultimo = true)
            }
            Spacer(Modifier.height(12.dp))
            val tarde = fin > SIESTA_MIN
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (tarde) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("😴", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (!tarde) {
                            "Terminamos a las ${fmtHora(fin)}, antes de la siesta de las ${fmtHora(SIESTA_MIN)}."
                        } else if (salidaMaxima >= 6 * 60) {
                            "Terminamos a las ${fmtHora(fin)}, después de la siesta. Salid a las ${fmtHora(salidaMaxima)} como muy tarde, o que duerma en la mochila."
                        } else {
                            "Esta ruta no cabe antes de la siesta: contad con que duerma en la mochila."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            TituloSeccion("🌤️", "Tiempo en la zona")
            Bloque {
                when (val t = tiempo) {
                    EstadoTiempo.Cargando -> Text("Mirando el cielo…", style = MaterialTheme.typography.bodyLarge)
                    EstadoTiempo.NoDisponible -> Text(
                        "Sin previsión para ese día. Hace falta conexión y que falten menos de 16 días.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    is EstadoTiempo.Listo -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(emojiTiempo(t.t), fontSize = 44.sp)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "${t.t.maxC.roundToInt()}° / ${t.t.minC.roundToInt()}°",
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                                Text(
                                    "☔ ${t.t.lluviaPct} %   💨 ${t.t.vientoKmH.roundToInt()} km/h",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        val avisos = avisosTiempo(t.t)
                        Spacer(Modifier.height(10.dp))
                        if (avisos.isEmpty()) {
                            Pastilla("👍 Buen día para salir", fondo = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            avisos.forEach {
                                Text("⚠️ $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            val lista = checklistPara(fecha)
            val hechos = lista.count { it in plan.marcados }
            TituloSeccion("🎒", "La mochila")
            Bloque {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (hechos == lista.size) "¡Mochila lista! 🎉" else "$hechos de ${lista.size} cosas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (hechos > 0) {
                        TextButton(onClick = { onChange(plan.copy(marcados = emptySet())) }) { Text("Desmarcar") }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Progreso(hechos.toFloat() / lista.size, Girasol)
                Spacer(Modifier.height(8.dp))
                lista.forEach { item ->
                    val marcado = item in plan.marcados
                    val alternar = {
                        onChange(plan.copy(marcados = if (marcado) plan.marcados - item else plan.marcados + item))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { alternar() },
                    ) {
                        Checkbox(
                            checked = marcado,
                            onCheckedChange = { alternar() },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        )
                        Text(
                            item,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (marcado) TextDecoration.LineThrough else TextDecoration.None,
                            ),
                            modifier = Modifier.alpha(if (marcado) 0.55f else 1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onHecha(plan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Girasol, contentColor = PinoOscuro),
            ) { Text("✅  ¡La hicimos! Apuntar la salida", style = MaterialTheme.typography.titleMedium) }
            TextButton(onClick = { onChange(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("🗑️  Borrar este plan")
            }
        }
    }
}

@Composable
private fun SinPlan(onIrARutas: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        CabeceraPaisaje(Entorno.VIA_VERDE, alto = 260.dp) {
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
            ) {
                Text("Sin plan todavía", style = estiloSobrePaisaje(MaterialTheme.typography.headlineMedium))
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🎒", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Elegid una ruta y pulsad «Planificar salida». Aquí tendréis el horario, el tiempo y la mochila.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onIrARutas, shape = RoundedCornerShape(20.dp), modifier = Modifier.height(52.dp)) {
                Text("🗺️  Elegir una ruta", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PasoHora(emoji: String, texto: String, minutos: Int, ultimo: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 20.sp) }
            if (!ultimo) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Row(Modifier.padding(top = 8.dp).weight(1f)) {
            Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(fmtHora(minutos), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Ajuste(titulo: String, valor: String, onMenos: () -> Unit, onMas: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onMenos, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.size(42.dp), shape = CircleShape) {
            Text("−", fontSize = 20.sp)
        }
        Text(valor, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp))
        OutlinedButton(onClick = onMas, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.size(42.dp), shape = CircleShape) {
            Text("+", fontSize = 20.sp)
        }
    }
}
