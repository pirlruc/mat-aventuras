package pt.mataventuras.domain.model

/**
 * Curriculum module. A module can belong to more than one age band.
 */
enum class LearningModule(
    val bands: Set<AgeBand>,
) {
    COUNTING(setOf(AgeBand.THREE_TO_FIVE)),
    SHAPES(setOf(AgeBand.THREE_TO_FIVE)),
    NUMBERS(setOf(AgeBand.THREE_TO_FIVE, AgeBand.SIX_TO_EIGHT)),
    ADDITION(setOf(AgeBand.THREE_TO_FIVE, AgeBand.SIX_TO_EIGHT)),
    SUBTRACTION(setOf(AgeBand.SIX_TO_EIGHT)),
    MULTIPLICATION(setOf(AgeBand.SIX_TO_EIGHT)),
    DIVISION(setOf(AgeBand.SIX_TO_EIGHT)),
    LOGIC(setOf(AgeBand.THREE_TO_FIVE, AgeBand.SIX_TO_EIGHT)),
    MEMORY(setOf(AgeBand.THREE_TO_FIVE, AgeBand.SIX_TO_EIGHT)),
    LETTERS(setOf(AgeBand.THREE_TO_FIVE, AgeBand.SIX_TO_EIGHT)),
    WORDS(setOf(AgeBand.SIX_TO_EIGHT)),
}

/**
 * Modules available for an age band, in curriculum order.
 */
fun modulesFor(age: AgeBand): List<LearningModule> = LearningModule.entries.filter { age in it.bands }

/**
 * True for the harder arithmetic and logic boards.
 * Shared modules such as [NUMBERS] stay on the younger board until the
 * generator takes an [AgeBand].
 */
fun LearningModule.isSevenYears(): Boolean =
    when (this) {
        LearningModule.ADDITION,
        LearningModule.SUBTRACTION,
        LearningModule.MULTIPLICATION,
        LearningModule.DIVISION,
        LearningModule.LOGIC,
        LearningModule.WORDS,
        -> true
        LearningModule.COUNTING,
        LearningModule.SHAPES,
        LearningModule.NUMBERS,
        LearningModule.MEMORY,
        LearningModule.LETTERS,
        -> false
    }
