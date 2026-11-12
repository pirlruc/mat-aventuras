package pt.mataventuras.domain.parent

import pt.mataventuras.domain.model.LearningModule

/**
 * Band shown on the parent heatmap. [label] is pt-PT UI copy.
 */
enum class MasteryBand(
    val label: String,
) {
    /** Accuracy under 50%. */
    BELOW_50("Abaixo de 50"),

    /** Accuracy from 50% up to, but not including, 80%. */
    FROM_50_TO_79("50 a 79"),

    /** Accuracy of 80% or more. */
    FROM_80("80 ou mais"),
}

/**
 * One module cell. [topic] is the pt-PT Aprendizagens Essenciais 1.º ciclo label.
 */
data class MasteryCell(
    val module: LearningModule,
    val topic: String,
    val band: MasteryBand,
    val hits: Int,
    val misses: Int,
)

/**
 * pt-PT Aprendizagens Essenciais 1.º ciclo topic already taught by [module].
 */
fun essentialTopic(module: LearningModule): String =
    numbersTopic(module) ?: geometryTopic(module) ?: languageTopic(module) ?: "Resolução de problemas"

/**
 * Band for an accuracy in 0..1. Callers with no attempts omit the cell instead.
 */
fun masteryBand(accuracy: Double): MasteryBand =
    when {
        accuracy >= HIGH_FROM -> MasteryBand.FROM_80
        accuracy >= MID_FROM -> MasteryBand.FROM_50_TO_79
        else -> MasteryBand.BELOW_50
    }

/**
 * Module rows for [summary]. An empty history returns an empty list.
 */
fun masteryGrid(summary: ParentSummary): List<MasteryCell> =
    summary.byModule.map { row ->
        MasteryCell(
            module = row.module,
            topic = essentialTopic(row.module),
            band = masteryBand(row.accuracy),
            hits = row.hits,
            misses = row.misses,
        )
    }

private fun numbersTopic(module: LearningModule): String? =
    when (module) {
        LearningModule.COUNTING, LearningModule.NUMBERS, LearningModule.ORDER, LearningModule.CLAPS ->
            "Números naturais"
        LearningModule.ADDITION -> "Adição"
        LearningModule.SUBTRACTION -> "Subtração"
        LearningModule.MULTIPLICATION -> "Multiplicação"
        LearningModule.DIVISION -> "Divisão"
        LearningModule.FRACTION -> "Números racionais"
        else -> null
    }

private fun geometryTopic(module: LearningModule): String? =
    when (module) {
        LearningModule.SHAPES, LearningModule.SORT, LearningModule.SHADOW, LearningModule.TANGRAM ->
            "Figuras geométricas"
        LearningModule.SIZE -> "Comprimento"
        LearningModule.CLOCK -> "Tempo"
        LearningModule.MONEY -> "Dinheiro"
        LearningModule.DOTS -> "Geometria e medida"
        LearningModule.PATTERNS -> "Regularidades"
        else -> null
    }

private fun languageTopic(module: LearningModule): String? =
    when (module) {
        LearningModule.LETTERS, LearningModule.SPELL -> "Consciência fonológica"
        LearningModule.WORDS -> "Leitura e escrita"
        LearningModule.LOGIC, LearningModule.WORD_PROBLEM, LearningModule.ODD_ONE, LearningModule.MEMORY ->
            "Resolução de problemas"
        else -> null
    }

private const val MID_FROM: Double = 0.5
private const val HIGH_FROM: Double = 0.8
