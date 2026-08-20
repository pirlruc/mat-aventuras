package pt.mataventuras.app.engine

import android.content.Context
import android.content.Intent
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.EngineKind
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.engineKindFor

/**
 * Native host → reward engine. 2D stays in-process; 3D runs in `:engine3d`.
 *
 * Plugin contract (MAT-003): a later Godot/Unity Activity may replace
 * [Kart3dActivity] or [Platformer2dActivity] when it:
 * 1. uses `android:process=":engine3d"` (or `:engine2d`) so the heap dies on finish,
 * 2. reads [EXTRA_MASCOT] and [EXTRA_NAME] from the launching Intent,
 * 3. returns [RESULT_FINISHED] via `setResult`,
 * 4. does not open Room, request INTERNET, or start analytics/cloud save.
 *
 * The Compose host must not hold a GL/Unity view. Domain simulation remains the
 * fallback when a plugin AAR is absent.
 */
object EngineLauncher {
    /** Intent extra: mascot code. */
    const val EXTRA_MASCOT: String = "mascot"

    /** Intent extra: child display name. */
    const val EXTRA_NAME: String = "name"

    /** Result extra: the reward level finished. */
    const val RESULT_FINISHED: String = "finished"

    /**
     * Isolated process name for the 3D (or later plugin) Activity.
     */
    const val PROCESS_ENGINE_3D: String = ":engine3d"

    /**
     * True when the reward Activity completed the level (host may award bonus points).
     */
    fun isFinished(
        resultCode: Int,
        finishedExtra: Boolean,
    ): Boolean = resultCode == android.app.Activity.RESULT_OK && finishedExtra

    /**
     * Intent for the age-appropriate reward Activity.
     */
    fun intentFor(context: Context, ageGroup: AgeGroup, mascot: Mascot, name: String): Intent {
        val destination = when (engineKindFor(ageGroup)) {
            EngineKind.TWO_D -> Platformer2dActivity::class.java
            EngineKind.THREE_D -> Kart3dActivity::class.java
        }
        return Intent(context, destination).apply {
            putExtra(EXTRA_MASCOT, mascot.code)
            putExtra(EXTRA_NAME, name)
        }
    }
}
