package pt.mataventuras.app.engine

import android.os.Bundle
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Age-3 2D reward. Compose Canvas host; no Unity/Godot.
 */
class Platformer2dActivity : IsolatedEngineActivity() {
    internal val loop = Platformer2dLoop()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascot = launchMascot()
        setContent {
            var state by remember { mutableStateOf(loop.state) }
            LaunchedEffect(Unit) {
                while (!loop.state.finished) {
                    withFrameNanos {
                        loop.tick()
                        state = loop.state
                    }
                }
                completeReward(ok = true)
            }
            BackHandler { completeReward(ok = false) }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { loop.jumping = true } },
            ) {
                drawRect(Color(0xFF81D4FA))
                val groundY = PlatformerScene.groundTop(size.height)
                drawRect(
                    Color(0xFF66BB6A),
                    topLeft = Offset(0f, groundY),
                    size = Size(size.width, size.height - groundY),
                )
                PlatformerScene.sprites(state, mascot, size.width, size.height).forEach { sprite ->
                    drawCircle(
                        Color(sprite.argb),
                        radius = sprite.radius,
                        center = Offset(sprite.x, sprite.y),
                    )
                }
            }
        }
    }
}
