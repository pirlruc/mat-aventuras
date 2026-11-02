package pt.mataventuras.app.ui

import pt.mataventuras.domain.math.GroupTokens
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule

/**
 * Star-grid is for counting visuals, not digit recognition.
 */
internal fun showsStarGrid(
    module: LearningModule,
    visualCount: Int,
    kind: PlayKind = PlayKind.CHOICE,
): Boolean = kind == PlayKind.CHOICE && visualCount > 0 && module == LearningModule.COUNTING

/**
 * Huge numeral for "which number is this" (age 3).
 */
internal fun showsNumberHero(
    module: LearningModule,
    kind: PlayKind = PlayKind.CHOICE,
): Boolean = kind == PlayKind.CHOICE && module == LearningModule.NUMBERS

/**
 * Target silhouette above shape options, when the exercise names a shape.
 */
internal fun targetShapeToDraw(
    module: LearningModule,
    target: GeometricShape?,
    kind: PlayKind = PlayKind.CHOICE,
): GeometricShape? {
    if (!showsShapeGlyph(module) || target == null) return null
    return if (kind == PlayKind.SOUP || kind == PlayKind.SUDOKU) null else target
}

/**
 * Shape names become glyphs on the option buttons.
 */
internal fun showsShapeGlyph(module: LearningModule): Boolean = module == LearningModule.SHAPES

/**
 * Shadow and tangram draw the shape and hide its name on the prompt.
 */
internal fun showsSilhouette(module: LearningModule): Boolean {
    val shadow = module == LearningModule.SHADOW
    val tangram = module == LearningModule.TANGRAM
    return shadow || tangram
}

/**
 * Counting and number options also show a dot strip.
 */
internal fun showsDotStrip(module: LearningModule): Boolean {
    val counting = module == LearningModule.COUNTING
    val numbers = module == LearningModule.NUMBERS
    val dots = module == LearningModule.DOTS
    return counting || numbers || dots
}

/**
 * Age-7 lessons fill the viewport instead of packing against the top.
 */
internal fun lessonFillsViewport(age: AgeBand): Boolean = age == AgeBand.SIX_TO_EIGHT

/**
 * Sudoku, soup, cipher, and puzzle boards are taller than a choice row, so
 * they scroll even when [lessonFillsViewport] is true. Keeps Sair on screen.
 */
internal fun lessonScrolls(
    age: AgeBand,
    kind: PlayKind,
): Boolean = !lessonFillsViewport(age) || scrollsForBoard(kind)

/**
 * Shape for an option label, or null when the label is not a shape name.
 */
@Suppress("MaxLineLength")
internal fun shapeKind(option: String): GeometricShape? = GeometricShape.entries.firstOrNull { it.displayName == option }

/**
 * Numeric option value, or null when the label is not a number.
 */
internal fun optionInt(option: String): Int? = option.toIntOrNull()

/**
 * Extra height so glyphs and dots fit inside the option chip.
 */
internal fun optionMinHeightDp(
    module: LearningModule,
    baseDp: Int,
): Int = if (showsShapeGlyph(module) || showsDotStrip(module)) baseDp + 16 else baseDp

/**
 * Sudoku uses the titled cell grid.
 */
internal fun showsSudokuGrid(kind: PlayKind): Boolean = kind == PlayKind.SUDOKU

/**
 * Cipher shows a legend plus the coded symbols.
 */
internal fun showsCipherLegend(kind: PlayKind): Boolean = kind == PlayKind.CIPHER

/**
 * Puzzle shows a frame with a glowing hole.
 */
internal fun showsPuzzleFrame(kind: PlayKind): Boolean = kind == PlayKind.PUZZLE

/**
 * Picture game: crossed counters, a dot array, or equal rows.
 */
internal fun showsGroupsBoard(kind: PlayKind): Boolean = kind == PlayKind.GROUPS

/**
 * pt-PT title above a picture game.
 */
internal fun groupsBanner(module: LearningModule): String =
    when (module) {
        LearningModule.SUBTRACTION -> "Jogo das cruzes"
        LearningModule.DIVISION -> "Jogo das filas"
        else -> "Jogo da grelha"
    }

/**
 * Kept counters stay green. Crossed counters are the ones that left.
 */
internal fun groupTokenArgb(cell: String): Long = if (cell == GroupTokens.REMOVED) 0xFFFFCDD2 else 0xFFC8E6C9

/**
 * Boards taller than a choice row scroll so Sair stays on screen.
 */
internal fun scrollsForBoard(kind: PlayKind): Boolean =
    showsPlayGrid(kind) || showsCipherLegend(kind) || showsPuzzleFrame(kind) || showsGroupsBoard(kind)

/**
 * Sudoku and word-search cell size. Ages 3–5 stay at 64 dp and scroll.
 */
internal fun playCellHeightDp(
    columns: Int,
    age: AgeBand = AgeBand.SIX_TO_EIGHT,
): Int {
    if (age == AgeBand.THREE_TO_FIVE) return 64
    return when {
        columns >= 6 -> 48
        columns >= 5 -> 52
        columns >= 4 -> 56
        else -> 72
    }
}

/**
 * False while a previous tap is still being scored.
 */
internal fun shouldAcceptAnswer(busy: Boolean): Boolean = !busy

/**
 * Soup cells are the answers; other kinds keep a palette.
 */
internal fun showsOptionPalette(kind: PlayKind): Boolean {
    if (kind == PlayKind.SOUP) return false
    if (kind == PlayKind.MEMORY) return false
    return kind != PlayKind.WORD_COMPLETE
}

/**
 * Pair grid. Taps are not a single correct option.
 */
internal fun showsMemoryBoard(kind: PlayKind): Boolean = kind == PlayKind.MEMORY

/**
 * Missing-letter word, drawn apart from the generic option list.
 */
internal fun showsWordBoard(kind: PlayKind): Boolean = kind == PlayKind.WORD_COMPLETE

/**
 * Ages 3–5 addition shows the sum as dots.
 */
internal fun showsSumDots(
    module: LearningModule,
    visualCount: Int,
): Boolean = module == LearningModule.ADDITION && visualCount in 1..10
