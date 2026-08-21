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
 * Each listed word occupies exactly one path; filler letters must not recreate it.
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
        val want = wordCount.coerceAtLeast(2)
        repeat(48) {
            val soup = attempt(size, want)
            if (soup.words.size >= 2 && WordSoupScanner.isUnique(soup)) return soup
        }
        return attempt(size, want)
    }

    private fun attempt(
        size: Int,
        wordCount: Int,
    ): WordSoup {
        val picks = WORDS.filter { it.length in 3..size }.shuffled(random).take(wordCount.coerceAtLeast(1))
        val grid = CharArray(size * size) { NUL }
        val paths = ArrayList<List<Int>>(picks.size)
        val placed = ArrayList<String>(picks.size)
        picks.forEach { word ->
            val snapshot = grid.clone()
            val path = placeWord(grid, size, word) ?: return@forEach
            if (WordSoupScanner.wordIsUnique(grid, size, word)) {
                paths += path
                placed += word
            } else {
                snapshot.copyInto(grid)
            }
        }
        fillBlanks(grid, size, placed)
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

    private fun fillBlanks(
        grid: CharArray,
        size: Int,
        words: List<String>,
    ) {
        val empties = grid.indices.filter { grid[it] == NUL }
        repeat(40) {
            empties.forEach { i -> grid[i] = LETTERS[random.nextInt(LETTERS.length)] }
            if (words.all { WordSoupScanner.wordIsUnique(grid, size, it) }) return
        }
        empties.forEach { i -> grid[i] = LETTERS[random.nextInt(LETTERS.length)] }
    }

    private companion object {
        const val NUL: Char = '\u0000'
        const val LETTERS: String = "kwyxj"
        val WORDS: List<String> =
            listOf(
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

/**
 * Counts accidental extra copies of a hidden word in every direction.
 */
object WordSoupScanner {
    /**
     * True when each listed word appears on exactly one unique cell path.
     */
    fun isUnique(soup: WordSoup): Boolean {
        val size = sizeOf(soup.cells.size)
        if (size <= 0) return soup.words.isEmpty()
        return soup.words.all { wordIsUnique(soup.cells.map { it.first() }.toCharArray(), size, it) }
    }

    /**
     * True when [word] occurs once (forwards or backwards counts as the same path).
     */
    fun wordIsUnique(
        grid: CharArray,
        size: Int,
        word: String,
    ): Boolean = occurrenceCount(grid, size, word) == 1

    /**
     * Distinct paths that spell [word] in any of the eight directions.
     */
    fun occurrenceCount(
        grid: CharArray,
        size: Int,
        word: String,
    ): Int {
        if (word.isEmpty() || size <= 0) return 0
        val seen = HashSet<List<Int>>()
        grid.indices.forEach { start ->
            DIRECTIONS.forEach { dir ->
                val path = match(grid, size, start, dir, word) ?: return@forEach
                seen += if (path.first() <= path.last()) path else path.asReversed()
            }
        }
        return seen.size
    }

    /**
     * Cells of extra copies of [word] that are not on an official path.
     */
    fun extraCells(
        grid: CharArray,
        size: Int,
        word: String,
        protected: Set<Int>,
    ): List<Int> {
        if (word.isEmpty() || size <= 0) return emptyList()
        val extra = ArrayList<Int>()
        grid.indices.forEach { start ->
            DIRECTIONS.forEach { dir ->
                val path = match(grid, size, start, dir, word) ?: return@forEach
                val key = if (path.first() <= path.last()) path else path.asReversed()
                if (key.any { it !in protected }) extra += key.filter { it !in protected }
            }
        }
        return extra.distinct()
    }

    /**
     * Grid edge length, or 0 when [cellCount] is not a square.
     */
    fun sizeOf(cellCount: Int): Int {
        if (cellCount <= 0) return 0
        var n = 1
        while (n * n < cellCount) n += 1
        return if (n * n == cellCount) n else 0
    }

    private fun match(
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
            if (grid[index] != ch) return null
            path += index
            row += dir[0]
            col += dir[1]
        }
        return path
    }

    private val DIRECTIONS: List<IntArray> =
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
