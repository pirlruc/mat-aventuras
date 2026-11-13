package pt.mataventuras.app.engine.godot

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import pt.mataventuras.app.engine.EngineExtras
import pt.mataventuras.app.engine.GodotBridge
import pt.mataventuras.app.engine.GodotCopy
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity
import pt.mataventuras.app.engine.PrizeIntro
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.model.EngineKind
import pt.mataventuras.plugin.KartPluginActivity

/**
 * Runtime Godot plugin: extras in, `completeReward` out. No Room, no network.
 */
internal class MatAventurasGodotPlugin(
    godot: Godot,
    private val host: IsolatedEngineActivity,
    private val scene: String,
) : GodotPlugin(godot) {
    override fun getPluginName(): String = GodotRuntime.PLUGIN_NAME

    @UsedByGodot
    fun mascotCode(): String = GodotBridge.mascotCode(host)

    @UsedByGodot
    fun childName(): String = GodotBridge.childName(host)

    @UsedByGodot
    fun youngBand(): Boolean = GodotBridge.youngBand(host)

    @UsedByGodot
    fun worldTheme(): Int = EngineExtras.theme(host.intent)

    @UsedByGodot
    fun seasonIndex(): Int = EngineExtras.season(host.intent)

    @UsedByGodot
    fun hudText(key: String): String = GodotCopy.text(host, key)

    @UsedByGodot
    fun prizeLimitSeconds(): Int {
        val raw = host.intent.getStringExtra(EnginePluginContract.EXTRA_PRIZE_LIMIT)
        return raw?.toIntOrNull() ?: 0
    }

    @UsedByGodot
    fun introHolding(): Boolean = PrizeIntro.isHolding(host)

    @UsedByGodot
    fun livesLine(
        current: Int,
        maximum: Int,
    ): String = GodotBridge.livesLine(host, current, maximum)

    @UsedByGodot
    fun prizeReady() {
        GodotBridge.prizeReady(host)
    }

    @UsedByGodot
    fun rewardScene(): String {
        val kind = if (host is KartPluginActivity) EngineKind.THREE_D else EngineKind.TWO_D
        return GodotBridge.rewardScene(scene, kind)
    }

    @UsedByGodot
    fun completeReward(ok: Boolean) {
        GodotBridge.finish(host, ok)
    }
}
