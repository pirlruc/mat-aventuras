package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. The child picks the first syllable of a short word.
 */
class SpellBoard(
    private val random: Random,
) {
    /**
     * Four syllables. Exactly one opens the word in the prompt.
     */
    fun make(): Exercise {
        val word = WORDS[random.nextInt(WORDS.size)]
        val options = WORDS.map { it.syllable }.shuffled(random)
        return Exercise(
            module = LearningModule.SPELL,
            prompt = "Qual é a primeira sílaba de ${word.text}?",
            spoken = VoiceScripts.SPELL_SPOKEN,
            options = options,
            correctIndex = options.indexOf(word.syllable),
        )
    }

    private data class SyllableWord(
        val text: String,
        val syllable: String,
    )

    companion object {
        private val WORDS: List<SyllableWord> =
            listOf(
                SyllableWord("bola", "bo"),
                SyllableWord("gato", "ga"),
                SyllableWord("sapo", "sa"),
                SyllableWord("pato", "pa"),
            )
    }
}
