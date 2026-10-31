package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.isSevenYears
import pt.mataventuras.domain.model.mascotFor

class MemoryAndWordsTest {
    @Test
    fun youngMemoryIsThreeIdenticalPairs() {
        val exercise = MemoryBoards(Random(1)).make(LearningModule.MEMORY, 0)
        assertEquals(3, exercise.play.columns)
        assertEquals(6, exercise.play.cells.size)
        assertEquals(exercise.play.cells.groupingBy { it }.eachCount().values.toSet(), setOf(2))
    }

    @Test
    fun letterMemoryIsSixteenCards() {
        val boards = MemoryBoards(Random(2))
        val exercise = boards.make(LearningModule.LETTERS, 0)
        assertEquals(4, exercise.play.columns)
        assertEquals(16, exercise.play.cells.size)
        val first = exercise.play.solution.indexOf("A")
        val second = exercise.play.solution.lastIndexOf("A")
        assertTrue(boards.isPair(exercise.play, first, second))
    }

    @Test
    fun wordCompleteHidesOneLetter() {
        val exercise = WordComplete(Random(3)).make(LearningModule.WORDS, 2)
        assertEquals(PlayKind.WORD_COMPLETE, exercise.play.kind)
        assertTrue(exercise.play.cells.single().contains("_"))
        assertTrue(exercise.isCorrect(exercise.correctIndex))
        val firstLetter = WordComplete(Random(1)).make(LearningModule.LETTERS, 0)
        assertTrue(firstLetter.play.cells.single().startsWith("_"))
        val later = WordComplete(Random(4)).make(LearningModule.COUNTING, 2)
        assertTrue(later.play.cells.single().contains("_"))
        repeat(8) { seed ->
            WordComplete(Random(seed)).make(LearningModule.WORDS, 0)
        }
    }

    @Test
    fun memoryRejectsTheSameCellAndAMismatch() {
        val boards = MemoryBoards(Random(5))
        val exercise = boards.make(LearningModule.MEMORY, 2)
        assertEquals(4, exercise.play.columns)
        assertFalse(boards.isPair(exercise.play, 0, 0))
        assertFalse(boards.isPair(exercise.play, -1, 0))
        assertFalse(boards.isPair(exercise.play, 0, 99))
        val mismatch =
            (0 until exercise.play.cells.size).first {
                it != 0 && exercise.play.solution[it] != exercise.play.solution[0]
            }
        assertFalse(boards.isPair(exercise.play, 0, mismatch))
    }

    @Test
    fun newModulesHaveAHostAndAreNotChoiceQuestions() {
        assertFalse(LearningModule.MEMORY.isSevenYears())
        assertFalse(LearningModule.LETTERS.isSevenYears())
        assertTrue(LearningModule.WORDS.isSevenYears())
        assertTrue(mascotFor(LearningModule.MEMORY) == mascotFor(LearningModule.LETTERS))
        assertTrue(mascotFor(LearningModule.WORDS) != mascotFor(LearningModule.MEMORY))
        assertEquals(listOf(PlayKind.MEMORY), PlayKinds.forModule(LearningModule.MEMORY))
        assertEquals(listOf(PlayKind.WORD_COMPLETE), PlayKinds.forModule(LearningModule.WORDS))
        org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            ExerciseGenerator(Random(1)).choice(LearningModule.MEMORY)
        }
    }
}
