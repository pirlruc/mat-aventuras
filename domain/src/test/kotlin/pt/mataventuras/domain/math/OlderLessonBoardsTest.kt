package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.mascotFor
import pt.mataventuras.domain.voice.VoiceScripts

class OlderLessonBoardsTest {
    @Test
    fun halvesStayEvenAndTheNextNumberStopsAtOneHundred() {
        val fraction = FractionBoard(Random(0)).make()
        val whole =
            fraction.prompt
                .substringAfter("metade de ")
                .removeSuffix("?")
                .toInt()
        assertEquals(0, whole % 2)
        assertEquals((whole / 2).toString(), fraction.options[fraction.correctIndex])
        val order = OrderBoard(Random(0)).make()
        val shown =
            order.prompt
                .substringAfter(": ")
                .removeSuffix("?")
                .toInt()
        assertTrue(shown in 1..99)
        assertEquals((shown + 1).toString(), order.options[order.correctIndex])
    }

    @Test
    fun aWordProblemAddsTheApplesAndSpellingOpensTheWord() {
        val story = WordProblemBoard(Random(1)).make()
        val had =
            story.prompt
                .substringAfter("tem ")
                .substringBefore(" maçãs")
                .toInt()
        val got =
            story.prompt
                .substringAfter("recebe ")
                .substringBefore(".")
                .toInt()
        assertEquals((had + got).toString(), story.options[story.correctIndex])
        val spell = SpellBoard(Random(1)).make()
        val word = spell.prompt.substringAfter("sílaba de ").removeSuffix("?")
        val syllable = spell.options[spell.correctIndex]
        assertTrue(word.startsWith(syllable))
        assertEquals(VoiceScripts.SPELL_SPOKEN, spell.spoken)
    }

    @Test
    fun tangramHidesThePieceName() {
        val piece = TangramBoard(Random(2)).make()
        val shape = checkNotNull(piece.targetShape)
        assertEquals(VoiceScripts.TANGRAM_PROMPT, piece.prompt)
        assertTrue(shape.displayName !in piece.prompt)
        assertEquals(shape.displayName, piece.options[piece.correctIndex])
    }

    @Test
    fun theGeneratorHostsEachOlderBoard() {
        val modules =
            listOf(
                LearningModule.FRACTION,
                LearningModule.ORDER,
                LearningModule.WORD_PROBLEM,
                LearningModule.TANGRAM,
                LearningModule.SPELL,
            )
        modules.forEach { module ->
            val round = ExerciseGenerator(Random(1)).generate(module, 0, AgeBand.SIX_TO_EIGHT)
            assertEquals(module, round.module)
        }
        assertEquals(Mascot.BRAVE_PLUMBER, mascotFor(LearningModule.FRACTION))
        assertEquals(Mascot.BRAVE_PLUMBER, mascotFor(LearningModule.ORDER))
        assertEquals(Mascot.HERO_PUP, mascotFor(LearningModule.WORD_PROBLEM))
        assertEquals(Mascot.PINK_PIGLET, mascotFor(LearningModule.TANGRAM))
        assertEquals(Mascot.HERO_PUP, mascotFor(LearningModule.SPELL))
        assertThrows(IllegalStateException::class.java) {
            olderRound(LearningModule.COUNTING, Random(0))
        }
    }
}
