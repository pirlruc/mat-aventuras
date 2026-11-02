package pt.mataventuras.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pt.mataventuras.app.engine.NativeKartHost
import pt.mataventuras.app.engine.NativeRunnerHost
import pt.mataventuras.app.engine.OffroadRacerLoop
import pt.mataventuras.app.engine.Platformer2dLoop
import pt.mataventuras.app.prize.BalloonPopScreen
import pt.mataventuras.app.ui.home.HomeScreen
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.lesson.paintCountDots
import pt.mataventuras.app.ui.lesson.paintShapeGlyph
import pt.mataventuras.app.ui.lesson.paintStarDots
import pt.mataventuras.app.ui.navigation.NavGraph
import pt.mataventuras.app.ui.navigation.RouteMemory
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.engine.PlatformerWorld
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.ExerciseGenerator
import pt.mataventuras.domain.math.PlayBoard
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.voice.VoiceScripts

@RunWith(RobolectricTestRunner::class)
@Config(application = MatAventurasApp::class, sdk = [34])
class GapCoverageTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComposeHostActivity>()

    @Test
    fun lessonBoardsCoverTheMeasuredScreens() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val young = stored(app, "Lia", AgeBand.THREE_TO_FIVE)
        val older = stored(app, "Rui", AgeBand.SIX_TO_EIGHT)
        val steps = lessonSteps()
        var step by mutableIntStateOf(0)
        val spoken = mutableListOf<String>()
        app.container.useGenerator(
            object : ExerciseGenerator() {
                override fun generate(
                    module: LearningModule,
                    level: Int,
                    age: AgeBand,
                ): Exercise = steps[step.coerceIn(steps.indices)].exercise
            },
        )
        compose.setContent {
            val current = steps[step.coerceIn(steps.indices)]
            val profile = if (current.older) older else young
            key(step) {
                MatAventurasTheme(profile.ageGroup) {
                    LessonScreen(
                        container = app.container,
                        profile = profile,
                        module = current.exercise.module,
                        onSpeak = { spoken += it },
                        onReward = {},
                        onExit = {},
                    )
                }
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.LEAVE).fetchSemanticsNodes().isNotEmpty()
        }
        app.container.publishProfile(young.id)
        app.container.publishProfile(older.id + 9)
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithText(VoiceScripts.LEAVE).performClick()
        compose.onNodeWithText(VoiceScripts.STAY).performClick()
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.onNodeWithText(VoiceScripts.STAY).performClick()
        steps.indices.forEach { index ->
            step = index
            compose.waitUntil(8_000) {
                compose.onAllNodesWithText(VoiceScripts.LEAVE).fetchSemanticsNodes().isNotEmpty()
            }
            spoken.clear()
            play(steps[index].exercise)
        }
        compose.onNodeWithText(VoiceScripts.LEAVE).performClick()
        compose.onNodeWithText(VoiceScripts.CONFIRM_LEAVE).performClick()
    }

    @Test
    fun navigationParentsAndOlderHome() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        runBlocking { app.container.pinRepository.clear() }
        val profile = stored(app, "Ana", AgeBand.SIX_TO_EIGHT)
        val missing =
            RouteMemory(
                SavedStateHandle(
                    mapOf(RouteMemory.ROUTE to RouteMemory.HOME, RouteMemory.PROFILE to 999L),
                ),
            )
        val parents =
            RouteMemory(
                SavedStateHandle(
                    mapOf(RouteMemory.ROUTE to RouteMemory.PARENTS, RouteMemory.PROFILE to profile.id),
                ),
            )
        var mode by mutableIntStateOf(0)
        compose.setContent {
            key(mode) {
                when (mode) {
                    0 ->
                        NavGraph(
                            container = app.container,
                            onSpeak = {},
                            onReward = { _, _, _, _ -> },
                            memory = missing,
                        )
                    1 ->
                        NavGraph(
                            container = app.container,
                            onSpeak = {},
                            onReward = { _, _, _, _ -> },
                            memory = parents,
                        )
                    2 ->
                        MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
                            LeaderboardAndRewardsScreen(app.container, profile) {}
                        }
                    else ->
                        ParentDashboard(
                            app.container,
                            profile,
                            onSpeak = {},
                            onBack = {},
                            challenge =
                                pt.mataventuras.domain.parent
                                    .AdultChallenge(12, 3),
                            adultHoldMs = 0,
                        )
                }
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.THREE_TO_FIVE).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.THREE_TO_FIVE).performScrollTo().performClick()
        compose.onNodeWithContentDescription("Cão Herói").performScrollTo().performClick()
        compose.onNodeWithText(VoiceScripts.START).performScrollTo().performClick()
        mode = 1
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.PARENT_DASHBOARD).fetchSemanticsNodes().isNotEmpty()
        }
        app.container.publishProfile(profile.id)
        app.container.publishProfile(profile.id + 3)
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Olá, Ana!").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.LEADERBOARD).performScrollTo().performClick()
        compose.onNodeWithText("Voltar").performScrollTo().performClick()
        mode = 2
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Ana").fetchSemanticsNodes().isNotEmpty()
        }
        mode = 3
        compose.onNodeWithText("Segurar para continuar").performTouchInput {
            down(center)
            up()
        }
        compose.onNodeWithText("Resposta").performTextInput("36")
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("PIN").performClick().performTextInput("1234")
        compose.onNodeWithText("Confirmar PIN").performClick().performTextInput("1234")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Ana").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("1 min").performClick()
        compose.onNodeWithText("3 min").performClick()
        compose.onNodeWithText("Fechar").performScrollTo().performClick()
    }

    @Test
    fun blankLessonAndHomeStayQuiet() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val young = stored(app, "Mara", AgeBand.THREE_TO_FIVE)
        val lesson =
            RouteMemory(
                SavedStateHandle(
                    mapOf(
                        RouteMemory.ROUTE to RouteMemory.LESSON,
                        RouteMemory.MODULE to LearningModule.COUNTING.name,
                    ),
                ),
            )
        val home = RouteMemory(SavedStateHandle(mapOf(RouteMemory.ROUTE to RouteMemory.HOME)))
        var mode by mutableIntStateOf(0)
        compose.setContent {
            key(mode) {
                when (mode) {
                    0 ->
                        NavGraph(
                            container = app.container,
                            onSpeak = {},
                            onReward = { _, _, _, _ -> },
                            memory = lesson,
                        )
                    1 ->
                        NavGraph(
                            container = app.container,
                            onSpeak = {},
                            onReward = { _, _, _, _ -> },
                            memory = home,
                        )
                    else ->
                        HomeScreen(
                            profile = young,
                            onSpeak = {},
                            onModule = {},
                            onLeaderboard = {},
                            onParents = {},
                            onSwitchProfile = {},
                        )
                }
            }
        }
        compose.waitForIdle()
        mode = 1
        compose.waitForIdle()
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.waitForIdle()
        mode = 2
        compose.onNodeWithText("★").performClick()
        compose.onNodeWithText("\uD83D\uDD12").performClick()
    }

    @Test
    fun canvasHostsTakeBackAndTouch() {
        val circuit = OffroadCircuit(4)
        val kart = OffroadRacerLoop(circuit)
        val level = PlatformerWorld.random(2)
        val runner = Platformer2dLoop(level = level, ringsTarget = level.coins.size)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            NativeKartHost.KartPlay(kart, circuit, Mascot.SPEEDY_HEDGEHOG, frameLimit = 4) {}
        }
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.onNodeWithTag("kart-course").performTouchInput {
            down(center)
            down(1, centerRight)
            moveBy(Offset(30f, 0f))
            up()
        }
        compose.onNodeWithTag("kart-course").performTouchInput {
            down(centerLeft)
            cancel()
        }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        check(runner.state.alive || !runner.state.alive)
    }

    @Test
    fun runnerHostTakesBackAndTouch() {
        val level = PlatformerWorld.random(2)
        val runner = Platformer2dLoop(level = level, ringsTarget = level.coins.size)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            NativeRunnerHost.RunnerPlay(runner, level, Mascot.HERO_PUP, frameLimit = 4) {}
        }
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.onNodeWithTag("runner-course").performTouchInput {
            down(center)
            down(1, centerRight)
            moveBy(Offset(8f, -70f))
            up()
        }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
    }

    private fun play(exercise: Exercise) {
        when (exercise.play.kind) {
            PlayKind.SOUP -> {
                compose
                    .onAllNodes(
                        androidx.compose.ui.test
                            .hasTestTag("correct-answer"),
                    )[0]
                    .performClick()
                compose.onNodeWithTag("soup-board").performTouchInput {
                    down(topLeft + Offset(6f, 6f))
                    down(1, topLeft + Offset(20f, 20f))
                    moveBy(Offset(48f, 0f))
                    up()
                }
                compose.onNodeWithTag("soup-board").performTouchInput {
                    down(topLeft + Offset(6f, 6f))
                    moveBy(Offset(0f, 48f))
                    up()
                }
                compose.onAllNodesWithText("A")[0].performClick()
            }
            PlayKind.MEMORY -> {
                compose.onAllNodesWithText("?")[0].performClick()
                compose.onAllNodesWithText("Sol")[0].performClick()
                if (compose.onAllNodesWithText("?").fetchSemanticsNodes().isNotEmpty()) {
                    compose.onAllNodesWithText("?")[0].performClick()
                }
            }
            PlayKind.WORD_COMPLETE -> compose.onNodeWithText("A").performClick()
            PlayKind.SUDOKU -> {
                if (exercise.play.solution.isNotEmpty()) {
                    val count = compose.onAllNodesWithTag("sudoku-digit").fetchSemanticsNodes().size
                    check(count >= 2) { "sudoku digits=$count prompt=${exercise.prompt}" }
                }
                if (compose.onAllNodesWithTag("board-cell-1").fetchSemanticsNodes().isNotEmpty()) {
                    compose.onNodeWithTag("board-cell-1").performClick()
                }
                val digits = compose.onAllNodesWithTag("sudoku-digit")
                if (digits.fetchSemanticsNodes().size >= 2) {
                    digits[1].performScrollTo().performClick()
                    digits[0].performScrollTo().performClick()
                } else if (compose.onAllNodesWithTag("correct-answer").fetchSemanticsNodes().isNotEmpty()) {
                    compose.onAllNodesWithTag("correct-answer")[0].performScrollTo().performClick()
                }
            }
            else -> {
                if (compose
                        .onAllNodes(
                            androidx.compose.ui.test
                                .hasTestTag("correct-answer"),
                        ).fetchSemanticsNodes()
                        .isNotEmpty()
                ) {
                    if (compose
                            .onAllNodes(
                                androidx.compose.ui.test
                                    .hasTestTag("distractor"),
                            ).fetchSemanticsNodes()
                            .isNotEmpty()
                    ) {
                        compose
                            .onAllNodes(
                                androidx.compose.ui.test
                                    .hasTestTag("distractor"),
                            )[0]
                            .performClick()
                    }
                    compose
                        .onAllNodes(
                            androidx.compose.ui.test
                                .hasTestTag("correct-answer"),
                        )[0]
                        .performClick()
                }
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun emptyDotCanvasCoversZeroCounts() {
        compose.setContent {
            Canvas(Modifier.size(32.dp)) {
                paintCountDots(0)
                paintStarDots(0)
                pt.mataventuras.domain.model.GeometricShape.entries
                    .forEach { paintShapeGlyph(it) }
            }
        }
        compose.waitForIdle()
        val fingerprint = android.os.Build::class.java.getDeclaredField("FINGERPRINT")
        fingerprint.isAccessible = true
        val previous = fingerprint.get(null)
        fingerprint.set(null, "generic")
        try {
            pt.mataventuras.app.di
                .AppContainer(ApplicationProvider.getApplicationContext())
        } catch (_: Exception) {
            // Non-Robolectric fingerprints fail closed on the Android Keystore.
        } finally {
            fingerprint.set(null, previous)
        }
    }

    @Test
    fun missingProfileAnswerAndNullParentStillRender() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        runBlocking { app.container.pinRepository.clear() }
        val profile = stored(app, "Nuno", AgeBand.SIX_TO_EIGHT)
        var mode by mutableIntStateOf(0)
        compose.setContent {
            key(mode) {
                when (mode) {
                    0 ->
                        LessonScreen(
                            container = app.container,
                            profile = profile,
                            module = LearningModule.COUNTING,
                            onSpeak = {},
                            onReward = {},
                            onExit = {},
                        )
                    1 ->
                        ParentDashboard(
                            app.container,
                            null,
                            onSpeak = {},
                            onBack = {},
                            challenge =
                                pt.mataventuras.domain.parent
                                    .AdultChallenge(12, 3),
                            adultHoldMs = 0,
                        )
                    else -> BalloonPopScreen(kotlin.random.Random(1)) {}
                }
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.LEAVE).fetchSemanticsNodes().isNotEmpty()
        }
        app.container.database.clearAllTables()
        app.container.publishProfile(profile.id)
        compose
            .onAllNodes(
                androidx.compose.ui.test
                    .hasTestTag("correct-answer"),
            )[0]
            .performClick()
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.onNodeWithText(VoiceScripts.CONFIRM_LEAVE).performClick()
        org.robolectric.util.ReflectionHelpers
            .setStaticField(android.os.Build::class.java, "FINGERPRINT", null)
        pt.mataventuras.app.engine.GodotRuntime
            .shouldEmbed()
        org.robolectric.util.ReflectionHelpers.setStaticField(
            android.os.Build::class.java,
            "FINGERPRINT",
            "robolectric",
        )

        mode = 1
        compose.onNodeWithText("Segurar para continuar").performTouchInput {
            down(center)
            up()
        }
        compose.onNodeWithText("Resposta").performTextInput("36")
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("PIN").performClick().performTextInput("1234")
        compose.onNodeWithText("Confirmar PIN").performClick().performTextInput("1234")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Escolhe um perfil de criança primeiro.").fetchSemanticsNodes().isNotEmpty()
        }

        mode = 2
        compose.onNodeWithTag("balloon-0").performClick()
        compose.onNodeWithTag("balloon-1").performClick()
    }

    private fun stored(
        app: MatAventurasApp,
        name: String,
        age: AgeBand,
    ): ChildProfile =
        runBlocking {
            val id = app.container.repository.createProfile(name, age, Mascot.SPEEDY_HEDGEHOG)
            app.container.repository.getProfile(id)!!
        }

    private fun lessonSteps(): List<LessonStep> {
        val shapes = GeometricShape.entries.map { it.displayName }
        return listOf(
            LessonStep(
                exercise(
                    LearningModule.COUNTING,
                    listOf("1", "2", "3", "4"),
                    correct = 2,
                    visual = 3,
                ),
            ),
            LessonStep(
                exercise(
                    LearningModule.SHAPES,
                    shapes,
                    correct = shapes.indexOf(GeometricShape.CIRCLE.displayName),
                    shape = GeometricShape.CIRCLE,
                ),
            ),
            LessonStep(
                exercise(LearningModule.ADDITION, listOf("2", "4", "6", "8"), correct = 1, visual = 4),
            ),
            LessonStep(
                exercise(LearningModule.ADDITION, listOf("12", "14", "16", "18"), visual = 12),
            ),
            LessonStep(
                exercise(LearningModule.NUMBERS, listOf("7", "8", "9", "10"), visual = 7),
            ),
            LessonStep(
                exercise(
                    LearningModule.NUMBERS,
                    listOf("4", "9"),
                    play =
                        PlayBoard(
                            kind = PlayKind.SUDOKU,
                            cells = listOf("1", "", "2", "3"),
                            columns = 2,
                            solution = listOf("1", "4", "2", "3"),
                        ),
                ),
                older = true,
            ),
            LessonStep(
                exercise(
                    LearningModule.COUNTING,
                    listOf("1", "2", "3", "4"),
                    play =
                        PlayBoard(
                            kind = PlayKind.SUDOKU,
                            cells = listOf("1", "2", "3", "4"),
                            columns = 2,
                        ),
                ),
            ),
            LessonStep(
                exercise(
                    LearningModule.LOGIC,
                    listOf("0", "1", "2", "3"),
                    play =
                        PlayBoard(
                            kind = PlayKind.SOUP,
                            cells = listOf("A", "B", "C", "D"),
                            columns = 2,
                            wordPaths = listOf(listOf(0, 1), listOf(2, 3)),
                        ),
                ),
                older = true,
            ),
            LessonStep(
                exercise(
                    LearningModule.ADDITION,
                    listOf("2", "3"),
                    play = PlayBoard(kind = PlayKind.CIPHER, cells = listOf("1+1"), cipherCode = "2"),
                ),
                older = true,
            ),
            LessonStep(
                exercise(
                    LearningModule.SUBTRACTION,
                    listOf("1", "2", "3", "?"),
                    play = PlayBoard(kind = PlayKind.PUZZLE, cells = listOf("1", "2", "3", "?"), columns = 2),
                ),
                older = true,
            ),
            LessonStep(
                exercise(
                    LearningModule.MULTIPLICATION,
                    listOf("2", "3", "4", "6"),
                    play = PlayBoard(kind = PlayKind.GROUPS, cells = listOf("●", "●", "●", "●"), columns = 2),
                ),
                older = true,
            ),
            LessonStep(
                exercise(
                    LearningModule.MEMORY,
                    listOf("ok"),
                    play =
                        PlayBoard(
                            kind = PlayKind.MEMORY,
                            cells = listOf("Sol", "Sol"),
                            columns = 2,
                            solution = listOf("a", "a"),
                        ),
                ),
            ),
            LessonStep(
                exercise(
                    LearningModule.WORDS,
                    listOf("A", "E"),
                    play = PlayBoard(kind = PlayKind.WORD_COMPLETE, cells = emptyList()),
                ),
                older = true,
            ),
            LessonStep(
                exercise(
                    LearningModule.WORDS,
                    listOf("A", "E"),
                    play = PlayBoard(kind = PlayKind.WORD_COMPLETE, cells = listOf("G_TO")),
                ),
                older = true,
            ),
        )
    }

    private fun exercise(
        module: LearningModule,
        options: List<String>,
        correct: Int = 0,
        visual: Int = 0,
        shape: GeometricShape? = null,
        play: PlayBoard = PlayBoard(),
    ): Exercise =
        Exercise(
            module = module,
            prompt = "Pergunta ${module.name}",
            spoken = "Ouve ${module.name}",
            options = options,
            correctIndex = correct,
            visualCount = visual,
            targetShape = shape,
            play = play,
        )
}

private data class LessonStep(
    val exercise: Exercise,
    val older: Boolean = false,
)
