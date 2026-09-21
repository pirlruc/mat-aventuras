package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class SudokuPlayTest {
    @Test
    fun sixBySixFillsEveryBlankInTurn() {
        val full = SudokuGrids.filled(6, Random(4))
        val cells = SudokuHoles.withHoles(full, 6, question = 7, extraWanted = 5, random = Random(8))
        val solution = full.map { it.toString() }
        val holes = SudokuPlay.blanks(cells)
        val alphabet = (1..6).map { it.toString() }
        assertTrue(holes.size > 1)
        holes.forEach { index ->
            assertEquals(listOf(solution[index]), SudokuHoles.candidates(cells, 6, index, alphabet))
        }
        var state = SudokuPlay.start(cells)
        assertEquals(holes.first(), state.focus)
        val later = holes.last()
        state = SudokuPlay.focus(state, later)
        assertEquals(later, state.focus)
        val wrong = SudokuPlay.place(state, token = "0", solution = solution)
        assertFalse(wrong.correct)
        assertEquals(state.cells, wrong.state.cells)
        var remaining = holes.size
        state = SudokuPlay.start(cells)
        while (remaining > 0) {
            val step = SudokuPlay.place(state, solution[state.focus], solution)
            assertTrue(step.correct)
            remaining -= 1
            assertEquals(remaining == 0, step.solved)
            state = step.state
        }
        assertTrue(SudokuPlay.blanks(state.cells).isEmpty())
    }

    @Test
    fun ageSevenBoardPublishesASolutionForEveryBlank() {
        val factory = PlayBoardFactory(Random(11)) { correct, min, max -> listOf(correct, min, max, min + 1) }
        val board = factory.sudoku(LearningModule.ADDITION, level = 2)
        assertEquals(36, board.play.cells.size)
        assertTrue(board.play.solution.size == 36)
        assertTrue(SudokuPlay.blanks(board.play.cells).size > 1)
        assertEquals("Preenche as casas vazias.", board.prompt)
        val started = SudokuPlay.start(board.play.cells)
        val step = SudokuPlay.place(started, board.play.solution[started.focus], board.play.solution)
        assertTrue(step.correct)
        assertFalse(step.solved)
    }

    @Test
    fun focusAndPlaceIgnoreFilledOrMissingHouses() {
        val cells = listOf("1", "", "3", SudokuHoles.EXTRA_BLANK)
        val solution = listOf("1", "2", "3", "4")
        val start = SudokuPlay.start(cells)
        assertEquals(1, start.focus)
        assertEquals(listOf(1, 3), SudokuPlay.blanks(cells))
        assertEquals(start, SudokuPlay.focus(start, 0))
        assertEquals(start, SudokuPlay.focus(start, 9))
        assertEquals(3, SudokuPlay.focus(start, 3).focus)
        val full = SudokuPlay.start(listOf("1", "2"))
        assertEquals(-1, full.focus)
        val missing = SudokuPlay.place(full, "1", listOf("1", "2"))
        assertFalse(missing.correct)
        assertFalse(missing.solved)
        val filled = SudokuBoardState(cells = listOf("1"), focus = 0)
        val stuck = SudokuPlay.place(filled, "1", listOf("1"))
        assertFalse(stuck.correct)
        assertEquals(filled, stuck.state)
        val placed = SudokuPlay.place(start, solution[1], solution)
        assertTrue(placed.correct)
        assertEquals(3, placed.state.focus)
        assertFalse(placed.solved)
    }
}
