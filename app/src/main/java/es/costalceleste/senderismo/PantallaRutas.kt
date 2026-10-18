@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package es.costalceleste.senderismo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun RutasScreen(salidas: List<Salida>, onOpen: (String) -> Unit) {
    var provincia by rememberSaveable { mutableStateOf<String?>(null) }
    var base by rememberSaveable { mutableStateOf<String?>(null) }
    var soloCarrito by rememberSaveable { mutableStateOf(false) }
    var maxKm by rememberSaveable { mutableStateOf(15f) }

    val stats = Estadisticas(salidas)
    val conseguidos = LOGROS.count { it.conseguido(stats) }
    val vecesPorRuta = salidas.groupingBy { it.rutaId }.eachCount()

    val lista = RUTAS.filter { r ->
        (provincia == null || r.provincia.name == provincia) &&
            (base == null || r.bases.any { it.name == base }) &&
            (!soloCarrito || r.modo != Modo.MOCHILA) &&
            r.km <= maxKm
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            CabeceraPaisaje(Entorno.SIERRA, alto = 230.dp) {
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
                ) {
                    Text("¡A caminar, familia!", style = estiloSobrePaisaje(MaterialTheme.typography.headlineLarge))
                    Text(
                        "${RUTAS.size} rutas por Sevilla, Cádiz y Huelva",
                        style = estiloSobrePaisaje(MaterialTheme.typography.bodyLarge),
                    )
                }
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-34).dp),
            ) {
                Cifra("✅", "${stats.distintas.size}/${RUTAS.size}", "rutas hechas", Modifier.weight(1f))
                Cifra("👣", fmtKm(stats.km), "km andados", Modifier.weight(1f))
                Cifra("🏆", "$conseguidos", "logros", Modifier.weight(1f))
            }
        }
        item {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-22).dp)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Filtro("🌍 Todas", provincia == null) { provincia = null }
                    Provincia.entries.forEach { p ->
                        Filtro("${p.emoji} ${p.label}", provincia == p.name) { provincia = if (provincia == p.name) null else p.name }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Filtro("🏙️ Desde Sevilla", base == Base.SEVILLA.name) {
                        base = if (base == Base.SEVILLA.name) null else Base.SEVILLA.name
                    }
                    Filtro("🏖️ Desde El Puerto", base == Base.PUERTO.name) {
                        base = if (base == Base.PUERTO.name) null else Base.PUERTO.name
                    }
                    Filtro("🚼 Con carrito", soloCarrito) { soloCarrito = !soloCarrito }
                }
                Spacer(Modifier.height(4.dp))
                Text("🥾 Hasta ${maxKm.roundToInt()} km", style = MaterialTheme.typography.labelLarge)
                Slider(value = maxKm, onValueChange = { maxKm = it }, valueRange = 1f..15f, steps = 13)
                Text(
                    if (lista.size == RUTAS.size) "Todas las rutas" else "${lista.size} de ${RUTAS.size} rutas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(lista, key = { it.id }) { r ->
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-22).dp)
            ) {
                TarjetaRuta(r, vecesPorRuta[r.id] ?: 0) { onOpen(r.id) }
            }
        }
        if (lista.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🦔", fontSize = 48.sp)
                    Text("Ninguna ruta con estos filtros. Prueba a subir los kilómetros.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun Filtro(texto: String, activo: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = activo,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.labelLarge) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
        ),
    )
}

@Composable
fun TarjetaRuta(r: Ruta, veces: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Paisaje(r.entorno, Modifier.fillMaxSize(), animado = false)
                Box(
                    Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) { Text(r.emoji, fontSize = 28.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(r.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (veces > 0) {
                        Spacer(Modifier.width(6.dp))
                        Pastilla(if (veces == 1) "✅" else "✅ ×$veces", fondo = MaterialTheme.colorScheme.primaryContainer)
                    }
                }
                Text(
                    "${r.provincia.emoji} ${r.zona}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Pastilla("${r.semaforo.emoji} ${r.semaforo.corto}")
                    Pastilla("🥾 ${fmtKm(r.km)} km")
                    Pastilla("⏱️ ${fmtDur(r.duracionMin())}")
                    Pastilla(r.modo.emoji)
                }
            }
        }
    }
}
