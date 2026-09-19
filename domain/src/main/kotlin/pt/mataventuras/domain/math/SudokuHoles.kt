package pt.mataventuras.domain.math

import kotlin.random.Random

/**
 * Punches extra sudoku blanks while keeping the question cell unique.
 */
object SudokuHoles {
    /**
     * Extra empty house that is not the question cell.
     */
    const val EXTRA_BLANK: String = "·"

    /**
     * One question hole plus extra blanks that still leave a unique answer.
     */
    fun withHoles(
        full: List<Int>,
        size: Int,
        question: Int,
        extraWanted: Int,
        random: Random,
        glyph: (Int) -> String = { it.toString() },
    ): List<String> {
        val extra = linkedSetOf<Int>()
        val order = full.indices.filter { it != question }.shuffled(random)
        val letters = full.map(glyph).distinct()
        for (index in order) {
            if (extra.size >= extraWanted) break
            extra += index
            val trial = render(full, question, extra, glyph)
            if (!questionUnique(trial, size, question, glyph(full[question]), letters)) {
                extra.remove(index)
            }
        }
        return render(full, question, extra, glyph)
    }

    private fun render(
        full: List<Int>,
        question: Int,
        extra: Set<Int>,
        glyph: (Int) -> String,
    ): List<String> =
        full.mapIndexed { i, value ->
            when {
                i == question -> ""
                i in extra -> EXTRA_BLANK
                else -> glyph(value)
            }
        }

    private fun questionUnique(
        cells: List<String>,
        size: Int,
        question: Int,
        expected: String,
        alphabet: List<String>,
    ): Boolean {
        val fits = alphabet.filter { canPlace(cells, size, question, it) }
        return fits.size == 1 && fits.first() == expected
    }

    private fun canPlace(
        cells: List<String>,
        size: Int,
        index: Int,
        token: String,
    ): Boolean {
        val row = index / size
        val col = index % size
        val box = boxShape(size)
        if (takenIn(cells, (0 until size).map { row * size + it }, index, token)) return false
        if (takenIn(cells, (0 until size).map { it * size + col }, index, token)) return false
        val band = (row / box.first) * box.first
        val stack = (col / box.second) * box.second
        val houses = boxCells(size, band, stack, box.first, box.second)
        return !takenIn(cells, houses, index, token)
    }

    private fun boxShape(size: Int): Pair<Int, Int> =
        when (size) {
            6 -> 2 to 3
            4 -> 2 to 2
            else -> size to 1
        }

    private fun boxCells(
        size: Int,
        band: Int,
        stack: Int,
        boxH: Int,
        boxW: Int,
    ): List<Int> {
        val box = ArrayList<Int>(size)
        var dr = 0
        while (dr < boxH) {
            var dc = 0
            while (dc < boxW) {
                box += (band + dr) * size + stack + dc
                dc += 1
            }
            dr += 1
        }
        return box
    }

    private fun takenIn(
        cells: List<String>,
        indices: List<Int>,
        skip: Int,
        token: String,
    ): Boolean = indices.any { it != skip && cells[it] == token }
}
