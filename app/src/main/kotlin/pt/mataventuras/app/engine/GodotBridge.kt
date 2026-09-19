package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.RewardCatalog
import pt.mataventuras.domain.model.EngineKind

/**
 * JVM bridge used by GDScript (`Engine.get_singleton("MatAventuras")`).
 * Kept free of Godot types so Robolectric can cover it.
 */
internal object GodotBridge {
    /**
     * Mascot extra for the Godot HUD tint.
     */
    fun mascotCode(host: IsolatedEngineActivity): String = host.mascotCode()

    /**
     * Child display name extra.
     */
    fun childName(host: IsolatedEngineActivity): String = host.childName()

    /**
     * Completes the reward on the UI thread and finishes the isolated process.
     */
    fun finish(
        host: IsolatedEngineActivity,
        ok: Boolean,
    ) {
        host.completeRewardOnUi(ok)
    }

    /**
     * Scene path GDScript should `change_scene_to_file` after the boot node.
     * Unknown paths fall back to the native prize for [kind].
     */
    fun rewardScene(
        requested: String,
        kind: EngineKind = EngineKind.THREE_D,
    ): String = RewardCatalog.packedScenePath(requested, kind)
}
