@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package es.costalceleste.senderismo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

// ---------- Diario ----------

@Composable
fun DiarioScreen(
    salidas: List<Salida>,
    onNueva: () -> Unit,
    onEditar: (Salida) -> Unit,
    onBorrar: (Salida) -> Unit,
) {
    val stats = Estadisticas(salidas)
    var aBorrar by remember { mutableStateOf<Salida?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            CabeceraPaisaje(Entorno.BOSQUE, alto = 220.dp) {
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
                ) {
                    Text("Nuestro diario", style = estiloSobrePaisaje(MaterialTheme.typography.headlineLarge))
                    Text(
                        when (salidas.size) {
                            0 -> "Aquí guardaréis cada aventura"
                            1 -> "1 aventura juntos"
                            else -> "${salidas.size} aventuras juntos"
                        },
                        style = estiloSobrePaisaje(MaterialTheme.typography.bodyLarge),
                    )
                }
            }
        }
        item {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-34).dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Cifra("🥾", "${stats.total}", "salidas", Modifier.weight(1f))
                    Cifra("👣", fmtKm(stats.km), "km", Modifier.weight(1f))
                    Cifra("🐾", "${stats.animales.size}", "especies", Modifier.weight(1f))
                    Cifra("😄", if (stats.mediaPeque > 0) String.format(ES, "%.1f", stats.mediaPeque) else "–", "nota peque", Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onNueva,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Girasol, contentColor = PinoOscuro),
                ) { Text("➕  Apuntar una salida", style = MaterialTheme.typography.titleMedium) }
            }
        }
        if (salidas.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🌱", fontSize = 56.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cuando volváis de una ruta, apuntadla: qué animales visteis, si se durmió en la mochila, qué nota le da cada uno… Cada salida desbloquea logros.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(salidas, key = { it.id }) { s ->
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                Postal(s, onEditar = { onEditar(s) }, onBorrar = { aBorrar = s })
            }
        }
    }

    aBorrar?.let { s ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text("¿Borrar esta salida?") },
            text = { Text("Se quita del diario y pueden bloquearse logros que dependían de ella.") },
            confirmButton = {
                TextButton(onClick = {
                    onBorrar(s)
                    aBorrar = null
                }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { aBorrar = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
fun Postal(s: Salida, onEditar: () -> Unit, onBorrar: () -> Unit) {
    val ruta = s.ruta() ?: return
    val dia = s.dia()
    Surface(shape = RoundedCornerShape(26.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                Paisaje(ruta.entorno, Modifier.fillMaxSize(), animado = false)
                Box(
                    Modifier
                        .padding(12.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) { Text(ruta.emoji, fontSize = 28.sp) }
                if (dia != null) {
                    Box(Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                        Pastilla("📅 ${fmtDia(dia)}", fondo = Color.White.copy(alpha = 0.92f))
                    }
                }
            }
            Column(Modifier.padding(16.dp)) {
                Text(ruta.nombre, style = MaterialTheme.typography.titleLarge)
                Text("${ruta.provincia.emoji} ${ruta.zona} · 🥾 ${fmtKm(ruta.km)} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                FilaEstrellas("👨", "Papá", s.estrellasPapa)
                FilaEstrellas("👩", "Mamá", s.estrellasMama)
                FilaEstrellas("👶", "El peque", s.estrellasPeque)
                if (s.animales.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        s.animales.mapNotNull { animal(it)?.emoji }.joinToString(" "),
                        fontSize = 26.sp,
                    )
                }
                if (s.momentos.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        s.momentos.mapNotNull { momento(it) }.forEach {
                            Pastilla("${it.emoji} ${it.texto}", fondo = MaterialTheme.colorScheme.secondaryContainer)
                        }
                    }
                }
                if (s.nota.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "“${s.nota}”",
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onEditar) { Text("✏️ Editar") }
                    TextButton(onClick = onBorrar) { Text("🗑️ Borrar") }
                }
            }
        }
    }
}

@Composable
private fun FilaEstrellas(emoji: String, quien: String, valor: Int) {
    if (valor == 0) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$emoji $quien", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(110.dp))
        Estrellas(valor, tam = 15)
    }
}

