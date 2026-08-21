package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule

/**
 * Builds interactive boards. Prompt text is pt-PT UI copy.
 */
class PlayBoardFactory(
    private val random: Random,
    private val numericOptions: (Int, Int, Int) -> List<Int>,
) {
    /**
     * Board for [kind] in [module]. [kind] is never [PlayKind.CHOICE].
     * [level] 0..3 grows the grid and the cipher alphabet.
     */
    fun make(
        kind: PlayKind,
        module: LearningModule,
        level: Int = 0,
    ): Exercise =
        when (kind) {
            PlayKind.SUDOKU -> sudoku(module, level)
            PlayKind.SOUP -> soup(module, level)
            PlayKind.PUZZLE -> puzzle(module, level)
            PlayKind.CIPHER -> cipher(module, level)
            PlayKind.CHOICE -> error("CHOICE is built by ExerciseGenerator.")
        }

    internal fun sudoku(
        module: LearningModule,
        level: Int = 0,
    ): Exercise =
        if (module == LearningModule.SHAPES) {
            shapeSudoku(level)
        } else {
            numberSudoku(module, level)
        }

    internal fun soup(
        module: LearningModule,
        level: Int = 0,
    ): Exercise =
        when (module) {
            LearningModule.LOGIC -> wordSoup(level)
            LearningModule.COUNTING, LearningModule.NUMBERS -> huntSoup(module, level)
            else -> shapeSoup(level)
        }

    internal fun puzzle(
        module: LearningModule,
        level: Int = 0,
    ): Exercise {
        val n = if (level >= 2) 3 else 2
        val hole = random.nextInt(n * n)
        val full = PuzzlePatterns.cells(module, n, random)
        val piece = full[hole]
        val cells = full.mapIndexed { i, value -> if (i == hole) "?" else value }
        val others = numericOrShapes(piece)
        val options = (others + piece).shuffled(random)
        return Exercise(
            module = module,
            prompt = "Qual é a peça que falta?",
            spoken = "Toca na peça que encaixa no sítio a brilhar.",
            options = options,
            correctIndex = options.indexOf(piece),
            targetShape = GeometricShape.entries.firstOrNull { it.displayName == piece },
            visualCount = piece.toIntOrNull() ?: 0,
            play = PlayBoard(kind = PlayKind.PUZZLE, cells = cells, columns = n),
        )
    }

    internal fun cipher(
        module: LearningModule,
        level: Int = 0,
    ): Exercise =
        when (module) {
            LearningModule.COUNTING, LearningModule.NUMBERS -> countCipher(module, level)
            LearningModule.LOGIC -> wordCipher(level)
            else -> mathCipher(module, level)
        }

    private fun shapeSudoku(level: Int): Exercise {
        val n = 2 + minOf(level, 1)
        val glyphs = GeometricShape.entries.take(n).map { it.displayName }
        val full = SudokuGrids.filled(n, random).map { glyphs[it - 1] }
        val hole = random.nextInt(full.size)
        val answer = full[hole]
        val cells = full.mapIndexed { i, value -> if (i == hole) "" else value }
        val others = GeometricShape.entries.map { it.displayName }.filter { it != answer }.take(3)
        val options = (others + answer).shuffled(random)
        return Exercise(
            module = LearningModule.SHAPES,
            prompt = "Que forma falta?",
            spoken = "Olha o quadrado mágico. Que forma falta?",
            options = options,
            correctIndex = options.indexOf(answer),
            targetShape = GeometricShape.entries.first { it.displayName == answer },
            play = PlayBoard(kind = PlayKind.SUDOKU, cells = cells, columns = n),
        )
    }

    private fun numberSudoku(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val n =
            if (module == LearningModule.NUMBERS || module == LearningModule.COUNTING) {
                2 + minOf(level, 1)
            } else {
                4 + minOf(level, 2)
            }
        val full = SudokuGrids.filled(n, random)
        val hole = random.nextInt(full.size)
        val answer = full[hole]
        val cells = full.mapIndexed { i, value -> if (i == hole) "" else value.toString() }
        val options = numericOptions(answer, 1, n).map { it.toString() }
        return Exercise(
            module = module,
            prompt = "Que número falta?",
            spoken = "Olha o quadrado. Que número falta na casa vazia?",
            options = options,
            correctIndex = options.indexOf(answer.toString()),
            visualCount = answer,
            play = PlayBoard(kind = PlayKind.SUDOKU, cells = cells, columns = n),
        )
    }

    private fun shapeSoup(level: Int): Exercise {
        val size = 3 + minOf(level, 2)
        val n = size * size
        val target = GeometricShape.entries[random.nextInt(GeometricShape.entries.size)]
        val others = GeometricShape.entries.filter { it != target }
        val cells = MutableList(n) { others[random.nextInt(others.size)].displayName }
        val index = random.nextInt(n)
        cells[index] = target.displayName
        return Exercise(
            module = LearningModule.SHAPES,
            prompt = "Toca no ${target.displayName}.",
            spoken = "Encontra o ${target.displayName} na sopa. Toca-lhe.",
            options = cells.toList(),
            correctIndex = index,
            targetShape = target,
            play =
                PlayBoard(
                    kind = PlayKind.SOUP,
                    cells = cells.toList(),
                    columns = size,
                    targetIndices = listOf(index),
                ),
        )
    }

    private fun huntSoup(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val size = 3 + minOf(level, 2)
        val n = size * size
        val counting = module == LearningModule.COUNTING
        val target = if (counting) random.nextInt(1, 10) else random.nextInt(0, 10)
        val cells =
            if (counting && n == 9) {
                (1..9).toList().shuffled(random).map { it.toString() }
            } else {
                val lo = if (counting) 1 else 0
                val others = (lo..9).filter { it != target }
                MutableList(n) { others[random.nextInt(others.size)].toString() }.also { list ->
                    list[random.nextInt(n)] = target.toString()
                }
            }
        val index = cells.indexOf(target.toString())
        val prompt = if (counting) "Toca na caixa com $target estrelas." else "Toca no número $target."
        val spoken =
            if (counting) {
                "Procura a caixa com $target estrelas."
            } else {
                "Encontra o número $target na sopa. Toca-lhe."
            }
        return Exercise(
            module = module,
            prompt = prompt,
            spoken = spoken,
            options = cells,
            correctIndex = index,
            visualCount = target,
            play =
                PlayBoard(
                    kind = PlayKind.SOUP,
                    cells = cells,
                    columns = size,
                    targetIndices = listOf(index),
                ),
        )
    }

    private fun wordSoup(level: Int): Exercise {
        val size = 4 + minOf(level, 2)
        val soup = WordSoupBuilder(random).build(size, 2 + minOf(level, 1))
        val listed = soup.words.joinToString(", ")
        val hits = soup.paths.flatten()
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = "Encontra as palavras: $listed.",
            spoken = "Procura as palavras $listed. Desliza o dedo por cada uma.",
            options = soup.cells,
            correctIndex = hits.first(),
            play =
                PlayBoard(
                    kind = PlayKind.SOUP,
                    cells = soup.cells,
                    columns = size,
                    targetIndices = hits,
                    wordPaths = soup.paths,
                ),
        )
    }

    private fun wordCipher(level: Int): Exercise {
        val width = 4 + minOf(level, 2)
        val candidates = WORDS.filter { it.length == width }
        val word = candidates[random.nextInt(candidates.size)]
        val symbols = SYMBOLS.take(word.length)
        val legend = word.mapIndexed { i, ch -> "${symbols[i]}=$ch" }
        val code = word.indices.joinToString("") { symbols[it] }
        val others = WORDS.filter { it != word }.shuffled(random).take(3)
        val options = (others + word).shuffled(random)
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = "Que palavra é o código?",
            spoken = "Lê a legenda. Que palavra formam os símbolos?",
            options = options,
            correctIndex = options.indexOf(word),
            play =
                PlayBoard(
                    kind = PlayKind.CIPHER,
                    cells = legend,
                    columns = 2,
                    cipherCode = code,
                ),
        )
    }

    private fun countCipher(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val kinds = 1 + minOf(level, 2)
        val stars = random.nextInt(1, 4)
        val dots = if (kinds > 1) random.nextInt(0, 3) else 0
        val boxes = if (kinds > 2) random.nextInt(0, 3) else 0
        val value = stars + dots * 2 + boxes * 3
        val legend = ArrayList<String>(kinds)
        legend.add("⭐=1")
        if (kinds > 1) legend.add("●=2")
        if (kinds > 2) legend.add("■=3")
        val code = "⭐".repeat(stars) + "●".repeat(dots) + "■".repeat(boxes)
        val spoken = CipherSpeech.counting(kinds)
        val options = numericOptions(value, 1, 12).map { it.toString() }
        return Exercise(
            module = module,
            prompt = "O código diz que número?",
            spoken = spoken,
            options = options,
            correctIndex = options.indexOf(value.toString()),
            visualCount = value,
            play =
                PlayBoard(
                    kind = PlayKind.CIPHER,
                    cells = legend,
                    columns = legend.size,
                    cipherCode = code,
                ),
        )
    }

    private fun mathCipher(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val left = random.nextInt(2 + level, 6 + level * 2)
        val right = random.nextInt(1, 5 + level)
        val mulBit = if (module == LearningModule.MULTIPLICATION) 1 else 0
        val subBit = if (module == LearningModule.SUBTRACTION) 1 else 0
        val third = (1 - mulBit) * (level / 2).coerceAtMost(1) * random.nextInt(1, 4)
        val answer =
            left * (mulBit * (right - 1) + 1) + (1 - subBit) * (1 - mulBit) * (right + third)
        val promptLeft = left + subBit * (right + third)
        val infix = " ${listOf("+", "−", "×")[subBit + 2 * mulBit]} "
        val cells =
            if (third == 0) {
                listOf("▲=$promptLeft", "●=$right")
            } else {
                listOf("▲=$promptLeft", "●=$right", "■=$third")
            }
        val code = if (third == 0) "▲$infix●" else "▲$infix●$infix■"
        val options = numericOptions(answer, 1, 80).map { it.toString() }
        return Exercise(
            module = module,
            prompt = "$code = ?",
            spoken = CipherSpeech.fromLegend(cells, "Quanto é?"),
            options = options,
            correctIndex = options.indexOf(answer.toString()),
            play =
                PlayBoard(
                    kind = PlayKind.CIPHER,
                    cells = cells,
                    columns = cells.size,
                    cipherCode = code,
                ),
        )
    }

    private fun numericOrShapes(piece: String): List<String> {
        val asNumber = piece.toIntOrNull()
        return if (asNumber != null) {
            numericOptions(asNumber, 1, 10).map { it.toString() }.filter { it != piece }.take(3)
        } else {
            GeometricShape.entries.map { it.displayName }.filter { it != piece }.take(3)
        }
    }

    private companion object {
        val WORDS: List<String> =
            listOf(
                "dois",
                "três",
                "seis",
                "sete",
                "oito",
                "nove",
                "cinco",
                "vinte",
                "treze",
                "quatro",
                "quinze",
                "trinta",
            )
        val SYMBOLS: List<String> = listOf("▲", "●", "■", "◆", "★", "♥")
    }
}
