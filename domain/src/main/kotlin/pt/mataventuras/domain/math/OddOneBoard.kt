package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. One picture in the row is not like the others.
 */
class OddOneBoard(
    private val random: Random,
) {
    /**
     * Four pictures. The unique one is the answer.
     */
    fun make(): Exercise {
        val row = rows[random.nextInt(rows.size)]
        val options = row.shuffled(random)
        val odd = row.groupingBy { it }.eachCount().entries.first { it.value == 1 }.key
        return Exercise(
            module = LearningModule.ODD_ONE,
            prompt = VoiceScripts.ODD_ONE_PROMPT,
            spoken = VoiceScripts.ODD_ONE_SPOKEN,
            options = options,
            correctIndex = options.indexOf(odd),
        )
    }

    private val rows =
        listOf(
            listOf("🔴", "🔴", "🔴", "🔵"),
            listOf("▲", "▲", "▲", "●"),
            listOf("1", "1", "1", "2"),
        )
}
