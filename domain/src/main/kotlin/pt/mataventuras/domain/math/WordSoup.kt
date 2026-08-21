package pt.mataventuras.domain.math

import kotlin.random.Random

/**
 * Letters and hidden-word paths for a sopa de letras.
 */
data class WordSoup(
    val cells: List<String>,
    val paths: List<List<Int>>,
    val words: List<String>,
)

/**
 * Places two or more words horizontally, vertically, and diagonally, both ways.
 */
class WordSoupBuilder(
    private val random: Random,
) {
    /**
     * Builds a [size]×[size] soup that hides [wordCount] number-words when they fit.
     */
    fun build(
        size: Int,
        wordCount: Int,
    ): WordSoup {
        repeat(16) {
            val soup = attempt(size, wordCount)
            if (soup.words.size >= minOf(wordCount, 2)) return soup
        }
        return attempt(size, 1)
    }

    private fun attempt(
        size: Int,
        wordCount: Int,
    ): WordSoup {
        val picks = WORDS.filter { it.length in 2..size }.shuffled(random).take(wordCount.coerceAtLeast(1))
        val grid = CharArray(size * size) { NUL }
        val paths = ArrayList<List<Int>>(picks.size)
        val placed = ArrayList<String>(picks.size)
        picks.forEach { word ->
            val path = placeWord(grid, size, word) ?: return@forEach
            paths += path
            placed += word
        }
        fillBlanks(grid)
        return WordSoup(grid.map { it.toString() }, paths, placed)
    }

    private fun placeWord(
        grid: CharArray,
        size: Int,
        word: String,
    ): List<Int>? {
        val starts = (0 until size * size).shuffled(random)
        val dirs = DIRECTIONS.shuffled(random)
        starts.forEach { start ->
            dirs.forEach { dir ->
                val path = pathIfFits(grid, size, start, dir, word)
                if (path != null) {
                    word.forEachIndexed { i, ch -> grid[path[i]] = ch }
                    return path
                }
            }
        }
        return null
    }

    private fun pathIfFits(
        grid: CharArray,
        size: Int,
        start: Int,
        dir: IntArray,
        word: String,
    ): List<Int>? {
        val path = ArrayList<Int>(word.length)
        var row = start / size
        var col = start % size
        word.forEach { ch ->
            if (row !in 0 until size || col !in 0 until size) return null
            val index = row * size + col
            val current = grid[index]
            if (current != NUL && current != ch) return null
            path += index
            row += dir[0]
            col += dir[1]
        }
        return path
    }

    private fun fillBlanks(grid: CharArray) {
        grid.indices.forEach { i ->
            if (grid[i] == NUL) grid[i] = LETTERS[random.nextInt(LETTERS.length)]
        }
    }

    private companion object {
        const val NUL: Char = '\u0000'
        const val LETTERS: String = "abcdefghijklmnopqrstuvwxyz"
        val WORDS: List<String> =
            listOf(
                "um",
                "par",
                "dez",
                "dois",
                "três",
                "seis",
                "sete",
                "oito",
                "nove",
                "soma",
                "cinco",
                "vinte",
                "treze",
                "quatro",
                "quinze",
                "trinta",
            )
        val DIRECTIONS: List<IntArray> =
            listOf(
                intArrayOf(0, 1),
                intArrayOf(0, -1),
                intArrayOf(1, 0),
                intArrayOf(-1, 0),
                intArrayOf(1, 1),
                intArrayOf(1, -1),
                intArrayOf(-1, 1),
                intArrayOf(-1, -1),
            )
    }
}
