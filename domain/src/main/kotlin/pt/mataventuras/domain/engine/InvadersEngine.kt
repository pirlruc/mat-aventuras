package pt.mataventuras.domain.engine

/**
 * Letter-invaders state. [aliens] is a 15-bit mask (5 columns × 3 rows).
 */
data class InvadersState(
    val shipX: Float,
    val shotX: Float,
    val shotY: Float,
    val aliens: Int,
    val alienOrigin: Float,
    val alienDir: Float,
    val bombX: Float,
    val bombY: Float,
    val hits: Int,
    val hitsTarget: Int,
    val alive: Boolean,
    val finished: Boolean,
)

/**
 * Space-invaders-style reward: drag to move, tap to shoot letter-ships.
 */
class InvadersEngine {
    /**
     * Ship centred, full alien grid.
     */
    fun initial(hitsTarget: Int = 8): InvadersState =
        InvadersState(
            shipX = 0.5f,
            shotX = -1f,
            shotY = -1f,
            aliens = FULL_GRID,
            alienOrigin = 0.12f,
            alienDir = 1f,
            bombX = -1f,
            bombY = -1f,
            hits = 0,
            hitsTarget = hitsTarget,
            alive = true,
            finished = false,
        )

    /**
     * [moveX] is -1..1. [fire] launches one shot when none is in flight.
     */
    fun step(
        state: InvadersState,
        dt: Float,
        moveX: Float,
        fire: Boolean,
    ): InvadersState {
        if (!state.alive || state.finished) return state
        val clamped = dt.coerceIn(0.001f, 0.05f)
        val ship = (state.shipX + moveX.coerceIn(-1f, 1f) * 0.7f * clamped).coerceIn(0.08f, 0.92f)
        val flying = advanceShot(state, clamped, ship, fire)
        val swarm = marchAliens(flying, clamped)
        val bombed = dropBomb(swarm, clamped, ship)
        val won = bombed.hits >= bombed.hitsTarget || bombed.aliens == 0
        return bombed.copy(shipX = ship, finished = won, alive = bombed.alive)
    }

    private fun advanceShot(
        state: InvadersState,
        dt: Float,
        ship: Float,
        fire: Boolean,
    ): InvadersState {
        var shotX = state.shotX
        var shotY = state.shotY
        if (shotY < 0f && fire) {
            shotX = ship
            shotY = 0.82f
        }
        if (shotY < 0f) return state
        shotY -= 1.1f * dt
        val miss = shotY < 0.06f
        val hit = if (miss) -1 else hitAlien(state.aliens, state.alienOrigin, shotX, shotY)
        if (miss || hit < 0) {
            return if (miss) state.copy(shotX = -1f, shotY = -1f) else state.copy(shotX = shotX, shotY = shotY)
        }
        val left = state.aliens and hit.inv()
        return state.copy(
            shotX = -1f,
            shotY = -1f,
            aliens = left,
            hits = (state.hits + 1).coerceAtMost(state.hitsTarget),
        )
    }

    private fun marchAliens(
        state: InvadersState,
        dt: Float,
    ): InvadersState {
        if (state.aliens == 0) return state
        var origin = state.alienOrigin + state.alienDir * 0.12f * dt
        var dir = state.alienDir
        if (origin < 0.04f || origin > 0.42f) {
            dir = -dir
            origin = origin.coerceIn(0.04f, 0.42f)
        }
        return state.copy(alienOrigin = origin, alienDir = dir)
    }

    private fun dropBomb(
        state: InvadersState,
        dt: Float,
        ship: Float,
    ): InvadersState {
        var bombX = state.bombX
        var bombY = state.bombY
        if (bombY < 0f && state.aliens != 0) {
            bombX = state.alienOrigin + 0.12f
            bombY = 0.22f
        }
        if (bombY < 0f) return state
        bombY += 0.45f * dt
        val gone = bombY > 0.95f
        val hitShip = kotlin.math.abs(bombX - ship) < 0.07f && bombY > 0.84f
        return when {
            gone -> state.copy(bombX = -1f, bombY = -1f)
            hitShip -> state.copy(alive = false, bombX = -1f, bombY = -1f)
            else -> state.copy(bombX = bombX, bombY = bombY)
        }
    }

    private fun hitAlien(
        mask: Int,
        origin: Float,
        shotX: Float,
        shotY: Float,
    ): Int {
        for (i in 0 until 15) {
            if (mask and (1 shl i) == 0) continue
            val col = i % 5
            val row = i / 5
            val ax = origin + col * 0.12f
            val ay = 0.12f + row * 0.12f
            if (kotlin.math.abs(ax - shotX) < 0.05f && kotlin.math.abs(ay - shotY) < 0.06f) {
                return 1 shl i
            }
        }
        return -1
    }

    private companion object {
        const val FULL_GRID: Int = (1 shl 15) - 1
    }
}
