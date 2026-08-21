package pt.mataventuras.app.ui.lesson

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.LessonFlow
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.domain.math.Exercise
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
    val pickLock = remember { AtomicBoolean(false) }
    val fillViewport = UiLogic.lessonFillsViewport(profile.ageGroup)
    val scrolls = UiLogic.lessonScrolls(profile.ageGroup, exercise.play.kind)
    val scrollState = rememberScrollState()
    val view = LocalView.current
    val cues = remember { AnswerCuePlayer.device() }
    var flashCorrect by remember { mutableStateOf(true) }
    var flashTick by remember { mutableIntStateOf(0) }

    DisposableEffect(cues) { onDispose { cues.release() } }
    RepeatPromptOnResume(spoken = exercise.spoken, onSpeak = onSpeak)

    LaunchedEffect(exercise.prompt) { onSpeak(exercise.spoken) }
    LaunchedEffect(profile.id) {
        container.profileTouches.collect { id ->
            if (id != profile.id) return@collect
            pointsGate.withLock {
                points = container.repository.getProfile(id)?.points ?: points
            }
        }
    }

    val noteAttempt: (Boolean, Boolean) -> Unit = handler@{ correct, advance ->
        if (!pickLock.compareAndSet(false, true)) return@handler
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
        if (advance) {
            if (container.rewards.shouldOpenReward(streak)) {
                onSpeak(VoiceScripts.LETS_PLAY)
                onReward(profile.ageGroup)
            }
            exercise = container.generator.generate(module, UiLogic.lessonLevel(hits))
        }
        pickLock.set(false)
    }
    val onPick: (Int) -> Unit = handler@{ index ->
        noteAttempt(exercise.isCorrect(index), true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LessonPlayColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                exercise = exercise,
                module = module,
                hits = hits,
                points = points,
                fillViewport = fillViewport,
                scrolls = scrolls,
                scrollState = scrollState,
                onPick = onPick,
                onMissKeep = { noteAttempt(false, false) },
            )
            LessonExitBar(
                ageGroup = profile.ageGroup,
                confirming = confirmingExit,
                onSpeak = onSpeak,
                onConfirm = { confirmingExit = true },
                onStay = { confirmingExit = false },
                onLeave = {
                    scope.launch {
                        LessonRecorder.persist(container, profile, module, hits, misses, startedAt)
                        onExit()
                    }
                },
            )
        }
        AnswerFlash(correct = flashCorrect, tick = flashTick)
    }
}

@Composable
private fun LessonPlayColumn(
    modifier: Modifier,
    exercise: Exercise,
    module: LearningModule,
    hits: Int,
    points: Int,
    fillViewport: Boolean,
    scrolls: Boolean,
    scrollState: ScrollState,
    onPick: (Int) -> Unit,
    onMissKeep: () -> Unit,
) {
    val tokens = LocalUiTokens.current
    Column(
        modifier = modifier
            .then(if (scrolls) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(20.dp),
        verticalArrangement = if (scrolls) Arrangement.spacedBy(12.dp) else Arrangement.Top,
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
        if (UiLogic.showsSudokuGrid(exercise.play.kind)) {
            Text(
                UiLogic.sudokuBanner(exercise.play.kind) ?: "Sudoku",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0D47A1),
                modifier = Modifier.testTag("sudoku-banner"),
            )
            SudokuBoard(exercise)
        }
        if (UiLogic.showsSoupBoard(exercise.play.kind)) {
            SoupBoard(exercise = exercise, onPick = onPick, onMissKeep = onMissKeep)
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
                .then(if (fillViewport && !scrolls) Modifier.weight(1f) else Modifier),
            verticalArrangement = if (fillViewport && !scrolls) {
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
    }
}

@Composable
private fun LessonExitBar(
    ageGroup: AgeGroup,
    confirming: Boolean,
    onSpeak: (String) -> Unit,
    onConfirm: () -> Unit,
    onStay: () -> Unit,
    onLeave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                if (LessonFlow.shouldAskExitConfirm(ageGroup, confirming)) {
                    onConfirm()
                    VoiceScripts.confirmExit(ageGroup)?.let(onSpeak)
                } else {
                    onLeave()
                }
            },
        ) { Text(LessonFlow.exitLabel(confirming)) }
        if (LessonFlow.showsStay(confirming)) {
            Button(onClick = onStay) { Text(VoiceScripts.STAY) }
        }
    }
}

