package pt.mataventuras.app.engine

import android.content.Context
import android.content.Intent
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.EngineKind
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.engineKindFor

/**
 * Native host → reward engine. 2D stays in-process; 3D runs in `:engine3d`.
 */
object EngineLauncher {
    const val EXTRA_MASCOT: String = "mascot"
    const val EXTRA_NAME: String = "name"
    const val RESULT_FINISHED: String = "finished"

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
