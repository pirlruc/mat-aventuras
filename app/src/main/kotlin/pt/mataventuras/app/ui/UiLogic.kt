package pt.mataventuras.app.ui

import android.media.ToneGenerator
import android.os.Build
import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import kotlin.math.cos
import kotlin.math.sin
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.domain.math.GroupTokens
import pt.mataventuras.domain.math.PlayBoard
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.math.SudokuHoles
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildName
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.parent.ModulePerformance
import pt.mataventuras.domain.parent.ParentSummary
import pt.mataventuras.domain.progress.AvatarCode
import pt.mataventuras.domain.progress.BadgeCode
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Non-Compose UI rules. Kept out of @Composable functions so branches are unit-tested.
 */
internal object UiLogic {
    /**
     * Star-grid is for counting visuals, not digit recognition.
     */
    fun showsStarGrid(
        module: LearningModule,
        visualCount: Int,
        kind: PlayKind = PlayKind.CHOICE,
    ): Boolean = kind == PlayKind.CHOICE && visualCount > 0 && module == LearningModule.COUNTING

    /**
     * Huge numeral for "which number is this" (age 3).
     */
    fun showsNumberHero(
        module: LearningModule,
        kind: PlayKind = PlayKind.CHOICE,
    ): Boolean = kind == PlayKind.CHOICE && module == LearningModule.NUMBERS

