package pt.mataventuras.app.engine

import pt.mataventuras.app.engine.godot.GodotEmbed
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.engine.RewardCatalog
import pt.mataventuras.domain.model.EngineKind
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
     * Hosts a 2D prize (runner, invaders, chomp, or climb) inside [activity].
     */
    fun bindRunner(
        activity: RunnerPluginActivity,
        embed: Boolean = GodotRuntime.shouldEmbed(),
        onGodot: (IsolatedEngineActivity, String) -> Unit = GodotEmbed::attach,
    ) {
        val game =
            RewardCatalog.fromName(
                activity.intent.getStringExtra(EnginePluginContract.EXTRA_SCENE),
                EngineKind.TWO_D,
            )
        val scene = RewardCatalog.scenePath(game)
        if (embed) {
            onGodot(activity, scene)
        } else {
            NativeRewardHost.attach(activity, game)
        }
    }
}
