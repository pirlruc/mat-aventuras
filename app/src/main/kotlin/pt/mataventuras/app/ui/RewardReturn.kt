package pt.mataventuras.app.ui

import android.content.Intent
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Host-side handling of a reward Activity result. Kept off MainActivity for tests.
 */
internal object RewardReturn {
    /**
     * Reads the finished extra, treating a missing Intent as not finished.
     */
    fun finishedExtra(data: Intent?): Boolean =
        data?.getBooleanExtra(EngineLauncher.RESULT_FINISHED, false) ?: false

    /**
     * Speaks the pt-PT return line and reports whether the level finished.
     */
    fun onResult(
        resultCode: Int,
        data: Intent?,
        speak: (String) -> Unit,
    ): Boolean {
        val finished = EngineLauncher.isFinished(resultCode, finishedExtra(data))
        speak(VoiceScripts.rewardReturn(finished))
        return finished
    }
}

/**
 * Age-3 icon navigation speaks the destination before opening it.
 */
internal object HomeNav {
    /**
     * Announces [label] then invokes [go].
     */
    fun announceAndGo(
        speak: (String) -> Unit,
        label: String,
        go: () -> Unit,
    ) {
        speak(label)
        go()
    }
}
