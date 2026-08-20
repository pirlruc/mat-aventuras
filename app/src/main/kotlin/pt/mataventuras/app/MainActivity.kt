package pt.mataventuras.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.ui.RewardReturn
import pt.mataventuras.app.ui.lesson.RewardRecorder
import pt.mataventuras.app.ui.navigation.NavGraph

/**
 * Compose host. Reward engines launch as separate Activities.
 */
class MainActivity : ComponentActivity() {
    private lateinit var speech: SpeechEngine

    private val engineLauncher = registerForActivityResult(
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
                onReward = { ageGroup, mascot, name ->
                    engineLauncher.launch(EngineLauncher.intentFor(this, ageGroup, mascot, name))
                },
            )
        }
    }

    /**
     * Applies a reward Activity result to speech and the last profile.
     */
    internal fun onEngineResult(
        resultCode: Int,
        data: Intent?,
    ) {
        val finished = RewardReturn.onResult(resultCode, data, speech::speak)
        val app = application as MatAventurasApp
        lifecycleScope.launch { RewardRecorder.apply(app.container, finished) }
    }

    override fun onDestroy() {
        speech.release()
        super.onDestroy()
    }
}
