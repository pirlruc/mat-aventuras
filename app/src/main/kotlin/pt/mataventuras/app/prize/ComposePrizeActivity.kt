package pt.mataventuras.app.prize

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlin.random.Random
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.prize.ComposePrize

/**
 * Compose reward. Balloons for ages 3–5 and the answer snake for ages 6–8.
 */
class ComposePrizeActivity : ComponentActivity() {
    override fun onDestroy() {
        PrizeLimit.cancel(this)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val deadline = intent.getLongExtra(PrizeLimit.EXTRA_DEADLINE, 0L)
        if (deadline > 0L) outState.putLong(PrizeLimit.EXTRA_DEADLINE, deadline)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restored = savedInstanceState?.getLong(PrizeLimit.EXTRA_DEADLINE, 0L) ?: 0L
        if (restored > 0L) intent.putExtra(PrizeLimit.EXTRA_DEADLINE, restored)
        val prize = prizeFrom(intent.getStringExtra(EXTRA))
        val random = if (intent.hasExtra(SEED)) Random(intent.getLongExtra(SEED, 1L)) else Random.Default
        PrizeLimit.arm(this)
        enableEdgeToEdge()
        setContent {
            MatAventurasTheme(ageFor(prize)) {
                PrizeScreen(
                    prize,
                    random,
                    introduce = true,
                    onExit = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                ) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA: String = "compose_prize"
        const val SEED: String = "compose_seed"

        /**
         * Intent for [prize]. [seed] keeps a test board stable.
         */
        fun intent(
            context: Context,
            prize: ComposePrize,
            seed: Long? = null,
            limitSeconds: Int = 0,
        ): Intent {
            val intent =
                Intent(context, ComposePrizeActivity::class.java)
                    .putExtra(EXTRA, prize.name)
            if (seed != null) intent.putExtra(SEED, seed)
            if (limitSeconds > 0) {
                intent.putExtra(EnginePluginContract.EXTRA_PRIZE_LIMIT, limitSeconds.toString())
            }
            return intent
        }
    }
}

/**
 * Unknown extras stay on the balloon prize.
 */
internal fun prizeFrom(extra: String?): ComposePrize {
    val match = ComposePrize.entries.firstOrNull { it.name == extra }
    return match ?: ComposePrize.BALLOONS
}

/**
 * Theme band for [prize].
 */
internal fun ageFor(prize: ComposePrize): AgeBand =
    when (prize) {
        ComposePrize.BALLOONS, ComposePrize.FISHING, ComposePrize.COLOURING, ComposePrize.BUBBLES ->
            AgeBand.THREE_TO_FIVE
        ComposePrize.SNAKE, ComposePrize.BREAKOUT, ComposePrize.TOWER, ComposePrize.RUNNER ->
            AgeBand.SIX_TO_EIGHT
    }
