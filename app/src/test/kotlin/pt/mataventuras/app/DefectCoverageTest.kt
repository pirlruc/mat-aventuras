package pt.mataventuras.app

import android.os.Looper
import android.view.View
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.prize.ComposePrizeActivity
import pt.mataventuras.app.prize.PrizeLimit
import pt.mataventuras.app.prize.finishTower
import pt.mataventuras.app.ui.debugAnswerTag
import pt.mataventuras.app.ui.lesson.ClockFace
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.lesson.ShapeGlyph
import pt.mataventuras.app.ui.lesson.SizeMark
import pt.mataventuras.app.ui.lesson.VisualOption
import pt.mataventuras.app.ui.lesson.clockHour
import pt.mataventuras.app.ui.lesson.colourOf
import pt.mataventuras.app.ui.lesson.drawSortMark
import pt.mataventuras.app.ui.parent.AdultHold
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.parent.ParentKeyReset
import pt.mataventuras.app.ui.parent.pinNotice
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.data.pin.PinKeyException
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.ExerciseGenerator
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.parent.AdultChallenge
import pt.mataventuras.domain.parent.PinGateResult
import pt.mataventuras.domain.parent.PinNotice
import pt.mataventuras.domain.prize.ComposePrize
import pt.mataventuras.domain.prize.TowerDefence
import pt.mataventuras.domain.voice.VoiceScripts
import pt.mataventuras.plugin.RunnerPluginActivity

