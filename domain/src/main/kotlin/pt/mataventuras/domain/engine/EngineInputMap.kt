package pt.mataventuras.domain.engine

/**
 * Normalised touch bands shared by the native kart and a later Godot/Unity
 * input map. X is 0 at the left edge and 1 at the right.
 */
object EngineInputMap {
    /** Taps left of this value steer left. */
    const val STEER_LEFT_MAX: Float = 0.34f

    /** Taps right of this value steer right. */
    const val STEER_RIGHT_MIN: Float = 0.66f

    /**
     * Steering in `[-1, 1]`. The centre band is 0 (boost, not steer).
     */
    fun steerFromNormalizedX(nx: Float): Float =
        when {
            nx < STEER_LEFT_MAX -> -1f
            nx > STEER_RIGHT_MIN -> 1f
            else -> 0f
        }

    /**
     * True when the tap is in the centre boost band (inclusive).
     */
    fun isBoostBand(nx: Float): Boolean = nx in STEER_LEFT_MAX..STEER_RIGHT_MIN
}
