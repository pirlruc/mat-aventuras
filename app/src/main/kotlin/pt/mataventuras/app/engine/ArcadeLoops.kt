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
    private val nowNs: () -> Long = { System.nanoTime() },
) {
    var moveX: Float = 0f
    var fire: Boolean = false
    var state: InvadersState = engine.initial()
        private set
    private var lastNs: Long = 0L

    fun tick(): InvadersState {
        if (state.finished || !state.alive) return state
        val dt = delta()
        val shot = fire
        fire = false
        state = engine.step(state, dt, moveX, shot)
        return state
    }

    private fun delta(): Float {
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNs = now
        return dt
    }
}

/**
 * Frame loop for the maze-chomp prize.
 */
internal class ChompLoop(
    private val engine: ChompEngine = ChompEngine(),
    private val nowNs: () -> Long = { System.nanoTime() },
) {
    var dirX: Int = 0
    var dirY: Int = 0
    var state: ChompState = engine.initial()
        private set
    private var lastNs: Long = 0L

    fun tick(): ChompState {
        if (state.finished || !state.alive) return state
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.08f)
        lastNs = now
        state = engine.step(state, dt, dirX, dirY)
        return state
    }
}

/**
 * Frame loop for the letter-climb prize.
 */
internal class ClimbLoop(
    private val engine: ClimbEngine = ClimbEngine(),
    private val nowNs: () -> Long = { System.nanoTime() },
) {
    var moveX: Float = 0f
    var jumping: Boolean = false
    var state: ClimbState = engine.initial()
        private set
    private var lastNs: Long = 0L

    fun tick(): ClimbState {
        if (state.finished || !state.alive) return state
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNs = now
        val jump = jumping
        jumping = false
        state = engine.step(state, dt, moveX, jump)
        return state
    }
}
