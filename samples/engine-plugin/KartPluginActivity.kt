package pt.mataventuras.plugin

import pt.mataventuras.app.engine.IsolatedEngineActivity

/**
 * TEMPLATE — not compiled into the host APK.
 *
 * Copy this class into a Godot 4 / Unity-as-a-library Android module and
 * keep the fully-qualified name. The host resolves it via
 * [pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS].
 *
 * If the player Activity must extend `Godot` or `UnityPlayerActivity`,
 * do not subclass [IsolatedEngineActivity]; instead:
 * 1. declare `android:process=":engine3d"` on that Activity,
 * 2. read extras `mascot` and `name`,
 * 3. `setResult(RESULT_OK, Intent().putExtra("finished", true))` then `finish()`.
 *
 * Never open Room. Never request INTERNET.
 */
class KartPluginActivity : IsolatedEngineActivity() {
    // Start Godot/Unity here, then completeReward(true) when the level ends.
}
