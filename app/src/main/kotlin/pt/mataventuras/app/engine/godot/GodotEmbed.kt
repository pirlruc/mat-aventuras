package pt.mataventuras.app.engine.godot

import android.os.Bundle
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

    /**
     * Replaces the Activity content with a Godot fragment running [scene].
     */
    fun attach(
        activity: IsolatedEngineActivity,
        scene: String,
    ) {
        activity.setContentView(R.layout.godot_host)
        val existing = activity.supportFragmentManager.findFragmentByTag(TAG)
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
     * Command line used by [RewardGodotFragment] when arguments are missing.
     */
    fun fallbackScene(): String = GodotRuntime.SCENE_KART
}
