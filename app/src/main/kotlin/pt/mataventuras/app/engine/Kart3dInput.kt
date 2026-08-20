package pt.mataventuras.app.engine

/**
 * Touch mapping for the isolated kart process (left steer, right steer, centre boost).
 */
object Kart3dInput {
    /**
     * Maps a normalised horizontal tap to a steering value in `[-1, 1]`.
     */
    fun steerFromTouch(nx: Float): Float =
        when {
            nx < 0.34f -> -1f
            nx > 0.66f -> 1f
            else -> 0f
        }

    /**
     * Returns true when the tap is in the boost band.
     */
    fun isBoostTouch(nx: Float): Boolean = nx in 0.34f..0.66f
}
