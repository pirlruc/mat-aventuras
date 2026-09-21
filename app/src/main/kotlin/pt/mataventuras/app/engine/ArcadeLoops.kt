package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.ChompEngine
import pt.mataventuras.domain.engine.ChompState
import pt.mataventuras.domain.engine.ClimbEngine
import pt.mataventuras.domain.engine.ClimbState
import pt.mataventuras.domain.engine.InvadersEngine
import pt.mataventuras.domain.engine.InvadersState

/**
 * Shared clock for prize loops. A settled game does not consume a frame.
 */
internal class RewardTicker(
    nowNs: () -> Long = { System.nanoTime() },
    maxDt: Float = 0.05f,
) {
    private val clock = FrameClock(nowNs, maxDt)

    /**
     * Returns [state] when [settled] is true; otherwise [step] with the frame delta.
     */
    fun <S> advance(
        state: S,
        settled: Boolean,
        step: (Float) -> S,
    ): S = if (settled) state else step(clock.delta())
}

/**
 * Frame loop for the letter-invaders prize.
 */
internal class InvadersLoop(
    private val engine: InvadersEngine = InvadersEngine(),
    nowNs: () -> Long = { System.nanoTime() },
    start: InvadersState = engine.initial(),
) {
    var moveX: Float = 0f
    var fire: Boolean = false
    var state: InvadersState = start
        private set
    private val ticker = RewardTicker(nowNs)

    fun tick(): InvadersState {
        state =
            ticker.advance(state, state.finished || !state.alive) { dt ->
                val shot = fire
                fire = false
                engine.step(state, dt, moveX, shot)
            }
        return state
    }
}

/**
 * Frame loop for the maze-chomp prize.
 */
internal class ChompLoop(
    private val engine: ChompEngine = ChompEngine(),
    nowNs: () -> Long = { System.nanoTime() },
    start: ChompState = engine.initial(),
) {
    var dirX: Int = 0
    var dirY: Int = 0
    var state: ChompState = start
        private set
    private val ticker = RewardTicker(nowNs, maxDt = 0.08f)

    fun tick(): ChompState {
        state =
            ticker.advance(state, state.finished || !state.alive) { dt ->
                engine.step(state, dt, dirX, dirY)
            }
        return state
    }
}

/**
 * Frame loop for the letter-climb prize.
 */
internal class ClimbLoop(
    private val engine: ClimbEngine = ClimbEngine(),
    nowNs: () -> Long = { System.nanoTime() },
    start: ClimbState = engine.initial(),
) {
    var moveX: Float = 0f
    var jumping: Boolean = false
    var state: ClimbState = start
        private set
    private val ticker = RewardTicker(nowNs)

    fun tick(): ClimbState {
        state =
            ticker.advance(state, state.finished || !state.alive) { dt ->
                val jump = jumping
                jumping = false
                engine.step(state, dt, moveX, jump)
            }
        return state
    }
}
