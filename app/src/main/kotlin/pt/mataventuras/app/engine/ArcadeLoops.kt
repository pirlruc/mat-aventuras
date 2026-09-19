package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.ChompEngine
import pt.mataventuras.domain.engine.ChompState
import pt.mataventuras.domain.engine.ClimbEngine
import pt.mataventuras.domain.engine.ClimbState
import pt.mataventuras.domain.engine.InvadersEngine
import pt.mataventuras.domain.engine.InvadersState

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
    private val clock = FrameClock(nowNs)

    fun tick(): InvadersState {
        if (state.finished || !state.alive) return state
        val dt = clock.delta()
        val shot = fire
        fire = false
        state = engine.step(state, dt, moveX, shot)
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
    private val clock = FrameClock(nowNs, maxDt = 0.08f)

    fun tick(): ChompState {
        if (state.finished || !state.alive) return state
        state = engine.step(state, clock.delta(), dirX, dirY)
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
    private val clock = FrameClock(nowNs)

    fun tick(): ClimbState {
        if (state.finished || !state.alive) return state
        val jump = jumping
        jumping = false
        state = engine.step(state, clock.delta(), moveX, jump)
        return state
    }
}
