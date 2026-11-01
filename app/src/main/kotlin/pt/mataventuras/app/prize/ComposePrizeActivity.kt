package pt.mataventuras.app.prize

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlin.random.Random
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.prize.ComposePrize

/**
 * Compose reward. Balloons for ages 3–5 and the answer snake for ages 6–8.
 */
class ComposePrizeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prize = prizeFrom(intent.getStringExtra(EXTRA))
        val random = Random(intent.getLongExtra(SEED, 1L))
        setContent {
            MatAventurasTheme(ageFor(prize)) {
                PrizeScreen(prize, random) {
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
            seed: Long = 1L,
        ): Intent =
            Intent(context, ComposePrizeActivity::class.java)
                .putExtra(EXTRA, prize.name)
                .putExtra(SEED, seed)
    }
}

/**
 * Unknown extras stay on the balloon prize.
 */
internal fun prizeFrom(extra: String?): ComposePrize =
    ComposePrize.entries.firstOrNull { it.name == extra } ?: ComposePrize.BALLOONS

/**
 * Theme band for [prize].
 */
internal fun ageFor(prize: ComposePrize): AgeBand =
    if (prize == ComposePrize.BALLOONS) AgeBand.THREE_TO_FIVE else AgeBand.SIX_TO_EIGHT
