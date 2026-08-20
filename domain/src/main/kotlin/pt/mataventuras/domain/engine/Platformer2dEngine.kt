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
        var y = state.y + vyJump * dt
        var onGround = false
        var vy = vyJump
        val pit = state.x > PIT_X
        if (y <= groundY && !pit) {
            y = groundY
            vy = 0f
            onGround = true
        }
        val x = state.x + state.vx * dt
        val alive = y >= -2f
        val finished = state.rings >= state.ringsTarget
        return state.copy(x = x, y = y, vy = vy, onGround = onGround, alive = alive, finished = finished)
    }

    /**
     * Collects a ring when the player is within [radius] of [ringX].
     */
    fun collect(
        state: Platformer2dState,
        ringX: Float,
        radius: Float = 1.2f,
    ): Platformer2dState {
        if (!state.alive || state.finished) return state
        val distance = kotlin.math.abs(state.x - ringX)
        if (distance > radius) return state
        val rings = state.rings + 1
        return state.copy(rings = rings, finished = rings >= state.ringsTarget)
    }

    /** World X where the floor ends (fall). */
    companion object {
        const val PIT_X: Float = 30f
    }
}
