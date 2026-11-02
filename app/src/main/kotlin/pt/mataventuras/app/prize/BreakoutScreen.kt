package pt.mataventuras.app.prize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import pt.mataventuras.domain.prize.BreakoutRound
import pt.mataventuras.domain.prize.LetterBreakout

/**
 * Ages 6–8. Tap the letter the prompt names. The prize clock still ends the activity.
 */
@Composable
fun BreakoutScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    val round = remember { LetterBreakout.round(random) }
    Column {
        Text(round.prompt)
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = { finishBreakout(round, index, onFinished) },
                modifier = Modifier.testTag("breakout-$index"),
            ) {
                Canvas(Modifier.size(28.dp)) { drawCircle(Color(0xFF43A047)) }
                Text(label)
            }
        }
    }
}

internal fun finishBreakout(
    round: BreakoutRound,
    index: Int,
    onFinished: () -> Unit,
) {
    if (LetterBreakout.broken(round, index)) onFinished()
}
