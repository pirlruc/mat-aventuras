package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Pair boards. [PlayBoard.solution] holds the pair key for each cell.
 */
class MemoryBoards(
    private val random: Random,
) {
    /**
     * 2×3 for ages 3–5. Ages 6–8 use a 4×4 match. [level] stays in range.
     */
    fun make(
        module: LearningModule,
        level: Int,
        age: AgeBand = AgeBand.SIX_TO_EIGHT,
    ): Exercise {
        val young = age == AgeBand.THREE_TO_FIVE
        val faces =
            youngFaces(module, young).let { built ->
                if (level < 0) built.take(6) else built
            }
        val order = faces.indices.shuffled(random)
        val cells = order.map { faces[it].label }
        val keys = order.map { faces[it].key }
        val columns = if (young) 3 else 4
        return Exercise(
            module = module,
            prompt = if (young) VoiceScripts.MEMORY_YOUNG else VoiceScripts.MEMORY_OLDER,
            spoken = VoiceScripts.MEMORY_SPOKEN,
            options = cells,
            correctIndex = 0,
            play =
                PlayBoard(
                    kind = PlayKind.MEMORY,
                    cells = cells,
                    columns = columns,
                    solution = keys,
                ),
        )
    }

    /**
     * True when the two cells share a pair key and are not the same cell.
     */
    fun isPair(
        play: PlayBoard,
        first: Int,
        second: Int,
    ): Boolean {
        if (first == second) return false
        if (first !in play.solution.indices || second !in play.solution.indices) return false
        return play.solution[first] == play.solution[second]
    }

    private fun youngFaces(
        module: LearningModule,
        young: Boolean,
    ): List<Face> =
        when {
            !young -> matchingFaces(module)
            module == LearningModule.LETTERS -> matchingFaces(module).take(6)
            else -> identicalFaces()
        }

    private fun identicalFaces(): List<Face> =
        listOf("1", "2", "3").flatMap { label ->
            listOf(Face(label, label), Face(label, label))
        }

    private fun matchingFaces(module: LearningModule): List<Face> {
        val pairs =
            if (module == LearningModule.LETTERS) {
                listOf(
                    "A" to "a",
                    "B" to "b",
                    "C" to "c",
                    "D" to "d",
                    "E" to "e",
                    "F" to "f",
                    "G" to "g",
                    "H" to "h",
                )
            } else {
                listOf(
                    "1" to "•",
                    "2" to "••",
                    "3" to "•••",
                    "4" to "••••",
                    "5" to "5",
                    "6" to "6",
                    "7" to "7",
                    "8" to "8",
                )
            }
        return pairs.take(8).flatMap { (left, right) ->
            listOf(Face(left, left), Face(right, left))
        }
    }

    private data class Face(
        val label: String,
        val key: String,
    )
}
