package pt.mataventuras.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import pt.mataventuras.app.R
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Resolves a pt-PT domain line through the active language pack.
 * TTS and Compose both use this so the child hears the same sentence they see.
 */
fun Context.packLine(text: String): String {
    EXACT[text]?.let { return getString(it) }
    prefixed(text)?.let { return it }
    return templated(text) ?: text
}

/** Compose wrapper around [packLine]. */
@Composable
fun packed(text: String): String = LocalContext.current.packLine(text)

private fun Context.prefixed(text: String): String? {
    for ((prefix, id) in PREFIXES) {
        if (text.length > prefix.length && text.startsWith(prefix)) {
            return getString(id) + text.removePrefix(prefix)
        }
    }
    return null
}

private fun Context.templated(text: String): String? =
    moneyLine(text)
        ?: letterLine(text)
        ?: balloonLine(text)
        ?: fishLine(text)
        ?: continueLine(text)
        ?: greetingLine(text)
        ?: mascotLine(text)

private fun Context.moneyLine(text: String): String? {
    val match = MONEY.matchEntire(text) ?: return null
    return getString(R.string.voice_money, match.groupValues[1].toInt(), match.groupValues[2].toInt())
}

private fun Context.letterLine(text: String): String? {
    val match = LETTER.matchEntire(text) ?: return null
    return getString(R.string.voice_missing_letter, match.groupValues[1])
}

private fun Context.balloonLine(text: String): String? {
    val match = BALLOON.matchEntire(text) ?: return null
    return getString(R.string.voice_balloon, match.groupValues[1])
}

private fun Context.fishLine(text: String): String? {
    val match = FISH.matchEntire(text) ?: return null
    return getString(R.string.voice_fish, match.groupValues[1])
}

private fun Context.continueLine(text: String): String? {
    val match = CONTINUE.matchEntire(text) ?: return null
    return getString(R.string.voice_continue_as, match.groupValues[1])
}

private fun Context.greetingLine(text: String): String? {
    YOUNG.matchEntire(text)?.let { return getString(R.string.voice_greeting_young, it.groupValues[1]) }
    OLDER.matchEntire(text)?.let { return getString(R.string.voice_greeting_older, it.groupValues[1]) }
    return null
}

private fun Context.mascotLine(text: String): String? {
    val match = MASCOT.matchEntire(text) ?: return null
    return getString(R.string.voice_mascot_me, match.groupValues[1])
}

private val MONEY = Regex("""Quanto é (\d+) € e (\d+) cêntimos\?""")
private val LETTER = Regex("""Qual é a letra que falta em (.+)\?""")
private val BALLOON = Regex("""Rebenta o balão (.+)\.""")
private val FISH = Regex("""Pesca o peixe (.+)\.""")
private val CONTINUE = Regex("""Continuar como (.+)""")
private val YOUNG = Regex("""Olá! Eu sou o (.+)\. Vamos brincar com números\?""")
private val OLDER = Regex("""Olá, eu sou o (.+)\. Pronto para um desafio de matemática\?""")
private val MASCOT = Regex("""Eu sou o (.+)!""")

private val PREFIXES: List<Pair<String, Int>> =
    listOf(
        VoiceScripts.SNAKE_PROMPT to R.string.voice_snake_prompt,
        VoiceScripts.TOWER_SPOKEN to R.string.voice_tower,
        VoiceScripts.BREAKOUT_SPOKEN to R.string.voice_breakout,
        VoiceScripts.RUNNER_SPOKEN to R.string.voice_runner_spoken,
        VoiceScripts.PATTERN_PROMPT to R.string.voice_pattern_prompt,
    )

