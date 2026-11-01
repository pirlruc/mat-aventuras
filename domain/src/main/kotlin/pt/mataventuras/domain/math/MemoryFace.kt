package pt.mataventuras.domain.math

/**
 * Open cards on a memory board. A mismatch stays face up until another card is chosen.
 */
data class MemoryFace(
    val up: List<Int> = emptyList(),
    val matched: Set<Int> = emptySet(),
) {
    /**
     * True when every cell has a partner showing.
     */
    fun solved(cellCount: Int): Boolean = cellCount > 0 && matched.size == cellCount

    /**
     * Turn [index] over. A second tap on the same card does nothing.
     */
    fun reveal(
        play: PlayBoard,
        index: Int,
    ): MemoryFace {
        if (!canOpen(play, index)) return this
        return when {
            up.size == 2 -> copy(up = listOf(index))
            up.isEmpty() -> copy(up = listOf(index))
            else -> openSecond(play, index)
        }
    }

    private fun canOpen(
        play: PlayBoard,
        index: Int,
    ): Boolean {
        if (index !in play.cells.indices) return false
        if (index in matched || index in up) return false
        return true
    }

    private fun openSecond(
        play: PlayBoard,
        index: Int,
    ): MemoryFace {
        val first = up.first()
        val same = play.solution.getOrNull(first) == play.solution.getOrNull(index)
        return if (same) {
            copy(up = emptyList(), matched = matched + first + index)
        } else {
            copy(up = listOf(first, index))
        }
    }
}
