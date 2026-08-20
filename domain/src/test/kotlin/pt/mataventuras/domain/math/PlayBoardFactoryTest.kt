package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class PlayBoardFactoryTest {
    private val generator = ExerciseGenerator(Random(7))
    private val factory = PlayBoardFactory(Random(7), generator::numericOptions)

    @Test
    fun kindsPerModuleStayAgeAppropriate() {
        val three = PlayKinds.forModule(LearningModule.COUNTING)
        assertTrue(PlayKind.SOUP in three)
        assertTrue(PlayKind.PUZZLE in three)
        assertTrue(PlayKind.CIPHER in three)
        assertFalse(PlayKind.SUDOKU in three)
        assertTrue(PlayKind.SUDOKU in PlayKinds.forModule(LearningModule.SHAPES))
        assertTrue(PlayKind.SOUP in PlayKinds.forModule(LearningModule.LOGIC))
        assertTrue(PlayKind.SOUP in PlayKinds.forModule(LearningModule.NUMBERS))
        assertTrue(PlayKind.CIPHER in PlayKinds.forModule(LearningModule.LOGIC))
        assertTrue(
            PlayKinds.pick(LearningModule.COUNTING, Random(0)) in PlayKinds.forModule(LearningModule.COUNTING),
        )
    }

    @Test
    fun eachBoardKindBuildsASolvableExercise() {
        val shapeSudoku = factory.sudoku(LearningModule.SHAPES)
        assertEquals(PlayKind.SUDOKU, shapeSudoku.play.kind)
        assertEquals(2, shapeSudoku.play.columns)
        assertTrue(shapeSudoku.play.cells.contains(""))
        assertTrue(shapeSudoku.isCorrect(shapeSudoku.correctIndex))

        val numberSudoku = factory.sudoku(LearningModule.LOGIC)
        assertEquals(4, numberSudoku.play.columns)
        assertEquals(16, numberSudoku.play.cells.size)
        val miniSudoku = factory.sudoku(LearningModule.NUMBERS)
        assertEquals(2, miniSudoku.play.columns)
        assertEquals(4, miniSudoku.play.cells.size)

        val shapeSoup = factory.soup(LearningModule.SHAPES)
        assertEquals(PlayKind.SOUP, shapeSoup.play.kind)
        assertTrue(shapeSoup.isCorrect(shapeSoup.play.targetIndices.first()))
        assertFalse(shapeSoup.isCorrect((shapeSoup.correctIndex + 1) % shapeSoup.options.size))

        val countSoup = factory.soup(LearningModule.COUNTING)
        assertEquals(9, countSoup.options.size)
        assertTrue(countSoup.prompt.contains("estrelas"))
        val numberSoup = factory.soup(LearningModule.NUMBERS)
        assertEquals(9, numberSoup.options.size)
        assertTrue(numberSoup.prompt.contains("número"))

        val words = factory.soup(LearningModule.LOGIC)
        assertEquals(16, words.options.size)
        assertTrue(words.play.targetIndices.size >= 4)
        words.play.targetIndices.forEach { assertTrue(words.isCorrect(it)) }

        val puzzle = factory.puzzle(LearningModule.SHAPES)
        assertEquals(PlayKind.PUZZLE, puzzle.play.kind)
        assertEquals("Qual é a peça que falta?", puzzle.prompt)

        val numberPuzzle = factory.puzzle(LearningModule.ADDITION)
        assertTrue(numberPuzzle.options[numberPuzzle.correctIndex].toInt() in 1..9)

        val cipher = factory.cipher(LearningModule.COUNTING)
        assertEquals(PlayKind.CIPHER, cipher.play.kind)
        assertTrue(cipher.play.cipherCode.contains("⭐"))

        val add = factory.cipher(LearningModule.ADDITION)
        assertTrue(add.prompt.contains("+"))
        val sub = factory.cipher(LearningModule.SUBTRACTION)
        assertTrue(sub.prompt.contains("−"))
        val mul = factory.cipher(LearningModule.MULTIPLICATION)
        assertTrue(mul.prompt.contains("×"))
        val numbers = factory.cipher(LearningModule.NUMBERS)
        assertEquals(PlayKind.CIPHER, numbers.play.kind)
        val wordCode = factory.cipher(LearningModule.LOGIC)
        assertTrue(wordCode.prompt.contains("palavra"))
        assertTrue(wordCode.options[wordCode.correctIndex].length >= 4)
    }

    @Test
    fun makeDispatchesAndRejectsChoice() {
        val soup = factory.make(PlayKind.SOUP, LearningModule.SHAPES)
        assertEquals(PlayKind.SOUP, soup.play.kind)
        assertEquals(PlayKind.SUDOKU, factory.make(PlayKind.SUDOKU, LearningModule.NUMBERS).play.kind)
        assertEquals(PlayKind.PUZZLE, factory.make(PlayKind.PUZZLE, LearningModule.COUNTING).play.kind)
        assertEquals(PlayKind.CIPHER, factory.make(PlayKind.CIPHER, LearningModule.ADDITION).play.kind)
        var failed = false
        try {
            factory.make(PlayKind.CHOICE, LearningModule.COUNTING)
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue(failed)
    }

    @Test
    fun generateMixesPlayKindsAcrossSeeds() {
        val kinds =
            (0..40).map { ExerciseGenerator(Random(it)).generate(LearningModule.SHAPES).play.kind }.toSet()
        assertTrue(kinds.size > 1)
        assertTrue(PlayKind.CHOICE in kinds)
    }

    @Test
    fun targetIndicesRejectOutOfRange() {
        var failed = false
        try {
            Exercise(
                module = LearningModule.SHAPES,
                prompt = "?",
                spoken = "?",
                options = listOf("a", "b"),
                correctIndex = 0,
                play = PlayBoard(targetIndices = listOf(5)),
            )
        } catch (_: IllegalArgumentException) {
            failed = true
        }
        assertTrue(failed)
    }
}
