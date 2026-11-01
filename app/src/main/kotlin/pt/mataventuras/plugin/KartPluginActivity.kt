package pt.mataventuras.plugin

import android.content.Intent
import android.os.Bundle
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.IsolatedEngineActivity
import pt.mataventuras.app.engine.OffroadRacerLoop

/**
 * Age-7 Godot off-road host. Runs in `android:process=":engine3d"`.
 *
 * On device this attaches a GodotFragment. Under Robolectric it falls back
 * to the native Canvas racer so unit tests never load `libgodot_android`.
 */
class KartPluginActivity : IsolatedEngineActivity() {
    /** Native Canvas loop when Godot is not embedded. */
    internal var nativeSession: OffroadRacerLoop? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GodotRewardBinder.bindKart(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        acceptRelaunch(intent)
    }

    /**
     * Applies a new launch while this singleInstance activity is still up.
     */
    internal fun acceptRelaunch(intent: Intent) {
        setIntent(intent)
        GodotRewardBinder.bindKart(this)
    }
}
