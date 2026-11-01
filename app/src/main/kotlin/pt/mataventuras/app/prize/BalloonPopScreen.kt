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
                onClick = {
                    if (BalloonPop.popped(round, index)) onFinished()
                },
                modifier = Modifier.testTag("balloon-$index"),
            ) {
                Text(label)
            }
        }
    }
}
