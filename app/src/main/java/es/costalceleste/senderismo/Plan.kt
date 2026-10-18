package es.costalceleste.senderismo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Hora habitual de la siesta del peque, en minutos desde medianoche (14:30). */
const val SIESTA_MIN = 14 * 60 + 30

data class Plan(
    val rutaId: String,
    val fecha: String,
    val base: Base,
    val salidaMin: Int,
    val cocheMin: Int,
    val marcados: Set<String>,
)

fun nuevoPlan(ruta: Ruta): Plan {
    val base = ruta.bases.first()
    val coche = ruta.minutosCoche[base] ?: 60
    // Salida que deja la ruta terminada un cuarto de hora antes de la siesta, entre las 7:00 y las 9:30.
    val ideal = SIESTA_MIN - 15 - ruta.duracionMin() - coche
    val salida = (ideal.coerceIn(7 * 60, 9 * 60 + 30) / 15) * 15
    val sabado = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
    return Plan(ruta.id, sabado.toString(), base, salida, coche, emptySet())
}

fun checklistPara(fecha: LocalDate): List<String> {
    val basicos = listOf(
        "🎒 Mochila portabebés",
        "💧 Agua (1,5 L por adulto)",
        "🍼 Agua y vaso del peque",
        "🥪 Comida y snacks",
        "🧷 Pañales y toallitas",
        "👕 Cambio de ropa del peque",
        "🧴 Crema solar",
        "🧢 Gorras",
        "🩹 Botiquín",
        "🔋 Móvil cargado y batería externa",
        "🗑️ Bolsa para la basura",
        "🧸 Chupete o muñeco favorito",
    )
    val temporada = when (fecha.monthValue) {
        in 6..9 -> listOf("💦 Agua extra por el calor", "👒 Gorra con cubrenuca para el peque", "🦟 Repelente de mosquitos")
        11, 12, 1, 2, 3 -> listOf("🧥 Capa impermeable", "🧣 Abrigo o funda de la mochila", "🧦 Calcetines de repuesto")
        else -> listOf("🌂 Chubasquero ligero")
    }
    return basicos + temporada
}

class Store(context: Context) {
    private val prefs = context.getSharedPreferences("senderismo", Context.MODE_PRIVATE)

    fun loadPlan(): Plan? {
        val raw = prefs.getString("plan", null) ?: return null
        return try {
            val o = JSONObject(raw)
            val arr = o.optJSONArray("marcados") ?: JSONArray()
            val marcados = (0 until arr.length()).map { arr.getString(it) }.toSet()
            Plan(
                rutaId = o.getString("rutaId"),
                fecha = o.getString("fecha"),
                base = Base.valueOf(o.getString("base")),
                salidaMin = o.getInt("salidaMin"),
                cocheMin = o.getInt("cocheMin"),
                marcados = marcados,
            )
        } catch (e: Exception) {
            null
        }
    }

    fun savePlan(plan: Plan?) {
        if (plan == null) {
            prefs.edit().remove("plan").apply()
            return
        }
        val o = JSONObject()
        o.put("rutaId", plan.rutaId)
        o.put("fecha", plan.fecha)
        o.put("base", plan.base.name)
        o.put("salidaMin", plan.salidaMin)
        o.put("cocheMin", plan.cocheMin)
        o.put("marcados", JSONArray(plan.marcados.toList()))
        prefs.edit().putString("plan", o.toString()).apply()
    }
}
