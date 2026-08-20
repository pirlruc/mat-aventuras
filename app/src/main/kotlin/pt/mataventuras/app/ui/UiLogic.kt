package pt.mataventuras.app.ui

import android.media.ToneGenerator
import android.os.Build
import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import kotlin.math.cos
import kotlin.math.sin
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.model.AgeGroup
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
    fun showsDotStrip(module: LearningModule): Boolean =
        module == LearningModule.COUNTING || module == LearningModule.NUMBERS

    /**
     * Age-7 lessons fill the viewport instead of packing against the top.
     */
    fun lessonFillsViewport(age: AgeGroup): Boolean = age == AgeGroup.SEVEN_YEARS

    /**
     * Shape for an option label, or null when the label is not a shape name.
     */
    fun shapeKind(option: String): GeometricShape? =
        GeometricShape.entries.firstOrNull { it.displayName == option }

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
     * Sudoku uses the disabled cell grid.
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
     * Lesson board size / cipher alphabet from consecutive hits (0, 3, 6, 9+).
     */
    fun lessonLevel(hits: Int): Int = (hits / 3).coerceIn(0, 3)

    /**
     * Sudoku/soup cell height so 5×5 and 6×6 boards still fit.
     */
    fun playCellHeightDp(columns: Int): Int =
        when {
            columns >= 6 -> 44
            columns >= 5 -> 52
            columns >= 4 -> 56
            else -> 72
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
    fun showsOptionPalette(kind: PlayKind): Boolean = kind != PlayKind.SOUP

    /**
     * Test tag for a tappable answer.
     */
    fun answerTag(correct: Boolean): String = if (correct) "correct-answer" else "distractor"

    /**
     * Rows needed to lay out [cellCount] in [columns].
     */
    fun boardRowCount(
        cellCount: Int,
        columns: Int,
    ): Int = if (columns <= 0) 0 else (cellCount + columns - 1) / columns

    /**
     * Empty sudoku cells show a question mark.
     */
    fun holeLabel(cell: String): String = cell.ifEmpty { "?" }

    /**
     * Puzzle and sudoku holes glow so the missing piece is obvious.
     */
    fun isBoardHole(cell: String): Boolean = cell.isEmpty() || cell == "?"

    /**
     * Glyph on the correct/wrong flash (kids can read a tick or cross).
     */
    fun answerFlashGlyph(correct: Boolean): String = if (correct) "✓" else "✗"

    /**
     * Caption under the flash glyph. Same pt-PT lines TTS already speaks.
     */
    fun answerFlashCaption(correct: Boolean): String =
        if (correct) VoiceScripts.WELL_DONE else VoiceScripts.TRY_AGAIN

    /**
     * Solid colour for the flash glyph.
     */
    fun answerFlashArgb(correct: Boolean): Long = if (correct) 0xFF2E7D32 else 0xFFC62828

    /**
     * Scrim behind the flash so the board still peeks through.
     */
    fun answerFlashScrimArgb(correct: Boolean): Long = if (correct) 0x882E7D32 else 0x88C62828

    /**
     * Flash length in milliseconds.
     */
    fun answerFlashMs(): Int = 480

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
    fun answerTone(correct: Boolean): Int =
        if (correct) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK

    /**
     * How long the chime plays.
     */
    fun answerToneMs(correct: Boolean): Int = if (correct) 180 else 260

    /**
     * STREAM_MUSIC volume for [ToneGenerator] (0..100).
     */
    fun answerVolumePercent(): Int = 80

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
    ): String = "$hits certos · $points pts"

    /**
     * PIN submit button (pt-PT).
     */
    fun pinSubmitLabel(settingPin: Boolean): String = if (settingPin) "Guardar PIN" else "Entrar"

    /**
     * Prompt above the PIN fields.
     */
    fun pinPrompt(settingPin: Boolean): String =
        if (settingPin) VoiceScripts.SET_PIN else VoiceScripts.ENTER_PIN

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
        "${module.module.name.lowercase()} — ${(module.accuracy * 100).toInt()}% " +
            "(${module.hits} certos, ${formatDuration(module.timeMs)})"

    /**
     * Needs-work copy. Empty list yields the all-clear sentence.
     */
    fun needsWorkLines(modules: List<LearningModule>): List<String> =
        if (modules.isEmpty()) {
            listOf("Nenhum módulo abaixo de 70% com amostra suficiente.")
        } else {
            modules.map { "• ${it.name.lowercase()}" }
        }

    /**
     * Child name when the text field is blank.
     */
    fun fallbackChildName(name: String): String = name.trim().ifBlank { "Amigo" }

    /**
     * Mascot chip size in dp.
     */
    fun mascotChipDp(age: AgeGroup): Int = if (age == AgeGroup.THREE_YEARS) 64 else 52

    /**
     * Age-button side in dp. Both bands share a side so the chips match.
     */
    fun ageButtonSideDp(): Int = 168

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
    fun usesIconNav(age: AgeGroup): Boolean = age == AgeGroup.THREE_YEARS

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
    ): String = if (owned) "★ ${code.title}" else "☆ ${code.title} (${code.minPoints} pts)"

    /**
     * TTS language is usable.
     */
    fun languageSupported(result: Int): Boolean =
        result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED

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
        if (columns <= 0 || cellCount <= 0 || width <= 0f || height <= 0f) return null
        val rows = boardRowCount(cellCount, columns)
        val cellW = (width - gap * (columns - 1)) / columns
        val cellH = (height - gap * (rows - 1)) / rows
        if (cellW <= 0f || cellH <= 0f) return null
        val col = soupSlot(x, cellW, gap, columns) ?: return null
        val row = soupSlot(y, cellH, gap, rows) ?: return null
        val index = row * columns + col
        return if (index in 0 until cellCount) index else null
    }

    /**
     * Slot index along one axis of a gapped grid, or null in the padding.
     */
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
    ): SoupRelease {
        if (selected.isEmpty() || targets.isEmpty()) return SoupRelease.IGNORE
        if (selected == targets || selected == targets.asReversed()) return SoupRelease.HIT
        return if (selected.size < targets.size) SoupRelease.IGNORE else SoupRelease.MISS
    }

    /**
     * Board index to score, or null when the gesture is incomplete.
     */
    fun soupPickIndex(
        selected: List<Int>,
        targets: List<Int>,
        cellCount: Int,
    ): Int? {
        val kind = soupReleaseKind(selected, targets)
        if (kind == SoupRelease.IGNORE) return null
        return if (kind == SoupRelease.HIT) targets.first() else soupMissIndex(targets, cellCount)
    }

    /**
     * A cell that is not part of the hidden word, used to record a miss.
     */
    fun soupMissIndex(
        targets: List<Int>,
        cellCount: Int,
    ): Int = (0 until cellCount).firstOrNull { it !in targets } ?: 0

    /**
     * Highlight colour while a soup path is held.
     */
    fun soupSelectedArgb(selected: Boolean): Long = if (selected) 0xFFFFCC80 else 0xFF90CAF9
}

/**
 * Result of a soup pointer-up. IGNORE leaves the current exercise in place.
 */
internal enum class SoupRelease {
    IGNORE,
    HIT,
    MISS,
}
