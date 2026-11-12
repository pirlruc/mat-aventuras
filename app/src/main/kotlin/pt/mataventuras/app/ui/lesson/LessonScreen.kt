package pt.mataventuras.app.ui.lesson

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pt.mataventuras.app.R
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.LessonFlow
import pt.mataventuras.app.ui.LocalCelebrate
import pt.mataventuras.app.ui.LocalPrizeClosed
import pt.mataventuras.app.ui.PromptCue
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.answerFlashArgb
import pt.mataventuras.app.ui.answerFlashCaption
import pt.mataventuras.app.ui.answerFlashGlyph
import pt.mataventuras.app.ui.answerFlashScale
import pt.mataventuras.app.ui.answerFlashScrimArgb
import pt.mataventuras.app.ui.groupsBanner
import pt.mataventuras.app.ui.lessonFillsViewport
import pt.mataventuras.app.ui.lessonLevel
import pt.mataventuras.app.ui.lessonScoreLine
import pt.mataventuras.app.ui.lessonScrolls
import pt.mataventuras.app.ui.optionAnswerTag
import pt.mataventuras.app.ui.optionMinHeightDp
import pt.mataventuras.app.ui.prizeStarLine
import pt.mataventuras.app.ui.prizeStars
import pt.mataventuras.app.ui.promptCue
import pt.mataventuras.app.ui.shouldDropPrizeHold
import pt.mataventuras.app.ui.shouldRepeatSpokenPrompt
import pt.mataventuras.app.ui.showsAnswerFlash
import pt.mataventuras.app.ui.showsCipherLegend
import pt.mataventuras.app.ui.showsGroupsBoard
import pt.mataventuras.app.ui.showsMemoryBoard
import pt.mataventuras.app.ui.showsNumberHero
import pt.mataventuras.app.ui.showsOptionPalette
import pt.mataventuras.app.ui.showsPuzzleFrame
import pt.mataventuras.app.ui.showsSilhouette
import pt.mataventuras.app.ui.showsSoupBoard
import pt.mataventuras.app.ui.showsStarGrid
import pt.mataventuras.app.ui.showsSudokuGrid
import pt.mataventuras.app.ui.showsSumDots
import pt.mataventuras.app.ui.showsWordBoard
import pt.mataventuras.app.ui.sudokuFillsEveryBlank
import pt.mataventuras.app.ui.targetShapeToDraw
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.SudokuPlay
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/** Changes when the lesson advances, even if the prompt sentence stays the same. */
internal val LocalExerciseId = compositionLocalOf { 0 }

/**
 * One exercise round. Prompts and options are pt-PT.
 */
