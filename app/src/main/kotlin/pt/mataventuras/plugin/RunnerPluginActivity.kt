package pt.mataventuras.plugin

import android.os.Bundle
import pt.mataventuras.app.engine.ChompLoop
import pt.mataventuras.app.engine.ClimbLoop
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.InvadersLoop
import pt.mataventuras.app.engine.IsolatedEngineActivity
import pt.mataventuras.app.engine.Platformer2dLoop

/**
 * Age-3 Godot ring-runner host. Runs in `android:process=":engine2d"`.
 *
 * On device this attaches a GodotFragment. Under Robolectric it falls back
 * to the native Compose Canvas runner.
 */
class RunnerPluginActivity : IsolatedEngineActivity() {
    /** Native Canvas loop when Godot is not embedded. */
    internal var loop: Platformer2dLoop? = null

    /** Letter-invaders loop on the native fallback. */
    internal var invaders: InvadersLoop? = null

    /** Maze-chomp loop on the native fallback. */
    internal var chomp: ChompLoop? = null

    /** Letter-climb loop on the native fallback. */
    internal var climb: ClimbLoop? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GodotRewardBinder.bindRunner(this)
    }
}
