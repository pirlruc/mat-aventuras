package pt.mataventuras.app.ui.home

import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot

/**
 * pt-PT module button titles hosted by a mascot.
 */
internal object ModuleTitles {
    /**
     * Button label for [module] hosted by [mascot].
     */
    fun of(
        module: LearningModule,
        mascot: Mascot,
    ): String = "${noun(module)} com o ${mascot.displayName}"

    internal fun noun(module: LearningModule): String = earlyNoun(module) ?: laterNoun(module)

    private fun earlyNoun(module: LearningModule): String? =
        when (module) {
            LearningModule.COUNTING -> "Contar"
            LearningModule.SHAPES -> "Formas"
            LearningModule.NUMBERS -> "Números"
            LearningModule.ADDITION -> "Somar"
            LearningModule.SUBTRACTION -> "Subtrair"
            else -> null
        }

    internal fun laterNoun(module: LearningModule): String =
        when (module) {
            LearningModule.MULTIPLICATION -> "Multiplicar"
            LearningModule.DIVISION -> "Dividir"
            LearningModule.LOGIC -> "Lógica"
            LearningModule.MEMORY -> "Memória"
            LearningModule.LETTERS -> "Letras"
            LearningModule.WORDS -> "Palavras"
            else -> addedNoun(module)
        }

    private fun addedNoun(module: LearningModule): String =
        when (module) {
            LearningModule.ODD_ONE -> "Qual é diferente"
            LearningModule.SIZE -> "Grande e pequeno"
            LearningModule.PATTERNS -> "Padrões"
            LearningModule.CLOCK -> "Que horas são"
            LearningModule.MONEY -> "Euros e cêntimos"
            else -> youngNoun(module)
        }

    private fun youngNoun(module: LearningModule): String =
        when (module) {
            LearningModule.SORT -> "Cores e formas"
            LearningModule.DOTS -> "Ligar os pontos"
            LearningModule.CLAPS -> "Palmas"
            LearningModule.SHADOW -> "Sombras"
            else -> olderNoun(module)
        }

    private fun olderNoun(module: LearningModule): String =
        when (module) {
            LearningModule.FRACTION -> "Frações"
            LearningModule.ORDER -> "Até 100"
            LearningModule.WORD_PROBLEM -> "Problemas"
            LearningModule.TANGRAM -> "Tangram"
            LearningModule.SPELL -> "Sílabas"
            else -> "Jogar"
        }
}