@Composable
fun LessonScreen(
    container: AppContainer,
    profile: ChildProfile,
    module: LearningModule,
    onSpeak: (String) -> Unit,
    onReward: (AgeBand) -> Unit,
    onExit: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var exercise by remember { mutableStateOf(container.generator.generate(module, 0, profile.ageGroup)) }
    var exerciseId by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var completed by remember { mutableIntStateOf(profile.pace.completed) }
    var points by remember { mutableIntStateOf(profile.points) }
    var confirmingExit by remember { mutableStateOf(false) }
    val startedAt = remember { System.currentTimeMillis() }
    val pointsGate = remember { Mutex() }
    val pointsTicket = remember { AtomicInteger(0) }
    val pickLock = remember { AtomicBoolean(false) }
    val fillViewport = lessonFillsViewport(profile.ageGroup)
    val scrolls = lessonScrolls(profile.ageGroup, exercise.play.kind)
    val scrollState = rememberScrollState()
    val view = LocalView.current
    val cues = remember { AnswerCuePlayer.device() }
    var flashCorrect by remember { mutableStateOf(true) }
    var flashTick by remember { mutableIntStateOf(0) }

    DisposableEffect(cues) { onDispose { cues.release() } }
    val leaveLesson: () -> Unit = {
        scope.launch {
            LessonRecorder.persist(container, profile, module, hits, misses, startedAt)
            onExit()
        }
    }
    BackHandler {
        if (LessonFlow.shouldAskExitConfirm(profile.ageGroup, confirmingExit)) {
            confirmingExit = true
            onSpeak(VoiceScripts.confirmExit(profile.ageGroup))
        } else {
            leaveLesson()
        }
    }
    val celebrate = LocalCelebrate.current
    val prizeClosed = LocalPrizeClosed.current
    var holdPrompt by remember { mutableStateOf(false) }
    var prizeReturned by remember { mutableStateOf(false) }
    var announcedId by remember { mutableIntStateOf(-1) }
    var seenClosed by remember { mutableIntStateOf(prizeClosed) }
    LaunchedEffect(celebrate) {
        if (celebrate) prizeReturned = true
    }
    LaunchedEffect(prizeClosed) {
        val drop = shouldDropPrizeHold(prizeClosed, seenClosed, celebrate, holdPrompt)
        seenClosed = prizeClosed
        if (drop) holdPrompt = false
    }
    val cue = promptCue(holdPrompt, prizeReturned, celebrate)
    LaunchedEffect(exerciseId, cue) {
        if (cue == PromptCue.Wait) return@LaunchedEffect
        if (cue == PromptCue.Release) holdPrompt = false
        if (announcedId == exerciseId) return@LaunchedEffect
        announcedId = exerciseId
        onSpeak(exercise.spoken)
    }
    RepeatPromptOnResume(
        spoken = exercise.spoken,
        onSpeak = onSpeak,
        prizeCovering = celebrate || holdPrompt,
    )
    LaunchedEffect(profile.id) {
        container.profileTouches.collect { id ->
            if (id != profile.id) return@collect
            pointsGate.withLock {
                points = container.repository.getProfile(id)?.points ?: points
            }
        }
    }

    val fillSudoku = sudokuFillsEveryBlank(exercise.play)
    var sudoku by remember(exerciseId) {
        mutableStateOf(SudokuPlay.start(exercise.play.cells))
    }

    val noteAttempt: (Boolean, Boolean) -> Unit = handler@{ correct, advance ->
        if (!pickLock.compareAndSet(false, true)) return@handler
        flashCorrect = correct
        flashTick += 1
        val gentle = !correct && profile.ageGroup == AgeBand.THREE_TO_FIVE
        cues.play(correct, gentle) { code -> view.performHapticFeedback(code) }
        onSpeak(if (correct) VoiceScripts.WELL_DONE else VoiceScripts.TRY_AGAIN)
        val delta = container.rewards.pointsForAttempt(correct, profile.ageGroup)
        points = container.rewards.applyPoints(points, delta)
        val ticket = pointsTicket.incrementAndGet()
        if (correct) hits += 1 else misses += 1
        if (advance) {
            completed = container.rewards.nextCompleted(completed, true)
            if (container.rewards.shouldOpenReward(completed, profile.pace.every)) {
                holdPrompt = true
                prizeReturned = false
                onReward(profile.ageGroup)
            }
            exercise = container.generator.generate(module, lessonLevel(hits), profile.ageGroup)
            exerciseId += 1
        }
        scope.launch {
            pointsGate.withLock {
                val stored =
                    container.repository.addPoints(profile.id, delta, advance) ?: return@withLock
                if (ticket == pointsTicket.get()) points = stored.points
            }
        }
        pickLock.set(false)
    }
    val onPick: (Int) -> Unit = { index ->
        sudoku =
            scoreSudokuPick(
                fillSudoku = fillSudoku,
                exercise = exercise,
                sudoku = sudoku,
                index = index,
                onAttempt = noteAttempt,
            )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LessonExitBar(
                ageGroup = profile.ageGroup,
                confirming = confirmingExit,
                onSpeak = onSpeak,
                onConfirm = { confirmingExit = true },
                onStay = { confirmingExit = false },
                onLeave = leaveLesson,
            )
            CompositionLocalProvider(LocalExerciseId provides exerciseId) {
                LessonPlayColumn(
                    modifier =
                        Modifier
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
                    boardCells = if (fillSudoku) sudoku.cells else exercise.play.cells,
                    boardFocus = if (fillSudoku) sudoku.focus else -1,
                    fillEveryBlank = fillSudoku,
                    onBoardFocus = { index -> sudoku = SudokuPlay.focus(sudoku, index) },
                    age = profile.ageGroup,
                    completed = completed,
                    every = profile.pace.every,
                    onSpeak = onSpeak,
                )
            }
        }
        AnswerFlash(
            correct = flashCorrect,
            tick = flashTick,
            gentle = !flashCorrect && profile.ageGroup == AgeBand.THREE_TO_FIVE,
            mascot = profile.favouriteMascot.displayName,
        )
    }
}

