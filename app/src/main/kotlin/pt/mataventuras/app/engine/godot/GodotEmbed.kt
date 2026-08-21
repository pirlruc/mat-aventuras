package pt.mataventuras.app.engine.godot

import android.os.Bundle
import android.view.View
import pt.mataventuras.app.R
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity

/**
 * Attaches [RewardGodotFragment] into [IsolatedEngineActivity].
 *
 * Loaded only on the device embed path. Robolectric never calls [attach].
 */
internal object GodotEmbed {
    private const val TAG: String = "godot"
    private const val WAITING: String = "godot-wait"
    private const val ATTACH_FALLBACK_MS: Long = 1_200L

    @Volatile
    private var restartedOnce: Boolean = false

    /**
     * Replaces the Activity content with a Godot fragment running [scene].
     * Waits until the host FrameLayout has a real size so GLES does not
     * start on a 0×0 SurfaceView (black screen).
     */
    fun attach(
        activity: IsolatedEngineActivity,
        scene: String,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (activity.findViewById<View>(R.id.godot_fragment_container) == null) {
            activity.setContentView(R.layout.godot_host)
        }
        val container = activity.findViewById<View>(R.id.godot_fragment_container)
        if (container == null || fragmentPresent(activity)) return
        if (GodotRuntime.isSurfaceReady(container.width, container.height)) {
            commitFragment(activity, scene)
        } else {
            waitThenAttach(activity, container, scene)
        }
    }

    /**
     * Asks the Compose host to relaunch this plugin Activity, then kills only
     * this isolated JVM so `libgodot_android` unloads.
     *
     * Godot's ProcessPhoenix stays stripped. Its default rebirth targets the
     * launcher, and starting the same `singleInstance` Activity from a dying
     * `:engine2d` / `:engine3d` process would drop `StartActivityForResult`.
     *
     * @return true when a restart result was delivered.
     */
    fun restartHost(activity: IsolatedEngineActivity): Boolean {
        val allowed =
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = restartedOnce,
                finishing = activity.isFinishing,
                destroyed = activity.isDestroyed,
                fromRelaunch = activity.isGodotRelaunch(),
            )
        if (!allowed || !activity.requestEngineRestart()) return false
        restartedOnce = true
        if (GodotRuntime.shouldEmbed()) {
            val view = activity.window?.decorView
            if (view != null) {
                view.post { killIsolatedProcess() }
            } else {
                killIsolatedProcess()
            }
        }
        return true
    }

    /**
     * Ends the current JVM. Isolated so Robolectric never calls it.
     */
    fun killIsolatedProcess() {
        Runtime.getRuntime().exit(0)
    }

    /**
     * Command line used by [RewardGodotFragment] when arguments are missing.
     */
    fun fallbackScene(): String = GodotRuntime.SCENE_KART

    private fun fragmentPresent(activity: IsolatedEngineActivity): Boolean {
        val existing =
            activity.supportFragmentManager.findFragmentById(R.id.godot_fragment_container)
                ?: activity.supportFragmentManager.findFragmentByTag(TAG)
        return existing is RewardGodotFragment
    }

    private fun commitFragment(
        activity: IsolatedEngineActivity,
        scene: String,
    ) {
        if (activity.isFinishing || activity.isDestroyed || fragmentPresent(activity)) return
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

    private fun waitThenAttach(
        activity: IsolatedEngineActivity,
        container: View,
        scene: String,
    ) {
        if (container.tag == WAITING) return
        container.tag = WAITING
        val attempt = Runnable { commitFragment(activity, scene) }
        container.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            if (GodotRuntime.isSurfaceReady(view.width, view.height)) view.post(attempt)
        }
        container.post {
            if (GodotRuntime.isSurfaceReady(container.width, container.height)) attempt.run()
        }
        container.postDelayed(attempt, ATTACH_FALLBACK_MS)
    }
}
