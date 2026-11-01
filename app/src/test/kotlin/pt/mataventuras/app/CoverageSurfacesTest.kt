package pt.mataventuras.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import kotlin.random.Random
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.engine.NativeKartHost
import pt.mataventuras.app.engine.NativeRunnerHost
import pt.mataventuras.app.engine.OffroadRacerLoop
import pt.mataventuras.app.engine.Platformer2dLoop
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.lesson.MemoryBoard
import pt.mataventuras.app.ui.lesson.WordCompleteBoard
import pt.mataventuras.app.ui.navigation.NavGraph
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.engine.PlatformerWorld
import pt.mataventuras.domain.math.ExerciseGenerator
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot

@RunWith(RobolectricTestRunner::class)
class CoverageSurfacesTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComposeHostActivity>()

    @Test
    fun lessonBoardsAndPrizeCardRender() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val young =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Lia", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.getProfile(id)!!
            }
        val older =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
                app.container.repository.getProfile(id)!!
            }
        val memory = app.container.generator.generate(LearningModule.MEMORY, 0, AgeBand.THREE_TO_FIVE)
        val word = app.container.generator.generate(LearningModule.WORDS, 0, AgeBand.SIX_TO_EIGHT)
        val circuit = OffroadCircuit(7)
        val level = PlatformerWorld.random(3)
        val stepState = mutableIntStateOf(0)
        var step by stepState
        compose.setContent {
            when (step) {
                in LearningModule.entries.indices -> {
                    val module = LearningModule.entries[step]
                    val profile = if (AgeBand.THREE_TO_FIVE in module.bands) young else older
                    MatAventurasTheme(profile.ageGroup) {
                        LessonScreen(
                            container = app.container,
                            profile = profile,
                            module = module,
                            onSpeak = {},
                            onReward = {},
                            onExit = {},
                        )
                    }
                }
                LearningModule.entries.size -> MemoryBoard(memory) {}
                LearningModule.entries.size + 1 -> WordCompleteBoard(word) {}
                LearningModule.entries.size + 2 ->
                    NavGraph(
                        container = app.container,
                        onSpeak = {},
                        onReward = { _, _, _, _ -> },
                        celebrate = true,
                        onDismissWin = {},
                    )
                LearningModule.entries.size + 3 ->
                    NativeKartHost.KartPlay(
                        loop = OffroadRacerLoop(circuit = circuit),
                        circuit = circuit,
                        mascot = Mascot.SPEEDY_HEDGEHOG,
                        frameLimit = 1,
                        onDone = {},
                    )
                else ->
                    NativeRunnerHost.RunnerPlay(
                        loop = Platformer2dLoop(level = level, ringsTarget = level.coins.size),
                        level = level,
                        mascot = Mascot.SPEEDY_HEDGEHOG,
                        frameLimit = 1,
                        onDone = {},
                    )
            }
        }
        repeat(LearningModule.entries.size) { index ->
            step = index
            compose.waitUntil(8_000) {
                compose.onAllNodesWithText("Sair").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Ouvir").performClick()
            val other = (index + 1) % LearningModule.entries.size
            step = other
            compose.waitForIdle()
            step = index
            compose.waitForIdle()
        }
        repeat(5) { shape ->
            show(app, stepState, LearningModule.SHAPES, 0, shape)
        }
        show(app, stepState, LearningModule.SHAPES, 5)
        dragSoup()
        show(app, stepState, LearningModule.SHAPES, 6)
        show(app, stepState, LearningModule.NUMBERS, 1)
        show(app, stepState, LearningModule.NUMBERS, 5)
        show(app, stepState, LearningModule.NUMBERS, 6)
        show(app, stepState, LearningModule.SUBTRACTION, 2)
        show(app, stepState, LearningModule.ADDITION, 4)
        show(app, stepState, LearningModule.LOGIC, 4)
        step = LearningModule.entries.size
        compose.waitForIdle()
        compose.onAllNodesWithText("?").onFirst().performClick()
        step = LearningModule.entries.size + 1
        compose.waitForIdle()
        compose.onNodeWithTag("word-complete").assertIsDisplayed()
        step = LearningModule.entries.size + 2
        compose.waitForIdle()
        compose.onNodeWithText("Continuar").performClick()
        step = LearningModule.entries.size + 3
        compose.waitForIdle()
        compose.onNodeWithTag("kart-course").performTouchInput {
            down(center)
            moveBy(Offset(40f, 0f))
            up()
        }
        step = LearningModule.entries.size + 4
        compose.waitForIdle()
        compose.onNodeWithTag("runner-course").performTouchInput {
            down(center)
            moveBy(Offset(24f, -80f))
            up()
        }
    }

    private fun show(
        app: MatAventurasApp,
        step: androidx.compose.runtime.MutableIntState,
        module: LearningModule,
        vararg slots: Int,
    ) {
        app.container.useGenerator(ExerciseGenerator(SlotRandom(slots)))
        step.intValue = LearningModule.entries.size + 9
        compose.waitForIdle()
        step.intValue = LearningModule.entries.indexOf(module)
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Sair").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun dragSoup() {
        if (compose.onAllNodesWithTag("soup-board").fetchSemanticsNodes().isEmpty()) return
        compose.onNodeWithTag("soup-board").performTouchInput {
            down(topLeft + Offset(8f, 8f))
            moveBy(Offset(80f, 0f))
            up()
        }
    }
}

private class SlotRandom(
    private val slots: IntArray,
) : Random() {
    private var index: Int = 0

    override fun nextBits(bitCount: Int): Int = 0

    override fun nextInt(until: Int): Int {
        val raw = if (index < slots.size) slots[index++] else 0
        return if (until <= 1) 0 else raw.coerceIn(0, until - 1)
    }
}
