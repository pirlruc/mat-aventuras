package pt.mataventuras.app.prize

import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.ComposeHostActivity
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.app.ui.lesson.ShapeGlyph
import pt.mataventuras.app.ui.lesson.StarGrid
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.prize.AnswerSnake
import pt.mataventuras.domain.prize.BalloonPop
import pt.mataventuras.domain.prize.ComposePrize
import pt.mataventuras.domain.prize.PrizeOffer

@RunWith(RobolectricTestRunner::class)
class ComposePrizeScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComposeHostActivity>()

    @Before
    fun accessibilityChecks() {
        compose.enableAccessibilityChecks()
    }

    @Test
    fun titlesNameTheNewLessons() {
        assertEquals("Qual é diferente", ModuleTitles.noun(LearningModule.ODD_ONE))
        assertEquals("Grande e pequeno", ModuleTitles.noun(LearningModule.SIZE))
        assertEquals("Padrões", ModuleTitles.noun(LearningModule.PATTERNS))
        assertEquals("Que horas são", ModuleTitles.noun(LearningModule.CLOCK))
        assertEquals("Euros e cêntimos", ModuleTitles.noun(LearningModule.MONEY))
        assertEquals("Frações", ModuleTitles.noun(LearningModule.FRACTION))
        assertEquals("Até 100", ModuleTitles.noun(LearningModule.ORDER))
        assertEquals("Problemas", ModuleTitles.noun(LearningModule.WORD_PROBLEM))
        assertEquals("Tangram", ModuleTitles.noun(LearningModule.TANGRAM))
        assertEquals("Sílabas", ModuleTitles.noun(LearningModule.SPELL))
    }

    @Test
    fun balloonsFinishOnTheNamedTap() {
        val round = BalloonPop.round(Random(1))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.BALLOONS, Random(1)) { finished += 1 }
        }
        val wrong = (round.labels.indexOf(round.target) + 1) % round.labels.size
        compose.onNodeWithTag("balloon-$wrong").performClick()
        assertEquals(0, finished)
        compose.onNodeWithTag("balloon-${round.labels.indexOf(round.target)}").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun snakeGrowsOnlyOnTheRightSum() {
        val first = AnswerSnake.round(Random(2))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.SNAKE, Random(2)) { finished += 1 }
        }
        val wrong = (first.correct + 1) % first.options.size
        compose.onNodeWithTag("snake-$wrong").performClick()
        compose.onNodeWithText("Cobra: 1").assertExists()
        val mirror = Random(2)
        var round = AnswerSnake.round(mirror)
        repeat(3) {
            compose.onNodeWithTag("snake-${round.correct}").performClick()
            round = AnswerSnake.bite(round, round.correct, mirror)
        }
        assertEquals(1, finished)
        compose.onNodeWithText(pt.mataventuras.domain.voice.VoiceScripts.SNAKE_DONE).assertExists()
    }

    @Test
    fun fishingFinishesOnTheNamedFish() {
        val fish =
            pt.mataventuras.domain.prize.FishCatch
                .round(Random(4))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.FISHING, Random(4)) { finished += 1 }
        }
        val miss = (fish.labels.indexOf(fish.target) + 1) % fish.labels.size
        compose.onNodeWithTag("fish-$miss").performClick()
        assertEquals(0, finished)
        compose.onNodeWithTag("fish-${fish.labels.indexOf(fish.target)}").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun colouringFinishesOnTheNamedColour() {
        val colour =
            pt.mataventuras.domain.prize.Colouring
                .round(Random(5))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.COLOURING, Random(5)) { finished += 1 }
        }
        compose.onNodeWithTag("paint").assertExists()
        val miss = (colour.labels.indexOf(colour.target) + 1) % colour.labels.size
        compose.onNodeWithTag("colour-$miss").performClick()
        assertEquals(0, finished)
        compose.onNodeWithTag("colour-${colour.labels.indexOf(colour.target)}").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun bubblesFinishOnlyInOrder() {
        val bubbles =
            pt.mataventuras.domain.prize.BubbleRhythm
                .round(Random(6))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.BUBBLES, Random(6)) { finished += 1 }
        }
        val miss = bubbles.labels.indexOfFirst { it != "1" }
        compose.onNodeWithTag("bubble-$miss").performClick()
        compose.onNodeWithText("Bolhas: 0").assertExists()
        var step = bubbles
        listOf("1", "2", "3").forEach { label ->
            compose.onNodeWithTag("bubble-${step.labels.indexOf(label)}").performClick()
            step =
                pt.mataventuras.domain.prize.BubbleRhythm
                    .pop(step, step.labels.indexOf(label))
        }
        assertEquals(1, finished)
    }

    @Test
    fun emptyAndSingleStarGridsDraw() {
        compose.setContent {
            StarGrid(0)
            StarGrid(1)
            GeometricShape.entries.forEach { ShapeGlyph(it) }
        }
    }

    @Test
    fun breakoutFinishesOnTheNamedLetter() {
        val round =
            pt.mataventuras.domain.prize.LetterBreakout
                .round(Random(7))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.BREAKOUT, Random(7)) { finished += 1 }
        }
        val hit = round.labels.indexOf(round.target)
        compose.onNodeWithTag("breakout-${(hit + 1) % round.labels.size}").performClick()
        assertEquals(0, finished)
        compose.onNodeWithTag("breakout-$hit").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun towerFinishesOnTheSum() {
        val round =
            pt.mataventuras.domain.prize.TowerDefence
                .round(Random(8))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.TOWER, Random(8)) { finished += 1 }
        }
        val hit = round.labels.indexOf(round.target)
        compose.onNodeWithTag("tower-${(hit + 1) % round.labels.size}").performClick()
        assertEquals(0, finished)
        compose.onNodeWithTag("tower-$hit").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun runnerFinishesOnTheRightGate() {
        val round =
            pt.mataventuras.domain.prize.MathGate
                .round(Random(9))
        var finished = 0
        compose.setContent {
            PrizeScreen(ComposePrize.RUNNER, Random(9)) { finished += 1 }
        }
        val hit = round.labels.indexOf(round.target)
        compose.onNodeWithTag("gate-${(hit + 1) % round.labels.size}").performClick()
        assertEquals(0, finished)
        compose.onNodeWithTag("gate-$hit").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun launchIntentsMatchTheOffer() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val balloons =
            PrizeLaunch.intent(
                context,
                AgeBand.THREE_TO_FIVE,
                Mascot.HERO_PUP,
                "Ana",
                PrizeOffer.Compose(ComposePrize.BALLOONS),
            )
        assertEquals(ComposePrizeActivity::class.java.name, balloons.component?.className)
        assertEquals(ComposePrize.BALLOONS, prizeFrom(balloons.getStringExtra(ComposePrizeActivity.EXTRA)))
        assertEquals(AgeBand.SIX_TO_EIGHT, ageFor(ComposePrize.SNAKE))
        assertEquals(AgeBand.SIX_TO_EIGHT, ageFor(ComposePrize.BREAKOUT))
        assertEquals(AgeBand.SIX_TO_EIGHT, ageFor(ComposePrize.TOWER))
        assertEquals(AgeBand.SIX_TO_EIGHT, ageFor(ComposePrize.RUNNER))
        assertEquals(AgeBand.THREE_TO_FIVE, ageFor(ComposePrize.BALLOONS))
        assertEquals(AgeBand.THREE_TO_FIVE, ageFor(ComposePrize.FISHING))
        assertEquals(AgeBand.THREE_TO_FIVE, ageFor(ComposePrize.COLOURING))
        assertEquals(AgeBand.THREE_TO_FIVE, ageFor(ComposePrize.BUBBLES))
        assertEquals(ComposePrize.BALLOONS, prizeFrom("missing"))
        val godot =
            PrizeLaunch.intent(
                context,
                AgeBand.SIX_TO_EIGHT,
                Mascot.BRAVE_PLUMBER,
                "Rui",
                PrizeOffer.Godot(pt.mataventuras.domain.engine.RewardGame.INVADERS),
            )
        val scene = godot.getStringExtra("scene")
        org.junit.Assert.assertTrue(scene == "INVADERS" || scene == "RUNNER")
        org.junit.Assert.assertTrue(godot.component?.className != ComposePrizeActivity::class.java.name)
    }

    @Test
    fun introSpeaksTheGoalAndVoltarLeaves() {
        var left = 0
        compose.setContent {
            PrizeScreen(
                ComposePrize.BALLOONS,
                Random(1),
                introduce = true,
                onExit = { left += 1 },
            ) { }
        }
        compose.onNodeWithText("Jogar").assertExists()
        compose.onNodeWithText("Voltar").performClick()
        assertEquals(1, left)
        compose.onNodeWithText("Jogar").performClick()
        compose.onNodeWithText("Jogar").assertDoesNotExist()
    }
}
