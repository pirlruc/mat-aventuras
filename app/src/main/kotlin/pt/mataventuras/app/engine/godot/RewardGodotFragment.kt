package pt.mataventuras.app.engine.godot

import org.godotengine.godot.Godot
import org.godotengine.godot.GodotFragment
import org.godotengine.godot.plugin.GodotPlugin
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity

/**
 * GodotFragment that selects the kart or runner scene and registers the JVM bridge.
 */
class RewardGodotFragment : GodotFragment() {
    override fun getCommandLine(): List<String> {
        val scene = arguments?.getString(ARG_SCENE) ?: GodotRuntime.SCENE_KART
        return GodotRuntime.commandLineFor(scene)
    }

    override fun getHostPlugins(engine: Godot): Set<GodotPlugin> {
        val host = activity as IsolatedEngineActivity
        return setOf(MatAventurasGodotPlugin(engine, host))
    }

    companion object {
        /** Bundle key for `res://….tscn`. */
        const val ARG_SCENE: String = "scene"
    }
}
