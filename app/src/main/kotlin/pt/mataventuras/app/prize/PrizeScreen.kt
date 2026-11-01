package pt.mataventuras.app.prize

import androidx.compose.runtime.Composable
import kotlin.random.Random
import pt.mataventuras.domain.prize.ComposePrize

/**
 * The Compose prize for [prize].
 */
@Composable
internal fun PrizeScreen(
    prize: ComposePrize,
    random: Random,
    onFinished: () -> Unit,
) {
    when (prize) {
        ComposePrize.BALLOONS -> BalloonPopScreen(random, onFinished)
        ComposePrize.SNAKE -> AnswerSnakeScreen(random, onFinished)
    }
}
