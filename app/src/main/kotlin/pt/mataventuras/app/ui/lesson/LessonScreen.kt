package pt.mataventuras.app.ui.lesson

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.LessonFlow
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.PlayKind
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
    var exercise by remember { mutableStateOf(container.generator.generate(module, 0)) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(profile.points) }
    var confirmingExit by remember { mutableStateOf(false) }
    val startedAt = remember { System.currentTimeMillis() }
    val pointsGate = remember { Mutex() }
    val pointsTicket = remember { AtomicInteger(0) }
    val fillViewport = UiLogic.lessonFillsViewport(profile.ageGroup)
    val view = LocalView.current
    val cues = remember { AnswerCuePlayer.device() }
    var flashCorrect by remember { mutableStateOf(true) }
    var flashTick by remember { mutableIntStateOf(0) }

    DisposableEffect(cues) { onDispose { cues.release() } }

    LaunchedEffect(exercise.prompt) { onSpeak(exercise.spoken) }
    LaunchedEffect(profile.id) {
        container.profileTouches.collect { id ->
            if (id != profile.id) return@collect
            pointsGate.withLock {
                points = container.repository.getProfile(id)?.points ?: points
            }
        }
    }

    val onPick: (Int) -> Unit = { index ->
        val current = exercise
        val correct = current.isCorrect(index)
        flashCorrect = correct
        flashTick += 1
        cues.play(correct) { code -> view.performHapticFeedback(code) }
        onSpeak(if (correct) VoiceScripts.WELL_DONE else VoiceScripts.TRY_AGAIN)
        val delta = container.rewards.pointsForAttempt(correct)
        points = container.rewards.applyPoints(points, delta)
        val ticket = pointsTicket.incrementAndGet()
        if (correct) {
            hits += 1
            streak += 1
        } else {
            misses += 1
            streak = 0
        }
        scope.launch {
            pointsGate.withLock {
                val stored = container.repository.addPoints(profile.id, delta) ?: return@withLock
                if (ticket == pointsTicket.get()) points = stored.points
            }
        }
        if (container.rewards.shouldOpenReward(streak)) {
            onSpeak(VoiceScripts.LETS_PLAY)
            onReward(profile.ageGroup)
        }
        exercise = container.generator.generate(module, UiLogic.lessonLevel(hits))    }

    Box(modifier = Modifier.fillMaxSize()) {
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
        if (UiLogic.showsStarGrid(module, exercise.visualCount, exercise.play.kind)) {
            StarGrid(exercise.visualCount)
        }
        if (UiLogic.showsNumberHero(module, exercise.play.kind)) {
            NumberHero(exercise.visualCount)
        }
        UiLogic.targetShapeToDraw(module, exercise.targetShape, exercise.play.kind)?.let { ShapeGlyph(it) }
        if (UiLogic.showsPlayGrid(exercise.play.kind)) {
            PlayGrid(exercise = exercise, onPick = onPick)
        }
        if (UiLogic.showsCipherLegend(exercise.play.kind)) {
            CipherPanel(exercise)
        }
        if (UiLogic.showsPuzzleFrame(exercise.play.kind)) {
            PuzzleFrame(exercise)
        }
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
            if (UiLogic.showsOptionPalette(exercise.play.kind)) {
                exercise.options.forEachIndexed { index, text ->
                    Button(
                        onClick = { onPick(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(UiLogic.optionMinHeightDp(module, tokens.minButtonDp).dp)
                            .testTag(UiLogic.answerTag(exercise.isCorrect(index))),
                    ) {
                        VisualOption(module = module, text = text)
                    }
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
        AnswerFlash(correct = flashCorrect, tick = flashTick)
    }
}

@Composable
private fun AnswerFlash(
    correct: Boolean,
    tick: Int,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(tick) {
        if (tick == 0) return@LaunchedEffect
        alpha.snapTo(1f)
        alpha.animateTo(0f, animationSpec = tween(UiLogic.answerFlashMs()))
    }
    val fade = alpha.value
    if (!UiLogic.showsAnswerFlash(fade)) return
    val color = Color(UiLogic.answerFlashArgb(correct))
    val scale = UiLogic.answerFlashScale(fade)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(UiLogic.answerFlashScrimArgb(correct)).copy(alpha = fade * 0.55f))
            .testTag("answer-flash"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                this.alpha = fade
                scaleX = scale
                scaleY = scale
            },
        ) {
            Text(
                UiLogic.answerFlashGlyph(correct),
                fontSize = 96.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
            )
            Text(
                UiLogic.answerFlashCaption(correct),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun PlayGrid(
    exercise: Exercise,
    onPick: (Int) -> Unit,
) {
    val columns = exercise.play.columns.coerceAtLeast(1)
    val cells = exercise.play.cells.ifEmpty { exercise.options }
    val tappable = exercise.play.kind == PlayKind.SOUP
    val cellDp = UiLogic.playCellHeightDp(columns)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(columns).forEachIndexed { row, rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCells.forEachIndexed { col, cell ->
                    val index = row * columns + col
                    val hole = UiLogic.isBoardHole(cell)
                    Button(
                        onClick = { if (tappable) onPick(index) },
                        enabled = tappable,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = if (hole) Color(0xFFFFE082) else Color(0xFFBBDEFB),
                            disabledContentColor = Color(0xFF0D47A1),
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(cellDp.dp)
                            .testTag(
                                if (tappable) {
                                    UiLogic.answerTag(exercise.isCorrect(index))
                                } else {
                                    "board-cell-$index"
                                },
                            ),
                    ) {
                        GridCellFace(module = exercise.module, cell = cell)
                    }
                }
            }
        }
    }
}

@Composable
private fun CipherPanel(exercise: Exercise) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        exercise.play.cells.forEach { line ->
            Text(line, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            exercise.play.cipherCode,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0D47A1),
        )
    }
}

@Composable
private fun PuzzleFrame(exercise: Exercise) {
    val cells = exercise.play.cells.ifEmpty { listOf("1", "2", "3", "?") }
    val columns = exercise.play.columns.coerceAtLeast(2)
    val cellDp = UiLogic.playCellHeightDp(columns)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cell ->
                    val hole = UiLogic.isBoardHole(cell)
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = if (hole) Color(0xFFFFE082) else Color(0xFFBBDEFB),
                            disabledContentColor = Color(0xFF0D47A1),
                        ),
                        modifier = Modifier.size(cellDp.dp),
                    ) {
                        GridCellFace(module = exercise.module, cell = cell)
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCellFace(
    module: LearningModule,
    cell: String,
) {
    val label = UiLogic.holeLabel(cell)
    val shape = UiLogic.shapeKind(label)
    val dots = UiLogic.optionInt(label) ?: 0
    when {
        shape != null -> ShapeGlyph(shape)
        dots > 0 && module == LearningModule.COUNTING -> DotStrip(dots)
        else -> Text(label, fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
