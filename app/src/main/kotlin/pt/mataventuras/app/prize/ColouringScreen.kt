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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import pt.mataventuras.app.ui.lesson.colourOf
import pt.mataventuras.domain.prize.ColourRound
import pt.mataventuras.domain.prize.Colouring
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. Tap a colour to paint the picture. The right colour ends the prize.
 */
@Composable
fun ColouringScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    val round = remember { Colouring.round(random) }
    var paint by remember { mutableStateOf(round.labels.first()) }
    Column {
        Text(round.prompt)
        Text(VoiceScripts.COLOUR_SPOKEN)
        Canvas(Modifier.size(72.dp).testTag("paint")) {
            drawCircle(colourOf(paint))
        }
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = {
                    paint = label
                    finishIfPainted(round, index, onFinished)
                },
                modifier = Modifier.testTag("colour-$index"),
            ) {
                Text(label)
            }
        }
    }
}

internal fun finishIfPainted(
    round: ColourRound,
    index: Int,
    onFinished: () -> Unit,
) {
    if (Colouring.painted(round, index)) onFinished()
}
