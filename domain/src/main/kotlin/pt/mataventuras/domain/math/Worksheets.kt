package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.modulesFor
import pt.mataventuras.domain.parent.essentialTopic

/**
 * One printed exercise. [topic] and [prompt] are pt-PT copy.
 */
data class WorksheetExercise(
    val topic: String,
    val prompt: String,
    val options: List<String>,
)

/**
 * Worksheet for one age band. The same [seed] repeats [exercises].
 */
data class Worksheet(
    val age: AgeBand,
    val seed: Long,
    val exercises: List<WorksheetExercise>,
)

/**
 * Builds a worksheet from the lesson generators. [pages] splits the print lines.
 */
object Worksheets {
    /** Title drawn above the exercises. pt-PT copy. */
    const val TITLE: String = "Ficha de exercícios"

    /**
     * One exercise per module in [age], drawn with [seed].
     */
    fun build(
        age: AgeBand,
        seed: Long,
    ): Worksheet {
        val generator = ExerciseGenerator(Random(seed))
        val exercises =
            modulesFor(age).map { module ->
                val exercise = generator.generate(module, level = 0, age = age)
                WorksheetExercise(
                    topic = essentialTopic(module),
                    prompt = exercise.prompt,
                    options = exercise.options,
                )
            }
        return Worksheet(age, seed, exercises)
    }

    /**
     * Print lines for [worksheet], chunked so each inner list fits on one page.
     */
    fun pages(
        worksheet: Worksheet,
        linesPerPage: Int = DEFAULT_LINES,
    ): List<List<String>> {
        val lines = ArrayList<String>()
        lines += TITLE
        worksheet.exercises.forEachIndexed { index, item -> appendExercise(lines, index, item) }
        return lines.chunked(linesPerPage.coerceAtLeast(1))
    }

    private fun appendExercise(
        lines: MutableList<String>,
        index: Int,
        item: WorksheetExercise,
    ) {
        lines += "${index + 1}. ${item.topic}"
        lines += item.prompt
        item.options.forEach { option -> lines += "• $option" }
    }

    private const val DEFAULT_LINES: Int = 32
}
