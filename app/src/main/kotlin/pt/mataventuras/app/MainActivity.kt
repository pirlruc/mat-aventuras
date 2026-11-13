package pt.mataventuras.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.prize.PrizeLaunch
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.ui.LocalPrizeClosed
import pt.mataventuras.app.ui.RewardReturn
import pt.mataventuras.app.ui.beginReward
import pt.mataventuras.app.ui.lesson.RewardRecorder
import pt.mataventuras.app.ui.navigation.NavGraph
import pt.mataventuras.domain.entitlement.FamilyGate
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.Mascot

/**
 * Compose host. Reward engines launch as separate Activities.
 */
class MainActivity : ComponentActivity() {
    private lateinit var speech: SpeechEngine
    private var celebrateWin by mutableStateOf(false)
    private var prizeClosed by mutableIntStateOf(0)

    private val engineLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result -> onEngineResult(result.resultCode, result.data) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MatAventurasApp
        speech = SpeechEngine(this)
        setContent {
            CompositionLocalProvider(LocalPrizeClosed provides prizeClosed) {
                NavGraph(
                    container = app.container,
                    onSpeak = { speech.speak(it) },
                    onReward = { ageGroup, mascot, name, limit ->
                        beginReward(speech::stop) {
                            engineLauncher.launch(
                                rewardIntent(this, app.container, ageGroup, mascot, name, limit),
                            )
                        }
                    },
                    celebrate = celebrateWin,
                    onDismissWin = { celebrateWin = false },
                )
            }
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
        prizeClosed += 1
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

/**
 * Reward intent for the parent theme and the device date, when those features are unlocked.
 */
internal fun rewardIntent(
    context: Context,
    container: AppContainer,
    age: AgeBand,
    mascot: Mascot,
    name: String,
    limit: Int,
): Intent {
    val today = LocalDate.now()
    val family =
        FamilyGate.extras(
            container.entitlements,
            container.family.theme(),
            today.monthValue,
            today.dayOfMonth,
        )
    return PrizeLaunch.intentForReward(
        context,
        age,
        mascot,
        name,
        container.rewards.prizeLimitSeconds(limit),
        family,
    )
}
