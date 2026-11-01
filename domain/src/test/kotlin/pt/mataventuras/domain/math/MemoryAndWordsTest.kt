package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.mascotFor

class MemoryAndWordsTest {
    @Test
    fun youngMemoryIsThreeIdenticalPairs() {
        val exercise = MemoryBoards(Random(1)).make(LearningModule.MEMORY, 0, AgeBand.THREE_TO_FIVE)
        assertEquals(3, exercise.play.columns)
        assertEquals(6, exercise.play.cells.size)
        assertEquals(
            exercise.play.cells
                .groupingBy { it }
                .eachCount()
                .values
                .toSet(),
            setOf(2),
        )
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
        assertTrue(
            exercise.play.cells
                .single()
                .contains("_"),
        )
        assertTrue(exercise.isCorrect(exercise.correctIndex))
        val firstLetter = WordComplete(Random(1)).make(LearningModule.LETTERS, 0)
        assertTrue(
            firstLetter.play.cells
                .single()
                .startsWith("_"),
        )
        val later = WordComplete(Random(4)).make(LearningModule.COUNTING, 2)
        assertTrue(
            later.play.cells
                .single()
                .contains("_"),
        )
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
        assertTrue(AgeBand.THREE_TO_FIVE in LearningModule.LETTERS.bands)
        assertFalse(AgeBand.THREE_TO_FIVE in LearningModule.WORDS.bands)
        val youngLetters = MemoryBoards(Random(4)).make(LearningModule.LETTERS, 0, AgeBand.THREE_TO_FIVE)
        assertEquals(3, youngLetters.play.columns)
        assertEquals(6, youngLetters.play.cells.size)
        assertTrue(mascotFor(LearningModule.MEMORY) == mascotFor(LearningModule.LETTERS))
        assertTrue(mascotFor(LearningModule.WORDS) != mascotFor(LearningModule.MEMORY))
        assertEquals(listOf(PlayKind.MEMORY), PlayKinds.forModule(LearningModule.MEMORY))
        assertEquals(listOf(PlayKind.WORD_COMPLETE), PlayKinds.forModule(LearningModule.WORDS))
        org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            ExerciseGenerator(Random(1)).choice(LearningModule.MEMORY)
        }
    }

    @Test
    fun memoryFaceKeepsMatchesAndIgnoresARepeat() {
        val play = MemoryBoards(Random(1)).make(LearningModule.MEMORY, 0, AgeBand.THREE_TO_FIVE).play
        val first = 0
        val partner = play.solution.indices.first { it != first && play.solution[it] == play.solution[first] }
        val other = play.solution.indices.first { play.solution[it] != play.solution[first] }
        val opened = MemoryFace().reveal(play, first)
        assertEquals(listOf(first), opened.reveal(play, first).up)
        val mismatch = opened.reveal(play, other)
        assertEquals(listOf(first, other), mismatch.up)
        val cleared = mismatch.reveal(play, partner)
        assertEquals(listOf(partner), cleared.up)
        assertEquals(cleared, cleared.reveal(play, -1))
        assertEquals(cleared, cleared.reveal(play, partner))
        var solved = MemoryFace()
        play.solution.withIndex().groupBy { it.value }.values.forEach { group ->
            solved = solved.reveal(play, group[0].index).reveal(play, group[1].index)
        }
        assertTrue(solved.solved(play.cells.size))
        assertFalse(solved.solved(0))
        assertEquals(solved, solved.reveal(play, first))
        MemoryBoards(Random(1)).make(LearningModule.MEMORY, -1, AgeBand.THREE_TO_FIVE)
        assertFalse(usesOlderBoard(LearningModule.ADDITION, AgeBand.THREE_TO_FIVE))
        assertTrue(usesOlderBoard(LearningModule.ADDITION, AgeBand.SIX_TO_EIGHT))
        assertFalse(usesOlderBoard(LearningModule.NUMBERS, AgeBand.SIX_TO_EIGHT))
    }
}
