package pt.mataventuras.app.engine.godot

import android.os.Bundle
import pt.mataventuras.app.R
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity
import java.io.File

/**
 * Attaches [RewardGodotFragment] into [IsolatedEngineActivity].
 *
 * Loaded only on the device embed path. Robolectric never calls [attach].
 */
internal object GodotEmbed {
    private const val TAG: String = "godot"

    @Volatile
    private var restartedOnce: Boolean = false

    /**
     * Replaces the Activity content with a Godot fragment running [scene].
     */
    fun attach(
        activity: IsolatedEngineActivity,
        scene: String,
    ) {
        activity.setContentView(R.layout.godot_host)
        val existing = activity.supportFragmentManager.findFragmentById(R.id.godot_fragment_container)
        if (existing is RewardGodotFragment) return
        val fragment =
            RewardGodotFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(RewardGodotFragment.ARG_SCENE, scene)
                    }
            }
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.godot_fragment_container, fragment, TAG)
            .commitNowAllowingStateLoss()
    }

    /**
     * Kills this isolated engine process and relaunches the same plugin Activity.
     *
     * Godot cannot de-initialize native libs in-place. ProcessPhoenix is stripped
     * because it would reincarnate the default (Compose) process. [Runtime.exit]
     * only ends `:engine2d` / `:engine3d`.
     */
    fun restartHost(activity: IsolatedEngineActivity) {
        val marker = rebirthFile(activity)
        if (!GodotRuntime.shouldRestartHost(
                alreadyRestarted = restartedOnce,
                finishing = activity.isFinishing,
                destroyed = activity.isDestroyed,
                rebirthConsumed = marker.exists(),
            )
        ) {
            return
        }
        restartedOnce = true
        runCatching { marker.writeText("1") }
        activity.startActivity(
            GodotRuntime.isolatedRebirthIntent(activity.intent, activity.javaClass.name),
        )
        activity.finish()
        killIsolatedProcess()
    }

    /**
     * Drops the rebirth marker after the Godot main loop has started.
     */
    fun clearRebirthMarker(activity: IsolatedEngineActivity) {
        rebirthFile(activity).delete()
    }

    /**
     * Ends the current JVM. Isolated to keep [restartHost] testable at the edges.
     */
    fun killIsolatedProcess() {
        Runtime.getRuntime().exit(0)
    }

    /**
     * Command line used by [RewardGodotFragment] when arguments are missing.
     */
    fun fallbackScene(): String = GodotRuntime.SCENE_KART

    private fun rebirthFile(activity: IsolatedEngineActivity): File =
        File(activity.filesDir, "${GodotRuntime.REBIRTH_MARKER}_${activity.javaClass.simpleName}")
}
