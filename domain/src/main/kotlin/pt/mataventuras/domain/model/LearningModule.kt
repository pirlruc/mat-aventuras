package pt.mataventuras.domain.model

/**
 * Curriculum module. Age 3: counting, shapes, numbers. Age 7: arithmetic and logic.
 */
enum class LearningModule {
    COUNTING,
    SHAPES,
    NUMBERS,
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    LOGIC,
}

/**
 * Modules available for an age band.
 */
fun modulesFor(age: AgeGroup): List<LearningModule> =
    when (age) {
        AgeGroup.THREE_YEARS ->
            listOf(
                LearningModule.COUNTING,
                LearningModule.SHAPES,
                LearningModule.NUMBERS,
            )
        AgeGroup.SEVEN_YEARS ->
            listOf(
                LearningModule.ADDITION,
                LearningModule.SUBTRACTION,
                LearningModule.MULTIPLICATION,
                LearningModule.LOGIC,
            )
    }
