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
    ODD_ONE(setOf(AgeBand.THREE_TO_FIVE)),
    SIZE(setOf(AgeBand.THREE_TO_FIVE)),
    PATTERNS(setOf(AgeBand.THREE_TO_FIVE)),
    CLOCK(setOf(AgeBand.SIX_TO_EIGHT)),
    MONEY(setOf(AgeBand.SIX_TO_EIGHT)),
}

/**
 * Modules available for an age band, in curriculum order.
 */
fun modulesFor(age: AgeBand): List<LearningModule> = LearningModule.entries.filter { age in it.bands }
