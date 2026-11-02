package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * One wave. The child taps the sum that stops it.
 */
data class TowerRound(
    val prompt: String,
    val labels: List<String>,
    val target: String,
)

/**
 * Ages 6–8. Stop the wave with the right sum.
 */
object TowerDefence {
    /**
     * Four answers. [target] is the sum in the prompt.
     */
    fun round(random: Random): TowerRound {
        val left = 2 + random.nextInt(4)
        val right = 2 + random.nextInt(4)
        val sum = left + right
        val labels = listOf(sum, sum + 1, sum - 1, sum + 2).map { it.toString() }.shuffled(random)
        return TowerRound(
            prompt = "${VoiceScripts.TOWER_SPOKEN} $left + $right",
            labels = labels,
            target = sum.toString(),
        )
    }

    /**
     * True when [index] is the sum.
     */
    fun stopped(
        round: TowerRound,
        index: Int,
    ): Boolean = round.labels.getOrNull(index) == round.target
}
