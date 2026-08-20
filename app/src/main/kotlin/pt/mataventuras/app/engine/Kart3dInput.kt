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

    /**
     * Centre-band boost is armed only on [android.view.MotionEvent.ACTION_DOWN].
     */
    fun boostOnAction(
        action: Int,
        nx: Float,
    ): Boolean = action == android.view.MotionEvent.ACTION_DOWN && isBoostTouch(nx)

    /**
     * Finger up or cancel clears steering.
     */
    fun releasesSteer(action: Int): Boolean =
        action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL
}
