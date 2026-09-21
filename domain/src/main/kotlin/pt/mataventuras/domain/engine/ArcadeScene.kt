package pt.mataventuras.domain.engine

/**
 * One filled rectangle in a native arcade viewport. Coordinates are pixels.
 */
data class ArcadeSpan(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val argb: Long,
)

/**
 * Pixel layout for the native Canvas fallback. Positions use the same
 * normalised centres as [InvadersEngine], [ChompMaze], and [ClimbEngine].
 */
object ArcadeScene {
    /** Night sky behind the letter ships. */
    const val SKY_ARGB: Long = 0xFF0D1B3A

    /** Player ship. */
    const val SHIP_ARGB: Long = 0xFF42A5F5

    /** Letter-ship still in the fleet. */
    const val ALIEN_ARGB: Long = 0xFFFFF176

    /** Shot travelling up. */
    const val SHOT_ARGB: Long = 0xFFFFFFFF

    /** Bomb travelling down. */
    const val BOMB_ARGB: Long = 0xFFE53935

    /** Maze wall. */
    const val WALL_ARGB: Long = 0xFF1565C0

    /** Pellet still on the board. */
    const val PELLET_ARGB: Long = 0xFFFFF59D

    /** Hero in the maze or on a floor. */
    const val HERO_ARGB: Long = 0xFFFFEE58

    /** Ghost. */
    const val GHOST_ARGB: Long = 0xFFEF5350

    /** Climb floor. */
    const val FLOOR_ARGB: Long = 0xFF8D6E63

    /** Letter still to collect. */
    const val LETTER_ARGB: Long = 0xFFFFF176

    /** Rolling barrel. */
    const val BARREL_ARGB: Long = 0xFF6D4C41

    /**
     * Clears [out] and paints the fleet, ship, shot, and bomb.
     */
    fun fillInvaders(
        out: MutableList<ArcadeSpan>,
        state: InvadersState,
        width: Float,
        height: Float,
    ) {
        out.clear()
        val w = width.coerceAtLeast(1f)
        val h = height.coerceAtLeast(1f)
        out += ArcadeSpan(0f, 0f, w, h, SKY_ARGB)
        for (index in 0 until InvadersEngine.FLEET) {
            if (state.aliens and (1 shl index) == 0) continue
            val col = index % InvadersEngine.COLUMNS
            val row = index / InvadersEngine.COLUMNS
            val ax = state.alienOrigin + col * InvadersEngine.STEP
            val ay = InvadersEngine.TOP + row * InvadersEngine.STEP
            addNorm(out, ax to ay, 0.05f to 0.04f, ALIEN_ARGB, w to h)
        }
        addNorm(out, state.shipX to 0.88f, 0.07f to 0.035f, SHIP_ARGB, w to h)
        if (state.shotY >= 0f) {
            addNorm(out, state.shotX to state.shotY, 0.012f to 0.03f, SHOT_ARGB, w to h)
        }
        if (state.bombY >= 0f) {
            addNorm(out, state.bombX to state.bombY, 0.02f to 0.03f, BOMB_ARGB, w to h)
        }
        addLives(out, state.lives)
    }

    /**
     * Clears [out] and paints walls, pellets, ghosts, and the hero.
     */
    fun fillChomp(
        out: MutableList<ArcadeSpan>,
        state: ChompState,
        width: Float,
        height: Float,
    ) {
        out.clear()
        val w = width.coerceAtLeast(1f)
        val h = height.coerceAtLeast(1f)
        val cell = minOf(w, h) / ChompMaze.SIZE.toFloat()
        for (y in 0 until ChompMaze.SIZE) {
            for (x in 0 until ChompMaze.SIZE) {
                val left = x * cell
                val top = y * cell
                if (!ChompMaze.isOpen(x, y)) {
                    out += ArcadeSpan(left, top, cell, cell, WALL_ARGB)
                } else if (state.pellets and ChompMaze.bit(x, y) != 0) {
                    val dot = if (ChompMaze.isPower(x, y)) cell * 0.45f else cell * 0.22f
                    val inset = (cell - dot) / 2f
                    out += ArcadeSpan(left + inset, top + inset, dot, dot, PELLET_ARGB)
                }
            }
        }
        addCell(out, state.ghostX, state.ghostY, cell, GHOST_ARGB)
        addCell(out, state.ghost2X, state.ghost2Y, cell, GHOST_ARGB)
        addCell(out, state.px, state.py, cell, if (state.form == 1) SHIP_ARGB else HERO_ARGB)
        addLives(out, state.lives)
    }

