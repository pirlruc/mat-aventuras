package pt.mataventuras.app.engine

import android.view.MotionEvent
import pt.mataventuras.domain.engine.EngineInputMap

/**
 * Touch mapping for the isolated kart process (left steer, right steer, centre boost).
 * Bands come from [EngineInputMap] so the Godot kart reuses the same numbers.
 */
object Kart3dInput {
    /**
     * Maps a normalised horizontal tap to a steering value in `[-1, 1]`.
     */
    fun steerFromTouch(nx: Float): Float = EngineInputMap.steerFromNormalizedX(nx)

    /**
     * Returns true when the tap is in the boost band.
     */
    fun isBoostTouch(nx: Float): Boolean = EngineInputMap.isBoostBand(nx)

    /**
     * Centre-band boost is armed only on [MotionEvent.ACTION_DOWN].
     */
    fun boostOnAction(
        action: Int,
        nx: Float,
    ): Boolean = action == MotionEvent.ACTION_DOWN && isBoostTouch(nx)

    /**
     * Finger up or cancel clears steering.
     */
    fun releasesSteer(action: Int): Boolean =
        action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
}
