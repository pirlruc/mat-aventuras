package pt.mataventuras.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.prize.PrizeLaunch
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.ui.RewardReturn
import pt.mataventuras.app.ui.lesson.RewardRecorder
import pt.mataventuras.app.ui.navigation.NavGraph

/**
 * Compose host. Reward engines launch as separate Activities.
 */
class MainActivity : ComponentActivity() {
    private lateinit var speech: SpeechEngine
    private var celebrateWin by mutableStateOf(false)

    private val engineLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result -> onEngineResult(result.resultCode, result.data) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MatAventurasApp
        speech = SpeechEngine(this)
        setContent {
            NavGraph(
                container = app.container,
                onSpeak = { speech.speak(it) },
                onReward = { ageGroup, mascot, name, limit ->
                    val seconds = app.container.rewards.prizeLimitSeconds(limit)
                    engineLauncher.launch(
                        PrizeLaunch.intent(this, ageGroup, mascot, name, limitSeconds = seconds),
                    )
                },
                celebrate = celebrateWin,
                onDismissWin = { celebrateWin = false },
            )
        }
    }

    /**
     * Applies a reward Activity result to speech and the last profile.
     * A GLES restart extra relaunches the same plugin Activity instead.
     */
    internal fun onEngineResult(
        resultCode: Int,
        data: Intent?,
    ) {
        if (isDestroyed) return
        if (isFinishing) return
        if (data?.getBooleanExtra(EngineLauncher.RESULT_RESTART, false) == true) {
            EngineLauncher.relaunchIntent(this, data)?.let { engineLauncher.launch(it) }
            return
        }
        val finished = RewardReturn.onResult(resultCode, data, speech::speak)
        if (finished) celebrateWin = true
        val app = application as MatAventurasApp
        lifecycleScope.launch(Dispatchers.IO) {
            RewardRecorder.apply(app.container, finished)
        }
    }

    override fun onStop() {
        if (::speech.isInitialized) speech.stop()
        super.onStop()
    }

    override fun onDestroy() {
        if (::speech.isInitialized) speech.release()
        super.onDestroy()
    }
}
