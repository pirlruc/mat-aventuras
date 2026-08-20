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
    ): String =
        when (module) {
            LearningModule.COUNTING -> "Contar com o ${mascot.displayName}"
            LearningModule.SHAPES -> "Formas com o ${mascot.displayName}"
            LearningModule.NUMBERS -> "Números com o ${mascot.displayName}"
            LearningModule.ADDITION -> "Somar com o ${mascot.displayName}"
            LearningModule.SUBTRACTION -> "Subtrair com o ${mascot.displayName}"
            LearningModule.MULTIPLICATION -> "Multiplicar com o ${mascot.displayName}"
            LearningModule.LOGIC -> "Lógica com o ${mascot.displayName}"
        }
}
