package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.mascotFor
import pt.mataventuras.domain.prize.AnswerSnake
import pt.mataventuras.domain.prize.BalloonPop
import pt.mataventuras.domain.prize.ComposePrize
import pt.mataventuras.domain.prize.PrizeOffer
import pt.mataventuras.domain.prize.PrizeOffers
import pt.mataventuras.domain.voice.VoiceScripts

class NewLessonBoardsTest {
    @Test
    fun youngBoardsNameTheAnswer() {
        val prompts = (0..12).map { SizeBoard(Random(it)).make().prompt }.toSet()
        assertTrue(VoiceScripts.SIZE_SMALL_PROMPT in prompts)
        assertTrue(VoiceScripts.SIZE_BIG_PROMPT in prompts)
        listOf(LearningModule.ODD_ONE, LearningModule.SIZE, LearningModule.PATTERNS).forEach { module ->
            val exercise = ExerciseGenerator(Random(2)).generate(module, 0, AgeBand.THREE_TO_FIVE)
            assertEquals(module, exercise.module)
            assertTrue(exercise.isCorrect(exercise.correctIndex))
            assertFalse(exercise.isCorrect((exercise.correctIndex + 1) % exercise.options.size))
        }
        assertEquals("Pequeno", SizeBoard(forcedSmall()).make().options[SizeBoard(forcedSmall()).make().correctIndex])
    }

    @Test
    fun olderBoardsReadTheClockAndThePrice() {
        val clock = ClockBoard(Random(3)).make()
        assertTrue(clock.prompt.startsWith("Que horas são?"))
        assertTrue(clock.isCorrect(clock.correctIndex))
        val money = MoneyBoard(Random(4))
        val exercise = money.make()
        assertEquals(money.price(1, 10), "1,10 €")
        assertTrue(exercise.prompt.contains("€"))
        assertTrue(exercise.isCorrect(exercise.correctIndex))
        assertEquals(MascotCheck.CLOCK, mascotFor(LearningModule.CLOCK))
        assertEquals(MascotCheck.MONEY, mascotFor(LearningModule.MONEY))
        assertEquals(MascotCheck.ODD, mascotFor(LearningModule.ODD_ONE))
        assertEquals(MascotCheck.SIZE, mascotFor(LearningModule.SIZE))
        assertEquals(MascotCheck.PATTERNS, mascotFor(LearningModule.PATTERNS))
    }

    @Test
    fun balloonsAndSnakeStayOnATap() {
        val round = BalloonPop.round(Random(5))
        val target = round.labels.indexOf(round.target)
        assertTrue(BalloonPop.popped(round, target))
        assertFalse(BalloonPop.popped(round, -1))
        assertFalse(BalloonPop.popped(round, (target + 1) % round.labels.size))
        val snake = AnswerSnake.round(Random(6))
        val miss = AnswerSnake.bite(snake, (snake.correct + 1) % snake.options.size, Random(7))
        assertEquals(snake.length, miss.length)
        var grown = snake
        repeat(3) { grown = AnswerSnake.bite(grown, grown.correct, Random(8 + it)) }
        assertTrue(AnswerSnake.full(grown))
        assertEquals(VoiceScripts.SNAKE_PROMPT, snake.prompt.substringBefore(" 1").take(VoiceScripts.SNAKE_PROMPT.length))
    }

    @Test
    fun eachAgeCanDrawItsComposePrize() {
        val young = (0..20).map { PrizeOffers.pick(AgeBand.THREE_TO_FIVE, Random(it)) }
        val older = (0..20).map { PrizeOffers.pick(AgeBand.SIX_TO_EIGHT, Random(it)) }
        assertTrue(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.BALLOONS })
        assertTrue(young.any { it is PrizeOffer.Godot })
        assertTrue(older.any { it is PrizeOffer.Compose && it.prize == ComposePrize.SNAKE })
        assertTrue(older.any { it is PrizeOffer.Godot })
        assertFalse(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.SNAKE })
    }

    private fun forcedSmall(): Random =
        object : Random() {
            override fun nextBits(bitCount: Int): Int = 1
        }
}

private object MascotCheck {
    val CLOCK = pt.mataventuras.domain.model.Mascot.BRAVE_PLUMBER
    val MONEY = pt.mataventuras.domain.model.Mascot.BRAVE_PLUMBER
    val ODD = pt.mataventuras.domain.model.Mascot.PINK_PIGLET
    val SIZE = pt.mataventuras.domain.model.Mascot.SPEEDY_HEDGEHOG
    val PATTERNS = pt.mataventuras.domain.model.Mascot.PINK_PIGLET
}
