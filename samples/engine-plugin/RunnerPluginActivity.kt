package pt.mataventuras.plugin

import pt.mataventuras.app.engine.IsolatedEngineActivity

/**
 * TEMPLATE — not compiled into the host APK.
 *
 * Age-3 ring runner. Keep this fully-qualified name
 * ([pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS]).
 * Declare `android:process=":engine2d"` on the real plugin Activity.
 */
class RunnerPluginActivity : IsolatedEngineActivity() {
    // Start Godot/Unity here, then completeReward(true) when the level ends.
}
