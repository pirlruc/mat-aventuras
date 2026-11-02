package pt.mataventuras.app.prize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import pt.mataventuras.domain.prize.FishCatch
import pt.mataventuras.domain.prize.FishRound

/**
 * Ages 3–5. Tap the fish the prompt names. The prize clock still ends the activity.
 */
@Composable
fun FishingScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    val round = remember { FishCatch.round(random) }
    Column {
        Text(round.prompt)
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = { finishIfCaught(round, index, onFinished) },
                modifier = Modifier.testTag("fish-$index"),
            ) {
                Canvas(Modifier.size(28.dp).testTag("fish-mark-$index")) {
                    drawCircle(Color(0xFF1E88E5))
                }
                Text(label)
            }
        }
    }
}

internal fun finishIfCaught(
    round: FishRound,
    index: Int,
    onFinished: () -> Unit,
) {
    if (FishCatch.caught(round, index)) onFinished()
}
