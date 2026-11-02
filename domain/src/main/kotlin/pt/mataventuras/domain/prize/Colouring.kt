package pt.mataventuras.domain.prize

import kotlin.random.Random

/**
 * One colouring. The child taps the colour the prompt names.
 */
data class ColourRound(
    val prompt: String,
    val labels: List<String>,
    val target: String,
)

/**
 * Ages 3–5. Paint the picture with one of four colours.
 */
object Colouring {
    private val COLOURS: List<String> = listOf("vermelho", "azul", "amarelo", "verde")

    /**
     * Four colours. [target] is the one the prompt asks for.
     */
    fun round(random: Random): ColourRound {
        val target = COLOURS[random.nextInt(COLOURS.size)]
        return ColourRound(
            prompt = "Pinta de $target.",
            labels = COLOURS.shuffled(random),
            target = target,
        )
    }

    /**
     * True when [index] is the asked-for colour.
     */
    fun painted(
        round: ColourRound,
        index: Int,
    ): Boolean = round.labels.getOrNull(index) == round.target
}
