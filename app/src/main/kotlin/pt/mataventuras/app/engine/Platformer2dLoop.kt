package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.engine.Platformer2dState

/**
 * Frame loop for the age-3 Canvas reward. Testable without `withFrameNanos`.
 */
internal class Platformer2dLoop(
    private val engine: Platformer2dEngine = Platformer2dEngine(),
    ringsTarget: Int = 5,
    private val nowNs: () -> Long = { System.nanoTime() },
) {
    /** Jump request consumed on the next [tick]. */
    var jumping: Boolean = false

    /** Latest simulation snapshot. */
    var state: Platformer2dState = engine.initial(ringsTarget = ringsTarget)
        private set

    private var lastNs: Long = 0L

    /**
     * Advances one frame. Returns the snapshot after collect.
     */
    fun tick(): Platformer2dState {
        if (state.finished || !state.alive) return state
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNs = now
        val jump = jumping
        jumping = false
        state = engine.step(state, dt, jump)
        val ring = (state.x / 8f).toInt() * 8f + 6f
        state = engine.collect(state, ring)
        return state
    }
}
