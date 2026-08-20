package pt.mataventuras.app.ui

import android.speech.tts.TextToSpeech
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
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
     * Star-grid is for counting/shapes visuals, not digit recognition.
     */
    fun showsStarGrid(
        module: LearningModule,
        visualCount: Int,
    ): Boolean = visualCount > 0 && module != LearningModule.NUMBERS

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
    fun mascotChipDp(age: AgeGroup): Int = if (age == AgeGroup.THREE_YEARS) 64 else 48

    /**
     * Age-button side in dp.
     */
    fun ageButtonSideDp(huge: Boolean): Int = if (huge) 180 else 150

    /**
     * Selected age buttons are fully opaque.
     */
    fun ageButtonAlpha(selected: Boolean): Float = if (selected) 1f else 0.55f

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
