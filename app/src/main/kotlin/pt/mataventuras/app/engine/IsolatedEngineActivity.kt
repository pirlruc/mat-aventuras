package pt.mataventuras.app.engine

import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager
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

    private val rewardLock = Any()

    @Volatile
    private var rewardSettled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        pauseEngineSurface()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!rewardSettled) resumeEngineSurface()
    }

    override fun onDestroy() {
        pauseEngineSurface()
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
     * Drops continuous rendering once the reward has a result.
     * [GLSurfaceView.setRenderMode] requires a GL thread; skip when none exists
     * (Robolectric, or finish before [setRenderer]).
     */
    internal fun stopEngineSurface() {
        val surface = pauseableSurface ?: return
        runCatching { surface.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY }
        surface.onPause()
    }

    /**
     * True after [completeReward] has already delivered a result.
     */
    internal fun isRewardSettled(): Boolean = rewardSettled

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
     * Reward scene extra, or empty when the host omitted it.
     */
    internal fun sceneCode(): String =
        intent.getStringExtra(EnginePluginContract.EXTRA_SCENE).orEmpty()

    /**
     * Resolves the mascot for tinting; unknown codes fall back to the hedgehog.
     */
    internal fun launchMascot(): Mascot = Mascot.fromCode(mascotCode())

    /**
     * Extras snapshot for tests and plugin hosts that need both fields.
     */
    internal fun extrasSnapshot(): Pair<String, String> = mascotCode() to childName()

    /**
     * True when the Compose host already relaunched this Activity after GLES setup.
     */
    internal fun isGodotRelaunch(): Boolean =
        intent.getBooleanExtra(EngineLauncher.EXTRA_GODOT_RELAUNCH, false)

    /**
     * Returns [EnginePluginContract.RESULT_FINISHED] and finishes this process's Activity.
     * A second call, or a call after destroy, is ignored so back-press cannot
     * cancel a just-finished reward.
     */
    internal fun completeReward(ok: Boolean): Boolean {
        val code = if (ok) RESULT_OK else RESULT_CANCELED
        return settleResult(code, Intent().putExtra(EnginePluginContract.RESULT_FINISHED, ok))
    }

    /**
     * Asks the Compose host to relaunch this plugin Activity in a fresh isolated
     * process. Preserves `StartActivityForResult` so a GLES restart cannot drop
     * the reward contract or reincarnate the host.
     */
    internal fun requestEngineRestart(): Boolean =
        settleResult(
            RESULT_OK,
            EngineLauncher.restartResultIntent(javaClass.name, mascotCode(), childName(), sceneCode()),
        )

    /**
     * Delivers [result] once. Later [completeReward] / [requestEngineRestart] calls no-op.
     */
    private fun settleResult(
        code: Int,
        result: Intent,
    ): Boolean {
        synchronized(rewardLock) {
            if (rewardSettled) return false
            if (isDestroyed) return false
            if (isFinishing) return false
            rewardSettled = true
        }
        stopEngineSurface()
        setResult(code, result)
        finish()
        return true
    }

    /**
     * [completeReward] posted to the UI thread (Godot callbacks arrive on the render thread).
     */
    internal fun completeRewardOnUi(ok: Boolean) {
        if (isDestroyed) return
        runOnUiThread { completeReward(ok) }
    }

    /**
     * GLES force-quit during boot. Restarts once; never marks the prize as lost
     * because that is what made invaders (and other scenes) close on first paint.
     */
    internal fun onEngineForceQuit(): Boolean {
        if (isRewardSettled()) return false
        if (isFinishing || isDestroyed) return false
        if (isGodotRelaunch()) return false
        return requestEngineRestart()
    }
}
