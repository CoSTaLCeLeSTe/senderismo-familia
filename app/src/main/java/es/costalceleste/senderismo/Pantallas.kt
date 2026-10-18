@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package es.costalceleste.senderismo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

private val ES = Locale("es", "ES")

// ---------- Utilidades de formato ----------

fun fmtKm(km: Double): String =
    if (km % 1.0 == 0.0) km.toInt().toString() else String.format(ES, "%.1f", km)

fun fmtDur(min: Int): String =
    if (min < 60) "≈ $min min" else if (min % 60 == 0) "≈ ${min / 60} h" else "≈ ${min / 60} h ${min % 60} min"

fun fmtHora(min: Int): String {
    val m = ((min % (24 * 60)) + 24 * 60) % (24 * 60)
    return String.format(Locale.US, "%02d:%02d", m / 60, m % 60)
}

fun fmtDia(d: LocalDate): String {
    val hoy = LocalDate.now()
    return when (d) {
        hoy -> "Hoy"
        hoy.plusDays(1) -> "Mañana"
        else -> d.format(DateTimeFormatter.ofPattern("EEE d MMM", ES))
    }
}

fun abrir(context: Context, uri: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    } catch (e: ActivityNotFoundException) {
        // Sin app para abrirlo: no hacemos nada.
    }
}

// ---------- App ----------

@Composable
fun App(store: Store) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var fichaId by rememberSaveable { mutableStateOf<String?>(null) }
    var plan by remember { mutableStateOf(store.loadPlan()) }
    val guardar: (Plan?) -> Unit = { p ->
        plan = p
        store.savePlan(p)
    }

    BackHandler(enabled = tab == 0 && fichaId != null) { fichaId = null }
    BackHandler(enabled = tab == 1) { tab = 0 }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Place, contentDescription = null) },
                    label = { Text("Rutas") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    label = { Text("Plan") },
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val ficha = fichaId?.let { id -> RUTAS.firstOrNull { it.id == id } }
            when {
                tab == 0 && ficha != null -> FichaScreen(
                    ruta = ficha,
                    onBack = { fichaId = null },
                    onPlanificar = {
                        guardar(nuevoPlan(ficha))
                        fichaId = null
                        tab = 1
                    },
                )
                tab == 0 -> RutasScreen(onOpen = { fichaId = it })
                else -> PlanScreen(plan = plan, onChange = guardar, onIrARutas = { tab = 0 })
            }
        }
    }
}

// ---------- Piezas comunes ----------

@Composable
fun PuntoSemaforo(s: Semaforo) {
    Box(
        Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(Color(s.color))
    )
}

