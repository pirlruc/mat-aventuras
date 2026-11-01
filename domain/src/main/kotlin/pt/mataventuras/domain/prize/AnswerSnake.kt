package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * One bite for the answer snake.
 */
data class SnakeRound(
    val prompt: String,
    val options: List<String>,
    val correct: Int,
    val length: Int,
)

/**
 * Ages 6–8. The snake grows when the child taps the right sum.
 */
object AnswerSnake {
    private const val FULL: Int = 4

    /**
     * A sum and four answers. [length] is how many correct bites the snake has.
     */
    fun round(
        random: Random,
        length: Int = 1,
    ): SnakeRound {
        val left = 1 + random.nextInt(8)
        val right = 1 + random.nextInt(8)
        val sum = left + right
        val options = listOf(sum, sum + 1, sum - 1, sum + 2).map { it.toString() }.shuffled(random)
        return SnakeRound(
            prompt = "${VoiceScripts.SNAKE_PROMPT} $left + $right",
            options = options,
            correct = options.indexOf(sum.toString()),
            length = length,
        )
    }

    /**
     * A correct bite grows the snake. A miss keeps the same round.
     */
    fun bite(
        round: SnakeRound,
        index: Int,
        random: Random,
    ): SnakeRound = if (index == round.correct) round(random, round.length + 1) else round

    /**
     * The snake is full after three correct bites.
     */
    fun full(round: SnakeRound): Boolean = round.length >= FULL
}
