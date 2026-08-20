package pt.mataventuras.domain.engine

/**
 * Game Boy-like brick layout for the age-3 runner, in world units.
 */
object PlatformerWorld {
    /** Left edge of the floor gap. */
    const val PIT_LEFT: Float = 28f

    /** Right edge of the floor gap. */
    const val PIT_RIGHT: Float = 36f

    /** Horizontal coin centres. */
    val COIN_X: FloatArray = floatArrayOf(8f, 16f, 24f, 42f, 54f)

    /**
     * Ledge tops the player can land on: x, y, width.
     */
    val LEDGES: Array<FloatArray> =
        arrayOf(
            floatArrayOf(14f, 3.5f, 8f),
            floatArrayOf(40f, 4.2f, 8f),
        )
}

/**
 * Simulatable 2D side-scroller state (age-3 reward).
 */
data class Platformer2dState(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val onGround: Boolean,
    val rings: Int,
    val ringsTarget: Int,
    val alive: Boolean,
    val finished: Boolean,
    val collectedMask: Int = 0,
)

/**
 * Simple side-scroller physics. Testable without Android.
 */
class Platformer2dEngine(
    private val groundY: Float = 0f,
    private val gravity: Float = -48f,
    private val jumpSpeed: Float = 22f,
    private val speedX: Float = 8f,
) {
    /**
     * Standing still on the ground.
     */
    fun initial(ringsTarget: Int = 5): Platformer2dState =
        Platformer2dState(
            x = 0f,
            y = groundY,
            vx = speedX,
            vy = 0f,
            onGround = true,
            rings = 0,
            ringsTarget = ringsTarget,
            alive = true,
            finished = false,
        )

    /**
     * Advances one simulation step in seconds.
     */
    fun step(
        state: Platformer2dState,
        dt: Float,
        jumping: Boolean,
    ): Platformer2dState {
        if (!state.alive || state.finished) return state
        val vyJump = if (jumping && state.onGround) jumpSpeed else state.vy + gravity * dt
        val x = state.x + state.vx * dt
        val rawY = state.y + vyJump * dt
        val landed = settle(x, rawY, vyJump)
        val y = landed.first
        val onGround = landed.second
        val vy = if (onGround) 0f else vyJump
        val alive = y >= -2f
        val finished = state.rings >= state.ringsTarget
        return state.copy(x = x, y = y, vy = vy, onGround = onGround, alive = alive, finished = finished)
    }

    /**
     * Collects a ring/coin when the player is within [radius] of [ringX].
     * When [coinIndex] is 0 or more, that slot is taken at most once.
     */
    fun collect(
        state: Platformer2dState,
        ringX: Float,
        radius: Float = 1.2f,
        coinIndex: Int = -1,
    ): Platformer2dState {
        if (!state.alive || state.finished) return state
        val taken = coinIndex >= 0 && (state.collectedMask shr coinIndex) and 1 == 1
        val far = kotlin.math.abs(state.x - ringX) > radius
        if (taken || far) return state
        val rings = state.rings + 1
        val mask = if (coinIndex >= 0) state.collectedMask or (1 shl coinIndex) else state.collectedMask
        return state.copy(rings = rings, finished = rings >= state.ringsTarget, collectedMask = mask)
    }

    private fun settle(
        x: Float,
        y: Float,
        vy: Float,
    ): Pair<Float, Boolean> {
        val inPit = x > PlatformerWorld.PIT_LEFT && x < PlatformerWorld.PIT_RIGHT
        if (y <= groundY && !inPit) return groundY to true
        val ledgeY = ledgeTop(x, y, vy)
        if (ledgeY != null) return ledgeY to true
        return y to false
    }

    private fun ledgeTop(
        x: Float,
        y: Float,
        vy: Float,
    ): Float? {
        PlatformerWorld.LEDGES.forEach { ledge ->
            val top = ledge[1]
            val onX = x >= ledge[0] && x <= ledge[0] + ledge[2]
            val hitting = vy <= 0f && y in (top - 1.5f)..top
            if (onX && hitting) return top
        }
        return null
    }

    /** World X where the floor gap begins (fall). */
    companion object {
        const val PIT_X: Float = PlatformerWorld.PIT_LEFT
    }
}
