package pt.mataventuras.app.engine

import android.os.Bundle

/**
 * Age-7 2.5D off-road reward in an isolated `:engine3d` process.
 * Canvas draws a rear-view dirt circuit. The heap dies with the process.
 *
 * Production launches [pt.mataventuras.plugin.KartPluginActivity] (Godot, with
 * this Activity as the native fallback). Direct use remains for tests.
 */
class Kart3dActivity : IsolatedEngineActivity() {
    internal lateinit var session: OffroadRacerLoop

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
