package pt.mataventuras.app.ui

import pt.mataventuras.domain.math.PlayKind

/**
 * Letter soup is a drag board; sudoku stays a static frame.
 */
internal fun showsSoupBoard(kind: PlayKind): Boolean = kind == PlayKind.SOUP

/**
 * Cell under a pointer in a soup grid, or null when the pointer is in a gap.
 */
internal fun soupIndexAt(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    columns: Int,
    cellCount: Int,
    gap: Float,
): Int? {
    val rows = soupRows(columns, cellCount)
    val cellW = soupCell(columns, width, gap)
    val cellH = soupCell(rows, height, gap)
    val col = soupSlotOrNull(cellW, x, gap, columns)
    val row = soupSlotOrNull(cellH, y, gap, rows)
    val index = if (col != null && row != null) row * columns + col else -1
    return index.takeIf { it in 0 until cellCount }
}

/**
 * Slot index along one axis of a gapped grid, or null in the padding.
 */
private fun soupRows(
    columns: Int,
    cellCount: Int,
): Int = if (columns > 0 && cellCount > 0) boardRowCount(cellCount, columns) else 0

private fun soupCell(
    count: Int,
    span: Float,
    gap: Float,
): Float = if (count > 0 && span > 0f) (span - gap * (count - 1)) / count else 0f

internal fun soupSlotOrNull(
    cell: Float,
    offset: Float,
    gap: Float,
    count: Int,
): Int? = if (cell > 0f && count > 0) soupSlot(offset, cell, gap, count) else null

internal fun soupSlot(
    offset: Float,
    cell: Float,
    gap: Float,
    count: Int,
): Int? {
    var start = 0f
    for (i in 0 until count) {
        val end = start + cell
        if (offset >= start && offset <= end) return i
        start = end + gap
    }
    return null
}

/**
 * True when [a] and [b] share an edge or a corner.
 */
internal fun soupAreAdjacent(
    a: Int,
    b: Int,
    columns: Int,
): Boolean {
    if (columns <= 0 || a == b) return false
    val dCol = kotlin.math.abs(a % columns - b % columns)
    val dRow = kotlin.math.abs(a / columns - b / columns)
    return dCol <= 1 && dRow <= 1
}

/**
 * Grows a soup path when [next] is a new neighbour of the last cell.
 */
internal fun soupExtendPath(
    path: List<Int>,
    next: Int,
    columns: Int,
): List<Int> {
    if (path.isEmpty()) return listOf(next)
    if (path.last() == next || next in path) return path
    return if (soupAreAdjacent(path.last(), next, columns)) path + next else path
}

/**
 * Outcome when the child lifts the finger after a soup slide.
 */
internal fun soupReleaseKind(
    selected: List<Int>,
    targets: List<Int>,
): SoupRelease = soupReleaseKind(selected, listOf(targets), emptySet())

/**
 * Outcome against every hidden word, skipping [found] path indices.
 */
internal fun soupReleaseKind(
    selected: List<Int>,
    paths: List<List<Int>>,
    found: Set<Int>,
): SoupRelease {
    if (selected.isEmpty() || paths.isEmpty()) return SoupRelease.IGNORE
    if (soupMatchedPath(selected, paths, found) != null) return SoupRelease.HIT
    var minLen: Int? = null
    var index = 0
    while (index < paths.size) {
        if (index !in found) {
            val size = paths[index].size
            if (minLen == null || size < minLen) minLen = size
        }
        index += 1
    }
    val needed = minLen ?: 1
    return if (selected.size < needed) SoupRelease.IGNORE else SoupRelease.MISS
}

/**
 * Index of the hidden word that matches [selected], or null.
 */
internal fun soupMatchedPath(
    selected: List<Int>,
    paths: List<List<Int>>,
    found: Set<Int>,
): Int? {
    paths.forEachIndexed { i, path ->
        val hit = selected == path || selected == path.asReversed()
        if (i !in found && hit) return i
    }
    return null
}

/**
 * Board index to score, or null when the gesture is incomplete.
 */
internal fun soupPickIndex(
    selected: List<Int>,
    targets: List<Int>,
    cellCount: Int,
): Int? = soupPickIndex(selected, listOf(targets), emptySet(), cellCount)

/**
 * Board index to score for a multi-word soup.
 */
internal fun soupPickIndex(
    selected: List<Int>,
    paths: List<List<Int>>,
    found: Set<Int>,
    cellCount: Int,
): Int? {
    if (soupReleaseKind(selected, paths, found) == SoupRelease.IGNORE) return null
    val hit = soupMatchedPath(selected, paths, found)
    return if (hit != null) paths[hit].first() else soupMissIndex(paths.flatten(), cellCount)
}

/**
 * True when a wrong full-length slide should keep the current multi-word soup.
 */
internal fun soupKeepsBoard(
    kind: SoupRelease,
    pathCount: Int,
): Boolean = kind == SoupRelease.MISS && pathCount > 1

/**
 * A cell that is not part of the hidden word, used to record a miss.
 */
internal fun soupMissIndex(
    targets: List<Int>,
    cellCount: Int,
): Int = (0 until cellCount).firstOrNull { it !in targets } ?: 0

/**
 * Highlight colour while a soup path is held or already found.
 */
internal fun soupSelectedArgb(
    selected: Boolean,
    found: Boolean = false,
): Long =
    when {
        found -> 0xFF81C784
        selected -> 0xFFFFCC80
        else -> 0xFF90CAF9
    }

/**
 * Result of a soup pointer-up. IGNORE leaves the current exercise in place.
 */
internal enum class SoupRelease {
    IGNORE,
    HIT,
    MISS,
}
