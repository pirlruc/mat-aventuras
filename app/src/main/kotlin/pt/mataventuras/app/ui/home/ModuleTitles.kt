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

    private fun noun(module: LearningModule): String =
        earlyNoun(module) ?: laterNoun(module)

    private fun earlyNoun(module: LearningModule): String? =
        when (module) {
            LearningModule.COUNTING -> "Contar"
            LearningModule.SHAPES -> "Formas"
            LearningModule.NUMBERS -> "Números"
            LearningModule.ADDITION -> "Somar"
            LearningModule.SUBTRACTION -> "Subtrair"
            else -> null
        }

    private fun laterNoun(module: LearningModule): String =
        when (module) {
            LearningModule.MULTIPLICATION -> "Multiplicar"
            LearningModule.DIVISION -> "Dividir"
            LearningModule.LOGIC -> "Lógica"
            LearningModule.MEMORY -> "Memória"
            LearningModule.LETTERS -> "Letras"
            LearningModule.WORDS -> "Palavras"
            else -> "Jogar"
        }
}
