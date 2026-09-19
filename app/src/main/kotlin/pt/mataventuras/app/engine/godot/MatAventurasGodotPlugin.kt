package pt.mataventuras.app.engine.godot

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import pt.mataventuras.app.engine.GodotBridge
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity
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
    fun rewardScene(): String {
        val kind = if (host is KartPluginActivity) EngineKind.THREE_D else EngineKind.TWO_D
        return GodotBridge.rewardScene(scene, kind)
    }

    @UsedByGodot
    fun completeReward(ok: Boolean) {
        GodotBridge.finish(host, ok)
    }
}
