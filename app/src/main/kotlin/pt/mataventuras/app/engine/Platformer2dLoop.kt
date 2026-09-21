package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.engine.Platformer2dState
import pt.mataventuras.domain.engine.PlatformerLevel
import pt.mataventuras.domain.engine.PlatformerWorld

/**
 * Frame loop for the age-3 Canvas reward. Testable without `withFrameNanos`.
 */
internal class Platformer2dLoop(
    private val engine: Platformer2dEngine = Platformer2dEngine(),
    private val level: PlatformerLevel = PlatformerWorld.DEFAULT,
    ringsTarget: Int = 5,
    nowNs: () -> Long = { System.nanoTime() },
) {
    /** Jump request consumed on the next [tick]. */
    var jumping: Boolean = false

    /** Horizontal run in `-1..1` from a finger drag. Zero when the finger is up. */
    var moveX: Float = 0f

    /** Latest simulation snapshot. */
    var state: Platformer2dState = engine.initial(ringsTarget = ringsTarget)
        private set

    private val ticker = RewardTicker(nowNs)

    /**
     * Advances one frame. Returns the snapshot after collect.
     */
    fun tick(): Platformer2dState {
        state =
            ticker.advance(state, state.finished || !state.alive) { dt ->
                val jump = jumping
                jumping = false
                var next = engine.step(state, dt, jump, moveX)
                level.coins.forEachIndexed { i, coinX ->
                    next = engine.collect(next, coinX, coinIndex = i)
                }
                next
            }
        return state
    }
}
