package pt.mataventuras.app.engine

import android.content.Intent
import pt.mataventuras.domain.engine.EnginePluginContract

/**
 * Theme and season extras. A missing extra means that feature is locked.
 */
object EngineExtras {
    /**
     * Parent theme index, or -1 when the intent does not carry one.
     */
    fun theme(intent: Intent): Int = optionalInt(intent, EnginePluginContract.EXTRA_WORLD_THEME)

    /**
     * Season index, or -1 when the intent does not carry one.
     */
    fun season(intent: Intent): Int = optionalInt(intent, EnginePluginContract.EXTRA_SEASON)

    /**
     * Copies theme and season when [source] has them.
     */
    fun copyFamily(
        source: Intent,
        target: Intent,
    ) {
        copyInt(source, target, EnginePluginContract.EXTRA_WORLD_THEME)
        copyInt(source, target, EnginePluginContract.EXTRA_SEASON)
    }

    private fun optionalInt(
        intent: Intent,
        key: String,
    ): Int {
        if (!intent.hasExtra(key)) return -1
        return intent.getIntExtra(key, 0)
    }

    private fun copyInt(
        source: Intent,
        target: Intent,
        key: String,
    ) {
        if (!source.hasExtra(key)) return
        target.putExtra(key, source.getIntExtra(key, 0))
    }
}
