package pt.mataventuras.domain.engine

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
    val inPitFall: Boolean = false,
)

/**
 * Side-scroller physics. Finger drag sets [moveX]; a forward flick jumps.
 */
class Platformer2dEngine(
    private val groundY: Float = 0f,
    private val gravity: Float = -48f,
    private val jumpSpeed: Float = 22f,
    private val speedX: Float = 8f,
    private val level: PlatformerLevel = PlatformerWorld.DEFAULT,
) {
    /**
     * Standing still on the ground.
     */
    fun initial(ringsTarget: Int = 5): Platformer2dState =
        Platformer2dState(
            x = 0f,
            y = groundY,
            vx = 0f,
            vy = 0f,
            onGround = true,
            rings = 0,
            ringsTarget = ringsTarget,
            alive = true,
            finished = false,
        )

    /**
     * Advances one simulation step in seconds.
     * [moveX] is -1..1 from a horizontal drag. Jump only applies with forward motion.
     */
    fun step(
        state: Platformer2dState,
        dt: Float,
        jumping: Boolean,
        moveX: Float = 1f,
    ): Platformer2dState {
        if (!state.alive || state.finished) return state
        val run = moveX.coerceIn(-1f, 1f)
        val motion = integrate(state, dt, jumping, run)
        val landed = settle(motion.x, motion.y, motion.vy, motion.falling)
        val onGround = landed.second
        val finished = state.rings >= state.ringsTarget
        return state.copy(
            x = motion.x,
            y = landed.first,
            vx = motion.vx,
            vy = if (onGround) 0f else motion.vy,
            onGround = onGround,
            alive = landed.first >= -2f,
            finished = finished,
            inPitFall = motion.falling && !onGround,
        )
    }

    private fun integrate(
        state: Platformer2dState,
        dt: Float,
        jumping: Boolean,
        run: Float,
    ): PitMotion {
        val leaped = jumping && state.onGround && run > 0.2f
        val vy = if (leaped) jumpSpeed else state.vy + gravity * dt
        val vx = if (state.inPitFall) 0f else run * speedX
        val x = state.x + vx * dt
        val y = state.y + vy * dt
        val falling = state.inPitFall || (level.inPit(x) && y <= groundY)
        return PitMotion(x, y, vx, vy, falling)
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
        falling: Boolean,
    ): Pair<Float, Boolean> {
        if (falling) return y to false
        if (y <= groundY && !level.inPit(x)) return groundY to true
        val ledgeY = ledgeTop(x, y, vy)
        return if (ledgeY != null) ledgeY to true else y to false
    }

    private fun ledgeTop(
        x: Float,
        y: Float,
        vy: Float,
    ): Float? {
        level.ledges.forEach { ledge ->
            val onX = x >= ledge.x && x <= ledge.x + ledge.width
            val hitting = vy <= 0f && y in (ledge.y - 1.5f)..ledge.y
            if (onX && hitting) return ledge.y
        }
        return null
    }

    /** World X where the first floor gap begins (fall). */
    companion object {
        const val PIT_X: Float = PlatformerWorld.PIT_LEFT
    }
}

private data class PitMotion(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val falling: Boolean,
)
