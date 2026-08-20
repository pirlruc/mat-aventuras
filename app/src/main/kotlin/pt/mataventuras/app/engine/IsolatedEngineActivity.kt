package pt.mataventuras.app.engine

import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.fragment.app.FragmentActivity
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.model.Mascot

/**
 * Base Activity for native rewards and the Godot plugin host.
 *
 * Subclasses must not open Room or request INTERNET. 3D (and any plugin)
 * should run in an isolated process so `finish()` kills the engine heap.
 * Extends [FragmentActivity] so a Godot fragment can attach on device
 * without sharing the Compose process.
 */
abstract class IsolatedEngineActivity : FragmentActivity() {
    /**
     * Native GLES surface to pause with the Activity. Null on the Godot path
     * and under Robolectric (no continuous GL thread).
     */
    internal var pauseableSurface: GLSurfaceView? = null

    override fun onPause() {
        pauseEngineSurface()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        resumeEngineSurface()
    }

    override fun onDestroy() {
        pauseableSurface = null
        super.onDestroy()
    }

    /**
     * Stops the native GL thread so a backgrounded fallback does not keep drawing.
     */
    internal fun pauseEngineSurface() {
        pauseableSurface?.onPause()
    }

    /**
     * Restarts the native GL thread after [onPause].
     */
    internal fun resumeEngineSurface() {
        pauseableSurface?.onResume()
    }

    /**
     * Mascot extra, or empty when the host omitted it.
     */
    internal fun mascotCode(): String =
        intent.getStringExtra(EnginePluginContract.EXTRA_MASCOT).orEmpty()

    /**
     * Child display name extra, or empty.
     */
    internal fun childName(): String =
        intent.getStringExtra(EnginePluginContract.EXTRA_NAME).orEmpty()

    /**
     * Resolves the mascot for tinting; unknown codes fall back to the hedgehog.
     */
    internal fun launchMascot(): Mascot = Mascot.fromCode(mascotCode())

    /**
     * Extras snapshot for tests and plugin hosts that need both fields.
     */
    internal fun extrasSnapshot(): Pair<String, String> = mascotCode() to childName()

    /**
     * Returns [EnginePluginContract.RESULT_FINISHED] and finishes this process's Activity.
     */
    internal fun completeReward(ok: Boolean) {
        val code = if (ok) RESULT_OK else RESULT_CANCELED
        setResult(
            code,
            Intent().putExtra(EnginePluginContract.RESULT_FINISHED, ok),
        )
        finish()
    }

    /**
     * [completeReward] posted to the UI thread (Godot callbacks arrive on the render thread).
     */
    internal fun completeRewardOnUi(ok: Boolean) {
        runOnUiThread { completeReward(ok) }
    }
}
