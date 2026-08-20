package pt.mataventuras.app.engine.godot

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import pt.mataventuras.app.engine.GodotBridge
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity

/**
 * Runtime Godot plugin: extras in, `completeReward` out. No Room, no network.
 */
internal class MatAventurasGodotPlugin(
    godot: Godot,
    private val host: IsolatedEngineActivity,
) : GodotPlugin(godot) {
    override fun getPluginName(): String = GodotRuntime.PLUGIN_NAME

    @UsedByGodot
    fun mascotCode(): String = GodotBridge.mascotCode(host)

    @UsedByGodot
    fun childName(): String = GodotBridge.childName(host)

    @UsedByGodot
    fun completeReward(ok: Boolean) {
        GodotBridge.finish(host, ok)
    }
}
