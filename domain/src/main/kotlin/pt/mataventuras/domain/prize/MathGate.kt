package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Two gates. One shows the sum.
 */
data class GateRound(
    val prompt: String,
    val labels: List<String>,
    val target: String,
)

/**
 * Ages 6–8. Run through the gate that shows the answer.
 */
object MathGate {
    /**
     * The right gate is the sum. The other gate is one more.
     */
    fun round(random: Random): GateRound {
        val left = 1 + random.nextInt(8)
        val right = 1 + random.nextInt(8)
        val sum = left + right
        val labels = listOf(sum.toString(), (sum + 1).toString()).shuffled(random)
        return GateRound(
            prompt = "${VoiceScripts.RUNNER_SPOKEN} $left + $right",
            labels = labels,
            target = sum.toString(),
        )
    }

    /**
     * True when [index] is the gate with the sum.
     */
    fun passed(
        round: GateRound,
        index: Int,
    ): Boolean = round.labels.getOrNull(index) == round.target
}
