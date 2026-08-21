package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.GeometricShape
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
        assertTrue(PlayKind.SUDOKU in three)
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
        assertTrue(words.play.wordPaths.size >= 2)
        assertTrue(words.play.targetIndices.isNotEmpty())
        assertTrue(words.spoken.contains("Desliza"))
        words.play.soupHits().forEach { assertTrue(words.isCorrect(it)) }
        assertTrue(SudokuGrids.isConsistent(SudokuGrids.filled(4, Random(1)), 4))
        assertTrue(SudokuGrids.isConsistent(SudokuGrids.filled(6, Random(2)), 6))
        assertTrue(SudokuGrids.isLatin(SudokuGrids.filled(3, Random(3)), 3))

        val puzzle = factory.puzzle(LearningModule.SHAPES)
        assertEquals(PlayKind.PUZZLE, puzzle.play.kind)
        assertEquals("Qual é a peça que falta?", puzzle.prompt)
        assertTrue(puzzle.play.cells.contains("?"))
        assertTrue(puzzle.options[puzzle.correctIndex] in GeometricShape.entries.map { it.displayName })

        val numberPuzzle = factory.puzzle(LearningModule.ADDITION)
        val hole = numberPuzzle.play.cells.indexOf("?")
        val answer = numberPuzzle.options[numberPuzzle.correctIndex]
        val filled = numberPuzzle.play.cells.mapIndexed { i, cell -> if (i == hole) answer else cell }.map { it.toInt() }
        val step = filled[1] - filled[0]
        assertTrue(filled.indices.all { filled[it] == filled[0] + it * step })

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

    @Test
    fun harderLevelsGrowBoardsAndCipherAlphabets() {
        val shapeSudoku = factory.sudoku(LearningModule.SHAPES, 2)
        assertEquals(3, shapeSudoku.play.columns)
        assertEquals(9, shapeSudoku.play.cells.size)
        val mini = factory.sudoku(LearningModule.NUMBERS, 3)
        assertEquals(3, mini.play.columns)
        val logic = factory.sudoku(LearningModule.LOGIC, 2)
        assertEquals(6, logic.play.columns)
        assertEquals(36, logic.play.cells.size)
        val shapeSoup = factory.soup(LearningModule.SHAPES, 2)
        assertEquals(5, shapeSoup.play.columns)
        assertEquals(25, shapeSoup.options.size)
        val countSoup = factory.soup(LearningModule.COUNTING, 2)
        assertEquals(5, countSoup.play.columns)
        assertEquals(25, countSoup.options.size)
        val numberSoup = factory.soup(LearningModule.NUMBERS, 1)
        assertEquals(4, numberSoup.play.columns)
        val words = factory.soup(LearningModule.LOGIC, 2)
        assertEquals(6, words.play.columns)
        assertEquals(36, words.options.size)
        (0..24).forEach { seed ->
            PlayBoardFactory(Random(seed), generator::numericOptions).soup(LearningModule.LOGIC, 2)
        }
        val puzzle = factory.puzzle(LearningModule.COUNTING, 2)
        assertEquals(3, puzzle.play.columns)
        assertEquals(9, puzzle.play.cells.size)
        val easyPuzzle = factory.puzzle(LearningModule.SHAPES, 0)
        assertEquals(2, easyPuzzle.play.columns)
        val countCode = factory.cipher(LearningModule.COUNTING, 0)
        assertEquals(1, countCode.play.cells.size)
        val mixed = factory.cipher(LearningModule.NUMBERS, 2)
        assertEquals(3, mixed.play.cells.size)
        assertTrue(mixed.play.cipherCode.contains("■"))
        val mid = factory.cipher(LearningModule.COUNTING, 1)
        assertEquals(2, mid.play.cells.size)
        val wordCode = factory.cipher(LearningModule.LOGIC, 2)
        assertEquals(6, wordCode.options[wordCode.correctIndex].length)
        assertEquals(6, wordCode.play.cells.size)
        val five = factory.cipher(LearningModule.LOGIC, 1)
        assertEquals(5, five.play.cells.size)
        val add = factory.cipher(LearningModule.ADDITION, 2)
        assertEquals(3, add.play.cells.size)
        val mul = factory.cipher(LearningModule.MULTIPLICATION, 2)
        assertEquals(2, mul.play.cells.size)
        val sub = factory.cipher(LearningModule.SUBTRACTION, 2)
        assertTrue(sub.play.cells.size >= 2)
        assertTrue(add.spoken.contains("Triângulo"))
        assertTrue(add.spoken.contains("Círculo"))
        assertTrue(add.spoken.contains("Quadrado"))
        assertTrue(mixed.spoken.contains("Quadrado"))
        assertEquals(5, factory.make(PlayKind.SOUP, LearningModule.SHAPES, 2).play.columns)
        val wide =
            (0..80).map { ExerciseGenerator(Random(it)).generate(LearningModule.LOGIC, 3) }
                .filter { it.play.kind == PlayKind.SUDOKU }
        assertTrue(wide.any { it.play.columns == 6 })
        assertTrue(PlayKind.SUDOKU in PlayKinds.forModule(LearningModule.ADDITION))
        assertEquals(PlayKind.SUDOKU, factory.make(PlayKind.SUDOKU, LearningModule.COUNTING).play.kind)
        assertFalse(SudokuGrids.isConsistent(List(16) { i -> (i / 4 + i % 4) % 4 + 1 }, 4))
        assertFalse(SudokuGrids.isConsistent(List(36) { i -> (i / 6 + i % 6) % 6 + 1 }, 6))
        val soup = WordSoupBuilder(Random(3)).build(6, 3)
        assertTrue(soup.words.size >= 2)
        assertTrue(soup.paths.size >= 2)
        assertEquals("Cada estrela vale um. Quantas são?", CipherSpeech.counting(1))
        assertTrue(CipherSpeech.counting(3).contains("Quadrado"))
        assertTrue(CipherSpeech.fromLegend(listOf("▲=4", "●=2", "■=1"), "Quanto é?").contains("Quadrado vale 1"))
        val seq = PuzzlePatterns.cells(LearningModule.COUNTING, 2, Random(1))
        assertEquals(4, seq.size)
    }
}
