package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. Add euros and cents.
 */
class MoneyBoard(
    private val random: Random,
) {
    /**
     * One price in euros and cents, and nearby wrong prices.
     */
    fun make(): Exercise {
        val euros = 1 + random.nextInt(5)
        val cents = listOf(10, 20, 50)[random.nextInt(3)]
        val correct = price(euros, cents)
        val decoys =
            listOf(
                price(euros, (cents + 30).coerceAtMost(90)),
                price(euros + 1, cents),
                price(euros, 0),
            ).filter { it != correct }.distinct()
        val options = (decoys + correct).shuffled(random)
        return Exercise(
            module = LearningModule.MONEY,
            prompt = VoiceScripts.moneyPrompt(euros, cents),
            spoken = VoiceScripts.MONEY_SPOKEN,
            options = options,
            correctIndex = options.indexOf(correct),
        )
    }

    /**
     * pt-PT price with a comma.
     */
    fun price(
        euros: Int,
        cents: Int,
    ): String = "$euros,${cents.toString().padStart(2, '0')} €"
}
