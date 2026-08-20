package pt.mataventuras.app.motor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.motor.MotorPlataforma2D

/**
 * Side-scroller 2D (prémio dos 3 anos). Motor em Compose Canvas; sem Unity/Godot.
 */
class AtividadeMotor2D : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascote = Mascote.deCodigo(intent.getStringExtra(LancadorMotor.EXTRA_MASCOTE) ?: "")
        setContent {
            val simulacao = remember { MotorPlataforma2D() }
            var estado by remember { mutableStateOf(simulacao.inicial(aneisAlvo = 5)) }
            var saltar by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                var ultimo = 0L
                while (!estado.concluido) {
                    withFrameNanos { agora ->
                        if (ultimo == 0L) ultimo = agora
                        val dt = ((agora - ultimo) / 1_000_000_000f).coerceAtMost(0.05f)
                        ultimo = agora
                        estado = simulacao.passo(estado, dt, saltar)
                        saltar = false
                        val anel = (estado.x / 8f).toInt() * 8f + 6f
                        estado = simulacao.recolher(estado, anel)
                    }
                }
                setResult(RESULT_OK, android.content.Intent().putExtra(LancadorMotor.RESULTADO_CONCLUIDO, true))
                finish()
            }
            BackHandler {
                setResult(RESULT_CANCELED)
                finish()
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { saltar = true } },
            ) {
                drawRect(Color(0xFF81D4FA))
                drawRect(Color(0xFF66BB6A), topLeft = Offset(0f, size.height * 0.75f), size = size.copy(height = size.height * 0.25f))
                val px = (estado.x * 40f) % (size.width + 80f)
                val py = size.height * 0.75f - estado.y * 12f - 40f
                drawCircle(Color(mascote.corPrincipalArgb), radius = 36f, center = Offset(px, py))
                repeat(5) { i ->
                    val ax = ((i * 180f) - (estado.x * 20f) + size.width * 4) % size.width
                    drawCircle(Color(0xFFFFD54F), radius = 18f, center = Offset(ax, size.height * 0.55f))
                }
            }
        }
    }
}