// ---------- Apuntar una salida ----------

@Composable
fun RegistroScreen(
    borrador: Salida,
    onChange: (Salida) -> Unit,
    onGuardar: (Salida) -> Unit,
    onCancelar: () -> Unit,
) {
    val ruta = borrador.ruta() ?: RUTAS.first()
    var menuRutas by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CabeceraPaisaje(ruta.entorno, alto = 190.dp) {
            Surface(
                onClick = onCancelar,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(16.dp)
                    .size(44.dp),
            ) { Box(contentAlignment = Alignment.Center) { Text("✕", fontSize = 20.sp, color = PinoOscuro) } }
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 34.dp)
            ) {
                Text("Apuntar salida ${ruta.emoji}", style = estiloSobrePaisaje(MaterialTheme.typography.headlineMedium))
            }
        }

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-22).dp)
        ) {
            Bloque {
                Text("🗺️ ¿Qué ruta?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Box {
                    Surface(
                        onClick = { menuRutas = true },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ruta.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(ruta.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text("▾", fontSize = 18.sp)
                        }
                    }
                    DropdownMenu(expanded = menuRutas, onDismissRequest = { menuRutas = false }) {
                        RUTAS.forEach { r ->
                            DropdownMenuItem(
                                text = { Text("${r.emoji}  ${r.nombre}") },
                                onClick = {
                                    onChange(borrador.copy(rutaId = r.id))
                                    menuRutas = false
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("📅 ¿Cuándo?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                val hoy = LocalDate.now()
                val dias = (0..20).map { hoy.minusDays(it.toLong()) }.toMutableList()
                borrador.dia()?.let { if (it !in dias) dias.add(0, it) }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dias) { d ->
                        FilterChip(
                            selected = d.toString() == borrador.fecha,
                            onClick = { onChange(borrador.copy(fecha = d.toString())) },
                            label = { Text(fmtDia(d), style = MaterialTheme.typography.labelLarge) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }

            TituloSeccion("⭐", "¿Qué tal ha ido?")
            Bloque {
                Valoracion("👨", "Papá", borrador.estrellasPapa) { onChange(borrador.copy(estrellasPapa = it)) }
                Valoracion("👩", "Mamá", borrador.estrellasMama) { onChange(borrador.copy(estrellasMama = it)) }
                Valoracion("👶", "El peque", borrador.estrellasPeque) { onChange(borrador.copy(estrellasPeque = it)) }
            }

            TituloSeccion("🐾", "¿Qué animales visteis?")
            Bloque {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ANIMALES.forEach { a ->
                        val sel = a.id in borrador.animales
                        FilterChip(
                            selected = sel,
                            onClick = {
                                onChange(borrador.copy(animales = if (sel) borrador.animales - a.id else borrador.animales + a.id))
                            },
                            label = { Text("${a.emoji} ${a.nombre}", style = MaterialTheme.typography.labelLarge) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                        )
                    }
                }
            }

            TituloSeccion("✨", "Momentos del día")
            Bloque {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MOMENTOS.forEach { m ->
                        val sel = m.id in borrador.momentos
                        FilterChip(
                            selected = sel,
                            onClick = {
                                onChange(borrador.copy(momentos = if (sel) borrador.momentos - m.id else borrador.momentos + m.id))
                            },
                            label = { Text("${m.emoji} ${m.texto}", style = MaterialTheme.typography.labelLarge) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }
                }
            }

            TituloSeccion("✍️", "Una nota para recordar")
            OutlinedTextField(
                value = borrador.nota,
                onValueChange = { onChange(borrador.copy(nota = it)) },
                placeholder = { Text("Lo que más le gustó, dónde comisteis, una anécdota…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(20.dp),
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onGuardar(borrador) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Girasol, contentColor = PinoOscuro),
            ) { Text("💾  Guardar en el diario", style = MaterialTheme.typography.titleMedium) }
            TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}

@Composable
private fun Valoracion(emoji: String, quien: String, valor: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$emoji $quien", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(110.dp))
        Estrellas(valor, tam = 26, onChange = onChange)
    }
}
