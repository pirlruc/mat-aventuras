package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Bubbles waiting to be popped in order.
 */
data class BubbleRound(
    val prompt: String,
    val labels: List<String>,
    val step: Int,
)

/**
 * Ages 3–5. Pop 1, then 2, then 3. A miss starts the count again.
 */
object BubbleRhythm {
    private const val DONE: Int = 3

    /**
     * Three bubbles in a shuffled order. [step] is how many were popped in order.
     */
    fun round(
        random: Random,
        step: Int = 0,
    ): BubbleRound {
        val labels = listOf("1", "2", "3").shuffled(random)
        return BubbleRound(VoiceScripts.BUBBLE_SPOKEN, labels, step)
    }

    /**
     * A tap on the next number advances. Any other bubble resets the count.
     */
    fun pop(
        round: BubbleRound,
        index: Int,
    ): BubbleRound {
        val expected = (round.step + 1).toString()
        val next = if (round.labels.getOrNull(index) == expected) round.step + 1 else 0
        return round.copy(step = next)
    }

    /**
     * True after 1, 2, and 3 have been popped in order.
     */
    fun done(round: BubbleRound): Boolean = round.step >= DONE
}
