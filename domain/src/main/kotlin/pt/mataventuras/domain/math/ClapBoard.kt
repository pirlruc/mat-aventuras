package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. The child counts the syllables of a short Portuguese word.
 */
class ClapBoard(
    private val random: Random,
) {
    /**
     * Four clap counts. The answer is the syllable count of the word in the prompt.
     */
    fun make(): Exercise {
        val word = WORDS[random.nextInt(WORDS.size)]
        val options = listOf("1", "2", "3", "4").shuffled(random)
        return Exercise(
            module = LearningModule.CLAPS,
            prompt = "Quantas palmas para ${word.first}?",
            spoken = VoiceScripts.CLAPS_SPOKEN,
            options = options,
            correctIndex = options.indexOf(word.second.toString()),
        )
    }

    companion object {
        /** Word to syllable count. One syllable is one clap. */
        val WORDS: List<Pair<String, Int>> =
            listOf(
                "sol" to 1,
                "mão" to 1,
                "gato" to 2,
                "bola" to 2,
                "banana" to 3,
                "sapato" to 3,
                "borboleta" to 4,
                "computador" to 4,
            )
    }
}
