package pt.mataventuras.app.prize

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.ComposeHostActivity
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.domain.model.AgeBand
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

    @Test
    fun titlesNameTheNewLessons() {
        assertEquals("Qual é diferente", ModuleTitles.noun(LearningModule.ODD_ONE))
        assertEquals("Grande e pequeno", ModuleTitles.noun(LearningModule.SIZE))
        assertEquals("Padrões", ModuleTitles.noun(LearningModule.PATTERNS))
        assertEquals("Que horas são", ModuleTitles.noun(LearningModule.CLOCK))
        assertEquals("Euros e cêntimos", ModuleTitles.noun(LearningModule.MONEY))
    }

    @Test
    fun balloonsFinishOnTheNamedTap() {
        val round = BalloonPop.round(Random(1))
        var finished = 0
        compose.setContent {
            BalloonPopScreen(Random(1)) { finished += 1 }
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
            AnswerSnakeScreen(Random(2)) { finished += 1 }
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
        assertEquals(AgeBand.THREE_TO_FIVE, ageFor(ComposePrize.BALLOONS))
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
}
