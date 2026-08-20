package pt.mataventuras.app.engine

import android.os.Bundle

/**
 * Age-7 3D kart reward in an isolated `:engine3d` process.
 * GLES ES1 draws meshes produced by :domain. The 3D heap dies with the process.
 *
 * Production launches [pt.mataventuras.plugin.KartPluginActivity] (Godot, with
 * this Activity as the native fallback). Direct use remains for tests.
 */
class Kart3dActivity : IsolatedEngineActivity() {
    internal lateinit var session: KartSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = NativeKartHost.attach(this)
    }

    /**
     * Returns [EngineLauncher.RESULT_FINISHED] and finishes (isolated process then dies).
     */
    internal fun closeFinished() {
        completeReward(true)
    }
}