private val EXACT: Map<String, Int> =
    mapOf(
        VoiceScripts.LETS_PLAY to R.string.voice_lets_play,
        VoiceScripts.ENTER_PIN to R.string.voice_enter_pin,
        VoiceScripts.SET_PIN to R.string.voice_set_pin,
        VoiceScripts.WRONG_PIN to R.string.voice_wrong_pin,
        VoiceScripts.STEER_HINT to R.string.voice_steer,
        VoiceScripts.JUMP_HINT to R.string.voice_jump,
        VoiceScripts.INVADERS_HINT to R.string.voice_invaders,
        VoiceScripts.CHOMP_HINT to R.string.voice_chomp,
        VoiceScripts.CLIMB_HINT to R.string.voice_climb,
        VoiceScripts.LEAVE to R.string.voice_leave,
        VoiceScripts.CONFIRM_LEAVE to R.string.voice_confirm_leave,
        VoiceScripts.STAY to R.string.voice_stay,
        VoiceScripts.START to R.string.voice_start,
        VoiceScripts.CHOOSE_FRIEND to R.string.voice_choose_friend,
        VoiceScripts.AGE_THREE_PREVIEW to R.string.voice_age_three,
        VoiceScripts.AGE_SEVEN_PREVIEW to R.string.voice_age_seven,
        VoiceScripts.MEMORY_YOUNG to R.string.voice_memory_young,
        VoiceScripts.MEMORY_OLDER to R.string.voice_memory_older,
        VoiceScripts.MEMORY_SPOKEN to R.string.voice_memory_spoken,
        VoiceScripts.REWARD_FINISHED to R.string.voice_reward_finished,
        VoiceScripts.REWARD_RETURN to R.string.voice_reward_return,
        VoiceScripts.ODD_ONE_PROMPT to R.string.voice_odd_prompt,
        VoiceScripts.ODD_ONE_SPOKEN to R.string.voice_odd_spoken,
        VoiceScripts.SIZE_SMALL_PROMPT to R.string.voice_size_small,
        VoiceScripts.SIZE_BIG_PROMPT to R.string.voice_size_big,
        VoiceScripts.SIZE_SPOKEN to R.string.voice_size_spoken,
        VoiceScripts.SORT_SPOKEN to R.string.voice_sort,
        VoiceScripts.DOTS_SPOKEN to R.string.voice_dots,
        VoiceScripts.CLAPS_SPOKEN to R.string.voice_claps,
        VoiceScripts.SHADOW_PROMPT to R.string.voice_shadow_prompt,
        VoiceScripts.SHADOW_SPOKEN to R.string.voice_shadow_spoken,
        VoiceScripts.PATTERN_PROMPT to R.string.voice_pattern_prompt,
        VoiceScripts.PATTERN_SPOKEN to R.string.voice_pattern_spoken,
        VoiceScripts.FRACTION_SPOKEN to R.string.voice_fraction,
        VoiceScripts.ORDER_SPOKEN to R.string.voice_order,
        VoiceScripts.WORD_PROBLEM_SPOKEN to R.string.voice_word_problem,
        VoiceScripts.TANGRAM_PROMPT to R.string.voice_tangram_prompt,
        VoiceScripts.TANGRAM_SPOKEN to R.string.voice_tangram_spoken,
        VoiceScripts.SPELL_SPOKEN to R.string.voice_spell,
        VoiceScripts.CLOCK_SPOKEN to R.string.voice_clock_spoken,
        VoiceScripts.MONEY_SPOKEN to R.string.voice_money_spoken,
        VoiceScripts.POP_BALLOON to R.string.voice_pop,
        VoiceScripts.SNAKE_PROMPT to R.string.voice_snake_prompt,
        VoiceScripts.SNAKE_DONE to R.string.voice_snake_done,
        VoiceScripts.CLOCK_ASK to R.string.voice_clock_ask,
        VoiceScripts.COLOUR_SPOKEN to R.string.voice_colour,
        VoiceScripts.BUBBLE_SPOKEN to R.string.voice_bubble,
        VoiceScripts.BREAKOUT_SPOKEN to R.string.voice_breakout,
        VoiceScripts.TOWER_SPOKEN to R.string.voice_tower,
        VoiceScripts.RUNNER_SPOKEN to R.string.voice_runner_spoken,
        VoiceScripts.APP_TITLE to R.string.app_name,
        VoiceScripts.APP_TAGLINE to R.string.app_tagline,
        VoiceScripts.APP_DESCRIPTION to R.string.app_description,
        VoiceScripts.AGE_SELECTION to R.string.age_selection,
        VoiceScripts.THREE_TO_FIVE to R.string.three_years,
        VoiceScripts.SIX_TO_EIGHT to R.string.seven_years,
        VoiceScripts.WELL_DONE to R.string.well_done,
        VoiceScripts.TRY_AGAIN to R.string.try_again,
        VoiceScripts.LEADERBOARD to R.string.leaderboard,
        VoiceScripts.REWARDS to R.string.rewards,
        VoiceScripts.PARENT_DASHBOARD to R.string.parent_dashboard,
        VoiceScripts.PIN_LOCKED to R.string.pin_locked,
        VoiceScripts.STAYS_ON_DEVICE to R.string.privacy,
        VoiceScripts.SWITCH_PROFILE to R.string.switch_profile,
        VoiceScripts.YOUR_NAME to R.string.your_name,
        "3 a 5 anos. Vamos brincar!" to R.string.voice_age_young_tap,
        "6 a 8 anos. Pronto para desafios?" to R.string.voice_age_older_tap,
    )