    /**
     * Clears [out] and paints floors, letters, the barrel, and the hero.
     */
    fun fillClimb(
        out: MutableList<ArcadeSpan>,
        state: ClimbState,
        width: Float,
        height: Float,
    ) {
        out.clear()
        val w = width.coerceAtLeast(1f)
        val h = height.coerceAtLeast(1f)
        out += ArcadeSpan(0f, 0f, w, h, SKY_ARGB)
        ClimbEngine.FLOORS.forEach { floor ->
            out += ArcadeSpan(0f, floor * h, w, 8f, FLOOR_ARGB)
        }
        ClimbEngine.LETTERS.forEachIndexed { index, spot ->
            if (state.collectedMask and (1 shl index) == 0) {
                addNorm(out, spot.first to spot.second, 0.04f to 0.03f, LETTER_ARGB, w to h)
            }
        }
        val floorY = ClimbEngine.FLOORS[state.barrelFloor.coerceIn(0, ClimbEngine.FLOORS.lastIndex)]
        addNorm(out, state.barrelX to floorY, 0.04f to 0.04f, BARREL_ARGB, w to h)
        val hero = if (state.form == 1) SHIP_ARGB else HERO_ARGB
        addNorm(out, state.x to state.y, 0.05f to 0.06f, hero, w to h)
        addLives(out, state.lives)
    }

    private fun addCell(
        out: MutableList<ArcadeSpan>,
        x: Int,
        y: Int,
        cell: Float,
        argb: Long,
    ) {
        val pad = cell * 0.2f
        out += ArcadeSpan(x * cell + pad, y * cell + pad, cell - pad * 2f, cell - pad * 2f, argb)
    }

    private fun addNorm(
        out: MutableList<ArcadeSpan>,
        centre: Pair<Float, Float>,
        size: Pair<Float, Float>,
        argb: Long,
        board: Pair<Float, Float>,
    ) {
        val sw = size.first * board.first
        val sh = size.second * board.second
        val left = centre.first * board.first - sw / 2f
        val top = centre.second * board.second - sh / 2f
        out += ArcadeSpan(left, top, sw, sh, argb)
    }

    private fun addLives(
        out: MutableList<ArcadeSpan>,
        lives: Int,
    ) {
        repeat(lives.coerceAtLeast(0)) { index ->
            out += ArcadeSpan(8f + index * 18f, 8f, 12f, 12f, BOMB_ARGB)
        }
    }
}

/**
 * Finger position mapped into the three arcade simulations.
 */
object ArcadeInput {
    /**
     * Horizontal steer in -1..1 from a normalised x in 0..1.
     */
    fun steer(nx: Float): Float = ((nx - 0.5f) * 2f).coerceIn(-1f, 1f)

    /**
     * Maze step from a drag. The larger axis wins; a tiny drag stays put.
     */
    fun chompDir(
        dx: Float,
        dy: Float,
    ): Pair<Int, Int> {
        if (kotlin.math.abs(dx) < 0.04f && kotlin.math.abs(dy) < 0.04f) return 0 to 0
        return if (kotlin.math.abs(dx) >= kotlin.math.abs(dy)) {
            (if (dx > 0f) 1 else -1) to 0
        } else {
            0 to (if (dy > 0f) 1 else -1)
        }
    }

    /**
     * True when the finger flicked upward (canvas y grows downward).
     */
    fun climbJump(dy: Float): Boolean = dy < -0.08f
}
