package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class GroupBoardsTest {
    @Test
    fun takeAwayCountsOnlyCircles() {
        repeat(8) { level ->
            val exercise = boards(level).takeAway(LearningModule.SUBTRACTION, level)
            val remain = exercise.play.cells.count { it == GroupTokens.KEPT }
            assertEquals(remain, answer(exercise))
            assertTrue(exercise.play.cells.any { it == GroupTokens.REMOVED })
            assertTrue(remain >= 1)
            assertEquals(6, exercise.play.columns)
            assertEquals(PlayKind.GROUPS, exercise.play.kind)
            assertTrue(exercise.spoken.contains("cruzes"))
        }
    }

    @Test
    fun arrayIsRowsTimesColumns() {
        repeat(6) { level ->
            val exercise = boards(level).array(LearningModule.MULTIPLICATION, level)
            val cols = exercise.play.columns
            val rows = exercise.play.cells.size / cols
            assertTrue(exercise.play.cells.all { it == GroupTokens.KEPT })
            assertEquals(0, exercise.play.cells.size % cols)
            assertEquals(rows * cols, answer(exercise))
            assertTrue(exercise.spoken.contains("Multiplica"))
        }
    }

    @Test
    fun shareAsksTheRowOrTheShare() {
        val prompts = (0..24).map { seed -> boards(seed).share(LearningModule.DIVISION, seed % 4) }
        assertTrue(prompts.any { it.prompt.contains("recebe") })
        assertTrue(prompts.any { it.prompt.contains("amigos há") })
        prompts.forEach { exercise ->
            val cols = exercise.play.columns
            val rows = exercise.play.cells.size / cols
            val expected = if (exercise.prompt.contains("recebe")) cols else rows
            assertEquals(expected, answer(exercise))
            assertTrue(exercise.play.cells.all { it == GroupTokens.KEPT })
        }
    }

    @Test
    fun makePicksThePictureForEachOperation() {
        val boards = boards(3)
        assertTrue(boards.make(LearningModule.SUBTRACTION, 1).prompt.contains("cruzes"))
        assertTrue(boards.make(LearningModule.DIVISION, 1).prompt.contains("amigo"))
        assertTrue(boards.make(LearningModule.MULTIPLICATION, 1).prompt.contains("ao todo"))
        assertEquals(LearningModule.ADDITION, boards.make(LearningModule.ADDITION, 0).module)
        val kinds =
            (0..36).map { ExerciseGenerator(Random(it)).generate(LearningModule.DIVISION).play.kind }.toSet()
        assertTrue(PlayKind.CHOICE in kinds)
        assertTrue(PlayKind.GROUPS in kinds)
        assertTrue(PlayKind.GROUPS in PlayKinds.forModule(LearningModule.SUBTRACTION))
        assertTrue(PlayKind.GROUPS in PlayKinds.forModule(LearningModule.MULTIPLICATION))
        assertTrue(PlayKind.GROUPS !in PlayKinds.forModule(LearningModule.ADDITION))
        assertTrue(PlayKind.GROUPS !in PlayKinds.forModule(LearningModule.COUNTING))
    }

    private fun boards(seed: Int): GroupBoards {
        val options = ExerciseGenerator(Random(seed + 3))::numericOptions
        return GroupBoards(Random(seed), options)
    }

    private fun answer(exercise: Exercise): Int = exercise.options[exercise.correctIndex].toInt()
}
