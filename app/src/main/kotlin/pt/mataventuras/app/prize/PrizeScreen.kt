package pt.mataventuras.app.prize

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import pt.mataventuras.app.debugInstantWin
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.domain.engine.PrizeBrief
import pt.mataventuras.domain.engine.PrizeHud
import pt.mataventuras.domain.prize.ComposePrize
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * The Compose prize for [prize]. [introduce] speaks the goal before play.
 */
@Composable
internal fun PrizeScreen(
    prize: ComposePrize,
    random: Random,
    introduce: Boolean = false,
    onExit: () -> Unit = {},
    onFinished: () -> Unit,
) {
    var playing by remember(prize) { mutableStateOf(!introduce) }
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().prizeMinHeight(),
        ) { Text(PrizeHud.BACK_LABEL) }
        if (!playing) {
            PrizeIntroCard(PrizeBrief.composeKey(prize.name)) { playing = true }
        } else {
            if (debugInstantWin && introduce) {
                DebugWinButton(onFinished)
            }
            PrizeBody(prize, random, onFinished)
        }
    }
}

@Composable
private fun PrizeIntroCard(
    scene: String,
    onPlay: () -> Unit,
) {
    val context = LocalContext.current
    val speech = remember { SpeechEngine(context) }
    DisposableEffect(speech) { onDispose { speech.release() } }
    LaunchedEffect(scene) {
        speech.speak("${VoiceScripts.LETS_PLAY} ${PrizeBrief.goal(scene)} ${PrizeBrief.controls(scene)}")
    }
    Text(PrizeBrief.goal(scene))
    Text(PrizeBrief.controls(scene))
    Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().prizeMinHeight()) {
        Text(PrizeHud.PLAY_LABEL)
    }
}

@Composable
private fun PrizeBody(
    prize: ComposePrize,
    random: Random,
    onFinished: () -> Unit,
) {
    when (prize) {
        ComposePrize.BALLOONS -> BalloonPopScreen(random, onFinished)
        ComposePrize.FISHING -> FishingScreen(random, onFinished)
        ComposePrize.COLOURING -> ColouringScreen(random, onFinished)
        ComposePrize.BUBBLES -> BubbleRhythmScreen(random, onFinished)
        ComposePrize.SNAKE -> AnswerSnakeScreen(random, onFinished)
        ComposePrize.BREAKOUT -> BreakoutScreen(random, onFinished)
        ComposePrize.TOWER -> TowerScreen(random, onFinished)
        ComposePrize.RUNNER -> RunnerGateScreen(random, onFinished)
    }
}

/** Compose prize controls stay at least 64 dp in both axes. */
internal fun Modifier.prizeMinHeight(): Modifier = defaultMinSize(minWidth = 64.dp, minHeight = 64.dp)
