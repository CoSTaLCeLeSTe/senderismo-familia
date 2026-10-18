package es.costalceleste.senderismo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(applicationContext)
        val salidas = cargarSalidas(applicationContext)
        setContent {
            SenderismoTheme {
                App(
                    store = store,
                    salidasIniciales = salidas,
                    guardarSalidas = { guardarSalidas(applicationContext, it) },
                )
            }
        }
    }
}
