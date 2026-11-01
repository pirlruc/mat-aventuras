package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. A repeating pattern with one missing step.
 */
class PatternBoard(
    private val random: Random,
) {
    /**
     * The prompt shows the pattern. The options complete it.
     */
    fun make(): Exercise {
        val pattern = patterns[random.nextInt(patterns.size)]
        val options = (listOf(pattern.next) + pattern.decoys).shuffled(random)
        return Exercise(
            module = LearningModule.PATTERNS,
            prompt = "${VoiceScripts.PATTERN_PROMPT} ${pattern.shown}",
            spoken = VoiceScripts.PATTERN_SPOKEN,
            options = options,
            correctIndex = options.indexOf(pattern.next),
        )
    }

    private data class Pattern(
        val shown: String,
        val next: String,
        val decoys: List<String>,
    )

    private val patterns =
        listOf(
            Pattern("▲ ● ▲ ●", "▲", listOf("●", "■", "★")),
            Pattern("1 2 1 2", "1", listOf("2", "3", "4")),
            Pattern("🔴 🔵 🔴 🔵", "🔴", listOf("🔵", "▲", "1")),
        )
}
