package pt.mataventuras.domain.engine

/**
 * Donkey-Kong-style climb: floors of letters, rolling barrels, a transforming pickup.
 */
data class ClimbState(
    val x: Float,
    val y: Float,
    val vy: Float,
    val onFloor: Boolean,
    val collectedMask: Int,
    val lettersTarget: Int,
    val barrelX: Float,
    val barrelFloor: Int,
    val form: Int,
    val alive: Boolean,
    val finished: Boolean,
) {
    /** Letters picked up so far. */
    val letters: Int get() = Integer.bitCount(collectedMask)
}

/**
 * Climb letter platforms while jumping barrels. A mushroom pickup grows the hero.
 */
class ClimbEngine {
    /**
     * Standing on the lowest floor.
     */
    fun initial(lettersTarget: Int = 5): ClimbState =
        ClimbState(
            x = 0.12f,
            y = FLOORS[0],
            vy = 0f,
            onFloor = true,
            collectedMask = 0,
            lettersTarget = lettersTarget,
            barrelX = 0.9f,
            barrelFloor = FLOORS.lastIndex,
            form = 0,
            alive = true,
            finished = false,
        )

    /**
     * [moveX] is -1..1. [jumping] hops up a floor when grounded.
     */
    fun step(
        state: ClimbState,
        dt: Float,
        moveX: Float,
        jumping: Boolean,
    ): ClimbState {
        if (!state.alive || state.finished) return state
        val clamped = dt.coerceIn(0.001f, 0.05f)
        val motion = move(state, clamped, moveX, jumping)
        val letters = collect(motion)
        val grown = if (near(letters, MUSHROOM_X, FLOORS[1])) letters.copy(form = 1) else letters
        val rolled = rollBarrel(grown, clamped)
        return strike(rolled)
    }

    private fun move(
        state: ClimbState,
        dt: Float,
        moveX: Float,
        jumping: Boolean,
    ): ClimbState {
        val vy = if (jumping && state.onFloor) JUMP else state.vy + GRAVITY * dt
        val x = (state.x + moveX.coerceIn(-1f, 1f) * 0.55f * dt).coerceIn(0.06f, 0.94f)
        val y = state.y + vy * dt
        val landed = land(y, vy)
        return state.copy(x = x, y = landed.first, vy = landed.second, onFloor = landed.third)
    }

    private fun land(
        y: Float,
        vy: Float,
    ): Triple<Float, Float, Boolean> {
        if (vy > 0f) return Triple(y, vy, false)
        val floor = FLOORS.firstOrNull { y <= it && y >= it - 0.08f }
        if (floor != null) return Triple(floor, 0f, true)
        val grounded = y <= FLOORS[0]
        return if (grounded) Triple(FLOORS[0], 0f, true) else Triple(y, vy, false)
    }

    private fun collect(state: ClimbState): ClimbState {
        var mask = state.collectedMask
        LETTERS.forEachIndexed { i, spot ->
            val bit = 1 shl i
            if (mask and bit == 0 && near(state, spot.first, spot.second)) mask = mask or bit
        }
        val count = Integer.bitCount(mask)
        return state.copy(collectedMask = mask, finished = count >= state.lettersTarget)
    }

    private fun rollBarrel(
        state: ClimbState,
        dt: Float,
    ): ClimbState {
        var x = state.barrelX + BARREL_SPEED * dt
        var floor = state.barrelFloor
        if (x > 0.94f) {
            x = 0.08f
            floor = if (floor <= 0) FLOORS.lastIndex else floor - 1
        }
        return state.copy(barrelX = x, barrelFloor = floor)
    }

    private fun strike(state: ClimbState): ClimbState {
        val floorY = FLOORS[state.barrelFloor.coerceIn(0, FLOORS.lastIndex)]
        val nearBarrel =
            kotlin.math.abs(state.y - floorY) < 0.05f &&
                kotlin.math.abs(state.x - state.barrelX) < 0.07f
        if (!nearBarrel) return state
        if (state.form == 1) return state.copy(form = 0)
        return state.copy(alive = false)
    }

    private fun near(
        state: ClimbState,
        x: Float,
        y: Float,
    ): Boolean = kotlin.math.abs(state.x - x) < 0.08f && kotlin.math.abs(state.y - y) < 0.08f

    private companion object {
        val FLOORS: FloatArray = floatArrayOf(0.12f, 0.34f, 0.56f, 0.78f)
        val LETTERS: Array<Pair<Float, Float>> =
            arrayOf(
                0.28f to 0.12f,
                0.72f to 0.34f,
                0.28f to 0.56f,
                0.72f to 0.56f,
                0.50f to 0.78f,
            )
        const val MUSHROOM_X: Float = 0.5f
        const val JUMP: Float = 0.85f
        const val GRAVITY: Float = -2.4f
        const val BARREL_SPEED: Float = 0.35f
    }
}
