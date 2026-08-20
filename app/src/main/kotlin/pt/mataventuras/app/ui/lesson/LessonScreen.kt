package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.LessonFlow
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.GeometricShape
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
    val fillViewport = UiLogic.lessonFillsViewport(profile.ageGroup)

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
            .then(
                if (fillViewport) {
                    Modifier
                } else {
                    Modifier.verticalScroll(rememberScrollState())
                },
            )
            .padding(20.dp),
        verticalArrangement = if (fillViewport) Arrangement.Top else Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(exercise.prompt, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (UiLogic.showsStarGrid(module, exercise.visualCount)) {
            StarGrid(exercise.visualCount)
        }
        if (UiLogic.showsNumberHero(module)) {
            NumberHero(exercise.visualCount)
        }
        UiLogic.targetShapeToDraw(module, exercise.targetShape)?.let { ShapeGlyph(it) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillViewport) Modifier.weight(1f) else Modifier),
            verticalArrangement = if (fillViewport) {
                Arrangement.SpaceEvenly
            } else {
                Arrangement.spacedBy(12.dp)
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            exercise.options.forEach { text ->
                Button(
                    onClick = {
                        val chosenIndex = exercise.options.indexOf(text)
                        val correct = chosenIndex >= 0 && exercise.isCorrect(chosenIndex)
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
                        .height(UiLogic.optionMinHeightDp(module, tokens.minButtonDp).dp)
                        .testTag("option:$text"),
                ) {
                    VisualOption(module = module, text = text)
                }
            }
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
private fun VisualOption(
    module: LearningModule,
    text: String,
) {
    val shape = if (UiLogic.showsShapeGlyph(module)) UiLogic.shapeKind(text) else null
    val dots = if (UiLogic.showsDotStrip(module)) UiLogic.optionInt(text) ?: 0 else 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (shape != null) {
            ShapeGlyph(shape)
        } else if (dots > 0) {
            DotStrip(dots)
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = if (shape != null) 18.sp else 28.sp)
    }
}

@Composable
private fun NumberHero(value: Int) {
    Text(
        text = value.toString(),
        fontSize = 88.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0D47A1),
    )
}

@Composable
private fun DotStrip(count: Int) {
    Canvas(modifier = Modifier.size(72.dp, 36.dp)) {
        UiLogic.starCenters(count, size.minDimension).forEach { (cx, cy) ->
            drawCircle(Color(0xFFFFC107), radius = 7f, center = Offset(cx, cy))
        }
    }
}

@Composable
private fun ShapeGlyph(shape: GeometricShape) {
    Canvas(modifier = Modifier.size(48.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2.4f
        val color = Color(0xFF1565C0)
        when (shape) {
            GeometricShape.CIRCLE -> drawCircle(color, radius = radius, center = Offset(cx, cy))
            GeometricShape.SQUARE -> {
                val side = radius * 1.6f
                drawRect(
                    color,
                    topLeft = Offset(cx - side / 2f, cy - side / 2f),
                    size = Size(side, side),
                )
            }
            GeometricShape.RECTANGLE -> {
                val width = radius * 2.1f
                val height = radius * 1.2f
                drawRect(
                    color,
                    topLeft = Offset(cx - width / 2f, cy - height / 2f),
                    size = Size(width, height),
                )
            }
            GeometricShape.TRIANGLE -> {
                val pts = UiLogic.triangleVertices(cx, cy, radius)
                drawPath(
                    Path().apply {
                        moveTo(pts[0].first, pts[0].second)
                        lineTo(pts[1].first, pts[1].second)
                        lineTo(pts[2].first, pts[2].second)
                        close()
                    },
                    color,
                )
            }
            GeometricShape.STAR -> {
                val pts = UiLogic.starVertices(cx, cy, radius)
                drawPath(
                    Path().apply {
                        moveTo(pts[0].first, pts[0].second)
                        pts.drop(1).forEach { lineTo(it.first, it.second) }
                        close()
                    },
                    color,
                )
            }
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
