package pt.mataventuras.app

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.app.engine.Platformer2dLoop
import pt.mataventuras.app.engine.kartKeepsTicking
import pt.mataventuras.app.engine.noteRunnerJump
import pt.mataventuras.app.engine.runnerKeepsTicking
import pt.mataventuras.app.prize.finishIfPopped
import pt.mataventuras.app.ui.SoupRelease
import pt.mataventuras.app.ui.lesson.extendSoup
import pt.mataventuras.app.ui.lesson.foundSoupCells
import pt.mataventuras.app.ui.lesson.puzzleCells
import pt.mataventuras.app.ui.lesson.scoreSudokuPick
import pt.mataventuras.app.ui.lesson.shownSudokuCells
import pt.mataventuras.app.ui.lesson.soupBoardCells
import pt.mataventuras.app.ui.lesson.sudokuExtraHeight
import pt.mataventuras.app.ui.lesson.sudokuTitle
import pt.mataventuras.app.ui.lesson.visualDotCount
import pt.mataventuras.app.ui.soupIndexAt
import pt.mataventuras.app.ui.soupPickIndex
import pt.mataventuras.app.ui.soupReleaseKind
import pt.mataventuras.app.ui.soupSlotOrNull
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.PlayBoard
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.math.SudokuPlay
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.prize.BalloonPop

class BranchGapTest {
    @Test
    fun lessonHelpersCoverTheEmptyAndPartialBoards() {
        assertEquals("Sudoku", sudokuTitle(PlayKind.SOUP, AgeBand.SIX_TO_EIGHT))
        assertEquals("Quadrado mágico", sudokuTitle(PlayKind.SUDOKU, AgeBand.THREE_TO_FIVE))
        assertEquals(0, sudokuExtraHeight(AgeBand.THREE_TO_FIVE))
        assertEquals(8, sudokuExtraHeight(AgeBand.SIX_TO_EIGHT))
        assertEquals(listOf("a"), shownSudokuCells(emptyList(), listOf("a")))
        assertEquals(listOf("1"), shownSudokuCells(listOf("1"), listOf("a")))
        assertEquals(listOf("b"), soupBoardCells(emptyList(), listOf("b")))
        assertEquals(listOf("2"), soupBoardCells(listOf("2"), emptyList()))
        assertEquals(emptySet<Int>(), foundSoupCells(emptySet(), listOf(listOf(1))))
        assertEquals(setOf(1, 2), foundSoupCells(setOf(0), listOf(listOf(1, 2))))
        assertEquals(setOf(1), foundSoupCells(setOf(0, 4), listOf(listOf(1))))
        assertEquals(emptySet<Int>(), foundSoupCells(setOf(-1), listOf(listOf(1))))
        assertEquals(listOf(0, 1), extendSoup(listOf(0), 1, 2))
        assertEquals(listOf(0), extendSoup(listOf(0), null, 2))
        assertEquals(listOf("1", "2", "3", "?"), puzzleCells(emptyList()))
        assertEquals(listOf("9"), puzzleCells(listOf("9")))
        assertEquals(0, visualDotCount(LearningModule.SHAPES, "3"))
        assertEquals(0, visualDotCount(LearningModule.COUNTING, "nope"))
        assertEquals(3, visualDotCount(LearningModule.COUNTING, "3"))

        val exercise =
            Exercise(
                module = LearningModule.NUMBERS,
                prompt = "Sudoku",
                spoken = "Sudoku",
                options = listOf("4", "5"),
                correctIndex = 0,
                play =
                    PlayBoard(
                        kind = PlayKind.SUDOKU,
                        cells = listOf("", "", "2", "3"),
                        columns = 2,
                        solution = listOf("4", "5", "2", "3"),
                    ),
            )
        var called = false
        val partial =
            scoreSudokuPick(true, exercise, SudokuPlay.start(exercise.play.cells), 0) { _, _ ->
                called = true
            }
        assertFalse(called)
        assertEquals("4", partial.cells[0])
        assertEquals("", partial.cells[1])
    }

    @Test
    fun prizeAndHostDecisionsCoverBothSides() {
        val round = BalloonPop.round(Random(1))
        var pops = 0
        finishIfPopped(round, -1) { pops += 1 }
        assertEquals(0, pops)
        finishIfPopped(round, round.labels.indexOf(round.target)) { pops += 1 }
        assertEquals(1, pops)

        assertFalse(runnerKeepsTicking(finished = true, alive = true, frames = 0, limit = 2))
        assertFalse(runnerKeepsTicking(finished = false, alive = false, frames = 0, limit = 2))
        assertFalse(runnerKeepsTicking(finished = false, alive = true, frames = 2, limit = 2))
        assertTrue(runnerKeepsTicking(finished = false, alive = true, frames = 0, limit = 2))
        assertFalse(kartKeepsTicking(finished = true, frames = 0, limit = 2))
        assertFalse(kartKeepsTicking(finished = false, frames = 2, limit = 2))
        assertTrue(kartKeepsTicking(finished = false, frames = 0, limit = 2))

        val loop = Platformer2dLoop()
        noteRunnerJump(loop, flickX = 0f, flickY = -10f)
        assertFalse(loop.jumping)
        noteRunnerJump(loop, flickX = 0f, flickY = -80f)
        assertTrue(loop.jumping)

        assertEquals(
            SoupRelease.MISS,
            soupReleaseKind(listOf(5), listOf(listOf(0, 1), listOf(2, 3)), setOf(0, 1)),
        )
        assertEquals(
            SoupRelease.IGNORE,
            soupReleaseKind(listOf(0), listOf(listOf(1, 2, 3)), emptySet()),
        )
        assertEquals(
            SoupRelease.MISS,
            soupReleaseKind(listOf(9), listOf(listOf(1, 2, 3), listOf(4)), emptySet()),
        )
        assertEquals(
            SoupRelease.MISS,
            soupReleaseKind(listOf(9), listOf(listOf(4), listOf(1, 2, 3)), emptySet()),
        )
        assertEquals(null, soupSlotOrNull(cell = 10f, offset = 0f, gap = 1f, count = 0))
        assertEquals(0, soupSlotOrNull(cell = 10f, offset = 1f, gap = 1f, count = 2))
        assertEquals(
            0,
            soupPickIndex(listOf(0, 1), listOf(listOf(2, 3)), emptySet(), 4),
        )
        assertEquals(null, soupIndexAt(1f, 1f, 40f, 40f, 0, 0, 1f))
    }
}
