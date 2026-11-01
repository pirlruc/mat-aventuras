package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * One balloon the child can tap. There is no drag.
 */
data class BalloonRound(
    val prompt: String,
    val labels: List<String>,
    val target: String,
)

/**
 * Ages 3–5. Pop the balloon named by the prompt.
 */
object BalloonPop {
    /**
     * Four numbered balloons. One matches the prompt.
     */
    fun round(random: Random): BalloonRound {
        val target = (1 + random.nextInt(4)).toString()
        val labels = (1..4).map { it.toString() }.shuffled(random)
        return BalloonRound(VoiceScripts.popBalloon(target), labels, target)
    }

    /**
     * True when [index] is the balloon named by the prompt.
     */
    fun popped(
        round: BalloonRound,
        index: Int,
    ): Boolean = round.labels.getOrNull(index) == round.target
}