    /**
     * Target silhouette above shape options, when the exercise names a shape.
     */
    fun targetShapeToDraw(
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
    fun showsShapeGlyph(module: LearningModule): Boolean = module == LearningModule.SHAPES

    /**
     * Counting and number options also show a dot strip.
     */
    fun showsDotStrip(module: LearningModule): Boolean {
        val counting = module == LearningModule.COUNTING
        val numbers = module == LearningModule.NUMBERS
        return counting || numbers
    }

    /**
     * Age-7 lessons fill the viewport instead of packing against the top.
     */
    fun lessonFillsViewport(age: AgeBand): Boolean = age == AgeBand.SIX_TO_EIGHT

    /**
     * Sudoku, soup, cipher, and puzzle boards are taller than a choice row, so
     * they scroll even when [lessonFillsViewport] is true. Keeps Sair on screen.
     */
    fun lessonScrolls(
        age: AgeBand,
        kind: PlayKind,
    ): Boolean = !lessonFillsViewport(age) || scrollsForBoard(kind)

    /**
     * Shape for an option label, or null when the label is not a shape name.
     */
    fun shapeKind(option: String): GeometricShape? = GeometricShape.entries.firstOrNull { it.displayName == option }

    /**
     * Numeric option value, or null when the label is not a number.
     */
    fun optionInt(option: String): Int? = option.toIntOrNull()

    /**
     * Extra height so glyphs and dots fit inside the option chip.
     */
    fun optionMinHeightDp(
        module: LearningModule,
        baseDp: Int,
    ): Int = if (showsShapeGlyph(module) || showsDotStrip(module)) baseDp + 16 else baseDp

    /**
     * Soup and sudoku draw a cell grid.
     */
    fun showsPlayGrid(kind: PlayKind): Boolean = kind == PlayKind.SUDOKU || kind == PlayKind.SOUP

    /**
     * Letter soup is a drag board; sudoku stays a static frame.
     */
    fun showsSoupBoard(kind: PlayKind): Boolean = kind == PlayKind.SOUP

    /**
     * Sudoku shows a titled board so it is not mistaken for a button row.
     */
    fun sudokuBanner(
        kind: PlayKind,
        age: AgeBand = AgeBand.SIX_TO_EIGHT,
    ): String? {
        if (kind != PlayKind.SUDOKU) return null
        return if (age == AgeBand.THREE_TO_FIVE) "Quadrado mágico" else "Sudoku"
    }

    /**
     * Box width in cells for mini-sudoku grid lines.
     */
    fun sudokuBoxWidth(columns: Int): Int =
        when (columns) {
            4 -> 2
            6 -> 3
            else -> maxOf(columns, 1)
        }

    /**
     * Box height in cells for mini-sudoku grid lines.
     */
    fun sudokuBoxHeight(columns: Int): Int =
        when (columns) {
            4, 6 -> 2
            else -> maxOf(columns, 1)
        }

    /**
     * Gutter in dp. Box edges are thicker so the 2×2 / 2×3 regions read as sudoku.
     */
    fun sudokuGapDp(
        indexInAxis: Int,
        box: Int,
    ): Int = if (indexInAxis > 0 && indexInAxis % box == 0) 4 else 1

    /**
     * Sudoku uses the titled cell grid.
     */
    fun showsSudokuGrid(kind: PlayKind): Boolean = kind == PlayKind.SUDOKU

    /**
     * Cipher shows a legend plus the coded symbols.
     */
    fun showsCipherLegend(kind: PlayKind): Boolean = kind == PlayKind.CIPHER

    /**
     * Puzzle shows a frame with a glowing hole.
     */
    fun showsPuzzleFrame(kind: PlayKind): Boolean = kind == PlayKind.PUZZLE

    /**
     * Picture game: crossed counters, a dot array, or equal rows.
     */
    fun showsGroupsBoard(kind: PlayKind): Boolean = kind == PlayKind.GROUPS

    /**
     * pt-PT title above a picture game.
     */
    fun groupsBanner(module: LearningModule): String =
        when (module) {
            LearningModule.SUBTRACTION -> "Jogo das cruzes"
            LearningModule.DIVISION -> "Jogo das filas"
            else -> "Jogo da grelha"
        }

    /**
     * Kept counters stay green. Crossed counters are the ones that left.
     */
    fun groupTokenArgb(cell: String): Long = if (cell == GroupTokens.REMOVED) 0xFFFFCDD2 else 0xFFC8E6C9

    /**
     * Boards taller than a choice row scroll so Sair stays on screen.
     */
    fun scrollsForBoard(kind: PlayKind): Boolean =
        showsPlayGrid(kind) || showsCipherLegend(kind) || showsPuzzleFrame(kind) || showsGroupsBoard(kind)

    /**
     * Lesson board size / cipher alphabet from consecutive hits (0, 3, 6, 9+).
     */
    fun lessonLevel(hits: Int): Int = (hits / 3).coerceIn(0, 3)

    /**
     * Sudoku and word-search cell size. Ages 3–5 stay at 64 dp and scroll.
     */
    fun playCellHeightDp(
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
    fun shouldAcceptAnswer(busy: Boolean): Boolean = !busy

    /**
     * Repeat the current prompt after a reward Activity covered the host.
     */
    fun shouldRepeatSpokenPrompt(stoppedInBackground: Boolean): Boolean = stoppedInBackground

    /**
     * Soup cells are the answers; other kinds keep a palette.
     */
    fun showsOptionPalette(kind: PlayKind): Boolean {
        if (kind == PlayKind.SOUP) return false
        if (kind == PlayKind.MEMORY) return false
        return kind != PlayKind.WORD_COMPLETE
    }

    /**
     * Pair grid. Taps are not a single correct option.
     */
    fun showsMemoryBoard(kind: PlayKind): Boolean = kind == PlayKind.MEMORY

    /**
     * Missing-letter word, drawn apart from the generic option list.
     */
    fun showsWordBoard(kind: PlayKind): Boolean = kind == PlayKind.WORD_COMPLETE

    /**
     * Ages 3–5 addition shows the sum as dots.
     */
    fun showsSumDots(
        module: LearningModule,
        visualCount: Int,
    ): Boolean = module == LearningModule.ADDITION && visualCount in 1..10

    /**
     * Test tag for a tappable answer.
     */
    fun answerTag(correct: Boolean): String = debugAnswerTag(correct, debugAnswerTags)

    /**
     * Age-7 sudoku fills every blank, so no single button is the whole answer.
     */
    fun optionAnswerTag(
        correct: Boolean,
        fillEveryBlank: Boolean,
    ): String = if (fillEveryBlank) "sudoku-digit" else answerTag(correct)

    /**
     * Rows needed to lay out [cellCount] in [columns].
     */
    fun boardRowCount(
        cellCount: Int,
        columns: Int,
    ): Int = if (columns <= 0) 0 else (cellCount + columns - 1) / columns

    /**
     * Empty sudoku cells show a question mark. Extra blanks stay empty.
     */
    fun holeLabel(
        cell: String,
        fillEveryBlank: Boolean = false,
    ): String =
        when {
            fillEveryBlank && isBoardHole(cell) -> ""
            isExtraBlank(cell) -> ""
            cell.isEmpty() -> "?"
            else -> cell
        }

    /**
     * Extra sudoku blanks that are not the question cell.
     */
    fun isExtraBlank(cell: String): Boolean = cell == SudokuHoles.EXTRA_BLANK

    /**
     * The cell whose value the option buttons ask for.
     */
    fun isQuestionHole(cell: String): Boolean = cell.isEmpty() || cell == "?"

    /**
     * Puzzle and sudoku holes, including extra blanks on harder boards.
     */
    fun isBoardHole(cell: String): Boolean = isQuestionHole(cell) || isExtraBlank(cell)

    /**
     * Question cells glow; extra blanks stay pale so the asked house is obvious.
     */
    fun sudokuCellArgb(
        cell: String,
        focused: Boolean = false,
        fillEveryBlank: Boolean = false,
    ): Long =
        when {
            !isBoardHole(cell) -> 0xFFE3F2FD
            fillEveryBlank && focused -> 0xFFFFF59D
            fillEveryBlank -> 0xFFEEEEEE
            isQuestionHole(cell) -> 0xFFFFF59D
            else -> 0xFFEEEEEE
        }

    /**
     * True when the board is solved house by house instead of one glowing cell.
     */
    fun sudokuFillsEveryBlank(play: PlayBoard): Boolean = play.solution.isNotEmpty()

    /**
     * Glyph on the correct/wrong flash (kids can read a tick or cross).
     */
    fun answerFlashGlyph(
        correct: Boolean,
        gentle: Boolean = false,
        mascot: String = "",
    ): String {
        if (correct) return "✓"
        if (gentle && mascot.isNotEmpty()) return mascot
        return "✗"
    }

    /**
     * Caption under the flash glyph. Same pt-PT lines TTS already speaks.
     */
    fun answerFlashCaption(correct: Boolean): String = if (correct) VoiceScripts.WELL_DONE else VoiceScripts.TRY_AGAIN

    /**
     * Solid colour for the flash glyph.
     */
    fun answerFlashArgb(
        correct: Boolean,
        gentle: Boolean = false,
    ): Long {
        if (correct) return 0xFF2E7D32
        if (gentle) return 0xFFF9A825
        return 0xFFC62828
    }

    /**
     * Scrim behind the flash so the board still peeks through.
     */
    fun answerFlashScrimArgb(
        correct: Boolean,
        gentle: Boolean = false,
    ): Long {
        if (correct) return 0x882E7D32
        if (gentle) return 0x88F9A825
        return 0x88C62828
    }

    /**
     * Flash length in milliseconds.
     */
    const val ANSWER_FLASH_MS: Int = 960

    /**
     * Scale of the flash glyph for [alpha] 0..1.
     */
    fun answerFlashScale(alpha: Float): Float = 0.85f + 0.35f * alpha

    /**
     * Draw the flash only while it is still fading.
     */
    fun showsAnswerFlash(alpha: Float): Boolean = alpha > 0f

    /**
     * ToneGenerator programme: ack on a hit, nack on a miss.
     */
    fun answerTone(
        correct: Boolean,
        gentle: Boolean = false,
    ): Int {
        if (correct) return ToneGenerator.TONE_PROP_ACK
        if (gentle) return ToneGenerator.TONE_PROP_BEEP
        return ToneGenerator.TONE_PROP_NACK
    }

    /**
     * How long the chime plays.
     */
    fun answerToneMs(
        correct: Boolean,
        gentle: Boolean = false,
    ): Int {
        if (correct) return 180
        if (gentle) return 140
        return 260
    }

    /**
     * Filled stars toward the next prize. A finished set shows all three.
     */
    fun prizeStars(streak: Int): Int {
        if (streak <= 0) return 0
        val remainder = streak % 3
        return if (remainder == 0) 3 else remainder
    }

    /**
     * Three stars, filled up to [prizeStars].
     */
    fun prizeStarLine(streak: Int): String {
        val filled = prizeStars(streak)
        return "★".repeat(filled) + "☆".repeat(3 - filled)
    }

    /**
     * Largest font scale applied to fixed-size lesson text.
     */
    const val MAX_FONT_SCALE: Float = 1.3f

    /**
     * STREAM_MUSIC volume for [ToneGenerator] (0..100).
     */
    const val ANSWER_VOLUME_PERCENT: Int = 80

    /**
     * View haptic code. CONFIRM/REJECT need API 30; older devices get a click or long-press.
     */
    fun answerHaptic(
        correct: Boolean,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): Int =
        if (sdkInt >= 30) {
            if (correct) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
        } else {
            if (correct) HapticFeedbackConstants.CONTEXT_CLICK else HapticFeedbackConstants.LONG_PRESS
        }

    /**
     * Vertices of an upward triangle centred on ([cx], [cy]).
     */
    fun triangleVertices(
        cx: Float,
        cy: Float,
        radius: Float,
    ): List<Pair<Float, Float>> =
        listOf(
            cx to cy - radius,
            cx - radius to cy + radius,
            cx + radius to cy + radius,
        )

    /**
     * 10-point star polygon centred on ([cx], [cy]).
     */
    fun starVertices(
        cx: Float,
        cy: Float,
        radius: Float,
    ): List<Pair<Float, Float>> {
        val inner = radius * 0.4f
        return List(10) { i ->
            val angle = (Math.PI / 2.0) + i * Math.PI / 5.0
            val rad = if (i % 2 == 0) radius else inner
            cx + (cos(angle) * rad).toFloat() to cy - (sin(angle) * rad).toFloat()
        }
    }

    /**
     * Score line under the options.
     */
    fun lessonScoreLine(
        hits: Int,
        points: Int,
    ): String = "$hits certos · $points pontos"

    /**
     * PIN submit button (pt-PT).
     */
    fun pinSubmitLabel(settingPin: Boolean): String = if (settingPin) "Guardar PIN" else "Entrar"

    /**
     * Prompt above the PIN fields.
     */
    fun pinPrompt(settingPin: Boolean): String = if (settingPin) VoiceScripts.SET_PIN else VoiceScripts.ENTER_PIN

    /**
     * True when the unlocked dashboard has nothing to summarise.
     */
    fun waitingForProfile(
        profile: ChildProfile?,
        data: ParentSummary?,
    ): Boolean = profile == null || data == null

    /**
     * One module row on the parental dashboard.
     */
    fun modulePerformanceLine(module: ModulePerformance): String =
        "${ModuleTitles.noun(module.module)} — ${(module.accuracy * 100).toInt()}% " +
            "(${module.hits} certos, ${formatDuration(module.timeMs)})"

    /**
     * Needs-work copy. Empty list yields the all-clear sentence.
     */
    fun needsWorkLines(modules: List<LearningModule>): List<String> =
        if (modules.isEmpty()) {
            listOf("Nenhum módulo abaixo de 70% com amostra suficiente.")
        } else {
            modules.map { "• ${ModuleTitles.noun(it)}" }
        }

    /**
     * Child name when the text field is blank.
     */
    fun fallbackChildName(name: String): String = ChildName.sanitize(name)

    /**
     * Mascot chip size in dp.
     */
    fun mascotChipDp(age: AgeBand): Int = if (age == AgeBand.THREE_TO_FIVE) 64 else 52

    /**
     * Age-button side in dp. Both bands share a side so the chips match.
     */
    const val AGE_BUTTON_SIDE_DP: Int = 168

    /**
     * Selected chips are fully opaque; unselected stay readable.
     */
    fun ageButtonAlpha(selected: Boolean): Float = if (selected) 1f else 0.7f

    /**
     * Selection ring width in dp.
     */
    fun selectionBorderDp(selected: Boolean): Int = if (selected) 6 else 2

    /**
     * Selection ring colour. Amber when chosen, slate otherwise.
     */
    fun selectionHighlightArgb(selected: Boolean): Long = if (selected) 0xFFE65100 else 0xFF90A4AE

    /**
     * Friend icon shown on the mascot colour chip.
     */
    fun mascotGlyph(mascot: Mascot): String =
        when (mascot) {
            Mascot.SPEEDY_HEDGEHOG -> "🦔"
            Mascot.HERO_PUP -> "🐶"
            Mascot.PINK_PIGLET -> "🐷"
            Mascot.BRAVE_PLUMBER -> "🔧"
            Mascot.MISCHIEVOUS_ALIEN -> "👽"
        }

    /**
     * Emoji on the age button.
     */
    fun ageButtonEmoji(huge: Boolean): String = if (huge) "🧸" else "🚀"

    /**
     * Age 3 uses icon nav; age 7 uses labelled buttons.
     */
    fun usesIconNav(age: AgeBand): Boolean = age == AgeBand.THREE_TO_FIVE

    /**
     * Badge collection row.
     */
    fun badgeLine(
        owned: Boolean,
        code: BadgeCode,
    ): String = if (owned) "★ ${code.title}" else "☆ ${code.title} — ${code.description}"

    /**
     * Avatar collection row.
     */
    fun avatarLine(
        owned: Boolean,
        code: AvatarCode,
    ): String = if (owned) "★ ${code.title}" else "☆ ${code.title} (${code.minPoints} pontos)"

    /**
     * TTS language is usable.
     */
    fun languageSupported(result: Int): Boolean {
        if (result == TextToSpeech.LANG_MISSING_DATA) return false
        return result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /**
     * Speak only when ready and the line is non-blank.
     */
    fun shouldSpeak(
        ready: Boolean,
        text: String,
    ): Boolean = ready && text.isNotBlank()

    /**
     * Star centres inside a square of [minDimension] pixels.
     */
    fun starCenters(
        count: Int,
        minDimension: Float,
    ): List<Pair<Float, Float>> {
        val columns = 5
        val step = minDimension / 6f
        return List(count) { i ->
            (i % columns) * step + step to (i / columns) * step + step
        }
    }

    /**
     * `{minutes}m {seconds}s` duration.
     */
    fun formatDuration(ms: Long): String {
        val minutes = ms / 60_000L
        val seconds = (ms / 1000L) % 60L
        return "${minutes}m ${seconds}s"
    }

    /**
     * Cell under a pointer in a soup grid, or null when the pointer is in a gap.
     */
    fun soupIndexAt(
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

    fun soupSlot(
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
    fun soupAreAdjacent(
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
    fun soupExtendPath(
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
    fun soupReleaseKind(
        selected: List<Int>,
        targets: List<Int>,
    ): SoupRelease = soupReleaseKind(selected, listOf(targets), emptySet())

    /**
     * Outcome against every hidden word, skipping [found] path indices.
     */
    fun soupReleaseKind(
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
    fun soupMatchedPath(
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
    fun soupPickIndex(
        selected: List<Int>,
        targets: List<Int>,
        cellCount: Int,
    ): Int? = soupPickIndex(selected, listOf(targets), emptySet(), cellCount)

    /**
     * Board index to score for a multi-word soup.
     */
    fun soupPickIndex(
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
    fun soupKeepsBoard(
        kind: SoupRelease,
        pathCount: Int,
    ): Boolean = kind == SoupRelease.MISS && pathCount > 1

    /**
     * A cell that is not part of the hidden word, used to record a miss.
     */
    fun soupMissIndex(
        targets: List<Int>,
        cellCount: Int,
    ): Int = (0 until cellCount).firstOrNull { it !in targets } ?: 0

    /**
     * Highlight colour while a soup path is held or already found.
     */
    fun soupSelectedArgb(
        selected: Boolean,
        found: Boolean = false,
    ): Long =
        when {
            found -> 0xFF81C784
            selected -> 0xFFFFCC80
            else -> 0xFF90CAF9
        }
}

/**
 * Result of a soup pointer-up. IGNORE leaves the current exercise in place.
 */
internal enum class SoupRelease {
    IGNORE,
    HIT,
    MISS,
}
