package pt.mataventuras.app.engine.godot

import org.godotengine.godot.Godot
import org.godotengine.godot.GodotFragment
import org.godotengine.godot.plugin.GodotPlugin
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.IsolatedEngineActivity

/**
 * GodotFragment that selects the kart or runner scene and registers the JVM bridge.
 *
 * [GodotFragment] forwards host APIs to a parent [org.godotengine.godot.GodotHost].
 * The plugin Activity is not a GodotHost (Robolectric must not load JNI types),
 * so this subclass supplies command line, plugins, and isolated-process restart.
 */
class RewardGodotFragment : GodotFragment() {
    override fun getCommandLine(): List<String> = GodotRuntime.commandLineFor()

    override fun getHostPlugins(engine: Godot): Set<GodotPlugin> {
        val host = activity as IsolatedEngineActivity
        val scene = arguments?.getString(ARG_SCENE) ?: GodotRuntime.SCENE_KART
        return setOf(MatAventurasGodotPlugin(engine, host, scene))
    }

    override fun onGodotRestartRequested(instance: Godot) {
        val host = activity as? IsolatedEngineActivity ?: return
        host.runOnUiThread { GodotEmbed.restartHost(host) }
    }

    override fun onGodotMainLoopStarted() {
        val host = activity as? IsolatedEngineActivity ?: return
        GodotEmbed.clearRebirthMarker(host)
    }

    override fun onGodotForceQuit(instance: Godot) {
        val host = activity as? IsolatedEngineActivity ?: return
        host.completeRewardOnUi(false)
    }

    companion object {
        /** Bundle key for `res://….tscn`. */
        const val ARG_SCENE: String = "scene"
    }
}
