package pt.mataventuras.domain.engine

/**
 * Touch mapping shared by native Canvas hosts and the Godot scripts.
 * X is 0 at the left edge and 1 at the right.
 */
object EngineInputMap {
    /** Half-width of the centre boost / idle band. */
    const val BOOST_DEADZONE: Float = 0.14f

    /** Pixels of upward travel that request a jump. Larger than a tap jitter. */
    const val JUMP_FLICK: Float = 56f

    /**
     * Full left/right steering in `[-1, 1]`. Centre dead-zone is 0 (boost tap).
     *
     * Kids tap a side; analog magnitude made small offsets feel unresponsive.
     */
    fun steerFromNormalizedX(nx: Float): Float {
        val x = nx.coerceIn(0f, 1f)
        val delta = x - 0.5f
        if (kotlin.math.abs(delta) <= BOOST_DEADZONE) return 0f
        return if (delta < 0f) -1f else 1f
    }

    /**
     * True when the tap is in the centre boost band.
     */
    fun isBoostBand(nx: Float): Boolean = kotlin.math.abs(nx.coerceIn(0f, 1f) - 0.5f) <= BOOST_DEADZONE

    /**
     * Hold-to-run from a normalised X. Centre is idle so a jump flick does not dash.
     */
    fun runFromNormalizedX(nx: Float): Float = steerFromNormalizedX(nx)

    /**
     * True when the pointer travelled up more than sideways by [JUMP_FLICK] pixels.
     */
    fun isJumpFlick(
        dx: Float,
        dy: Float,
    ): Boolean = dy <= -JUMP_FLICK && kotlin.math.abs(dy) >= kotlin.math.abs(dx) * 1.2f
}
