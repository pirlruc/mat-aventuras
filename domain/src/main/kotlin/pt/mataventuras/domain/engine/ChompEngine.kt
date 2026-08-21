package pt.mataventuras.domain.engine

/**
 * Maze-chomp state. [pellets] bits match open cells in [ChompMaze].
 */
data class ChompState(
    val px: Int,
    val py: Int,
    val ghostX: Int,
    val ghostY: Int,
    val ghost2X: Int,
    val ghost2Y: Int,
    val pellets: Int,
    val powerTimer: Float,
    val form: Int,
    val lives: Int,
    val invuln: Float,
    val alive: Boolean,
    val finished: Boolean,
)

/**
 * Pac-man-style letter maze: eat dots, power-pellets transform the hero, avoid ghosts.
 */
class ChompEngine {
    /**
     * Start at the bottom corridor with every pellet present.
     */
    fun initial(): ChompState =
        ChompState(
            px = 1,
            py = 3,
            ghostX = 3,
            ghostY = 1,
            ghost2X = 1,
            ghost2Y = 1,
            pellets = ChompMaze.ALL_PELLETS,
            powerTimer = 0f,
            form = 0,
            lives = LIVES_MAX,
            invuln = GRACE,
            alive = true,
            finished = false,
        )

    /**
     * [dirX]/[dirY] are -1, 0, or 1. Power form lets the hero walk through a ghost.
     */
    fun step(
        state: ChompState,
        dt: Float,
        dirX: Int,
        dirY: Int,
    ): ChompState {
        if (!state.alive || state.finished) return state
        val clamped = dt.coerceIn(0.001f, 0.08f)
        val ticking = state.copy(invuln = (state.invuln - clamped).coerceAtLeast(0f))
        val eaten = eat(walk(ticking, dirX, dirY))
        val timer = (eaten.powerTimer - clamped).coerceAtLeast(0f)
        val form = if (timer > 0f) 1 else 0
        val tagged = eaten.copy(powerTimer = timer, form = form)
        val ghosts = if (tagged.invuln > 0f) tagged else chase(tagged, form == 1)
        val hurt = hurt(ghosts)
        val won = hurt.pellets == 0
        return hurt.copy(finished = won && hurt.alive)
    }

    private fun walk(
        state: ChompState,
        dirX: Int,
        dirY: Int,
    ): ChompState {
        val nx = state.px + dirX.coerceIn(-1, 1)
        val ny = state.py + dirY.coerceIn(-1, 1)
        if (!ChompMaze.isOpen(nx, ny)) return state
        return state.copy(px = nx, py = ny)
    }

    private fun eat(state: ChompState): ChompState {
        val bit = ChompMaze.bit(state.px, state.py)
        if (bit == 0 || state.pellets and bit == 0) return state
        val power = ChompMaze.isPower(state.px, state.py)
        return state.copy(
            pellets = state.pellets and bit.inv(),
            powerTimer = if (power) 2.4f else state.powerTimer,
        )
    }

    private fun chase(
        state: ChompState,
        flee: Boolean,
    ): ChompState {
        val g1 = ghostStep(state.ghostX, state.ghostY, state.px, state.py, flee)
        val g2 = ghostStep(state.ghost2X, state.ghost2Y, state.px, state.py, flee)
        return state.copy(ghostX = g1.first, ghostY = g1.second, ghost2X = g2.first, ghost2Y = g2.second)
    }

    private fun ghostStep(
        x: Int,
        y: Int,
        tx: Int,
        ty: Int,
        flee: Boolean,
    ): Pair<Int, Int> {
        val sx = if (flee) x - sign(tx - x) else x + sign(tx - x)
        val sy = if (flee) y - sign(ty - y) else y + sign(ty - y)
        if (ChompMaze.isOpen(sx, y) && sx != x) return sx to y
        if (ChompMaze.isOpen(x, sy) && sy != y) return x to sy
        return x to y
    }

    private fun hurt(state: ChompState): ChompState {
        if (state.form == 1 || state.invuln > 0f || !touched(state)) return state
        val lives = state.lives - 1
        if (lives <= 0) return state.copy(lives = 0, alive = false)
        return state.copy(lives = lives, invuln = GRACE, px = 1, py = 3)
    }

    private fun touched(state: ChompState): Boolean {
        val a = state.px == state.ghostX && state.py == state.ghostY
        val b = state.px == state.ghost2X && state.py == state.ghost2Y
        return a || b
    }

    private fun sign(v: Int): Int =
        when {
            v > 0 -> 1
            v < 0 -> -1
            else -> 0
        }

    private companion object {
        const val LIVES_MAX: Int = 3
        const val GRACE: Float = 1.6f
    }
}

/**
 * Fixed 5×5 maze. Open cells hold pellets; corners are power pellets.
 */
object ChompMaze {
    /** Width and height. */
    const val SIZE: Int = 5

    private val LAYOUT: Array<String> =
        arrayOf(
            "#####",
            "#...#",
            "#.#.#",
            "#...#",
            "#####",
        )

    /** Every open-cell bit set. */
    val ALL_PELLETS: Int = pelletMask()

    /**
     * True when ([x], [y]) is a corridor.
     */
    fun isOpen(
        x: Int,
        y: Int,
    ): Boolean {
        if (x !in 0 until SIZE || y !in 0 until SIZE) return false
        return LAYOUT[y][x] == '.'
    }

    /**
     * Pellet bit for a cell, or 0 for a wall.
     */
    fun bit(
        x: Int,
        y: Int,
    ): Int {
        if (!isOpen(x, y)) return 0
        return 1 shl (y * SIZE + x)
    }

    /**
     * Corner cells transform the hero.
     */
    fun isPower(
        x: Int,
        y: Int,
    ): Boolean = (x == 1 || x == 3) && (y == 1 || y == 3)

    private fun pelletMask(): Int {
        var mask = 0
        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                if (isOpen(x, y)) mask = mask or (1 shl (y * SIZE + x))
            }
        }
        return mask
    }
}
