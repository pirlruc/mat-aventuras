package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Fill the missing letter of a pt-PT word.
 */
class WordComplete(
    private val random: Random,
) {
    /**
     * A picture word with the first letter missing, or a longer word with one hole.
     */
    fun make(
        module: LearningModule,
        level: Int,
    ): Exercise {
        val entry = WORDS[random.nextInt(WORDS.size)]
        val hole = if (module == LearningModule.WORDS || level >= 2) entry.hole else 0
        val answer = entry.word[hole].toString()
        val shown = entry.word.replaceRange(hole, hole + 1, "_")
        val distractors = ('a'..'z').map { it.toString() }.filter { it != answer.lowercase() }.shuffled(random).take(3)
        val options = (distractors + answer.lowercase()).shuffled(random)
        return Exercise(
            module = module,
            prompt = "${entry.picture} $shown",
            spoken = VoiceScripts.missingLetter(entry.word),
            options = options,
            correctIndex = options.indexOf(answer.lowercase()),
            play = PlayBoard(kind = PlayKind.WORD_COMPLETE, cells = listOf(shown)),
        )
    }

    private data class Word(
        val word: String,
        val picture: String,
        val hole: Int,
    )

    private companion object {
        val WORDS =
            listOf(
                Word("gato", "🐱", 2),
                Word("casa", "🏠", 1),
                Word("bola", "⚽", 2),
                Word("mão", "✋", 1),
                Word("pão", "🍞", 1),
                Word("sol", "☀️", 0),
                Word("mãe", "👩", 1),
                Word("água", "💧", 1),
            )
    }
}
