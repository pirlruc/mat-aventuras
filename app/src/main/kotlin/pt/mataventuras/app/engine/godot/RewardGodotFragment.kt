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
 *
 * Do not call `renderView.onPause()` here. Pausing the GL surface on the UI
 * thread while the engine thread is swapping is what produced
 * `EGL_BAD_SURFACE` / `BufferQueue has no connected producer` and a black view.
 * [GodotFragment] already pauses and resumes the renderer in order.
 *
 * Do not treat [onGodotForceQuit] as a lost prize. Godot can request quit
 * during first-time GLES setup; finishing with `false` closed the game on
 * the first frame.
 */
class RewardGodotFragment : GodotFragment() {
    override fun getCommandLine(): List<String> = GodotRuntime.commandLineFor()

    override fun getHostPlugins(engine: Godot): Set<GodotPlugin> {
        val host = activity as? IsolatedEngineActivity ?: return emptySet()
        val scene = arguments?.getString(ARG_SCENE) ?: GodotRuntime.SCENE_KART
        return setOf(MatAventurasGodotPlugin(engine, host, scene))
    }

    override fun onGodotRestartRequested(instance: Godot) {
        val host = activity as? IsolatedEngineActivity ?: return
        host.runOnUiThread { GodotEmbed.restartHost(host) }
    }

    override fun onGodotForceQuit(instance: Godot) {
        val host = activity as? IsolatedEngineActivity ?: return
        host.runOnUiThread { host.onEngineForceQuit() }
    }

    override fun onGodotForceQuit(godotInstanceId: Int): Boolean {
        val host = activity as? IsolatedEngineActivity ?: return false
        host.runOnUiThread { host.onEngineForceQuit() }
        return true
    }

    companion object {
        /** Bundle key for `res://….tscn`. */
        const val ARG_SCENE: String = "scene"
    }
}
