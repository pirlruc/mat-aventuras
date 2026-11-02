package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.mascotFor
import pt.mataventuras.domain.prize.AnswerSnake
import pt.mataventuras.domain.prize.BalloonPop
import pt.mataventuras.domain.prize.ComposePrize
import pt.mataventuras.domain.prize.PrizeClock
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
        assertEquals("Que horas são?", clock.prompt)
        assertFalse(clock.prompt.any { it.isDigit() })
        assertTrue(clock.isCorrect(clock.correctIndex))
        val money = MoneyBoard(Random(4))
        val exercise = money.make()
        assertEquals(money.price(1, 10), "1,10 €")
        val short = mutableListOf(money.price(2, 10))
        money.fillPrices(short, money.price(2, 10), 1, 10)
        assertEquals(3, short.size)
        val skipped = mutableListOf(money.price(3, 10))
        money.fillPrices(skipped, "9,99 €", 1, 10)
        assertEquals(3, skipped.size)
        val full = mutableListOf("1,00 €", "2,00 €", "3,00 €")
        money.fillPrices(full, "9,00 €", 1, 0)
        assertEquals(3, full.size)
        assertTrue(exercise.prompt.contains("€"))
        assertTrue(exercise.isCorrect(exercise.correctIndex))
        repeat(30) { seed ->
            val round = MoneyBoard(Random(seed)).make()
            assertEquals(4, round.options.size)
            assertEquals(round.options.size, round.options.toSet().size)
        }
        assertEquals(MascotCheck.CLOCK, mascotFor(LearningModule.CLOCK))
        assertEquals(MascotCheck.MONEY, mascotFor(LearningModule.MONEY))
        assertEquals(MascotCheck.ODD, mascotFor(LearningModule.ODD_ONE))
        assertEquals(MascotCheck.SIZE, mascotFor(LearningModule.SIZE))
        assertEquals(MascotCheck.PATTERNS, mascotFor(LearningModule.PATTERNS))
    }

    @Test
    fun youngRoundsCoverColourDotsClapsAndShadow() {
        val sort = (0..8).map { SortBoard(Random(it)).make() }
        assertTrue(sort.any { it.options.contains("vermelho") })
        assertTrue(sort.any { it.targetShape != null })
        sort.forEach { round ->
            assertEquals(LearningModule.SORT, round.module)
            assertTrue(round.isCorrect(round.correctIndex))
        }
        val dots = DotsBoard(Random(2)).make()
        assertEquals(dots.visualCount + 1, dots.options[dots.correctIndex].toInt())
        assertTrue(dots.visualCount in 1..9)
        val claps = ClapBoard(Random(3)).make()
        val word = claps.prompt.substringAfter("para ").removeSuffix("?")
        val expected = ClapBoard.WORDS.first { it.first == word }.second
        assertEquals(expected.toString(), claps.options[claps.correctIndex])
        val shadow = ShadowBoard(Random(4)).make()
        val shadowShape = checkNotNull(shadow.targetShape)
        assertEquals(VoiceScripts.SHADOW_PROMPT, shadow.prompt)
        assertFalse(shadow.prompt.contains(shadowShape.displayName))
        assertEquals(shadowShape.displayName, shadow.options[shadow.correctIndex])
        listOf(LearningModule.SORT, LearningModule.DOTS, LearningModule.CLAPS, LearningModule.SHADOW).forEach {
            val round = ExerciseGenerator(Random(1)).generate(it, 0, AgeBand.THREE_TO_FIVE)
            assertEquals(it, round.module)
        }
        assertEquals(Mascot.SPEEDY_HEDGEHOG, mascotFor(LearningModule.DOTS))
        assertEquals(Mascot.PINK_PIGLET, mascotFor(LearningModule.SORT))
        assertEquals(Mascot.PINK_PIGLET, mascotFor(LearningModule.SHADOW))
        assertEquals(Mascot.HERO_PUP, mascotFor(LearningModule.CLAPS))
        assertThrows(IllegalArgumentException::class.java) {
            Exercise(
                module = LearningModule.SORT,
                prompt = "a",
                spoken = "b",
                options = listOf("a"),
                correctIndex = 3,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            Exercise(
                module = LearningModule.SORT,
                prompt = "a",
                spoken = "b",
                options = listOf("a"),
                correctIndex = 0,
                play = PlayBoard(targetIndices = listOf(4)),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            Exercise(
                module = LearningModule.SORT,
                prompt = "a",
                spoken = "b",
                options = listOf("a"),
                correctIndex = 0,
                play = PlayBoard(wordPaths = listOf(listOf(4))),
            )
        }
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
        assertEquals(
            VoiceScripts.SNAKE_PROMPT,
            snake.prompt.substringBefore(" 1").take(VoiceScripts.SNAKE_PROMPT.length),
        )
    }

    @Test
    fun eachAgeCanDrawItsComposePrize() {
        val young = (0..60).map { PrizeOffers.pick(AgeBand.THREE_TO_FIVE, Random(it)) }
        val older = (0..80).map { PrizeOffers.pick(AgeBand.SIX_TO_EIGHT, Random(it)) }
        assertTrue(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.BALLOONS })
        assertTrue(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.FISHING })
        assertTrue(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.COLOURING })
        assertTrue(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.BUBBLES })
        assertTrue(young.any { it is PrizeOffer.Godot })
        assertTrue(older.any { it is PrizeOffer.Compose && it.prize == ComposePrize.SNAKE })
        assertTrue(older.any { it is PrizeOffer.Compose && it.prize == ComposePrize.BREAKOUT })
        assertTrue(older.any { it is PrizeOffer.Compose && it.prize == ComposePrize.TOWER })
        assertTrue(older.any { it is PrizeOffer.Compose && it.prize == ComposePrize.RUNNER })
        assertFalse(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.BREAKOUT })
        assertTrue(older.any { it is PrizeOffer.Godot })
        assertFalse(young.any { it is PrizeOffer.Compose && it.prize == ComposePrize.SNAKE })
        val wide = (0 until 80).map { PrizeOffers.pick(AgeBand.SIX_TO_EIGHT, Random(it)) }
        assertTrue(wide.any { it is PrizeOffer.Godot && it.game == RewardGame.KART })
        assertFalse(
            (0 until 40).any { seed ->
                val offer = PrizeOffers.pick(AgeBand.THREE_TO_FIVE, Random(seed))
                offer is PrizeOffer.Godot && offer.game == RewardGame.KART
            },
        )
        val deadline = PrizeClock.deadlineMs(1_000L, 120)
        assertFalse(PrizeClock.expired(1_000L, deadline, 120))
        assertTrue(PrizeClock.expired(deadline, deadline, 120))
        assertTrue(wordDistractorLetters.containsAll(listOf("ã", "ç", "é")))
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
