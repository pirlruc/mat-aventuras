package pt.mataventuras.plugin

import android.os.Bundle
import pt.mataventuras.app.engine.GodotRewardBinder
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GodotRewardBinder.bindRunner(this)
    }
}