@Composable
private fun RepeatPromptOnResume(
    spoken: String,
    onSpeak: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, spoken) {
        var stopped = false
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> stopped = true
                    Lifecycle.Event.ON_RESUME -> {
                        if (UiLogic.shouldRepeatSpokenPrompt(stopped)) onSpeak(spoken)
                        stopped = false
                    }
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
private fun SudokuBoard(exercise: Exercise) {
    val columns = exercise.play.columns.coerceAtLeast(1)
    val cells = exercise.play.cells.ifEmpty { exercise.options }
    val cellDp = UiLogic.playCellHeightDp(columns) + 8
    val boxW = UiLogic.sudokuBoxWidth(columns)
    val boxH = UiLogic.sudokuBoxHeight(columns)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D47A1))
            .padding(6.dp),
    ) {
        cells.chunked(columns).forEachIndexed { row, rowCells ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiLogic.sudokuGapDp(row, boxH).dp),
            ) {
                rowCells.forEachIndexed { col, cell ->
                    val index = row * columns + col
                    val hole = UiLogic.isBoardHole(cell)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(cellDp.dp)
                            .padding(start = UiLogic.sudokuGapDp(col, boxW).dp)
                            .background(if (hole) Color(0xFFFFF59D) else Color(0xFFE3F2FD))
                            .testTag("board-cell-$index"),
                    ) {
                        GridCellFace(module = exercise.module, cell = cell)
                    }
                }
            }
        }
    }
}

@Composable
private fun SoupBoard(
    exercise: Exercise,
    onPick: (Int) -> Unit,
    onMissKeep: () -> Unit,
) {
    val columns = exercise.play.columns.coerceAtLeast(1)
    val cells = exercise.play.cells.ifEmpty { exercise.options }
    val paths = exercise.play.soupPaths()
    var selected by remember(exercise.prompt) { mutableStateOf(emptyList<Int>()) }
    var found by remember(exercise.prompt) { mutableStateOf(emptySet<Int>()) }
    val foundCells = found.flatMap { paths.getOrElse(it) { emptyList() } }.toSet()
    val cellDp = UiLogic.playCellHeightDp(columns)
    val onGesture: (List<Int>) -> Unit = { path ->
        val match = UiLogic.soupMatchedPath(path, paths, found)
        if (match != null) {
            val next = found + match
            found = next
            if (next.size >= paths.size) onPick(paths[match].first())
        } else {
            val kind = UiLogic.soupReleaseKind(path, paths, found)
            if (UiLogic.soupKeepsBoard(kind, paths.size)) {
                onMissKeep()
            } else {
                UiLogic.soupPickIndex(path, paths, found, cells.size)?.let(onPick)
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(exercise.prompt, columns, cells.size, found) {
                awaitEachGesture {
                    val gap = 8.dp.toPx()
                    val path = collectSoupPath(columns, cells.size, gap, size.width.toFloat(), size.height.toFloat()) {
                        selected = it
                    }
                    onGesture(path)
                    selected = emptyList()
                }
            },
    ) {
        cells.chunked(columns).forEachIndexed { row, rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCells.forEachIndexed { col, cell ->
                    val index = row * columns + col
                    SoupCell(
                        module = exercise.module,
                        cell = cell,
                        correct = exercise.isCorrect(index),
                        selected = index in selected,
                        found = index in foundCells,
                        heightDp = cellDp,
                        onTap = { onGesture(listOf(index)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.SoupCell(
    module: LearningModule,
    cell: String,
    correct: Boolean,
    selected: Boolean,
    found: Boolean,
    heightDp: Int,
    onTap: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .height(heightDp.dp)
            .background(Color(UiLogic.soupSelectedArgb(selected, found)))
            .testTag(UiLogic.answerTag(correct))
            .semantics { onClick { onTap(); true } },
    ) {
        GridCellFace(module = module, cell = cell)
    }
}

private suspend fun AwaitPointerEventScope.collectSoupPath(
    columns: Int,
    cellCount: Int,
    gapPx: Float,
    width: Float,
    height: Float,
    onPath: (List<Int>) -> Unit,
): List<Int> {
    val down = awaitFirstDown()
    var path = listOfNotNull(
        UiLogic.soupIndexAt(down.position.x, down.position.y, width, height, columns, cellCount, gapPx),
    )
    onPath(path)
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == down.id } ?: break
        if (!change.pressed) {
            change.consume()
            break
        }
        val next = UiLogic.soupIndexAt(
            change.position.x,
            change.position.y,
            width,
            height,
            columns,
            cellCount,
            gapPx,
        )
        if (next != null) {
            path = UiLogic.soupExtendPath(path, next, columns)
            onPath(path)
        }
        change.consume()
    }
    return path
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
