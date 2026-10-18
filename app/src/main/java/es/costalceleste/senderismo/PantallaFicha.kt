@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package es.costalceleste.senderismo

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FichaScreen(
    ruta: Ruta,
    salidas: List<Salida>,
    onBack: () -> Unit,
    onPlanificar: () -> Unit,
    onRegistrar: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CabeceraPaisaje(ruta.entorno, alto = 280.dp) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(16.dp)
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("←", fontSize = 22.sp, color = PinoOscuro) }
            }
            Box(
                Modifier
                    .align(Alignment.Center)
                    .offset(y = (-18).dp)
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center,
            ) { Text(ruta.emoji, fontSize = 46.sp) }
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
            ) {
                Text(ruta.nombre, style = estiloSobrePaisaje(MaterialTheme.typography.headlineMedium))
                Text("${ruta.provincia.emoji} ${ruta.zona}", style = estiloSobrePaisaje(MaterialTheme.typography.bodyMedium))
            }
        }

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-24).dp)
        ) {
            Bloque {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ruta.semaforo.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(ruta.semaforo.label, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pastilla("🥾 ${fmtKm(ruta.km)} km", fondo = MaterialTheme.colorScheme.primaryContainer)
                    Pastilla("⏱️ ${fmtDur(ruta.duracionMin())}", fondo = MaterialTheme.colorScheme.secondaryContainer)
                    Pastilla("${ruta.entorno.emoji} ${ruta.entorno.label}", fondo = MaterialTheme.colorScheme.tertiaryContainer)
                }
            }

            TituloSeccion("💚", "Por qué os va a gustar")
            Bloque {
                Text(ruta.porque, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        "Buscad: ${ruta.buscar}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
            if (ruta.avisos.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp)) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(ruta.avisos, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            TituloSeccion("🧭", "La ruta")
            Bloque {
                DatoEmoji("🥾", "Recorrido propuesto", ruta.recorridoPropuesto)
                DatoEmoji("📏", "Distancia oficial", ruta.distanciaOficial)
                DatoEmoji("⏱️", "A ritmo de familia, con paradas", fmtDur(ruta.duracionMin()))
                DatoEmoji(ruta.modo.emoji, "Cómo va el peque", ruta.modo.label)
                DatoEmoji(
                    "🚗",
                    "Coche (aproximado)",
                    Base.entries.mapNotNull { b -> ruta.minutosCoche[b]?.let { "desde ${b.label}: $it min" } }.joinToString("\n"),
                )
            }

            if (salidas.isNotEmpty()) {
                TituloSeccion("📖", "Vuestras veces aquí")
                Bloque {
                    Text(
                        if (salidas.size == 1) "La habéis hecho 1 vez." else "La habéis hecho ${salidas.size} veces.",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    salidas.firstOrNull { it.nota.isNotBlank() }?.let {
                        Spacer(Modifier.height(6.dp))
                        Text("“${it.nota}”", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onPlanificar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
            ) { Text("🎒  Planificar salida", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onRegistrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Girasol, contentColor = PinoOscuro),
            ) { Text("📖  Ya la hicimos: apuntarla", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { abrir(context, "geo:0,0?q=" + Uri.encode(ruta.busquedaMapa)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) { Text("🗺️  Cómo llegar") }
            TextButton(onClick = { abrir(context, ruta.fuente) }, modifier = Modifier.fillMaxWidth()) {
                Text("📄  Ver ficha oficial")
            }
        }
    }
}
