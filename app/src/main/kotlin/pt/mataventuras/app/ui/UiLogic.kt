package pt.mataventuras.app.ui

import android.speech.tts.TextToSpeech
import kotlin.math.cos
import kotlin.math.sin
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
    ): Boolean = visualCount > 0 && module == LearningModule.COUNTING

    /**
     * Huge numeral for "which number is this" (age 3).
     */
    fun showsNumberHero(module: LearningModule): Boolean = module == LearningModule.NUMBERS

    /**
     * Target silhouette above shape options, when the exercise names a shape.
     */
    fun targetShapeToDraw(
        module: LearningModule,
        target: GeometricShape?,
    ): GeometricShape? = if (showsShapeGlyph(module)) target else null

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
    fun mascotChipDp(age: AgeGroup): Int = if (age == AgeGroup.THREE_YEARS) 72 else 56

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
}
