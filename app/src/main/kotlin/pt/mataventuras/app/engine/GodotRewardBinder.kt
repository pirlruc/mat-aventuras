package pt.mataventuras.app.engine

import pt.mataventuras.app.engine.godot.GodotEmbed
import pt.mataventuras.plugin.KartPluginActivity
import pt.mataventuras.plugin.RunnerPluginActivity

/**
 * Chooses Godot (device) or the native Canvas/GLES fallback (Robolectric).
 *
 * [onGodot] defaults to [GodotEmbed.attach] and is only invoked when [embed]
 * is true, so Robolectric never resolves `libgodot_android`.
 */
internal object GodotRewardBinder {
    /**
     * Hosts the age-7 kart inside [activity].
     */
    fun bindKart(
        activity: KartPluginActivity,
        embed: Boolean = GodotRuntime.shouldEmbed(),
        onGodot: (IsolatedEngineActivity, String) -> Unit = GodotEmbed::attach,
    ) {
        if (embed) {
            onGodot(activity, GodotRuntime.SCENE_KART)
        } else {
            activity.nativeSession = NativeKartHost.attach(activity)
        }
    }

    /**
     * Hosts the age-3 runner inside [activity].
     */
    fun bindRunner(
        activity: RunnerPluginActivity,
        embed: Boolean = GodotRuntime.shouldEmbed(),
        onGodot: (IsolatedEngineActivity, String) -> Unit = GodotEmbed::attach,
    ) {
        if (embed) {
            onGodot(activity, GodotRuntime.SCENE_RUNNER)
        } else {
            activity.loop = NativeRunnerHost.attach(activity)
        }
    }
}