@Composable
fun Etiqueta(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun Dato(titulo: String, valor: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun Aviso(texto: String, alerta: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (alerta) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (alerta) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(texto, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Titulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

// ---------- Catálogo ----------

@Composable
fun RutasScreen(onOpen: (String) -> Unit) {
    var provincia by rememberSaveable { mutableStateOf<String?>(null) }
    var base by rememberSaveable { mutableStateOf<String?>(null) }
    var soloCarrito by rememberSaveable { mutableStateOf(false) }
    var maxKm by rememberSaveable { mutableStateOf(15f) }

    val lista = RUTAS.filter { r ->
        (provincia == null || r.provincia.name == provincia) &&
            (base == null || r.bases.any { it.name == base }) &&
            (!soloCarrito || r.modo != Modo.MOCHILA) &&
            r.km <= maxKm
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column {
                Text("Rutas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${lista.size} de ${RUTAS.size} rutas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = provincia == null, onClick = { provincia = null }, label = { Text("Todas las provincias") })
                    Provincia.entries.forEach { p ->
                        FilterChip(
                            selected = provincia == p.name,
                            onClick = { provincia = p.name },
                            label = { Text(p.label) },
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = base == null, onClick = { base = null }, label = { Text("Cualquier origen") })
                    Base.entries.forEach { b ->
                        FilterChip(
                            selected = base == b.name,
                            onClick = { base = b.name },
                            label = { Text("Desde ${b.label}") },
                        )
                    }
                    FilterChip(
                        selected = soloCarrito,
                        onClick = { soloCarrito = !soloCarrito },
                        label = { Text("Aptas para carrito") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Recorrido máximo: ${maxKm.roundToInt()} km", style = MaterialTheme.typography.bodyMedium)
                Slider(value = maxKm, onValueChange = { maxKm = it }, valueRange = 1f..15f, steps = 13)
            }
        }
        items(lista, key = { it.id }) { r ->
            RutaCard(r) { onOpen(r.id) }
        }
        if (lista.isEmpty()) {
            item { Text("Ninguna ruta cumple los filtros.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
fun RutaCard(r: Ruta, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PuntoSemaforo(r.semaforo)
                Spacer(Modifier.width(8.dp))
                Text(
                    r.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(r.zona, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Etiqueta(r.provincia.label)
                Etiqueta("${fmtKm(r.km)} km")
                Etiqueta(fmtDur(r.duracionMin()))
                Etiqueta(r.modo.label)
            }
        }
    }
}

// ---------- Ficha ----------

@Composable
fun FichaScreen(ruta: Ruta, onBack: () -> Unit, onPlanificar: () -> Unit) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        Text(ruta.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(ruta.zona, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PuntoSemaforo(ruta.semaforo)
            Spacer(Modifier.width(8.dp))
            Text("Semáforo familiar: ${ruta.semaforo.label}", style = MaterialTheme.typography.bodyMedium)
        }

        Titulo("Datos")
        Dato("Recorrido propuesto", "${ruta.recorridoPropuesto} · ${fmtKm(ruta.km)} km")
        Dato("Distancia oficial", ruta.distanciaOficial)
        Dato("Duración a ritmo familiar (con paradas)", fmtDur(ruta.duracionMin()))
        Dato("Cómo va el peque", ruta.modo.label)
        Dato(
            "Coche (aprox.)",
            Base.entries.mapNotNull { b -> ruta.minutosCoche[b]?.let { "${b.label}: $it min" } }.joinToString(" · "),
        )

        Titulo("Por qué")
        Text(ruta.porque, style = MaterialTheme.typography.bodyLarge)
        if (ruta.avisos.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Aviso(ruta.avisos, alerta = true)
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onPlanificar, modifier = Modifier.fillMaxWidth()) {
            Text("Planificar salida")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { abrir(context, "geo:0,0?q=" + Uri.encode(ruta.busquedaMapa)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cómo llegar (Google Maps)")
        }
        TextButton(onClick = { abrir(context, ruta.fuente) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver ficha oficial")
        }
    }
}

// ---------- Plan de salida ----------

sealed interface EstadoTiempo {
    data object Cargando : EstadoTiempo
    data class Listo(val t: Tiempo) : EstadoTiempo
    data object NoDisponible : EstadoTiempo
}

@Composable
fun PlanScreen(plan: Plan?, onChange: (Plan?) -> Unit, onIrARutas: () -> Unit) {
    if (plan == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("No hay ninguna salida planificada.", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onIrARutas) { Text("Elegir una ruta") }
        }
        return
    }
    val ruta = RUTAS.firstOrNull { it.id == plan.rutaId }
    if (ruta == null) {
        Column(Modifier.padding(24.dp)) {
            Text("La ruta del plan ya no existe.")
            Button(onClick = { onChange(null) }) { Text("Borrar plan") }
        }
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
            .padding(16.dp)
    ) {
        Text("Próxima salida", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(ruta.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "${fmtKm(ruta.km)} km · ${fmtDur(duracion)} · ${ruta.modo.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Titulo("Día")
        val hoy = LocalDate.now()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items((0..13).toList()) { i ->
                val d = hoy.plusDays(i.toLong())
                FilterChip(
                    selected = d == fecha,
                    onClick = { onChange(plan.copy(fecha = d.toString())) },
                    label = { Text(fmtDia(d)) },
                )
            }
        }

        Titulo("Salimos desde")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Base.entries.forEach { b ->
                FilterChip(
                    selected = plan.base == b,
                    onClick = { onChange(plan.copy(base = b, cocheMin = ruta.minutosCoche[b] ?: plan.cocheMin)) },
                    label = { Text(b.label) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Ajuste(
            titulo = "Salida de casa",
            valor = fmtHora(plan.salidaMin),
            onMenos = { onChange(plan.copy(salidaMin = (plan.salidaMin - 15).coerceIn(5 * 60, 14 * 60))) },
            onMas = { onChange(plan.copy(salidaMin = (plan.salidaMin + 15).coerceIn(5 * 60, 14 * 60))) },
        )
        Ajuste(
            titulo = "Coche (aprox., ajústalo)",
            valor = "${plan.cocheMin} min",
            onMenos = { onChange(plan.copy(cocheMin = (plan.cocheMin - 5).coerceIn(5, 300))) },
            onMas = { onChange(plan.copy(cocheMin = (plan.cocheMin + 5).coerceIn(5, 300))) },
        )

        Titulo("Horario")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                FilaHora("Salida de casa", plan.salidaMin)
                FilaHora("Llegada al inicio", llegada)
                FilaHora("Fin de la ruta", fin)
                FilaHora("Llegada a casa", casa)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (fin > SIESTA_MIN) {
            val consejo = if (salidaMaxima >= 6 * 60) {
                "Para acabar antes, salid a las ${fmtHora(salidaMaxima)} como muy tarde, o contad con que duerma en la mochila."
            } else {
                "Esta ruta no cabe antes de la siesta: contad con que duerma en la mochila."
            }
            Aviso("La ruta acaba a las ${fmtHora(fin)}, después de la siesta (${fmtHora(SIESTA_MIN)}). $consejo", alerta = true)
        } else {
            Aviso("Acabáis a las ${fmtHora(fin)}, antes de la siesta (${fmtHora(SIESTA_MIN)}).", alerta = false)
        }

        Titulo("Tiempo en la zona")
        when (val t = tiempo) {
            EstadoTiempo.Cargando -> Text("Consultando la previsión…", style = MaterialTheme.typography.bodyMedium)
            EstadoTiempo.NoDisponible -> Text(
                "Previsión no disponible (sin conexión o fecha demasiado lejana).",
                style = MaterialTheme.typography.bodyMedium,
            )
            is EstadoTiempo.Listo -> {
                Text(
                    "Máx. ${t.t.maxC.roundToInt()} °C · mín. ${t.t.minC.roundToInt()} °C · lluvia ${t.t.lluviaPct} % · viento ${t.t.vientoKmH.roundToInt()} km/h",
                    style = MaterialTheme.typography.bodyLarge,
                )
                val avisos = avisosTiempo(t.t)
                if (avisos.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Aviso("Buen día para salir.", alerta = false)
                } else {
                    avisos.forEach {
                        Spacer(Modifier.height(8.dp))
                        Aviso(it, alerta = true)
                    }
                }
            }
        }

        val lista = checklistPara(fecha)
        val hechos = lista.count { it in plan.marcados }
        Titulo("Mochila ($hechos de ${lista.size})")
        lista.forEach { item ->
            val marcado = item in plan.marcados
            val alternar = {
                onChange(plan.copy(marcados = if (marcado) plan.marcados - item else plan.marcados + item))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { alternar() },
            ) {
                Checkbox(checked = marcado, onCheckedChange = { alternar() })
                Text(item, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { onChange(plan.copy(marcados = emptySet())) }, modifier = Modifier.fillMaxWidth()) {
            Text("Desmarcar toda la mochila")
        }
        TextButton(onClick = { onChange(null) }, modifier = Modifier.fillMaxWidth()) {
            Text("Borrar este plan")
        }
    }
}

@Composable
fun FilaHora(titulo: String, minutos: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(fmtHora(minutos), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Ajuste(titulo: String, valor: String, onMenos: () -> Unit, onMas: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onMenos, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(44.dp)) {
            Text("−")
        }
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        OutlinedButton(onClick = onMas, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(44.dp)) {
            Text("+")
        }
    }
}
