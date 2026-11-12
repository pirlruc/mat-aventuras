package pt.mataventuras.app.prize

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlin.random.Random
import pt.mataventuras.domain.prize.BalloonPop
import pt.mataventuras.domain.prize.BalloonRound

/**
 * Ages 3–5. Tap the balloon named by the prompt. Nothing else moves it.
 */
@Composable
fun BalloonPopScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    var round by remember { mutableStateOf(BalloonPop.round(random)) }
    Column {
        Text(round.prompt)
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = { finishIfPopped(round, index, onFinished) },
                modifier = Modifier.prizeMinHeight().testTag("balloon-$index"),
            ) {
                Text(label)
            }
        }
    }
}

internal fun finishIfPopped(
    round: BalloonRound,
    index: Int,
    onFinished: () -> Unit,
) {
    if (BalloonPop.popped(round, index)) onFinished()
}
