package pt.mataventuras.app.ui

import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildName
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.parent.ModulePerformance
import pt.mataventuras.domain.parent.ParentSummary
import pt.mataventuras.domain.progress.AvatarCode
import pt.mataventuras.domain.progress.BadgeCode
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * PIN submit button (pt-PT).
 */
internal fun pinSubmitLabel(
    settingPin: Boolean,
    save: String = "Guardar PIN",
    enter: String = "Entrar",
): String = if (settingPin) save else enter

/**
 * Prompt above the PIN fields.
 */
internal fun pinPrompt(settingPin: Boolean): String = if (settingPin) VoiceScripts.SET_PIN else VoiceScripts.ENTER_PIN

/**
 * True when the unlocked dashboard has nothing to summarise.
 */
internal fun waitingForProfile(
    profile: ChildProfile?,
    data: ParentSummary?,
): Boolean = profile == null || data == null

/**
 * One module row on the parental dashboard.
 */
internal fun modulePerformanceLine(module: ModulePerformance): String =
    "${ModuleTitles.noun(module.module)} — ${(module.accuracy * 100).toInt()}% " +
        "(${module.hits} certos, ${formatDuration(module.timeMs)})"

/**
 * Needs-work copy. Empty list yields the all-clear sentence.
 */
internal fun needsWorkLines(
    modules: List<LearningModule>,
    clear: String = "Nenhum módulo abaixo de 70% com amostra suficiente.",
): List<String> =
    if (modules.isEmpty()) {
        listOf(clear)
    } else {
        modules.map { "• ${ModuleTitles.noun(it)}" }
    }

/**
 * Child name when the text field is blank.
 */
internal fun fallbackChildName(name: String): String = ChildName.sanitize(name)

/**
 * Mascot chip size in dp.
 */
internal fun mascotChipDp(age: AgeBand): Int = if (age == AgeBand.THREE_TO_FIVE) 64 else 52

/**
 * Selected chips are fully opaque; unselected stay readable.
 */
internal fun ageButtonAlpha(selected: Boolean): Float = if (selected) 1f else 0.7f

/**
 * Selection ring width in dp.
 */
internal fun selectionBorderDp(selected: Boolean): Int = if (selected) 6 else 2

/**
 * Selection ring colour. Amber when chosen, slate otherwise.
 */
internal fun selectionHighlightArgb(selected: Boolean): Long = if (selected) 0xFFE65100 else 0xFF90A4AE

/**
 * Friend icon shown on the mascot colour chip.
 */
internal fun mascotGlyph(mascot: Mascot): String =
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
internal fun ageButtonEmoji(huge: Boolean): String = if (huge) "🧸" else "🚀"

/**
 * Age 3 uses icon nav; age 7 uses labelled buttons.
 */
internal fun usesIconNav(age: AgeBand): Boolean = age == AgeBand.THREE_TO_FIVE

/**
 * Badge collection row.
 */
internal fun badgeLine(
    owned: Boolean,
    code: BadgeCode,
): String = if (owned) "★ ${code.title}" else "☆ ${code.title} — ${code.description}"

/**
 * Avatar collection row.
 */
internal fun avatarLine(
    owned: Boolean,
    code: AvatarCode,
): String = if (owned) "★ ${code.title}" else "☆ ${code.title} (${code.minPoints} pontos)"
