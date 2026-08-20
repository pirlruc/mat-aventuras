package pt.mataventuras.app.engine

import android.content.Intent
import androidx.activity.ComponentActivity
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.model.Mascot

/**
 * Base Activity for native rewards and Godot/Unity plugin hosts.
 *
 * Subclasses must not open Room or request INTERNET. 3D (and any plugin)
 * should run in an isolated process so `finish()` kills the engine heap.
 */
abstract class IsolatedEngineActivity : ComponentActivity() {
    /**
     * Mascot extra, or empty when the host omitted it.
     */
    protected fun mascotCode(): String =
        intent.getStringExtra(EnginePluginContract.EXTRA_MASCOT).orEmpty()

    /**
     * Child display name extra, or empty.
     */
    protected fun childName(): String =
        intent.getStringExtra(EnginePluginContract.EXTRA_NAME).orEmpty()

    /**
     * Resolves the mascot for tinting; unknown codes fall back to the hedgehog.
     */
    protected fun launchMascot(): Mascot = Mascot.fromCode(mascotCode())

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
}
