package es.costalceleste.senderismo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Capturas de las pantallas para revisar el diseño. Solo se ejecutan en GitHub Actions
 * (el script ci/capturas.sh activa Paparazzi).
 */
class CapturasTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    private val hoy = LocalDate.now()

    private val ejemplo = listOf(
        Salida(
            id = "1", rutaId = "torunos", fecha = hoy.minusDays(1).toString(),
            estrellasPapa = 5, estrellasMama = 4, estrellasPeque = 5,
            animales = setOf("flamenco", "pato", "garza", "pajarito"),
            momentos = setOf("siesta", "mochila"),
            nota = "Se durmió en la mochila justo al llegar a la pasarela.",
        ),
        Salida(
            id = "2", rutaId = "cascadas-hueznar", fecha = hoy.minusDays(8).toString(),
            estrellasPapa = 4, estrellasMama = 5, estrellasPeque = 5,
            animales = setOf("pez", "mariposa"),
            momentos = setOf("agua", "solo"),
        ),
        Salida(
            id = "3", rutaId = "dehesa-abajo", fecha = hoy.minusDays(15).toString(),
            estrellasPapa = 4, estrellasMama = 4, estrellasPeque = 3,
            animales = setOf("ciguena", "caballo"),
            momentos = setOf("madrugada"),
        ),
    )

    private fun captura(content: @Composable () -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(AnimacionesActivas provides false) {
                SenderismoTheme { FondoNaturaleza { content() } }
            }
        }
    }

    @Test
    fun rutas() = captura { RutasScreen(salidas = ejemplo, onOpen = {}) }

    @Test
    fun ficha() = captura {
        FichaScreen(
            ruta = RUTAS.first { it.id == "majaceite" },
            salidas = emptyList(),
            onBack = {}, onPlanificar = {}, onRegistrar = {},
        )
    }

    @Test
    fun plan() = captura {
        PlanScreen(
            plan = Plan("majaceite", hoy.plusDays(3).toString(), Base.PUERTO, 9 * 60, 60, setOf("🎒 Mochila portabebés", "💧 Agua (1,5 L por adulto)")),
            onChange = {}, onIrARutas = {}, onHecha = {},
        )
    }

    @Test
    fun sinPlan() = captura { PlanScreen(plan = null, onChange = {}, onIrARutas = {}, onHecha = {}) }

    @Test
    fun diario() = captura { DiarioScreen(salidas = ejemplo, onNueva = {}, onEditar = {}, onBorrar = {}) }

    @Test
    fun diarioVacio() = captura { DiarioScreen(salidas = emptyList(), onNueva = {}, onEditar = {}, onBorrar = {}) }

    @Test
    fun logros() = captura { LogrosScreen(salidas = ejemplo) }

    @Test
    fun registro() = captura { RegistroScreen(borrador = ejemplo[0], onChange = {}, onGuardar = {}, onCancelar = {}) }

    @Test
    fun celebracion() = captura { Celebracion(LOGROS.take(3)) {} }

    @Test
    fun appCompleta() = captura {
        App(store = Store(paparazzi.context), salidasIniciales = ejemplo, guardarSalidas = {})
    }
}
