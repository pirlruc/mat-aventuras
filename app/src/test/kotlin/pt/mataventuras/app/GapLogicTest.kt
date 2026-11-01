package pt.mataventuras.app

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.widget.Button
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import pt.mataventuras.app.di.pinIterationsFor
import pt.mataventuras.app.di.roomAllowsMainThread
import pt.mataventuras.app.engine.FrameClock
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.NativeKartHost
import pt.mataventuras.app.engine.NativeRunnerHost
import pt.mataventuras.app.engine.OffroadRacerLoop
import pt.mataventuras.app.engine.OffroadScene
import pt.mataventuras.app.engine.OffroadSpan
import pt.mataventuras.app.engine.Platformer2dLoop
import pt.mataventuras.app.engine.PlatformerRect
import pt.mataventuras.app.engine.PlatformerScene
import pt.mataventuras.app.engine.paintKartCourse
import pt.mataventuras.app.engine.paintRunnerCourse
import pt.mataventuras.app.ui.ProfileResume
import pt.mataventuras.app.ui.SoupRelease
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.app.ui.lesson.AnswerCuePlayer
import pt.mataventuras.app.ui.lesson.resolveSoupGesture
import pt.mataventuras.app.ui.lesson.scoreSudokuPick
import pt.mataventuras.app.ui.lesson.paintCountDots
import pt.mataventuras.app.ui.lesson.paintShapeGlyph
import pt.mataventuras.app.ui.lesson.paintStarDots
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.engine.PlatformerEnemy
import pt.mataventuras.domain.engine.PlatformerPowerup
import pt.mataventuras.domain.engine.PlatformerWorld
import pt.mataventuras.domain.engine.PrizeHud
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.PlayBoard
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.math.SudokuPlay
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.plugin.KartPluginActivity
import pt.mataventuras.plugin.RunnerPluginActivity

@RunWith(RobolectricTestRunner::class)
@Config(application = MatAventurasApp::class, sdk = [34])
class GapLogicTest {
    @Test
    fun drawHelpersAndEdgeBranches() {
        val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        val canvas = androidx.compose.ui.graphics.Canvas(android.graphics.Canvas(bitmap))
        val circuit = OffroadCircuit(7)
        val kart = OffroadRacerLoop(circuit)
        val level =
            PlatformerWorld.DEFAULT.copy(
                powerups = listOf(PlatformerPowerup(1f, grow = false), PlatformerPowerup(2f, grow = true)),
                enemies = listOf(PlatformerEnemy(0f, 4f, 1f)),
            )
        val runner = Platformer2dLoop(level = level, ringsTarget = 1)
        runner.state = runner.state.copy(form = 2)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(80f, 80f)) {
            GeometricShape.entries.forEach { paintShapeGlyph(it) }
            paintStarDots(3)
            paintCountDots(4)
            paintKartCourse(ArrayList<OffroadSpan>(16), kart.state, circuit, Mascot.SPEEDY_HEDGEHOG)
            paintRunnerCourse(ArrayList<PlatformerRect>(32), runner.state, Mascot.BRAVE_PLUMBER, level)
        }
        val tiles = ArrayList<PlatformerRect>(32)
        PlatformerScene.fillTiles(tiles, runner.state, Mascot.SPEEDY_HEDGEHOG, 400f, 800f, level)
        assertTrue(tiles.isNotEmpty())
        assertTrue(OffroadScene.skyArgb(circuit) != 0L)

        val settled = Platformer2dLoop()
        settled.state = settled.state.copy(finished = true)
        settled.tick()
        settled.state = settled.state.copy(finished = false, alive = false)
        settled.tick()

        var now = 1L
        val clock = FrameClock({ now })
        assertEquals(0f, clock.delta())
        now = 1_000_000_000L
        assertTrue(clock.delta() > 0f)

        assertFalse(GodotRuntime.isSurfaceReady(0, 100))
        assertFalse(GodotRuntime.isSurfaceReady(100, 10))
        assertTrue(GodotRuntime.isSurfaceReady(33, 33))
        assertFalse(GodotRuntime.isRobolectricFingerprint("google/sdk"))
        assertTrue(GodotRuntime.isRobolectricFingerprint("Robolectric"))
        assertFalse(GodotRuntime.shouldRestartHost(alreadyRestarted = true, finishing = false, destroyed = false, fromRelaunch = false))
        assertTrue(GodotRuntime.shouldRestartHost(false, false, false, false))