@Composable
internal fun LessonPlayColumn(
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
    boardCells: List<String>,
    boardFocus: Int,
    fillEveryBlank: Boolean,
    onBoardFocus: (Int) -> Unit,
    age: AgeBand,
    completed: Int,
    every: Int,
    onSpeak: (String) -> Unit,
) {
    val tokens = LocalUiTokens.current
    Column(
        modifier =
            modifier
                .then(if (scrolls) Modifier.verticalScroll(scrollState) else Modifier)
                .padding(20.dp),
        verticalArrangement = if (scrolls) Arrangement.spacedBy(12.dp) else Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val listen = stringResource(R.string.listen)
        val listenAgain = stringResource(R.string.listen_again)
        val stars = stringResource(R.string.stars_toward, prizeStars(completed, every), every)
        val score = lessonScoreLine(hits, points, stringResource(R.string.score_line))
        Text(exercise.prompt, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button(
            onClick = { onSpeak(exercise.spoken) },
            modifier =
                Modifier
                    .height(64.dp)
                    .semantics(mergeDescendants = true) { contentDescription = listenAgain },
        ) { Text(listen) }
        Text(
            prizeStarLine(completed, every),
            modifier = Modifier.semantics { contentDescription = stars },
        )
        if (showsStarGrid(module, exercise.visualCount, exercise.play.kind)) {
            StarGrid(exercise.visualCount)
        }
        if (showsSumDots(module, exercise.visualCount)) {
            DotStrip(exercise.visualCount)
        }
        if (showsNumberHero(module, exercise.play.kind)) {
            NumberHero(exercise.visualCount)
        }
        targetShapeToDraw(module, exercise.targetShape, exercise.play.kind)?.let { ShapeGlyph(it) }
        if (showsSudokuGrid(exercise.play.kind)) {
            CappedScale {
                Text(
                    sudokuTitle(exercise.play.kind, age),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D47A1),
                    modifier = Modifier.testTag("sudoku-banner"),
                )
            }
            SudokuBoard(
                exercise = exercise,
                cells = boardCells,
                focus = boardFocus,
                fillEveryBlank = fillEveryBlank,
                onFocus = onBoardFocus,
                age = age,
            )
        }
        if (showsSoupBoard(exercise.play.kind)) {
            SoupBoard(exercise = exercise, onPick = onPick, onMissKeep = onMissKeep, age = age)
        }
        if (showsCipherLegend(exercise.play.kind)) {
            CipherPanel(exercise)
        }
        if (showsPuzzleFrame(exercise.play.kind)) {
            PuzzleFrame(exercise, age)
        }
        if (showsMemoryBoard(exercise.play.kind)) {
            MemoryBoard(exercise, onPick)
        }
        if (showsWordBoard(exercise.play.kind)) {
            WordCompleteBoard(exercise, onPick)
        }
        if (showsGroupsBoard(exercise.play.kind)) {
            Text(
                groupsBanner(module),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.testTag("groups-banner"),
            )
            GroupsBoard(exercise, age)
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (fillViewport && !scrolls) Modifier.weight(1f) else Modifier),
            verticalArrangement =
                if (fillViewport && !scrolls) {
                    Arrangement.SpaceEvenly
                } else {
                    Arrangement.spacedBy(12.dp)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (module == LearningModule.CLOCK) {
                key(LocalExerciseId.current) {
                    ClockFace(clockHour(exercise.options.getOrNull(exercise.correctIndex).orEmpty()))
                }
            }
            if (showsSilhouette(module)) {
                exercise.targetShape?.let { shape ->
                    val tag = if (module == LearningModule.SHADOW) "shadow" else "tangram"
                    Canvas(modifier = Modifier.size(96.dp).testTag(tag)) {
                        paintShapeGlyph(shape, Color.Black)
                    }
                }
            }
            if (showsOptionPalette(exercise.play.kind)) {
                exercise.options.forEachIndexed { index, text ->
                    Button(
                        onClick = { onPick(index) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(optionMinHeightDp(module, tokens.minButtonDp).dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription =
                                        if (
                                            module == LearningModule.SIZE ||
                                            module == LearningModule.SORT
                                        ) {
                                            "Opção ${index + 1}"
                                        } else {
                                            text
                                        }
                                }.testTag(
                                    optionAnswerTag(
                                        correct = exercise.isCorrect(index),
                                        fillEveryBlank = fillEveryBlank,
                                    ),
                                ),
                    ) {
                        VisualOption(module = module, text = text, option = index + 1)
                    }
                }
            }
        }
        Text(score)
    }
}

@Composable
internal fun LessonExitBar(
    ageGroup: AgeBand,
    confirming: Boolean,
    onSpeak: (String) -> Unit,
    onConfirm: () -> Unit,
    onStay: () -> Unit,
    onLeave: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                if (LessonFlow.shouldAskExitConfirm(ageGroup, confirming)) {
                    onConfirm()
                    onSpeak(VoiceScripts.confirmExit(ageGroup))
                } else {
                    onLeave()
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = LessonFlow.exitLabel(confirming)
                    },
        ) { Text(LessonFlow.exitLabel(confirming)) }
        if (LessonFlow.showsStay(confirming)) {
            Button(
                onClick = onStay,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) { Text(VoiceScripts.STAY) }
        }
    }
}

@Composable
internal fun RepeatPromptOnResume(
    spoken: String,
    onSpeak: (String) -> Unit,
    prizeCovering: Boolean = false,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, spoken, prizeCovering) {
        var stopped = false
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> stopped = true
                    Lifecycle.Event.ON_RESUME -> {
                        if (shouldRepeatSpokenPrompt(stopped, prizeCovering)) onSpeak(spoken)
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
internal fun AnswerFlash(
    correct: Boolean,
    tick: Int,
    gentle: Boolean,
    mascot: String,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(tick) {
        if (tick == 0) return@LaunchedEffect
        alpha.snapTo(1f)
        alpha.animateTo(0f, animationSpec = tween(UiLogic.ANSWER_FLASH_MS))
    }
    val fade = alpha.value
    if (!showsAnswerFlash(fade)) return
    val color = Color(answerFlashArgb(correct, gentle))
    val scale = answerFlashScale(fade)
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(answerFlashScrimArgb(correct, gentle)).copy(alpha = fade * 0.55f))
                .testTag("answer-flash"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier.graphicsLayer {
                    this.alpha = fade
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            CappedScale {
                Text(
                    answerFlashGlyph(correct, gentle, mascot),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                )
            }
            Text(
                answerFlashCaption(correct),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}
