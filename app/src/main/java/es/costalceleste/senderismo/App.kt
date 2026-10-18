package es.costalceleste.senderismo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private data class Pestana(val emoji: String, val nombre: String)

private val PESTANAS = listOf(
    Pestana("🗺️", "Rutas"),
    Pestana("🎒", "Plan"),
    Pestana("📖", "Diario"),
    Pestana("🏆", "Logros"),
)

@Composable
fun App(store: Store, salidasIniciales: List<Salida>, guardarSalidas: (List<Salida>) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var fichaId by rememberSaveable { mutableStateOf<String?>(null) }
    var plan by remember { mutableStateOf(store.loadPlan()) }
    var salidas by remember { mutableStateOf(salidasIniciales) }
    var borrador by remember { mutableStateOf<Salida?>(null) }
    var borradorDesdePlan by remember { mutableStateOf(false) }
    var nuevosLogros by remember { mutableStateOf<List<Logro>>(emptyList()) }

    val cambiarPlan: (Plan?) -> Unit = { p ->
        plan = p
        store.savePlan(p)
    }
    val abrirRegistro: (Salida, Boolean) -> Unit = { s, desdePlan ->
        borrador = s
        borradorDesdePlan = desdePlan
    }
    val guardarSalida: (Salida) -> Unit = { s ->
        val antes = logrosConseguidos(salidas)
        val lista = (salidas.filter { it.id != s.id } + s).sortedByDescending { it.fecha }
        val despues = logrosConseguidos(lista)
        salidas = lista
        guardarSalidas(lista)
        nuevosLogros = LOGROS.filter { it.id in despues && it.id !in antes }
        if (borradorDesdePlan) cambiarPlan(null)
        borrador = null
        fichaId = null
        tab = 2
    }
    val borrarSalida: (Salida) -> Unit = { s ->
        val lista = salidas.filter { it.id != s.id }
        salidas = lista
        guardarSalidas(lista)
    }

    BackHandler(enabled = borrador != null) { borrador = null }
    BackHandler(enabled = borrador == null && tab == 0 && fichaId != null) { fichaId = null }
    BackHandler(enabled = borrador == null && tab != 0) { tab = 0 }

    FondoNaturaleza {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (borrador == null) {
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        PESTANAS.forEachIndexed { i, p ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = {
                                    tab = i
                                    if (i == 0) fichaId = null
                                },
                                icon = { Text(p.emoji, fontSize = if (tab == i) 26.sp else 22.sp) },
                                label = { Text(p.nombre, style = MaterialTheme.typography.labelMedium) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val ficha = fichaId?.let { id -> RUTAS.firstOrNull { it.id == id } }
                val actual = borrador
                when {
                    actual != null -> RegistroScreen(
                        borrador = actual,
                        onChange = { borrador = it },
                        onGuardar = { guardarSalida(it) },
                        onCancelar = { borrador = null },
                    )
                    tab == 0 && ficha != null -> FichaScreen(
                        ruta = ficha,
                        salidas = salidas.filter { it.rutaId == ficha.id },
                        onBack = { fichaId = null },
                        onPlanificar = {
                            cambiarPlan(nuevoPlan(ficha))
                            fichaId = null
                            tab = 1
                        },
                        onRegistrar = { abrirRegistro(Salida(rutaId = ficha.id, fecha = java.time.LocalDate.now().toString()), false) },
                    )
                    tab == 0 -> RutasScreen(salidas = salidas, onOpen = { fichaId = it })
                    tab == 1 -> PlanScreen(
                        plan = plan,
                        onChange = cambiarPlan,
                        onIrARutas = { tab = 0 },
                        onHecha = { p ->
                            val todo = checklistPara(java.time.LocalDate.parse(p.fecha)).all { it in p.marcados }
                            val momentos = buildSet {
                                if (todo) add("mochila")
                                if (p.salidaMin < 8 * 60) add("madrugada")
                            }
                            abrirRegistro(Salida(rutaId = p.rutaId, fecha = p.fecha, momentos = momentos), true)
                        },
                    )
                    tab == 2 -> DiarioScreen(
                        salidas = salidas,
                        onNueva = { abrirRegistro(Salida(rutaId = RUTAS.first().id, fecha = java.time.LocalDate.now().toString()), false) },
                        onEditar = { abrirRegistro(it, false) },
                        onBorrar = borrarSalida,
                    )
                    else -> LogrosScreen(salidas = salidas)
                }
            }
        }
    }

    if (nuevosLogros.isNotEmpty()) {
        Celebracion(nuevosLogros) {
            nuevosLogros = emptyList()
            tab = 3
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Celebracion(logros: List<Logro>, onCerrar: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.2f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow),
        label = "medallas",
    )
    Dialog(onDismissRequest = onCerrar) {
        Surface(shape = RoundedCornerShape(32.dp), color = Color.White) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 48.sp, modifier = Modifier.scale(escala))
                Spacer(Modifier.height(8.dp))
                Text(
                    if (logros.size == 1) "¡Nuevo logro!" else "¡${logros.size} logros nuevos!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    logros.take(6).forEach { l ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
                            Box(Modifier.scale(escala)) { Medalla(l, conseguida = true, tam = 72.dp) }
                            Spacer(Modifier.height(6.dp))
                            Text(l.nombre, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                        }
                    }
                }
                if (logros.size > 6) {
                    Spacer(Modifier.height(8.dp))
                    Text("y ${logros.size - 6} más", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver mis logros 🏆")
                }
            }
        }
    }
}
