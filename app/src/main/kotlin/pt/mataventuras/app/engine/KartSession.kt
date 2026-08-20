package pt.mataventuras.app.engine

import android.view.MotionEvent
import pt.mataventuras.domain.engine.Kart3dEngine
import pt.mataventuras.domain.model.Mascot

/**
 * Touch + HUD wiring for the isolated kart Activity. Keeps GLES out of unit tests.
 */
internal class KartSession(
    mascot: Mascot,
    engine: Kart3dEngine = Kart3dEngine(),
    nowNs: () -> Long = { System.nanoTime() },
    lapsTarget: Int = 3,
    private val onHud: (String, String) -> Unit,
    private val onFinished: () -> Unit,
) {
    /** Renderer driven by this session. */
    val renderer: KartRenderer =
        KartRenderer(mascot, engine, nowNs, lapsTarget) { state, finished ->
            val overlay = kartOverlay(state)
            onHud(overlay.first, overlay.second)
            if (finished) onFinished()
        }

    /**
     * Maps a normalised tap to steer / boost. [action] is a [MotionEvent] action.
     */
    fun handleTouch(
        normalizedX: Float,
        action: Int,
    ): Boolean {
        renderer.steer = Kart3dInput.steerFromTouch(normalizedX)
        if (action == MotionEvent.ACTION_DOWN && Kart3dInput.isBoostTouch(normalizedX)) {
            renderer.boostRequested = true
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            renderer.steer = 0f
        }
        return true
    }
}
