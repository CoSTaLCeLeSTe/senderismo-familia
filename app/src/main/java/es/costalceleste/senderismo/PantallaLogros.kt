package es.costalceleste.senderismo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LogrosScreen(salidas: List<Salida>) {
    val stats = Estadisticas(salidas)
    val conseguidos = LOGROS.filter { it.conseguido(stats) }.map { it.id }.toSet()
    val (indice, nivel) = nivelPara(conseguidos.size)
    val siguiente = NIVELES.getOrNull(indice + 1)
    var detalle by remember { mutableStateOf<Logro?>(null) }

    val proximos = LOGROS
        .filter { it.id !in conseguidos }
        .sortedByDescending { it.progreso(stats).toFloat() / it.objetivo }
        .take(3)

    LazyColumn(
        contentPadding = PaddingValues(bottom = 28.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            CabeceraPaisaje(Entorno.HUMEDAL, alto = 270.dp) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(nivel.emoji, fontSize = 52.sp) }
                    Spacer(Modifier.height(8.dp))
                    Text("Nivel ${indice + 1} · ${nivel.nombre}", style = estiloSobrePaisaje(MaterialTheme.typography.headlineSmall))
                    Text(
                        "${conseguidos.size} de ${LOGROS.size} logros",
                        style = estiloSobrePaisaje(MaterialTheme.typography.bodyLarge),
                    )
                }
            }
        }
        item {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-26).dp)
            ) {
                Bloque {
                    if (siguiente != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Siguiente nivel: ${siguiente.emoji} ${siguiente.nombre}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${conseguidos.size}/${siguiente.desde}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val tramo = (siguiente.desde - nivel.desde).coerceAtLeast(1)
                        Progreso((conseguidos.size - nivel.desde).toFloat() / tramo, Girasol, alto = 12.dp)
                    } else {
                        Text("¡Sois guardianes del bosque! Lo habéis conseguido todo. 👑", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (proximos.isNotEmpty()) {
                    TituloSeccion("🎯", "A por los siguientes")
                    proximos.forEach { l ->
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { detalle = l },
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Medalla(l, conseguida = false, tam = 56.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(l.nombre, style = MaterialTheme.typography.titleMedium)
                                    Text(l.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(6.dp))
                                    Progreso(l.progreso(stats).toFloat() / l.objetivo, Color(l.categoria.color2), alto = 8.dp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Text("${l.progreso(stats)}/${l.objetivo}", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
        items(Categoria.entries.toList()) { cat ->
            val delGrupo = LOGROS.filter { it.categoria == cat }
            val hechos = delGrupo.count { it.id in conseguidos }
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-26).dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)) {
                    Text(cat.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(cat.label, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Pastilla("$hechos/${delGrupo.size}", fondo = Color(cat.color1).copy(alpha = 0.25f))
                }
                Bloque {
                    delGrupo.chunked(3).forEach { fila ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            fila.forEach { l ->
                                CeldaLogro(l, l.id in conseguidos, stats, Modifier.weight(1f)) { detalle = l }
                            }
                            repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    detalle?.let { l ->
        val hecho = l.id in conseguidos
        AlertDialog(
            onDismissRequest = { detalle = null },
            icon = { Medalla(l, conseguida = hecho, tam = 88.dp) },
            title = { Text(l.nombre, textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(l.descripcion, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    if (hecho) {
                        Pastilla("✅ ¡Conseguido!", fondo = MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        Progreso(l.progreso(stats).toFloat() / l.objetivo, Color(l.categoria.color2), alto = 12.dp)
                        Spacer(Modifier.height(6.dp))
                        Text("${l.progreso(stats)} de ${l.objetivo}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { detalle = null }) { Text("Vale") } },
            containerColor = Color.White,
        )
    }
}

@Composable
private fun CeldaLogro(l: Logro, hecho: Boolean, stats: Estadisticas, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Medalla(l, conseguida = hecho, tam = 70.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            l.nombre,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (hecho) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!hecho && l.objetivo > 1) {
            Text(
                "${l.progreso(stats)}/${l.objetivo}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
