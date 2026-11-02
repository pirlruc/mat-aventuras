package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. One addition story. The numbers stay small enough to count.
 */
class WordProblemBoard(
    private val random: Random,
) {
    /**
     * Four totals. Exactly one is the sum in the story.
     */
    fun make(): Exercise {
        val had = random.nextInt(1, 6)
        val got = random.nextInt(1, 6)
        val total = had + got
        val decoys = (1..12).filter { it != total }.shuffled(random).take(3)
        val options = (decoys + total).map { it.toString() }.shuffled(random)
        return Exercise(
            module = LearningModule.WORD_PROBLEM,
            prompt = "A Ana tem $had maçãs e recebe $got. Quantas tem agora?",
            spoken = VoiceScripts.WORD_PROBLEM_SPOKEN,
            options = options,
            correctIndex = options.indexOf(total.toString()),
        )
    }
}
