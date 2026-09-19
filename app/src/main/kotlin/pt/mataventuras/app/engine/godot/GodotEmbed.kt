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
    private const val ATTACH_RETRY_MS: Long = 1_200L
    private const val ATTACH_FORCE_MS: Long = 4_800L

    @Volatile
    private var restartedOnce: Boolean = false

    /**
     * Replaces the Activity content with a Godot fragment running [scene].
     * Waits until the host FrameLayout has a real size so GLES does not
     * start on a 0×0 SurfaceView.
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
        waitThenAttach(activity, container, scene)
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
        if (activity.isFinishing || activity.isDestroyed || alreadyAttached(activity)) return
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
     * Layout is the happy path. A 1.2 s retry still requires a real size so a
     * slow first layout does not recreate the 0×0 black screen. Only the 4.8 s
     * last resort attaches anyway, so a headless view cannot hang forever.
     */
    private fun waitThenAttach(
        activity: IsolatedEngineActivity,
        container: View,
        scene: String,
    ) {
        if (container.tag == WAITING) return
        container.tag = WAITING
        val listener =
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
                    commitFragment(activity, scene)
                }
            }
        container.addOnLayoutChangeListener(listener)
        val retry = Runnable { tryCommit(activity, container, scene, listener, force = false) }
        container.post(retry)
        container.postDelayed(retry, ATTACH_RETRY_MS)
        container.postDelayed(
            { tryCommit(activity, container, scene, listener, force = true) },
            ATTACH_FORCE_MS,
        )
    }

    private fun tryCommit(
        activity: IsolatedEngineActivity,
        container: View,
        scene: String,
        listener: View.OnLayoutChangeListener,
        force: Boolean,
    ) {
        if (activity.isFinishing || activity.isDestroyed || alreadyAttached(activity)) {
            container.removeOnLayoutChangeListener(listener)
            return
        }
        if (!force && !GodotRuntime.isSurfaceReady(container.width, container.height)) return
        container.removeOnLayoutChangeListener(listener)
        commitFragment(activity, scene)
    }
}