@RunWith(RobolectricTestRunner::class)
class DefectCoverageTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun adultHoldWithoutABackButtonHasNoBackLabel() {
        compose.setContent {
            AdultHold(
                challenge = AdultChallenge(6, 7),
                holdMs = 0,
                holdLabel = "hold",
                onPassed = {},
            )
        }
        assertTrue(compose.onAllNodesWithText("Voltar").fetchSemanticsNodes().isEmpty())
        compose.onNodeWithText("hold").assertExists()
    }

    @Test
    fun aWrongTowerIndexDoesNotFinish() {
        val round = TowerDefence.round(Random(1))
        var finished = false
        finishTower(round, -1) { finished = true }
        assertFalse(finished)
    }

    @Test
    fun clockFaceAndSizeMarkDrawWithoutTheAnswerWord() {
        assertEquals(3, clockHour("3:00"))
        assertEquals(12, clockHour("nope"))
        assertEquals(1, clockHour("0:00"))
        compose.setContent {
            ClockFace(3)
            ShapeGlyph(GeometricShape.CIRCLE)
            SizeMark("Grande", option = 1)
            SizeMark("Pequeno", option = 2)
        }
        compose.onNodeWithTag("clock-face").assertExists()
        compose.onNodeWithContentDescription("Opção 1").assertExists()
        compose.onNodeWithContentDescription("Opção 2").assertExists()
    }

    @Test
    fun answerTagHidesTheCorrectOptionOutsideDebug() {
        assertEquals("correct-answer", debugAnswerTag(correct = true, debug = true))
        assertEquals("option", debugAnswerTag(correct = true, debug = false))
        assertEquals("option", debugAnswerTag(correct = false, debug = false))
        assertEquals("distractor", debugAnswerTag(correct = false, debug = true))
    }

    @Test
    fun prizeDeadlineSurvivesRecreation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = ComposePrizeActivity.intent(context, ComposePrize.BALLOONS, limitSeconds = 30)
        val host = Robolectric.buildActivity(ComposePrizeActivity::class.java, intent).setup()
        val deadline = host.get().intent.getLongExtra(PrizeLimit.EXTRA_DEADLINE, 0L)
        assertTrue(deadline > 0L)
        host.recreate()
        assertEquals(deadline, host.get().intent.getLongExtra(PrizeLimit.EXTRA_DEADLINE, 0L))
    }

    @Test
    fun prizeLimitFinishesTheComposePrizeAndTheCanvasHost() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val composeIntent = ComposePrizeActivity.intent(context, ComposePrize.BALLOONS, seed = 1L, limitSeconds = 1)
        val composeHost = Robolectric.buildActivity(ComposePrizeActivity::class.java, composeIntent).setup()
        shadowOf(Looper.getMainLooper()).idleFor(1200, TimeUnit.MILLISECONDS)
        assertTrue(composeHost.get().isFinishing || composeHost.get().isDestroyed)

        val engine =
            EngineLauncher.intentFor(
                context,
                AgeBand.THREE_TO_FIVE,
                Mascot.SPEEDY_HEDGEHOG,
                "Ana",
                limitSeconds = 1,
            )
        assertEquals("1", engine.getStringExtra(EnginePluginContract.EXTRA_PRIZE_LIMIT))
        val runner = Robolectric.buildActivity(RunnerPluginActivity::class.java, engine).setup()
        PrizeLimit.arm(runner.get())
        shadowOf(Looper.getMainLooper()).idleFor(1200, TimeUnit.MILLISECONDS)
        assertTrue(runner.get().isFinishing || runner.get().isDestroyed)
        PrizeLimit.cancel(runner.get())
    }

    @Test
    fun keyResetNeedsTheHoldAndTheSum() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        var reset = 0
        var left = 0
        compose.setContent {
            ParentKeyReset(
                app.container,
                onBack = { left += 1 },
                holdMs = 0,
                challenge = AdultChallenge(6, 7),
                onReset = { reset += 1 },
            )
        }
        compose.onNodeWithText("Segurar para repor").performTouchInput {
            down(center)
            up()
        }
        compose.onNodeWithText("Quanto é 6 × 7?").assertExists()
        compose.onNodeWithText("Resposta").performTextInput("ab")
        compose.onNodeWithText("Repor").performClick()
        assertEquals(0, reset)
        assertEquals(0, reset)
        compose.onNodeWithText("Resposta").performTextInput("42")
        compose.onNodeWithText("Repor").performClick()
        compose.waitUntil(8_000) { reset == 1 }
        compose.onNodeWithText("Voltar").performClick()
        assertEquals(1, left)
        assertFalse(reset == 0)
    }

    @Test
    fun parentDashboardWithoutAnActivitySkipsTheSecureFlag() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        compose.setContent {
            CompositionLocalProvider(LocalView provides View(app)) {
                ParentDashboard(app.container, null, onSpeak = {}, onBack = {})
            }
        }
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
    }

    @Test
    fun aShortHoldDoesNotOpenTheResetSum() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        compose.setContent {
            ParentKeyReset(
                app.container,
                onBack = {},
                holdMs = 60_000,
                challenge = AdultChallenge(6, 7),
                onReset = {},
            )
        }
        compose.onNodeWithText("Segurar para repor").performTouchInput {
            down(center)
            up()
        }
        assertTrue(compose.onAllNodesWithText("Quanto é 6 × 7?").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun unlockedDashboardWithoutAChildAsksForAProfile() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        kotlinx.coroutines.runBlocking {
            app.container.pinRepository.clear()
            app.container.pinRepository.save(app.container.pinPolicy.create("4242"))
        }
        compose.setContent {
            ParentDashboard(app.container, null, onSpeak = {}, onBack = {})
        }
        compose.onNodeWithText("PIN").performTextInput("a42421")
        compose.onNodeWithText("Entrar").performClick()
        compose.waitUntil(8_000) {
            compose
                .onAllNodesWithText("Escolhe um perfil de criança primeiro.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun debugAnswerFlagFollowsRobolectricOrTheDebuggableBit() {
        assertEquals("", fingerprintOrEmpty(null))
        assertEquals("robolectric", fingerprintOrEmpty("robolectric"))
        assertTrue(debugAnswersFor("robolectric", 0))
        assertTrue(debugAnswersFor("other", android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE))
        assertFalse(debugAnswersFor("other", 0))
    }

    @Test
    fun aMissingPrizeLimitDoesNotFinishTheHost() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent =
            ComposePrizeActivity.intent(context, ComposePrize.SNAKE, seed = 2L)
        val host = Robolectric.buildActivity(ComposePrizeActivity::class.java, intent).setup()
        PrizeLimit.arm(host.get())
        PrizeLimit.cancel(host.get())
        shadowOf(Looper.getMainLooper()).idleFor(200, TimeUnit.MILLISECONDS)
        assertFalse(host.get().isFinishing)
    }

    @Test
    fun unreadablePinOffersTheReset() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        app.container.pinRepository.failNextRead(PinKeyException(IllegalStateException("gone")))
        val profile =
            ChildProfile(
                id = 1L,
                name = "Ana",
                ageGroup = AgeBand.SIX_TO_EIGHT,
                favouriteMascot = Mascot.HERO_PUP,
                avatarId = "hero_pup",
                points = 0,
                createdAtEpochMs = 0L,
            )
        compose.setContent {
            ParentDashboard(
                app.container,
                profile,
                onSpeak = {},
                onBack = {},
                challenge = AdultChallenge(6, 7),
                adultHoldMs = 0,
            )
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Segurar para repor").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Segurar para repor").performTouchInput {
            down(center)
            up()
        }
        compose.onNodeWithText("Resposta").performTextInput("42")
        compose.onNodeWithText("Repor").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Segurar para continuar").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun clockAndSizeLessonsShowAPictureInsteadOfTheWord() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val older =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Ana", AgeBand.SIX_TO_EIGHT, Mascot.HERO_PUP)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
                LessonScreen(app.container, older, LearningModule.CLOCK, {}, {}, {})
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithTag("clock-face").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.CLOCK_ASK).assertIsDisplayed()
    }

    @Test
    fun sizeLessonShowsCirclesInsteadOfTheWords() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val younger =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Rui", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(app.container, younger, LearningModule.SIZE, {}, {}, {})
            }
        }
        assertTrue(compose.onAllNodesWithContentDescription("Grande").fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithContentDescription("Opção 1").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun sortDotsClapsAndShadowLessonsDrawThePicture() {
        assertEquals(Color(0xFFE53935), colourOf("vermelho"))
        assertEquals(Color(0xFF1E88E5), colourOf("azul"))
        assertEquals(Color(0xFFFDD835), colourOf("amarelo"))
        assertEquals(Color(0xFF43A047), colourOf("verde"))
        assertEquals(Color(0xFF90A4AE), colourOf("nada"))
        drawBothSortMarks()
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val younger =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Lia", AgeBand.THREE_TO_FIVE, Mascot.PINK_PIGLET)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(app.container, younger, LearningModule.SHADOW, {}, {}, {})
            }
        }
        compose.onNodeWithTag("shadow").assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.SHADOW_PROMPT).assertIsDisplayed()
    }

    @Test
    fun sortLessonDrawsAColourAndAShape() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val younger = youngProfile(app, "Nuno")
        val seed = sortSeed(colour = true)
        val colour =
            sortRound(seed).options.first { it in listOf("vermelho", "azul", "amarelo", "verde") }
        app.container.useGenerator(sortGenerator(seed))
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(app.container, younger, LearningModule.SORT, {}, {}, {})
            }
        }
        assertTrue(compose.onAllNodesWithContentDescription(colour).fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithContentDescription("Opção 1").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun sortLessonDrawsTheShapeRound() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val younger = youngProfile(app, "Ines")
        val seed = sortSeed(colour = false)
        val name = checkNotNull(sortRound(seed).targetShape).displayName
        app.container.useGenerator(sortGenerator(seed))
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(app.container, younger, LearningModule.SORT, {}, {}, {})
            }
        }
        assertTrue(compose.onAllNodesWithContentDescription(name).fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithContentDescription("Opção 1").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun dotsAndClapsLessonsSpeakThePrompt() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val younger = youngProfile(app, "Eva")
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(app.container, younger, LearningModule.DOTS, {}, {}, {})
            }
        }
        compose.onNodeWithText("Qual é o próximo ponto?").assertIsDisplayed()
    }

    @Test
    fun clapsLessonAsksForTheWord() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val younger = youngProfile(app, "Rita")
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(app.container, younger, LearningModule.CLAPS, {}, {}, {})
            }
        }
        compose.onNodeWithText("Quantas palmas", substring = true).assertIsDisplayed()
    }

    @Test
    fun prizeLimitIgnoresAnEarlyTickAFinishedHostAndAClearedReference() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = ComposePrizeActivity.intent(context, ComposePrize.BALLOONS, limitSeconds = 30)
        assertFalse(intent.hasExtra(ComposePrizeActivity.SEED))
        val host = Robolectric.buildActivity(ComposePrizeActivity::class.java, intent).setup()
        PrizeLimit.arm(host.get())
        PrizeLimit.arm(host.get())
        val tick = pendingPrizeTick()
        tick.run()
        assertFalse(host.get().isFinishing)
        PrizeLimit.arm(host.get())
        forgetPrizeHosts()
        pendingPrizeTick().run()
        PrizeLimit.cancel(host.get())

        val now = android.os.SystemClock.elapsedRealtime()
        PrizeLimit.stopIfDue(null, now, 1)
        PrizeLimit.stopIfDue(host.get(), now + 60_000, 30)
        assertFalse(host.get().isFinishing)
        host.get().finish()
        PrizeLimit.stopIfDue(host.get(), now, 1)

        val destroyed = ComposePrizeActivity.intent(context, ComposePrize.SNAKE, seed = 3L, limitSeconds = 1)
        val gone = Robolectric.buildActivity(ComposePrizeActivity::class.java, destroyed).setup()
        gone.destroy()
        PrizeLimit.stopIfDue(gone.get(), now, 1)
        val engine =
            EngineLauncher.intentFor(
                context,
                AgeBand.SIX_TO_EIGHT,
                Mascot.HERO_PUP,
                "Ana",
                limitSeconds = 1,
            )
        val runner = Robolectric.buildActivity(RunnerPluginActivity::class.java, engine).setup()
        PrizeLimit.stopIfDue(runner.get(), now, 1)
        assertTrue(runner.get().isFinishing || runner.get().isDestroyed)
    }

    @Test
    fun plainPinReadFailuresStayOnTheGate() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        kotlinx.coroutines.runBlocking {
            app.container.pinRepository.clear()
            app.container.pinRepository.save(app.container.pinPolicy.create("1234"))
        }
        app.container.pinRepository.failNextRead(IllegalStateException("torn"))
        compose.setContent {
            ParentDashboard(app.container, null, onSpeak = {}, onBack = {})
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.TRY_AGAIN).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun submitFailuresEitherRetryOrOfferAReset() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        kotlinx.coroutines.runBlocking {
            app.container.pinRepository.clear()
            app.container.pinRepository.save(app.container.pinPolicy.create("1234"))
        }
        compose.setContent {
            ParentDashboard(app.container, null, onSpeak = {}, onBack = {})
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Entrar").fetchSemanticsNodes().isNotEmpty()
        }
        app.container.pinRepository.failNextRead(IllegalArgumentException("no"))
        compose.onNodeWithText("PIN").performTextInput("1234")
        compose.onNodeWithText("Entrar").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.TRY_AGAIN).fetchSemanticsNodes().isNotEmpty()
        }
        app.container.pinRepository.failNextRead(PinKeyException(IllegalStateException("later")))
        compose.onNodeWithText("Entrar").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Segurar para repor").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun fractionLessonAsksForHalf() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val older = olderProfile(app, "Rui")
        compose.setContent {
            MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
                LessonScreen(app.container, older, LearningModule.FRACTION, {}, {}, {})
            }
        }
        compose.onNodeWithText("metade", substring = true).assertIsDisplayed()
    }

    @Test
    fun tangramLessonDrawsTheMissingPiece() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val older = olderProfile(app, "Lia")
        compose.setContent {
            MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
                LessonScreen(app.container, older, LearningModule.TANGRAM, {}, {}, {})
            }
        }
        compose.onNodeWithTag("tangram").assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.TANGRAM_PROMPT).assertIsDisplayed()
    }

    @Test
    fun aTangramOptionThatIsNotAShapeDrawsNoGlyph() {
        compose.setContent {
            VisualOption(module = LearningModule.TANGRAM, text = "nada")
        }
        assertTrue(compose.onAllNodesWithText("nada").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun pinNoticeMapsEveryNoticeOntoTheResolvedCopy() {
        val lines = listOf("w0", "w1")
        val mismatch = PinGateResult.Stay(PinNotice.MISMATCH)
        val format = PinGateResult.Stay(PinNotice.FORMAT)
        val locked = PinGateResult.Stay(PinNotice.LOCKED)
        val missing = PinGateResult.Stay(PinNotice.MISSING)
        assertEquals("mismatch", pinNotice(mismatch, "mismatch", "format", "locked", "missing", lines))
        assertEquals("format", pinNotice(format, "mismatch", "format", "locked", "missing", lines))
        assertEquals("locked", pinNotice(locked, "mismatch", "format", "locked", "missing", lines))
        assertEquals("missing", pinNotice(missing, "mismatch", "format", "locked", "missing", lines))
        assertEquals(
            "w1",
            pinNotice(PinGateResult.Stay(PinNotice.WRONG, 1), "mismatch", "format", "locked", "missing", lines),
        )
        assertEquals(
            "w0",
            pinNotice(PinGateResult.Stay(PinNotice.WRONG, -1), "mismatch", "format", "locked", "missing", lines),
        )
        assertEquals(
            "w1",
            pinNotice(PinGateResult.Stay(PinNotice.WRONG, 9), "mismatch", "format", "locked", "missing", lines),
        )
    }
}

private fun sortSeed(colour: Boolean): Int =
    (0..40).first { seed ->
        (sortRound(seed).targetShape == null) == colour
    }

private fun sortRound(seed: Int): Exercise =
    ExerciseGenerator(Random(seed)).generate(
        LearningModule.SORT,
        0,
        AgeBand.THREE_TO_FIVE,
    )

private fun sortGenerator(seed: Int): ExerciseGenerator = ExerciseGenerator(Random(seed))

private fun olderProfile(
    app: MatAventurasApp,
    name: String,
): ChildProfile =
    kotlinx.coroutines.runBlocking {
        val id = app.container.repository.createProfile(name, AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
        app.container.repository.getProfile(id)!!
    }

private fun youngProfile(
    app: MatAventurasApp,
    name: String,
): ChildProfile =
    kotlinx.coroutines.runBlocking {
        val id = app.container.repository.createProfile(name, AgeBand.THREE_TO_FIVE, Mascot.PINK_PIGLET)
        app.container.repository.getProfile(id)!!
    }

private fun pendingPrizeTick(): Runnable {
    val field = PrizeLimit::class.java.getDeclaredField("timers")
    field.isAccessible = true
    val timers = field.get(PrizeLimit) as Map<*, *>
    return timers.values.filterIsInstance<Runnable>().first()
}

private fun drawBothSortMarks() {
    val bitmap = android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas =
        androidx.compose.ui.graphics
            .Canvas(android.graphics.Canvas(bitmap))
    androidx.compose.ui.graphics.drawscope.CanvasDrawScope().draw(
        androidx.compose.ui.unit
            .Density(1f),
        androidx.compose.ui.unit.LayoutDirection.Ltr,
        canvas,
        androidx.compose.ui.geometry
            .Size(64f, 64f),
    ) {
        drawSortMark(GeometricShape.CIRCLE, GeometricShape.CIRCLE.displayName)
        drawSortMark(null, "vermelho")
    }
}

private fun forgetPrizeHosts() {
    val field = PrizeLimit::class.java.getDeclaredField("hosts")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val hosts = field.get(PrizeLimit) as MutableMap<Int, java.lang.ref.WeakReference<*>>
    hosts.clear()
}
