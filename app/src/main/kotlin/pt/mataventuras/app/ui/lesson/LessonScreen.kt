package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * One exercise round. Prompts and options are pt-PT.
 */
@Composable
fun LessonScreen(
    container: AppContainer,
    profile: ChildProfile,
    module: LearningModule,
    onSpeak: (String) -> Unit,
    onReward: (AgeGroup) -> Unit,
    onExit: () -> Unit,
) {
    val tokens = LocalUiTokens.current
    val scope = rememberCoroutineScope()
    var exercise by remember { mutableStateOf(container.generator.generate(module)) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(profile.points) }
    val startedAt = remember { System.currentTimeMillis() }

    LaunchedEffect(exercise.prompt) { onSpeak(exercise.spoken) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(exercise.prompt, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (exercise.visualCount > 0 && module != LearningModule.NUMBERS) {
            StarGrid(exercise.visualCount)
        }
        exercise.options.forEachIndexed { index, text ->
            Button(
                onClick = {
                    val correct = exercise.isCorrect(index)
                    onSpeak(if (correct) VoiceScripts.WELL_DONE else VoiceScripts.TRY_AGAIN)
                    val delta = container.rewards.pointsForAttempt(correct)
                    points = container.rewards.applyPoints(points, delta)
                    if (correct) {
                        hits += 1
                        streak += 1
                    } else {
                        misses += 1
                        streak = 0
                    }
                    scope.launch {
                        val current = container.repository.getProfile(profile.id) ?: return@launch
                        container.repository.updateProfile(current.copy(points = points))
                    }
                    if (container.rewards.shouldOpenReward(streak)) {
                        onSpeak(VoiceScripts.LETS_PLAY)
                        onReward(profile.ageGroup)
                    }
                    exercise = container.generator.generate(module)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.minButtonDp.dp),
            ) { Text(text, fontWeight = FontWeight.Bold) }
        }
        Text("$hits certos · $points pts")
        Button(onClick = {
            scope.launch {
                LessonRecorder.persist(container, profile, points, module, hits, misses, startedAt)
                onExit()
            }
        }) { Text("Sair") }
    }
}

@Composable
private fun StarGrid(count: Int) {
    Canvas(modifier = Modifier.size(220.dp)) {
        val columns = 5
        val step = size.minDimension / 6f
        repeat(count) { i ->
            val cx = (i % columns) * step + step
            val cy = (i / columns) * step + step
            drawCircle(Color(0xFFFFC107), radius = 16f, center = Offset(cx, cy))
        }
    }
}

