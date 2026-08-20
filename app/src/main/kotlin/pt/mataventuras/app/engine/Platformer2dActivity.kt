package pt.mataventuras.app.engine

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
import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.model.Mascot

/**
 * Age-3 2D reward. Compose Canvas host; no Unity/Godot.
 */
class Platformer2dActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascot = Mascot.fromCode(intent.getStringExtra(EngineLauncher.EXTRA_MASCOT) ?: "")
        setContent {
            val simulation = remember { Platformer2dEngine() }
            var state by remember { mutableStateOf(simulation.initial(ringsTarget = 5)) }
            var jumping by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                var last = 0L
                while (!state.finished) {
                    withFrameNanos { now ->
                        if (last == 0L) last = now
                        val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                        last = now
                        state = simulation.step(state, dt, jumping)
                        jumping = false
                        val ring = (state.x / 8f).toInt() * 8f + 6f
                        state = simulation.collect(state, ring)
                    }
                }
                setResult(
                    RESULT_OK,
                    android.content.Intent().putExtra(EngineLauncher.RESULT_FINISHED, true),
                )
                finish()
            }
            BackHandler {
                setResult(RESULT_CANCELED)
                finish()
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { jumping = true } },
            ) {
                drawRect(Color(0xFF81D4FA))
                drawRect(
                    Color(0xFF66BB6A),
                    topLeft = Offset(0f, size.height * 0.75f),
                    size = size.copy(height = size.height * 0.25f),
                )
                val px = (state.x * 40f) % (size.width + 80f)
                val py = size.height * 0.75f - state.y * 12f - 40f
                drawCircle(Color(mascot.primaryArgb), radius = 36f, center = Offset(px, py))
                repeat(5) { i ->
                    val ax = ((i * 180f) - (state.x * 20f) + size.width * 4) % size.width
                    drawCircle(Color(0xFFFFD54F), radius = 18f, center = Offset(ax, size.height * 0.55f))
                }
            }
        }
    }
}
