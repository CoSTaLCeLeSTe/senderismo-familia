package es.costalceleste.senderismo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/** Una salida que la familia ha hecho de verdad. */
data class Salida(
    val id: String = UUID.randomUUID().toString(),
    val rutaId: String,
    val fecha: String,
    val estrellasPapa: Int = 0,
    val estrellasMama: Int = 0,
    val estrellasPeque: Int = 0,
    val animales: Set<String> = emptySet(),
    val momentos: Set<String> = emptySet(),
    val nota: String = "",
) {
    fun ruta(): Ruta? = RUTAS.firstOrNull { it.id == rutaId }
    fun dia(): LocalDate? = try { LocalDate.parse(fecha) } catch (e: Exception) { null }
}

data class Animal(val id: String, val emoji: String, val nombre: String)

val ANIMALES: List<Animal> = listOf(
    Animal("buitre", "🦅", "Buitre"),
    Animal("flamenco", "🦩", "Flamenco"),
    Animal("ciguena", "🪺", "Cigüeña"),
    Animal("pato", "🦆", "Pato"),
    Animal("garza", "🪶", "Garza"),
    Animal("pajarito", "🐦", "Pajarito"),
    Animal("ardilla", "🐿️", "Ardilla"),
    Animal("ciervo", "🦌", "Ciervo"),
    Animal("jabali", "🐗", "Jabalí"),
    Animal("zorro", "🦊", "Zorro"),
    Animal("conejo", "🐇", "Conejo"),
    Animal("tortuga", "🐢", "Tortuga"),
    Animal("rana", "🐸", "Rana"),
    Animal("lagartija", "🦎", "Lagartija"),
    Animal("mariposa", "🦋", "Mariposa"),
    Animal("pez", "🐟", "Pez"),
    Animal("cabra", "🐐", "Cabra"),
    Animal("vaca", "🐄", "Vaca"),
    Animal("caballo", "🐎", "Caballo"),
    Animal("cerdo", "🐖", "Cerdo ibérico"),
)

data class Momento(val id: String, val emoji: String, val texto: String)

val MOMENTOS: List<Momento> = listOf(
    Momento("siesta", "😴", "Siesta en la mochila"),
    Momento("solo", "👣", "Caminó un tramo solito"),
    Momento("agua", "💦", "Jugó con agua"),
    Momento("lluvia", "🌧️", "Nos llovió"),
    Momento("madrugada", "🌅", "Salimos antes de las 8"),
    Momento("mochila", "🎒", "Mochila completa"),
)

fun animal(id: String): Animal? = ANIMALES.firstOrNull { it.id == id }
fun momento(id: String): Momento? = MOMENTOS.firstOrNull { it.id == id }

fun cargarSalidas(context: Context): List<Salida> {
    val prefs = context.getSharedPreferences("senderismo", Context.MODE_PRIVATE)
    val raw = prefs.getString("salidas", null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Salida(
                id = o.getString("id"),
                rutaId = o.getString("rutaId"),
                fecha = o.getString("fecha"),
                estrellasPapa = o.optInt("papa", 0),
                estrellasMama = o.optInt("mama", 0),
                estrellasPeque = o.optInt("peque", 0),
                animales = o.optJSONArray("animales").aConjunto(),
                momentos = o.optJSONArray("momentos").aConjunto(),
                nota = o.optString("nota", ""),
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun guardarSalidas(context: Context, salidas: List<Salida>) {
    val arr = JSONArray()
    salidas.forEach { s ->
        val o = JSONObject()
        o.put("id", s.id)
        o.put("rutaId", s.rutaId)
        o.put("fecha", s.fecha)
        o.put("papa", s.estrellasPapa)
        o.put("mama", s.estrellasMama)
        o.put("peque", s.estrellasPeque)
        o.put("animales", JSONArray(s.animales.toList()))
        o.put("momentos", JSONArray(s.momentos.toList()))
        o.put("nota", s.nota)
        arr.put(o)
    }
    context.getSharedPreferences("senderismo", Context.MODE_PRIVATE)
        .edit().putString("salidas", arr.toString()).apply()
}

private fun JSONArray?.aConjunto(): Set<String> {
    if (this == null) return emptySet()
    return (0 until length()).map { getString(it) }.toSet()
}
