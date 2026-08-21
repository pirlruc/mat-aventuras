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
    private val nowNs: () -> Long = { System.nanoTime() },
) {
    /** Latest simulation snapshot. */
    var state: OffroadState = engine.initial(lapsTarget = lapsTarget)
        private set

    private var lastNs: Long = 0L
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
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNs = now
        val burst = boost
        boost = false
        state = engine.step(state, dt, steer, burst)
        return state
    }
}
