package pt.mataventuras.app.engine

import android.view.MotionEvent
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
import pt.mataventuras.app.prize.PrizeLimit
import pt.mataventuras.domain.engine.KartHud
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Attaches the native Compose Canvas off-road racer to an isolated Activity.
 */
internal object NativeKartHost {
    /**
     * Starts the 2.5D dirt circuit. [showUi] is false under Robolectric so the
     * Compose frame clock does not keep Espresso busy.
     */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
        frameLimit: Int = Int.MAX_VALUE,
    ): OffroadRacerLoop {
        PrizeLimit.arm(activity)
        val seed = kotlin.math.abs(activity.mascotCode().hashCode() * 31 + activity.childName().hashCode())
        val circuit = OffroadCircuit(if (seed == 0) 7 else seed)
        val loop = OffroadRacerLoop(circuit = circuit)
        if (!showUi) {
            activity.setContentView(
                TextView(activity).apply { text = VoiceScripts.STEER_HINT },
            )
            return loop
        }
        val mascot = activity.launchMascot()
        activity.setContent {
            KartPlay(loop, circuit, mascot, frameLimit) { ok -> activity.completeReward(ok) }
        }
        return loop
    }

    /**
     * Canvas race. [frameLimit] stops the clock in unit tests.
     */
    @Composable
    internal fun KartPlay(
        loop: OffroadRacerLoop,
        circuit: OffroadCircuit,
        mascot: Mascot,
        frameLimit: Int,
        onDone: (Boolean) -> Unit,
    ) {
        var state by remember { mutableStateOf(loop.state) }
        var done by remember { mutableStateOf(false) }
        val spans = remember { ArrayList<OffroadSpan>(96) }
        LaunchedEffect(Unit) {
            var frames = 0
            while (kartKeepsTicking(loop.state.finished, frames, frameLimit)) {
                withFrameNanos {
                    loop.tick()
                    state = loop.state
                }
                frames += 1
            }
            if (!done) {
                done = true
                onDone(true)
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
                        .testTag("kart-course")
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                val nx = down.position.x / size.width.coerceAtLeast(1).toFloat()
                                loop.handleTouch(nx, MotionEvent.ACTION_DOWN)
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        loop.handleTouch(nx, MotionEvent.ACTION_UP)
                                        change.consume()
                                        return@awaitEachGesture
                                    }
                                    val next = change.position.x / size.width.coerceAtLeast(1).toFloat()
                                    loop.handleTouch(next, MotionEvent.ACTION_MOVE)
                                    change.consume()
                                }
                            }
                        },
            ) {
                paintKartCourse(spans, state, circuit, mascot)
            }
            val hud = KartHud.raceOverlay(state, circuit.length)
            Text(
                text =
                    listOf(
                        KartHud.CONTROL_HINT,
                        KartHud.META_HINT,
                        hud.first,
                        hud.second,
                    ).joinToString("\n"),
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

    /**
     * Overlay lines for tests and HUD helpers.
     */
    fun hudLines(loop: OffroadRacerLoop): Pair<String, String> = KartHud.raceOverlay(loop.state, loop.circuit.length)
}

internal fun kartKeepsTicking(
    finished: Boolean,
    frames: Int,
    limit: Int,
): Boolean = !finished && frames < limit

internal fun DrawScope.paintKartCourse(
    spans: MutableList<OffroadSpan>,
    state: pt.mataventuras.domain.engine.OffroadState,
    circuit: OffroadCircuit,
    mascot: Mascot,
) {
    drawRect(Color(OffroadScene.skyArgb(circuit)))
    OffroadScene.fill(spans, state, circuit, mascot, size.width, size.height)
    spans.forEach { span ->
        drawRect(
            Color(span.argb),
            topLeft = Offset(span.x, span.y),
            size = Size(span.w, span.h),
        )
    }
}
