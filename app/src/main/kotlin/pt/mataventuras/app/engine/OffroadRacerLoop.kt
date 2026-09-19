package pt.mataventuras.app.engine

import android.view.MotionEvent
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.engine.OffroadRacerEngine
import pt.mataventuras.domain.engine.OffroadState

/**
 * Frame loop for the age-7 Canvas off-road racer.
 */
internal class OffroadRacerLoop(
    val circuit: OffroadCircuit = OffroadCircuit(1),
    private val engine: OffroadRacerEngine = OffroadRacerEngine(circuit),
    lapsTarget: Int = 3,
    nowNs: () -> Long = { System.nanoTime() },
) {
    /** Latest simulation snapshot. */
    var state: OffroadState = engine.initial(lapsTarget = lapsTarget)
        private set

    private val clock = FrameClock(nowNs)
    private var steer: Float = 0f
    private var boost: Boolean = false

    /**
     * Maps a normalised tap to steer / boost. [action] is a [MotionEvent] action.
     */
    fun handleTouch(
        normalizedX: Float,
        action: Int,
    ): Boolean {
        steer = Kart3dInput.steerFromTouch(normalizedX)
        if (Kart3dInput.boostOnAction(action, normalizedX)) boost = true
        if (Kart3dInput.releasesSteer(action)) steer = 0f
        return true
    }

    /**
     * Advances one frame.
     */
    fun tick(): OffroadState {
        if (state.finished) return state
        val burst = boost
        boost = false
        state = engine.step(state, clock.delta(), steer, burst)
        return state
    }
}
