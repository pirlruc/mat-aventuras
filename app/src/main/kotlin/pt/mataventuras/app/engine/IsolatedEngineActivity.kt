package pt.mataventuras.app.engine

import android.content.Intent
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
    private val rewardLock = Any()

    @Volatile
    private var rewardSettled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
}