        assertFalse(roomAllowsMainThread("google/sdk"))
        assertTrue(roomAllowsMainThread("robolectric"))
        assertEquals(600_000, pinIterationsFor("google/sdk"))
        assertEquals("Jogar", ModuleTitles.laterNoun(LearningModule.COUNTING))
        AnswerCuePlayer.createGenerator()

        PlayKind.entries.forEach { UiLogic.showsOptionPalette(it) }
        assertFalse(UiLogic.showsSumDots(LearningModule.ADDITION, 0))
        assertFalse(UiLogic.showsSumDots(LearningModule.ADDITION, 11))
        assertTrue(UiLogic.showsSumDots(LearningModule.ADDITION, 4))
        assertFalse(UiLogic.showsSumDots(LearningModule.SHAPES, 4))
        assertFalse(UiLogic.showsStarGrid(LearningModule.SHAPES, 3))
        assertTrue(UiLogic.showsNumberHero(LearningModule.NUMBERS, PlayKind.CHOICE))
        assertFalse(UiLogic.showsNumberHero(LearningModule.NUMBERS, PlayKind.SOUP))
        assertEquals(null, UiLogic.soupIndexAt(1f, 1f, 0f, 10f, 2, 4, 1f))
        assertFalse(UiLogic.soupAreAdjacent(0, 2, 3))
        assertFalse(UiLogic.soupAreAdjacent(0, 6, 3))
        assertTrue(UiLogic.soupAreAdjacent(0, 4, 3))
        assertEquals(listOf(1), UiLogic.soupExtendPath(emptyList(), 1, 2))
        assertEquals(listOf(0), UiLogic.soupExtendPath(listOf(0), 0, 2))
        assertEquals(listOf(0), UiLogic.soupExtendPath(listOf(0), 5, 2))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(emptyList(), emptyList()))
        assertEquals(
            SoupRelease.MISS,
            UiLogic.soupReleaseKind(listOf(0, 1), listOf(listOf(2, 3)), setOf(0)),
        )
        assertEquals(null, UiLogic.soupPickIndex(listOf(0), listOf(listOf(0, 1)), emptySet(), 4))
        assertEquals(0, UiLogic.soupPickIndex(listOf(0, 1), listOf(listOf(0, 1)), emptySet(), 4))
        assertEquals(0, UiLogic.soupPickIndex(listOf(0, 1), listOf(listOf(2, 3)), emptySet(), 4))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(emptyList(), listOf(listOf(0)), emptySet()))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(listOf(0), emptyList(), emptySet()))
        assertEquals(SoupRelease.MISS, UiLogic.soupReleaseKind(listOf(0, 1), listOf(listOf(0)), setOf(0)))
        var picked = -1
        var missed = false
        val found =
            resolveSoupGesture(
                path = listOf(0, 1),
                paths = listOf(listOf(0, 1), listOf(2, 3)),
                found = emptySet(),
                cellCount = 4,
                onPick = { picked = it },
                onMissKeep = { missed = true },
            )
        assertEquals(setOf(0), found)
        assertEquals(-1, picked)
        val done =
            resolveSoupGesture(listOf(2, 3), listOf(listOf(0, 1), listOf(2, 3)), found, 4, { picked = it }, { missed = true })
        assertEquals(2, picked)
        assertEquals(setOf(0, 1), done)
        resolveSoupGesture(listOf(0, 1), listOf(listOf(2, 3), listOf(4, 5)), emptySet(), 8, {}, { missed = true })
        assertTrue(missed)
        resolveSoupGesture(listOf(0), listOf(listOf(1)), emptySet(), 2, { picked = it }, {})
        val sudokuExercise =
            Exercise(
                module = LearningModule.NUMBERS,
                prompt = "Sudoku",
                spoken = "Sudoku",
                options = listOf("4", "9"),
                correctIndex = 0,
                play = PlayBoard(
                    kind = PlayKind.SUDOKU,
                    cells = listOf("1", "", "2", "3"),
                    columns = 2,
                    solution = listOf("1", "4", "2", "3"),
                ),
            )
        val started = SudokuPlay.start(sudokuExercise.play.cells)
        val attempts = mutableListOf<Pair<Boolean, Boolean>>()
        val missedToken = scoreSudokuPick(true, sudokuExercise, started, 9, { a, b -> attempts += a to b })
        assertEquals(started, missedToken)
        val wrong = scoreSudokuPick(true, sudokuExercise, started, 1, { a, b -> attempts += a to b })
        assertEquals(false, attempts.last().first)
        val solved = scoreSudokuPick(true, sudokuExercise, wrong, 0, { a, b -> attempts += a to b })
        assertEquals("4", solved.cells[1])
        scoreSudokuPick(false, sudokuExercise, solved, 0, { a, b -> attempts += a to b })
        assertEquals(true, attempts.last().second)
    }

    @Test
    fun plainContainerAndProfileRemember() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        runBlocking {
            val id = app.container.repository.createProfile("Lia", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
            val profile = app.container.repository.getProfile(id)!!
            ProfileResume.remember(app.container.lastProfile, profile)
            assertEquals(id, app.container.lastProfile.read())
        }
    }

    @Test
    fun emptyExtrasAndMinifiedAttach() {
        val kart =
            Robolectric.buildActivity(KartPluginActivity::class.java, Intent()).setup().get()
        assertEquals("", kart.sceneCode())
        assertEquals("", kart.ageCode())
        assertEquals("", kart.childName())
        kart.godotRestarted = true
        assertTrue(kart.godotRestarted)
        NativeKartHost.attach(kart, showUi = true, frameLimit = 1)
        ShadowLooper.idleMainLooper()
        kart.onBackPressedDispatcher.onBackPressed()
        ShadowLooper.idleMainLooper()

        val runner =
            Robolectric.buildActivity(RunnerPluginActivity::class.java, Intent()).setup().get()
        runner.acceptRelaunch(Intent())
        NativeRunnerHost.attach(runner, showUi = true, frameLimit = 1)
        ShadowLooper.idleMainLooper()
        runner.onBackPressedDispatcher.onBackPressed()
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun bootBackOpensTheCanvasFallback() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kart =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                pt.mataventuras.app.engine.EngineLauncher.intentFor(
                    context,
                    AgeBand.SIX_TO_EIGHT,
                    Mascot.BRAVE_PLUMBER,
                    "Rui",
                    pt.mataventuras.domain.model.EngineKind.THREE_D,
                ),
            ).setup().get()
        GodotRewardBinder.bindKart(kart, embed = true) { _, _ -> }
        clickLabeled(kart, PrizeHud.RETRY_LABEL)
        clickLabeled(kart, PrizeHud.BACK_LABEL)
        assertNotNull(kart.nativeSession)

        val runner =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                pt.mataventuras.app.engine.EngineLauncher.intentFor(
                    context,
                    AgeBand.THREE_TO_FIVE,
                    Mascot.SPEEDY_HEDGEHOG,
                    "Ana",
                    game = RewardGame.RUNNER,
                ),
            ).setup().get()
        GodotRewardBinder.bindRunner(runner, embed = true) { _, _ -> }
        clickLabeled(runner, PrizeHud.BACK_LABEL)
        assertNotNull(runner.loop)
    }

    private fun clickLabeled(activity: Activity, label: String) {
        val root = activity.window.decorView as ViewGroup
        val button = findButton(root, label)
        assertNotNull(button)
        button!!.performClick()
    }

    private fun findButton(view: android.view.View, label: String): Button? {
        if (view is Button && (view.text?.toString() == label || view.tag == "prize-boot-back" || view.tag == "prize-boot-retry")) {
            if (view.text?.toString() == label || view.tag?.toString()?.endsWith(if (label == PrizeHud.BACK_LABEL) "back" else "retry") == true) {
                return view
            }
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findButton(view.getChildAt(index), label)?.let { return it }
            }
        }
        return null
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class Sdk26ProcessNameTest {
    @Test
    fun hostProcessNameUsesProcOnOlderApis() {
        val name = hostProcessName()
        assertNotNull(name)
    }
}
