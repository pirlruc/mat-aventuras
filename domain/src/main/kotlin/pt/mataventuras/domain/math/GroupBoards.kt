package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule

/**
 * Glyphs on a picture-game board. [REMOVED] marks counters that have left.
 */
object GroupTokens {
    const val KEPT: String = "●"

    const val REMOVED: String = "✕"
}

/**
 * Picture games: crossed counters, dot arrays, and equal rows.
 * Prompt text is pt-PT.
 */
class GroupBoards(
    private val random: Random,
    private val numericOptions: (Int, Int, Int) -> List<Int>,
) {
    /**
     * Picture game for [module]. Other modules get the array game.
     */
    fun make(
        module: LearningModule,
        level: Int,
    ): Exercise =
        when (module) {
            LearningModule.SUBTRACTION -> takeAway(module, level)
            LearningModule.DIVISION -> share(module, level)
            else -> array(module, level)
        }

    /**
     * Count the counters that were not crossed out.
     */
    internal fun takeAway(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val total = 6 + 2 * minOf(level.coerceAtLeast(0), 3)
        val taken = random.nextInt(1, total)
        val remain = total - taken
        val cells = (List(remain) { GroupTokens.KEPT } + List(taken) { GroupTokens.REMOVED }).shuffled(random)
        return board(
            module = module,
            prompt = "As cruzes saíram do jogo. Quantos círculos ficam?",
            spoken = "Conta só os círculos. As cruzes já não contam.",
            picture = Picture(cells, 6, remain),
            min = 0,
            max = total,
        )
    }

    /**
     * Array model: rows times columns.
     */
    internal fun array(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val rows = span(level)
        val cols = span(level)
        return board(
            module = module,
            prompt = "Conta as filas e os círculos de cada fila. Quantos há ao todo?",
            spoken = "Multiplica o número de filas pelos círculos de cada fila.",
            picture = Picture(dots(rows, cols), cols, rows * cols),
            min = 2,
            max = 40,
        )
    }

    /**
     * Equal rows. Asks either the share in each row or how many rows there are.
     */
    internal fun share(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val rows = span(level)
        val cols = span(level)
        val askShare = random.nextBoolean()
        val prompt =
            if (askShare) {
                "Cada fila é um amigo. Quantos círculos recebe cada um?"
            } else {
                "Cada fila é um amigo. Quantos amigos há?"
            }
        val spoken =
            if (askShare) {
                "Reparte. Cada fila recebe a mesma quantidade."
            } else {
                "Conta as filas iguais. Cada uma é um amigo."
            }
        return board(
            module = module,
            prompt = prompt,
            spoken = spoken,
            picture = Picture(dots(rows, cols), cols, if (askShare) cols else rows),
            min = 2,
            max = 8,
        )
    }

    private fun span(level: Int): Int = 2 + random.nextInt(2 + minOf(level.coerceAtLeast(0), 2))

    private fun dots(
        rows: Int,
        cols: Int,
    ): List<String> = List(rows * cols) { GroupTokens.KEPT }

    private fun board(
        module: LearningModule,
        prompt: String,
        spoken: String,
        picture: Picture,
        min: Int,
        max: Int,
    ): Exercise =
        numericChoice(
            module = module,
            prompt = prompt,
            spoken = spoken,
            options = numericOptions(picture.answer, min, max),
            correct = picture.answer,
        ).copy(
            play = PlayBoard(kind = PlayKind.GROUPS, cells = picture.cells, columns = picture.columns),
        )
}

private data class Picture(
    val cells: List<String>,
    val columns: Int,
    val answer: Int,
)
