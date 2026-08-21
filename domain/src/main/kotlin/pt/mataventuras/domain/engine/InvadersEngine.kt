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
    val lives: Int,
    val livesMax: Int,
    val invuln: Float,
    val grace: Float,
    val alive: Boolean,
    val finished: Boolean,
)

/**
 * Space-invaders-style reward: drag to move, tap to shoot letter-ships.
 * Ends only when every ship is destroyed or all lives are gone.
 */
class InvadersEngine {
    /**
     * Ship centred, full alien grid, [LIVES_MAX] lives.
     */
    fun initial(hitsTarget: Int = FLEET): InvadersState =
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
            lives = LIVES_MAX,
            livesMax = LIVES_MAX,
            invuln = 0f,
            grace = GRACE,
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
        val ticking = tick(state.copy(shipX = ship), clamped)
        val flying = advanceShot(ticking, clamped, ship, fire)
        val swarm = marchAliens(flying, clamped)
        return conclude(dropBomb(swarm, clamped, ship))
    }

    private fun tick(
        state: InvadersState,
        dt: Float,
    ): InvadersState =
        state.copy(
            invuln = (state.invuln - dt).coerceAtLeast(0f),
            grace = (state.grace - dt).coerceAtLeast(0f),
        )

    private fun conclude(state: InvadersState): InvadersState {
        val won = state.aliens == 0
        return state.copy(finished = won, alive = state.lives > 0)
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
        if (state.grace > 0f) return state
        if (state.aliens == 0) return state
        var bombX = state.bombX
        var bombY = state.bombY
        if (bombY < 0f) {
            val spawn = bomber(state)
            bombX = spawn.first
            bombY = spawn.second
        }
        bombY += 0.45f * dt
        val gone = bombY > 0.95f
        val hitShip = kotlin.math.abs(bombX - ship) < 0.07f && bombY > 0.84f
        return when {
            gone -> state.copy(bombX = -1f, bombY = -1f)
            hitShip && state.invuln <= 0f -> takeHit(state)
            else -> state.copy(bombX = bombX, bombY = bombY)
        }
    }

    private fun takeHit(state: InvadersState): InvadersState {
        val lives = state.lives - 1
        return state.copy(
            lives = lives,
            invuln = HIT_INVULN,
            bombX = -1f,
            bombY = -1f,
            alive = lives > 0,
        )
    }

    private fun bomber(state: InvadersState): Pair<Float, Float> {
        val i = Integer.numberOfTrailingZeros(state.aliens)
        val col = i % 5
        val row = i / 5
        return (state.alienOrigin + col * 0.12f) to (0.12f + row * 0.12f)
    }

    private fun hitAlien(
        mask: Int,
        origin: Float,
        shotX: Float,
        shotY: Float,
    ): Int {
        for (i in 0 until FLEET) {
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
        const val FLEET: Int = 15
        const val FULL_GRID: Int = (1 shl FLEET) - 1
        const val LIVES_MAX: Int = 5
        const val GRACE: Float = 1.8f
        const val HIT_INVULN: Float = 1.4f
    }
}
