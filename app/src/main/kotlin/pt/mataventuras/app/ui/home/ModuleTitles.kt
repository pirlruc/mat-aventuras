package pt.mataventuras.app.ui.home

import pt.mataventuras.app.R
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot

/**
 * Module titles. [noun] is the pt-PT key. Screens read [nounId] from the language pack.
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

    /** String resource for [module]. Unknown modules use the play label. */
    internal fun nounId(module: LearningModule): Int = NOUN_IDS[module] ?: R.string.module_play

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

    private val NOUN_IDS: Map<LearningModule, Int> =
        mapOf(
            LearningModule.COUNTING to R.string.module_counting,
            LearningModule.SHAPES to R.string.module_shapes,
            LearningModule.NUMBERS to R.string.module_numbers,
            LearningModule.ADDITION to R.string.module_addition,
            LearningModule.SUBTRACTION to R.string.module_subtraction,
            LearningModule.MULTIPLICATION to R.string.module_multiplication,
            LearningModule.DIVISION to R.string.module_division,
            LearningModule.LOGIC to R.string.module_logic,
            LearningModule.MEMORY to R.string.module_memory,
            LearningModule.LETTERS to R.string.module_letters,
            LearningModule.WORDS to R.string.module_words,
            LearningModule.ODD_ONE to R.string.module_odd_one,
            LearningModule.SIZE to R.string.module_size,
            LearningModule.PATTERNS to R.string.module_patterns,
            LearningModule.CLOCK to R.string.module_clock,
            LearningModule.MONEY to R.string.module_money,
            LearningModule.SORT to R.string.module_sort,
            LearningModule.DOTS to R.string.module_dots,
            LearningModule.CLAPS to R.string.module_claps,
            LearningModule.SHADOW to R.string.module_shadow,
            LearningModule.FRACTION to R.string.module_fraction,
            LearningModule.ORDER to R.string.module_order,
            LearningModule.WORD_PROBLEM to R.string.module_word_problem,
            LearningModule.TANGRAM to R.string.module_tangram,
            LearningModule.SPELL to R.string.module_spell,
        )
}
