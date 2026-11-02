package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Letters to break. The child taps the one the prompt names.
 */
data class BreakoutRound(
    val prompt: String,
    val labels: List<String>,
    val target: String,
)

/**
 * Ages 6–8. Break the named letter. The others stay.
 */
object LetterBreakout {
    private val LETTERS: List<String> = listOf("a", "e", "i", "o")

    /**
     * Four letters. [target] is the one to break.
     */
    fun round(random: Random): BreakoutRound {
        val target = LETTERS[random.nextInt(LETTERS.size)]
        return BreakoutRound(
            prompt = "${VoiceScripts.BREAKOUT_SPOKEN} $target",
            labels = LETTERS.shuffled(random),
            target = target,
        )
    }

    /**
     * True when [index] is the named letter.
     */
    fun broken(
        round: BreakoutRound,
        index: Int,
    ): Boolean = round.labels.getOrNull(index) == round.target
}
