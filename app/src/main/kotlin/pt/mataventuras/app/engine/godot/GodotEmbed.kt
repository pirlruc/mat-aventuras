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

    @Volatile
    private var restartedOnce: Boolean = false

    /**
     * Replaces the Activity content with a Godot fragment running [scene].
     */
    fun attach(
        activity: IsolatedEngineActivity,
        scene: String,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (activity.findViewById<View>(R.id.godot_fragment_container) == null) {
            activity.setContentView(R.layout.godot_host)
        }
        val container = activity.findViewById<View>(R.id.godot_fragment_container) ?: return
        if (alreadyAttached(activity)) return
        if (GodotRuntime.isSurfaceReady(container.width, container.height)) {
            commitFragment(activity, scene)
            return
        }
        container.post { waitForSurface(activity, container, scene) }
    }

    /**
     * Asks the Compose host to relaunch this plugin Activity, then kills only
     * this isolated JVM so `libgodot_android` unloads.
     *
     * Godot's ProcessPhoenix stays stripped. Its default rebirth targets the
     * launcher, and starting the same `singleInstance` Activity from a dying
     * `:engine2d` / `:engine3d` process would drop `StartActivityForResult`.
     */
    fun restartHost(activity: IsolatedEngineActivity) {
        if (!GodotRuntime.shouldRestartHost(
                alreadyRestarted = restartedOnce,
                finishing = activity.isFinishing,
                destroyed = activity.isDestroyed,
                fromRelaunch = activity.isGodotRelaunch(),
            )
        ) {
            return
        }
        if (!activity.requestEngineRestart()) return
        restartedOnce = true
        if (!GodotRuntime.shouldEmbed()) return
        val view = activity.window?.decorView
        if (view != null) {
            view.post { killIsolatedProcess() }
        } else {
            killIsolatedProcess()
        }
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

    private fun waitForSurface(
        activity: IsolatedEngineActivity,
        container: View,
        scene: String,
    ) {
        if (activity.isFinishing || activity.isDestroyed || alreadyAttached(activity)) return
        if (GodotRuntime.isSurfaceReady(container.width, container.height)) {
            commitFragment(activity, scene)
            return
        }
        container.addOnLayoutChangeListener(
            object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int,
                ) {
                    if (!GodotRuntime.isSurfaceReady(v.width, v.height)) return
                    v.removeOnLayoutChangeListener(this)
                    if (activity.isFinishing || activity.isDestroyed || alreadyAttached(activity)) return
                    commitFragment(activity, scene)
                }
            },
        )
    }

    private fun alreadyAttached(activity: IsolatedEngineActivity): Boolean {
        val existing =
            activity.supportFragmentManager.findFragmentById(R.id.godot_fragment_container)
                ?: activity.supportFragmentManager.findFragmentByTag(TAG)
        return existing is RewardGodotFragment
    }

    private fun commitFragment(
        activity: IsolatedEngineActivity,
        scene: String,
    ) {
        if (alreadyAttached(activity)) return
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
}
