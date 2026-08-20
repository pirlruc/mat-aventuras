package pt.mataventuras.app.engine

import android.os.Bundle

/**
 * Age-3 2D reward. Compose Canvas host used as the Godot fallback and in tests.
 *
 * Production launches [pt.mataventuras.plugin.RunnerPluginActivity] in `:engine2d`.
 */
class Platformer2dActivity : IsolatedEngineActivity() {
    internal lateinit var loop: Platformer2dLoop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loop = NativeRunnerHost.attach(this)
    }
}
