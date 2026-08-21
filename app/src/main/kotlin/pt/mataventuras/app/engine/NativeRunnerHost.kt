package pt.mataventuras.app.engine

import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.engine.PlatformerWorld
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Attaches the native Compose Canvas runner to an isolated Activity.
 */
internal object NativeRunnerHost {
    /**
     * Starts the 2D coin platformer. [showUi] is false under Robolectric so the
     * Compose frame clock does not keep Espresso busy.
     */
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): Platformer2dLoop {
        val seed = kotlin.math.abs(activity.mascotCode().hashCode() + activity.childName().hashCode())
        val level = PlatformerWorld.random(seed)
        val loop =
            Platformer2dLoop(
                engine = Platformer2dEngine(level = level),
                level = level,
                ringsTarget = level.coins.size,
            )
        if (!showUi) {
            activity.setContentView(
                TextView(activity).apply { text = VoiceScripts.JUMP_HINT },
            )
            return loop
        }
        val mascot = activity.launchMascot()
        activity.setContent {
            var state by remember { mutableStateOf(loop.state) }
            var done by remember { mutableStateOf(false) }
            val tiles = remember { ArrayList<PlatformerRect>(64) }
            LaunchedEffect(Unit) {
                while (!loop.state.finished && loop.state.alive) {
                    withFrameNanos {
                        loop.tick()
                        state = loop.state
                    }
                }
                if (!done) {
                    done = true
                    activity.completeReward(ok = loop.state.finished)
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
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                var lastX = down.position.x
                                loop.moveX = 0f
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    val dx = change.position.x - lastX
                                    lastX = change.position.x
                                    loop.moveX = (dx / 12f).coerceIn(-1f, 1f)
                                    if (dx > 14f) loop.jumping = true
                                    change.consume()
                                }
                                loop.moveX = 0f
                            }
                        },
            ) {
                drawRect(Color(level.skyArgb))
                drawRect(
                    Color(level.skyBandArgb),
                    topLeft = Offset(0f, size.height * 0.45f),
                    size = Size(size.width, size.height * 0.3f),
                )
                PlatformerScene.fillTiles(tiles, state, mascot, size.width, size.height, level)
                tiles.forEach { tile ->
                    drawRect(
                        Color(tile.argb),
                        topLeft = Offset(tile.x, tile.y),
                        size = Size(tile.w, tile.h),
                    )
                }
            }
        }
        return loop
    }
}
