package pt.mataventuras.app.engine

import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Attaches the native Compose Canvas runner to an isolated Activity.
 */
internal object NativeRunnerHost {
    /**
     * Starts the 2D ring loop. [showUi] is false under Robolectric so the
     * Compose frame clock does not keep Espresso busy.
     */
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): Platformer2dLoop {
        val loop = Platformer2dLoop()
        if (!showUi) {
            activity.setContentView(
                TextView(activity).apply { text = VoiceScripts.STEER_HINT },
            )
            return loop
        }
        val mascot = activity.launchMascot()
        activity.setContent {
            var state by remember { mutableStateOf(loop.state) }
            var done by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                while (!loop.state.finished) {
                    withFrameNanos {
                        loop.tick()
                        state = loop.state
                    }
                }
                if (!done) {
                    done = true
                    activity.completeReward(ok = true)
                }
            }
            BackHandler(enabled = !done) {
                done = true
                activity.completeReward(ok = false)
            }
            Canvas(
                modifier =
                    Modifier
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
        return loop
    }
}
