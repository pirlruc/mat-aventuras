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
     */
    fun make(
        kind: PlayKind,
        module: LearningModule,
    ): Exercise =
        when (kind) {
            PlayKind.SUDOKU -> sudoku(module)
            PlayKind.SOUP -> soup(module)
            PlayKind.PUZZLE -> puzzle(module)
            PlayKind.CIPHER -> cipher(module)
            PlayKind.CHOICE -> error("CHOICE is built by ExerciseGenerator.")
        }

    internal fun sudoku(module: LearningModule): Exercise =
        if (module == LearningModule.SHAPES) shapeSudoku() else numberSudoku(module)

    internal fun soup(module: LearningModule): Exercise =
        when (module) {
            LearningModule.LOGIC -> wordSoup()
            LearningModule.COUNTING -> countSoup()
            LearningModule.NUMBERS -> numberSoup()
            else -> shapeSoup()
        }

    internal fun puzzle(module: LearningModule): Exercise {
        val piece =
            if (module == LearningModule.SHAPES) {
                GeometricShape.entries[random.nextInt(GeometricShape.entries.size)].displayName
            } else {
                random.nextInt(1, 10).toString()
            }
        val others = numericOrShapes(piece, module)
        val options = (others + piece).shuffled(random)
        return Exercise(
            module = module,
            prompt = "Qual é a peça que falta?",
            spoken = "Toca na peça que encaixa no sítio a brilhar.",
            options = options,
            correctIndex = options.indexOf(piece),
            targetShape = GeometricShape.entries.firstOrNull { it.displayName == piece },
            visualCount = piece.toIntOrNull() ?: 0,
            play = PlayBoard(kind = PlayKind.PUZZLE, cells = listOf("1", "2", "3", "?"), columns = 2),
        )
    }

    internal fun cipher(module: LearningModule): Exercise =
        when (module) {
            LearningModule.COUNTING, LearningModule.NUMBERS -> countCipher(module)
            LearningModule.LOGIC -> wordCipher()
            else -> mathCipher(module)
        }

    private fun shapeSudoku(): Exercise {
        val first = GeometricShape.CIRCLE.displayName
        val second = GeometricShape.SQUARE.displayName
        val full = listOf(first, second, second, first)
        val hole = random.nextInt(4)
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
            play = PlayBoard(kind = PlayKind.SUDOKU, cells = cells, columns = 2),
        )
    }

    private fun numberSudoku(module: LearningModule): Exercise {
        val mini = module == LearningModule.NUMBERS
        val full =
            if (mini) {
                listOf(1, 2, 2, 1)
            } else {
                List(16) { i -> (i / 4 + i % 4) % 4 + 1 }
            }
        val hole = random.nextInt(full.size)
        val answer = full[hole]
        val cells = full.mapIndexed { i, value -> if (i == hole) "" else value.toString() }
        val maxDigit = if (mini) 2 else 4
        val options = numericOptions(answer, 1, maxDigit).map { it.toString() }
        return Exercise(
            module = module,
            prompt = "Que número falta?",
            spoken = "Olha o quadrado. Que número falta na casa vazia?",
            options = options,
            correctIndex = options.indexOf(answer.toString()),
            visualCount = answer,
            play = PlayBoard(kind = PlayKind.SUDOKU, cells = cells, columns = if (mini) 2 else 4),
        )
    }

    private fun shapeSoup(): Exercise {
        val target = GeometricShape.entries[random.nextInt(GeometricShape.entries.size)]
        val others = GeometricShape.entries.filter { it != target }
        val cells = MutableList(9) { others[random.nextInt(others.size)].displayName }
        val index = random.nextInt(9)
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
                    columns = 3,
                    targetIndices = listOf(index),
                ),
        )
    }

    private fun countSoup(): Exercise {
        val target = random.nextInt(1, 10)
        val cells = (1..9).toList().shuffled(random).map { it.toString() }
        val index = cells.indexOf(target.toString())
        return Exercise(
            module = LearningModule.COUNTING,
            prompt = "Toca na caixa com $target estrelas.",
            spoken = "Procura a caixa com $target estrelas.",
            options = cells,
            correctIndex = index,
            visualCount = target,
            play =
                PlayBoard(
                    kind = PlayKind.SOUP,
                    cells = cells,
                    columns = 3,
                    targetIndices = listOf(index),
                ),
        )
    }

    private fun numberSoup(): Exercise {
        val target = random.nextInt(0, 10)
        val others = (0..9).filter { it != target }
        val cells = MutableList(9) { others[random.nextInt(others.size)].toString() }
        val index = random.nextInt(9)
        cells[index] = target.toString()
        return Exercise(
            module = LearningModule.NUMBERS,
            prompt = "Toca no número $target.",
            spoken = "Encontra o número $target na sopa. Toca-lhe.",
            options = cells.toList(),
            correctIndex = index,
            visualCount = target,
            play =
                PlayBoard(
                    kind = PlayKind.SOUP,
                    cells = cells.toList(),
                    columns = 3,
                    targetIndices = listOf(index),
                ),
        )
    }

    private fun wordSoup(): Exercise {
        val word = WORDS[random.nextInt(WORDS.size)]
        val pool = LETTERS.filter { it !in word }.ifEmpty { LETTERS }
        val letters = MutableList(16) { pool[random.nextInt(pool.length)].toString() }
        val row = random.nextInt(4)
        val start = row * 4
        val path =
            word.mapIndexed { i, char ->
                val index = start + i
                letters[index] = char.toString()
                index
            }
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = "Encontra a palavra $word na sopa.",
            spoken = "Procura a palavra $word. Toca numa letra dela.",
            options = letters.toList(),
            correctIndex = path.first(),
            play =
                PlayBoard(
                    kind = PlayKind.SOUP,
                    cells = letters.toList(),
                    columns = 4,
                    targetIndices = path,
                ),
        )
    }

    private fun wordCipher(): Exercise {
        val word = WORDS[random.nextInt(WORDS.size)]
        val symbols = listOf("▲", "●", "■", "◆")
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

    private fun countCipher(module: LearningModule): Exercise {
        val value = random.nextInt(1, 5)
        val options = numericOptions(value, 1, 9).map { it.toString() }
        return Exercise(
            module = module,
            prompt = "O código diz que número?",
            spoken = "Cada estrela vale um. Quantas são?",
            options = options,
            correctIndex = options.indexOf(value.toString()),
            visualCount = value,
            play =
                PlayBoard(
                    kind = PlayKind.CIPHER,
                    cells = listOf("⭐=1"),
                    columns = 1,
                    cipherCode = List(value) { "⭐" }.joinToString(""),
                ),
        )
    }

    private fun mathCipher(module: LearningModule): Exercise {
        val left = random.nextInt(2, 6)
        val right = random.nextInt(1, 5)
        val answer =
            when (module) {
                LearningModule.SUBTRACTION -> left
                LearningModule.MULTIPLICATION -> left * right
                else -> left + right
            }
        val promptLeft = if (module == LearningModule.SUBTRACTION) left + right else left
        val symbol =
            when (module) {
                LearningModule.SUBTRACTION -> "−"
                LearningModule.MULTIPLICATION -> "×"
                else -> "+"
            }
        val options = numericOptions(answer, 1, 40).map { it.toString() }
        return Exercise(
            module = module,
            prompt = "▲ $symbol ● = ?",
            spoken = "Triângulo vale $promptLeft. Círculo vale $right. Quanto é?",
            options = options,
            correctIndex = options.indexOf(answer.toString()),
            play =
                PlayBoard(
                    kind = PlayKind.CIPHER,
                    cells = listOf("▲=$promptLeft", "●=$right"),
                    columns = 2,
                    cipherCode = "▲ $symbol ●",
                ),
        )
    }

    private fun numericOrShapes(
        piece: String,
        module: LearningModule,
    ): List<String> {
        val asNumber = piece.toIntOrNull()
        return if (asNumber != null && module != LearningModule.SHAPES) {
            numericOptions(asNumber, 1, 10).map { it.toString() }.filter { it != piece }.take(3)
        } else {
            GeometricShape.entries.map { it.displayName }.filter { it != piece }.take(3)
        }
    }

    private companion object {
        val WORDS: List<String> = listOf("dois", "três", "seis", "sete", "oito", "nove")
        const val LETTERS: String = "abcdefghijklmnopqrstuvwxyz"
    }
}
