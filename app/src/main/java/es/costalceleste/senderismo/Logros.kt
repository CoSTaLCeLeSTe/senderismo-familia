package es.costalceleste.senderismo

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

enum class Categoria(val label: String, val emoji: String, val color1: Long, val color2: Long) {
    PRIMEROS("Primeros pasos", "🥾", 0xFF8BC34A, 0xFF33691E),
    KILOMETROS("Kilómetros", "👣", 0xFFFFB74D, 0xFFE65100),
    EXPLORADOR("Exploradores", "🧭", 0xFF4FC3F7, 0xFF01579B),
    PAISAJES("Paisajes", "🏞️", 0xFF81C784, 0xFF1B5E20),
    ANIMALES("Animales", "🦉", 0xFFA1887F, 0xFF4E342E),
    ESTACIONES("Estaciones", "🍂", 0xFFFFD54F, 0xFFBF360C),
    PEQUE("El peque", "👶", 0xFFF48FB1, 0xFF880E4F),
    CONSTANCIA("Constancia", "🔥", 0xFFFF8A65, 0xFFB71C1C),
    ESPECIALES("Especiales", "✨", 0xFFB39DDB, 0xFF311B92),
}

class Logro(
    val id: String,
    val emoji: String,
    val nombre: String,
    val descripcion: String,
    val categoria: Categoria,
    val objetivo: Int,
    val valor: (Estadisticas) -> Int,
) {
    fun progreso(e: Estadisticas): Int = valor(e).coerceAtMost(objetivo)
    fun conseguido(e: Estadisticas): Boolean = valor(e) >= objetivo
}

/** Todo lo que se puede contar a partir del diario. */
class Estadisticas(val salidas: List<Salida>) {
    private val fechas: List<LocalDate> = salidas.mapNotNull { it.dia() }
    private val rutasHechas: List<Ruta> = salidas.mapNotNull { it.ruta() }

    val total = salidas.size
    val km: Double = rutasHechas.sumOf { it.km }
    val distintas: Set<String> = salidas.map { it.rutaId }.toSet()
    private val rutasDistintas = RUTAS.filter { it.id in distintas }
    val animales: Set<String> = salidas.flatMap { it.animales }.toSet()

    fun hechasEn(p: Provincia) = rutasDistintas.count { it.provincia == p }
    fun totalEn(p: Provincia) = RUTAS.count { it.provincia == p }
    fun entorno(e: Entorno) = rutasDistintas.count { it.entorno == e }
    val entornosDistintos = rutasDistintas.map { it.entorno }.toSet().size
    fun vio(a: String) = if (a in animales) 1 else 0
    fun momento(m: String) = salidas.count { m in it.momentos }
    val estaciones: Set<String> = fechas.map { estacion(it) }.toSet()
    fun en(estacion: String) = if (estacion in estaciones) 1 else 0
    val amarillas = rutasDistintas.count { it.semaforo == Semaforo.AMARILLO }
    val mesesDistintos = fechas.map { YearMonth.from(it) }.toSet().size
    val maxEnUnMes = fechas.groupingBy { YearMonth.from(it) }.eachCount().values.maxOrNull() ?: 0
    val maxRepeticiones = salidas.groupingBy { it.rutaId }.eachCount().values.maxOrNull() ?: 0
    val pequeCinco = salidas.count { it.estrellasPeque == 5 }
    val familiaCinco = salidas.count { it.estrellasPapa == 5 && it.estrellasMama == 5 && it.estrellasPeque == 5 }
    val notas = salidas.count { it.nota.isNotBlank() }
    val granDia = rutasHechas.count { it.km >= 10.0 }

    /** Semanas seguidas (lunes a domingo) con al menos una salida. */
    val rachaSemanas: Int = run {
        val semanas = fechas.map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }.toSortedSet().toList()
        var mejor = 0
        var actual = 0
        var anterior: LocalDate? = null
        for (s in semanas) {
            actual = if (anterior != null && anterior.plusWeeks(1) == s) actual + 1 else 1
            if (actual > mejor) mejor = actual
            anterior = s
        }
        mejor
    }

    val mediaPeque: Double = salidas.filter { it.estrellasPeque > 0 }.map { it.estrellasPeque }.average().let { if (it.isNaN()) 0.0 else it }
}

