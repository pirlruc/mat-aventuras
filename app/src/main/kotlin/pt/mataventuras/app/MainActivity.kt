package pt.mataventuras.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.ui.lesson.RewardRecorder
import pt.mataventuras.app.ui.navigation.NavGraph
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Compose host. Reward engines launch as separate Activities.
 */
class MainActivity : ComponentActivity() {
    private lateinit var speech: SpeechEngine

    private val engineLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val finished =
            EngineLauncher.isFinished(
                result.resultCode,
                result.data?.getBooleanExtra(EngineLauncher.RESULT_FINISHED, false) ?: false,
            )
        speech.speak(VoiceScripts.rewardReturn(finished))
        val app = application as MatAventurasApp
        lifecycleScope.launch { RewardRecorder.apply(app.container, finished) }
    }

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

    override fun onDestroy() {
        speech.release()
        super.onDestroy()
    }
}
