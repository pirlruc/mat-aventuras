package pt.mataventuras.plugin

import android.os.Bundle
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.IsolatedEngineActivity
import pt.mataventuras.app.engine.KartSession

/**
 * Age-7 Godot kart host. Runs in `android:process=":engine3d"`.
 *
 * On device this attaches a GodotFragment. Under Robolectric it falls back
 * to the native GLES kart so unit tests never load `libgodot_android`.
 */
class KartPluginActivity : IsolatedEngineActivity() {
    /** Native GLES session when Godot is not embedded. */
    internal var nativeSession: KartSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GodotRewardBinder.bindKart(this)
    }
}
