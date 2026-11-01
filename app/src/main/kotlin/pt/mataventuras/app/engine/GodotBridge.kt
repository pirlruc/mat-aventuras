package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.PrizeHud
import pt.mataventuras.domain.engine.RewardCatalog
import pt.mataventuras.domain.model.AgeBand
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
     * True for the 3–5 band. The prize HUD then uses hearts.
     */
    fun youngBand(host: IsolatedEngineActivity): Boolean = host.ageCode() == AgeBand.THREE_TO_FIVE.name

    /**
     * Life line for the Godot label. Ages 3–5 get hearts.
     */
    fun livesLine(
        host: IsolatedEngineActivity,
        current: Int,
        maximum: Int,
    ): String = PrizeHud.livesLine(youngBand(host), current, maximum)

    /**
     * Hides the mascot overlay once the prize scene is drawing.
     */
    fun prizeReady(host: IsolatedEngineActivity) {
        host.runOnUiThread { PrizeBoot.ready(host) }
    }

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
     * Scene path GDScript should load after the boot node.
     * Unknown paths fall back to the native prize for [kind].
     */
    fun rewardScene(
        requested: String,
        kind: EngineKind = EngineKind.THREE_D,
    ): String = RewardCatalog.packedScenePath(requested, kind)
}