fun estacion(d: LocalDate): String = when (d.monthValue) {
    3, 4, 5 -> "primavera"
    6, 7, 8 -> "verano"
    9, 10, 11 -> "otono"
    else -> "invierno"
}

val LOGROS: List<Logro> = buildList {
    val P = Categoria.PRIMEROS
    add(Logro("salidas-1", "🥾", "Primeros pasos", "Haced vuestra primera ruta en familia.", P, 1) { it.total })
    add(Logro("salidas-3", "🌱", "Brotando", "Completad 3 salidas.", P, 3) { it.total })
    add(Logro("salidas-5", "🌿", "Echando raíces", "Completad 5 salidas.", P, 5) { it.total })
    add(Logro("salidas-10", "🌳", "Familia árbol", "Completad 10 salidas.", P, 10) { it.total })
    add(Logro("salidas-25", "🌲", "Bosque entero", "Completad 25 salidas.", P, 25) { it.total })
    add(Logro("salidas-50", "🏔️", "Leyendas del sendero", "Completad 50 salidas.", P, 50) { it.total })

    val K = Categoria.KILOMETROS
    add(Logro("km-5", "👟", "Calentando motores", "Sumad 5 km caminando.", K, 5) { it.km.toInt() })
    add(Logro("km-10", "🥾", "Diez de diez", "Sumad 10 km.", K, 10) { it.km.toInt() })
    add(Logro("km-21", "🏃", "Media maratón familiar", "Sumad 21 km.", K, 21) { it.km.toInt() })
    add(Logro("km-42", "🏅", "Maratón familiar", "Sumad 42 km.", K, 42) { it.km.toInt() })
    add(Logro("km-100", "💯", "Cien kilómetros", "Sumad 100 km.", K, 100) { it.km.toInt() })
    add(Logro("km-250", "🌍", "Vuelta a Andalucía", "Sumad 250 km a pie.", K, 250) { it.km.toInt() })

    val E = Categoria.EXPLORADOR
    add(Logro("distintas-3", "🧭", "Curiosos", "Haced 3 rutas distintas.", E, 3) { it.distintas.size })
    add(Logro("distintas-8", "🗺️", "Exploradores", "Haced 8 rutas distintas.", E, 8) { it.distintas.size })
    add(Logro("distintas-todas", "👑", "Todas las rutas", "Haced todas las rutas del catálogo.", E, RUTAS.size) { it.distintas.size })
    add(Logro("prov-sevilla", "🍊", "Debut en Sevilla", "Primera ruta en la provincia de Sevilla.", E, 1) { it.hechasEn(Provincia.SEVILLA) })
    add(Logro("prov-cadiz", "⚓", "Debut en Cádiz", "Primera ruta en la provincia de Cádiz.", E, 1) { it.hechasEn(Provincia.CADIZ) })
    add(Logro("prov-huelva", "🍓", "Debut en Huelva", "Primera ruta en la provincia de Huelva.", E, 1) { it.hechasEn(Provincia.HUELVA) })
    add(Logro("prov-tres", "🗺️", "Tres provincias", "Caminad en Sevilla, Cádiz y Huelva.", E, 3) { e -> Provincia.entries.count { e.hechasEn(it) > 0 } })
    add(Logro("completa-sevilla", "🏅", "Sevilla al completo", "Todas las rutas de Sevilla.", E, RUTAS.count { it.provincia == Provincia.SEVILLA }) { it.hechasEn(Provincia.SEVILLA) })
    add(Logro("completa-cadiz", "🏅", "Cádiz al completo", "Todas las rutas de Cádiz.", E, RUTAS.count { it.provincia == Provincia.CADIZ }) { it.hechasEn(Provincia.CADIZ) })
    add(Logro("completa-huelva", "🏅", "Huelva al completo", "Todas las rutas de Huelva.", E, RUTAS.count { it.provincia == Provincia.HUELVA }) { it.hechasEn(Provincia.HUELVA) })

    val PA = Categoria.PAISAJES
    add(Logro("rio-3", "💧", "Amigos del río", "Haced 3 rutas de río distintas.", PA, 3) { it.entorno(Entorno.RIO) })
    add(Logro("costa-2", "🌊", "Lobos de mar", "Haced las 2 rutas de costa.", PA, 2) { it.entorno(Entorno.COSTA) })
    add(Logro("humedal-3", "🦆", "Reyes de la marisma", "Haced 3 rutas de marisma o laguna.", PA, 3) { it.entorno(Entorno.HUMEDAL) })
    add(Logro("bosque-1", "🌲", "Hijos del bosque", "Haced una ruta de bosque.", PA, 1) { it.entorno(Entorno.BOSQUE) })
    add(Logro("via-2", "🚂", "Tren fantasma", "Recorred las 2 vías verdes.", PA, 2) { it.entorno(Entorno.VIA_VERDE) })
    add(Logro("sierra-1", "⛰️", "Roca viva", "Haced una ruta de sierra.", PA, 1) { it.entorno(Entorno.SIERRA) })
    add(Logro("paisajes-todos", "🌈", "Todos los paisajes", "Río, costa, sierra, marisma, bosque y vía verde.", PA, Entorno.entries.size) { it.entornosDistintos })

    val A = Categoria.ANIMALES
    add(Logro("animales-1", "🔭", "Primer avistamiento", "Apuntad el primer animal que veáis.", A, 1) { it.animales.size })
    add(Logro("animales-5", "🦋", "Naturalistas", "Ved 5 especies distintas.", A, 5) { it.animales.size })
    add(Logro("animales-10", "🛶", "Arca de Noé", "Ved 10 especies distintas.", A, 10) { it.animales.size })
    add(Logro("animales-todos", "📚", "Enciclopedia viviente", "Ved todas las especies de la lista.", A, ANIMALES.size) { it.animales.size })
    add(Logro("ver-buitre", "🦅", "Señores del cielo", "Ved un buitre leonado.", A, 1) { it.vio("buitre") })
    add(Logro("ver-flamenco", "🦩", "Todo de rosa", "Ved flamencos.", A, 1) { it.vio("flamenco") })
    add(Logro("ver-ciguena", "🪺", "Vecinos del campanario", "Ved una cigüeña.", A, 1) { it.vio("ciguena") })
    add(Logro("ver-jabali", "🐗", "Bosque salvaje", "Ved un jabalí (¡de lejos!).", A, 1) { it.vio("jabali") })
    add(Logro("ver-mariposa", "🦋", "Cazamariposas", "Ved una mariposa.", A, 1) { it.vio("mariposa") })

    val EST = Categoria.ESTACIONES
    add(Logro("primavera", "🌸", "Primavera en flor", "Una ruta en primavera.", EST, 1) { it.en("primavera") })
    add(Logro("verano", "☀️", "Verano valiente", "Una ruta en verano.", EST, 1) { it.en("verano") })
    add(Logro("otono", "🍂", "Hojas de otoño", "Una ruta en otoño.", EST, 1) { it.en("otono") })
    add(Logro("invierno", "❄️", "Abrigaditos", "Una ruta en invierno.", EST, 1) { it.en("invierno") })
    add(Logro("estaciones-4", "🔄", "Todo el año", "Caminad en las cuatro estaciones.", EST, 4) { it.estaciones.size })

    val PQ = Categoria.PEQUE
    add(Logro("siesta-1", "😴", "Siesta en la mochila", "El peque se duerme durante la ruta.", PQ, 1) { it.momento("siesta") })
    add(Logro("siesta-5", "💤", "Dormilón profesional", "5 siestas en ruta.", PQ, 5) { it.momento("siesta") })
    add(Logro("solo-1", "👣", "Primeros pasitos", "Camina un tramo solito.", PQ, 1) { it.momento("solo") })
    add(Logro("solo-5", "🦶", "Pies de explorador", "Camina solito en 5 salidas.", PQ, 5) { it.momento("solo") })
    add(Logro("agua-1", "💦", "Chapoteo", "Juega con agua en una ruta.", PQ, 1) { it.momento("agua") })
    add(Logro("peque-5", "😄", "Peque feliz", "El peque le da 5 estrellas a una ruta.", PQ, 1) { it.pequeCinco })
    add(Logro("peque-5x5", "🤩", "Fan del monte", "5 rutas con 5 estrellas del peque.", PQ, 5) { it.pequeCinco })

    val CO = Categoria.CONSTANCIA
    add(Logro("racha-2", "🔥", "Dos findes seguidos", "Salid dos semanas seguidas.", CO, 2) { it.rachaSemanas })
    add(Logro("racha-4", "🔥", "Racha de un mes", "Salid cuatro semanas seguidas.", CO, 4) { it.rachaSemanas })
    add(Logro("mes-4", "📆", "Mes aventurero", "4 salidas en un mismo mes.", CO, 4) { it.maxEnUnMes })
    add(Logro("meses-6", "🗓️", "Medio año en ruta", "Salidas en 6 meses distintos.", CO, 6) { it.mesesDistintos })
    add(Logro("repetir-3", "🔁", "Nuestra ruta favorita", "Repetid la misma ruta 3 veces.", CO, 3) { it.maxRepeticiones })

    val SP = Categoria.ESPECIALES
    add(Logro("madrugada", "🌅", "Madrugadores", "Salid antes de las 8 de la mañana.", SP, 1) { it.momento("madrugada") })
    add(Logro("lluvia", "🌧️", "Valientes de la lluvia", "Terminad una ruta aunque llueva.", SP, 1) { it.momento("lluvia") })
    add(Logro("mochila-1", "🎒", "Mochila perfecta", "Salid con toda la lista de la mochila marcada.", SP, 1) { it.momento("mochila") })
    add(Logro("mochila-5", "🧳", "Expertos en mochilas", "5 salidas con la mochila completa.", SP, 5) { it.momento("mochila") })
    add(Logro("amarilla-1", "🟡", "Primer reto", "Haced una ruta de semáforo amarillo.", SP, 1) { it.amarillas })
    add(Logro("amarilla-3", "💪", "Retos superados", "Haced 3 rutas amarillas distintas.", SP, 3) { it.amarillas })
    add(Logro("familia-5", "⭐", "Familia cinco estrellas", "Los tres dais 5 estrellas a una ruta.", SP, 1) { it.familiaCinco })
    add(Logro("notas-5", "✍️", "Cronistas", "Escribid una nota en 5 salidas.", SP, 5) { it.notas })
    add(Logro("gran-dia", "🏆", "Gran día", "Haced una ruta de 10 km o más.", SP, 1) { it.granDia })
}

data class Nivel(val desde: Int, val emoji: String, val nombre: String)

val NIVELES: List<Nivel> = listOf(
    Nivel(0, "🌰", "Semilla"),
    Nivel(3, "🌱", "Brote"),
    Nivel(7, "🌿", "Matorral"),
    Nivel(12, "🌳", "Arbolito"),
    Nivel(18, "🐿️", "Ardillas"),
    Nivel(25, "🦊", "Zorros"),
    Nivel(33, "🦌", "Ciervos"),
    Nivel(42, "🦅", "Águilas"),
    Nivel(52, "🐾", "Linces de Doñana"),
    Nivel(LOGROS.size, "👑", "Guardianes del bosque"),
)

fun nivelPara(conseguidos: Int): Pair<Int, Nivel> {
    val i = NIVELES.indexOfLast { conseguidos >= it.desde }.coerceAtLeast(0)
    return i to NIVELES[i]
}

fun logrosConseguidos(salidas: List<Salida>): Set<String> {
    val e = Estadisticas(salidas)
    return LOGROS.filter { it.conseguido(e) }.map { it.id }.toSet()
}
