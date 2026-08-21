package pt.mataventuras.domain.math

import kotlin.random.Random

/**
 * Mini-sudoku boards that keep rows, columns, and boxes unique.
 */
object SudokuGrids {
    /**
     * Filled grid of [size]×[size] with symbols 1..[size].
     */
    fun filled(
        size: Int,
        random: Random,
    ): List<Int> {
        val base =
            when (size) {
                4 -> GRID4
                6 -> GRID6
                else -> latin(size)
            }
        return remap(base, size, random)
    }

    /**
     * True when every row and column contains 1..[size] once.
     */
    fun isLatin(
        cells: List<Int>,
        size: Int,
    ): Boolean {
        if (cells.size != size * size) return false
        val want = (1..size).toSet()
        return (0 until size).all { i ->
            row(cells, size, i).toSet() == want && column(cells, size, i).toSet() == want
        }
    }

    /**
     * Latin plus 2×2 boxes on 4×4 and 2×3 boxes on 6×6.
     */
    fun isConsistent(
        cells: List<Int>,
        size: Int,
    ): Boolean {
        if (!isLatin(cells, size)) return false
        return when (size) {
            4 -> boxesUnique(cells, 4, 2, 2)
            6 -> boxesUnique(cells, 6, 2, 3)
            else -> true
        }
    }

    private fun latin(size: Int): List<Int> = List(size * size) { i -> (i / size + i % size) % size + 1 }

    private fun remap(
        cells: List<Int>,
        size: Int,
        random: Random,
    ): List<Int> {
        val map = (1..size).shuffled(random)
        return cells.map { map[it - 1] }
    }

    private fun row(
        cells: List<Int>,
        size: Int,
        index: Int,
    ): List<Int> = cells.subList(index * size, index * size + size)

    private fun column(
        cells: List<Int>,
        size: Int,
        index: Int,
    ): List<Int> = List(size) { row -> cells[row * size + index] }

    private fun boxesUnique(
        cells: List<Int>,
        size: Int,
        boxRows: Int,
        boxCols: Int,
    ): Boolean {
        val want = (1..size).toSet()
        val bands = 0 until size step boxRows
        val stacks = 0 until size step boxCols
        return bands.all { band ->
            stacks.all { stack ->
                cellsIn(cells, size, band until band + boxRows, stack until stack + boxCols).toSet() == want
            }
        }
    }

    private fun cellsIn(
        cells: List<Int>,
        size: Int,
        rows: IntRange,
        cols: IntRange,
    ): List<Int> {
        val box = ArrayList<Int>(size)
        for (r in rows) {
            for (c in cols) {
                box += cells[r * size + c]
            }
        }
        return box
    }

    private val GRID4: List<Int> =
        listOf(
            1, 2, 3, 4,
            3, 4, 1, 2,
            2, 1, 4, 3,
            4, 3, 2, 1,
        )

    private val GRID6: List<Int> =
        listOf(
            1, 2, 3, 4, 5, 6,
            4, 5, 6, 1, 2, 3,
            2, 3, 1, 5, 6, 4,
            5, 6, 4, 2, 3, 1,
            3, 1, 2, 6, 4, 5,
            6, 4, 5, 3, 1, 2,
        )
}
