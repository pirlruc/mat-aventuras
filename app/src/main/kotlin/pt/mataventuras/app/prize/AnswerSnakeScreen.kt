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
import pt.mataventuras.domain.prize.AnswerSnake
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. Tap the sum the snake should eat.
 */
@Composable
fun AnswerSnakeScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    var round by remember { mutableStateOf(AnswerSnake.round(random)) }
    Column {
        Text(round.prompt)
        Text(modifier = Modifier.testTag("snake-length"), text = "Cobra: ${round.length}")
        round.options.forEachIndexed { index, label ->
            Button(
                onClick = {
                    val next = AnswerSnake.bite(round, index, random)
                    round = next
                    if (AnswerSnake.full(next)) onFinished()
                },
                modifier = Modifier.testTag("snake-$index"),
            ) {
                Text(label)
            }
        }
        if (AnswerSnake.full(round)) Text(VoiceScripts.SNAKE_DONE)
    }
}
