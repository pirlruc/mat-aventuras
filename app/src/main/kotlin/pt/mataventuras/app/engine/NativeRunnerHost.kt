package pt.mataventuras.app.engine

import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.engine.EngineInputMap
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
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
        frameLimit: Int = Int.MAX_VALUE,
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
            RunnerPlay(loop, level, mascot, frameLimit) { ok -> activity.completeReward(ok) }
        }
        return loop
    }

    /**
     * Canvas runner. [frameLimit] stops the clock in unit tests.
     */
    @Composable
    internal fun RunnerPlay(
        loop: Platformer2dLoop,
        level: pt.mataventuras.domain.engine.PlatformerLevel,
        mascot: Mascot,
        frameLimit: Int,
        onDone: (Boolean) -> Unit,
    ) {
            var state by remember { mutableStateOf(loop.state) }
            var done by remember { mutableStateOf(false) }
            val tiles = remember { ArrayList<PlatformerRect>(64) }
            LaunchedEffect(Unit) {
                var frames = 0
                while (runnerKeepsTicking(loop.state.finished, loop.state.alive, frames, frameLimit)) {
                    withFrameNanos {
                        loop.tick()
                        state = loop.state
                    }
                    frames += 1
                }
                if (!done) {
                    done = true
                    onDone(loop.state.finished)
                }
            }
            BackHandler(enabled = !done) {
                done = true
                onDone(false)
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag("runner-course")
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    var lastX = down.position.x
                                    var lastY = down.position.y
                                    var flickX = 0f
                                    var flickY = 0f
                                    val width = size.width.coerceAtLeast(1).toFloat()
                                    loop.moveX = EngineInputMap.runFromNormalizedX(down.position.x / width)
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) {
                                            change.consume()
                                            return@awaitEachGesture
                                        }
                                        val dx = change.position.x - lastX
                                        val dy = change.position.y - lastY
                                        lastX = change.position.x
                                        lastY = change.position.y
                                        flickX += dx
                                        flickY += dy
                                        loop.moveX = EngineInputMap.runFromNormalizedX(change.position.x / width)
                                        noteRunnerJump(loop, flickX, flickY)
                                        change.consume()
                                    }
                                    loop.moveX = 0f
                                }
                            },
                ) {
                    paintRunnerCourse(tiles, state, mascot, level)
                }
                Text(
                    text = "${VoiceScripts.JUMP_HINT}\nMoedas ${state.rings}/${state.ringsTarget}",
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .background(Color(0x99000000), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
    }
}

internal fun runnerKeepsTicking(
    finished: Boolean,
    alive: Boolean,
    frames: Int,
    limit: Int,
): Boolean = !finished && alive && frames < limit

internal fun noteRunnerJump(
    loop: Platformer2dLoop,
    flickX: Float,
    flickY: Float,
) {
    if (EngineInputMap.isJumpFlick(flickX, flickY)) loop.jumping = true
}

internal fun DrawScope.paintRunnerCourse(
    tiles: MutableList<PlatformerRect>,
    state: pt.mataventuras.domain.engine.Platformer2dState,
    mascot: Mascot,
    level: pt.mataventuras.domain.engine.PlatformerLevel,
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
