package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.LessonFlow
import pt.mataventuras.app.ui.UiLogic
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
    var confirmingExit by remember { mutableStateOf(false) }
    val startedAt = remember { System.currentTimeMillis() }
    val pointsGate = remember { Mutex() }

    LaunchedEffect(exercise.prompt) { onSpeak(exercise.spoken) }
    LaunchedEffect(profile.id) {
        container.profileTouches.collect { id ->
            if (id != profile.id) return@collect
            pointsGate.withLock {
                points = container.repository.getProfile(id)?.points ?: points
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(exercise.prompt, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (UiLogic.showsStarGrid(module, exercise.visualCount)) {
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
                        pointsGate.withLock {
                            val stored =
                                container.repository.addPoints(profile.id, delta) ?: return@withLock
                            points = stored.points
                        }
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
        Text(UiLogic.lessonScoreLine(hits, points))
        Button(onClick = {
            if (LessonFlow.shouldAskExitConfirm(profile.ageGroup, confirmingExit)) {
                confirmingExit = true
                VoiceScripts.confirmExit(profile.ageGroup)?.let(onSpeak)
            } else {
                scope.launch {
                    LessonRecorder.persist(container, profile, module, hits, misses, startedAt)
                    onExit()
                }
            }
        }) { Text(LessonFlow.exitLabel(confirmingExit)) }
        if (LessonFlow.showsStay(confirmingExit)) {
            Button(onClick = { confirmingExit = false }) { Text(VoiceScripts.STAY) }
        }
    }
}

@Composable
private fun StarGrid(count: Int) {
    Canvas(modifier = Modifier.size(220.dp)) {
        UiLogic.starCenters(count, size.minDimension).forEach { (cx, cy) ->
            drawCircle(Color(0xFFFFC107), radius = 16f, center = Offset(cx, cy))
        }
    }
}

