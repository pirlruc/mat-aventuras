package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * One cast. The child taps the fish the prompt names.
 */
data class FishRound(
    val prompt: String,
    val labels: List<String>,
    val target: String,
)

/**
 * Ages 3–5. Four fish. One matches the spoken number.
 */
object FishCatch {
    /**
     * Shuffled fish labelled 1 to 4.
     */
    fun round(random: Random): FishRound {
        val target = (1 + random.nextInt(4)).toString()
        val labels = (1..4).map { it.toString() }.shuffled(random)
        return FishRound(VoiceScripts.catchFish(target), labels, target)
    }

    /**
     * True when [index] is the named fish.
     */
    fun caught(
        round: FishRound,
        index: Int,
    ): Boolean = round.labels.getOrNull(index) == round.target
}
