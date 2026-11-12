package pt.mataventuras.app.prize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import pt.mataventuras.domain.prize.BubbleRhythm
import pt.mataventuras.domain.prize.BubbleRound

/**
 * Ages 3–5. Pop the bubbles in order. A miss returns to the first bubble.
 */
@Composable
fun BubbleRhythmScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    var round by remember { mutableStateOf(BubbleRhythm.round(random)) }
    Column {
        Text(round.prompt)
        Text(modifier = Modifier.testTag("bubble-step"), text = "Bolhas: ${round.step}")
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = {
                    round = advanceBubbles(round, index, onFinished)
                },
                modifier = Modifier.prizeMinHeight().testTag("bubble-$index"),
            ) {
                Canvas(Modifier.size(28.dp)) {
                    drawCircle(Color(0xFF7E57C2))
                }
                Text(label)
            }
        }
    }
}

internal fun advanceBubbles(
    round: BubbleRound,
    index: Int,
    onFinished: () -> Unit,
): BubbleRound {
    val next = BubbleRhythm.pop(round, index)
    if (BubbleRhythm.done(next)) onFinished()
    return next
}
