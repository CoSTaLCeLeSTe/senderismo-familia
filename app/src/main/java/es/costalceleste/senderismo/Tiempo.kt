package es.costalceleste.senderismo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

data class Tiempo(
    val maxC: Double,
    val minC: Double,
    val lluviaPct: Int,
    val vientoKmH: Double,
)

/** Previsión diaria de Open-Meteo (gratuita, sin clave). Disponible hasta unos 16 días vista. */
suspend fun cargarTiempo(lat: Double, lon: Double, fecha: String): Tiempo = withContext(Dispatchers.IO) {
    val url = URL(
        String.format(
            Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max" +
                "&timezone=Europe%%2FMadrid&start_date=%s&end_date=%s",
            lat, lon, fecha, fecha,
        )
    )
    val con = url.openConnection() as HttpURLConnection
    con.connectTimeout = 10_000
    con.readTimeout = 10_000
    try {
        val texto = con.inputStream.bufferedReader().use { it.readText() }
        val d = JSONObject(texto).getJSONObject("daily")
        val max = d.getJSONArray("temperature_2m_max").optDouble(0)
        val min = d.getJSONArray("temperature_2m_min").optDouble(0)
        if (max.isNaN() || min.isNaN()) throw IllegalStateException("Sin datos")
        Tiempo(
            maxC = max,
            minC = min,
            lluviaPct = d.getJSONArray("precipitation_probability_max").optInt(0, 0),
            vientoKmH = d.getJSONArray("wind_speed_10m_max").optDouble(0, 0.0),
        )
    } finally {
        con.disconnect()
    }
}

fun avisosTiempo(t: Tiempo): List<String> = buildList {
    if (t.maxC >= 30) add("Calor: ${t.maxC.roundToInt()} °C de máxima. Salid temprano y buscad sombra.")
    if (t.lluviaPct >= 50) add("Probabilidad de lluvia del ${t.lluviaPct} %. Llevad capa para el peque.")
    if (t.vientoKmH >= 40) add("Viento fuerte: ${t.vientoKmH.roundToInt()} km/h.")
    if (t.minC <= 5) add("Frío a primera hora: ${t.minC.roundToInt()} °C.")
}
