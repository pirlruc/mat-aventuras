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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.domain.engine.KartHud
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Attaches the native Compose Canvas off-road racer to an isolated Activity.
 */
internal object NativeKartHost {
    /**
     * Starts the 2.5D dirt circuit. [showUi] is false under Robolectric so the
     * Compose frame clock does not keep Espresso busy.
     */
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): OffroadRacerLoop {
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
            var state by remember { mutableStateOf(loop.state) }
            var done by remember { mutableStateOf(false) }
            val spans = remember { ArrayList<OffroadSpan>(96) }
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
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
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
                                            break
                                        }
                                        val next = change.position.x / size.width.coerceAtLeast(1).toFloat()
                                        loop.handleTouch(next, MotionEvent.ACTION_MOVE)
                                        change.consume()
                                    }
                                }
                            },
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
                val extra = KartHud.offTrackLabel(state) ?: KartHud.boostLabel(state)
                val place = KartHud.placeLabel(
                    pt.mataventuras.domain.engine.RivalPack.place(state, circuit.length),
                    pt.mataventuras.domain.engine.RivalPack.fieldSize(state),
                )
                Text(
                    text = listOfNotNull(
                        KartHud.CONTROL_HINT,
                        KartHud.META_HINT,
                        KartHud.lapLabel(state),
                        place,
                        KartHud.gatesLabel(state),
                        extra,
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
        return loop
    }

    /**
     * Overlay lines for tests that still call the GLES helper.
     */
    fun hudLines(loop: OffroadRacerLoop): Pair<String, String> {
        val extra = KartHud.offTrackLabel(loop.state) ?: KartHud.boostLabel(loop.state)
        val place =
            KartHud.placeLabel(
                pt.mataventuras.domain.engine.RivalPack.place(loop.state, loop.circuit.length),
                pt.mataventuras.domain.engine.RivalPack.fieldSize(loop.state),
            )
        val second = listOfNotNull(place, KartHud.gatesLabel(loop.state), extra).joinToString(" · ")
        return KartHud.lapLabel(loop.state) to second
    }
}
