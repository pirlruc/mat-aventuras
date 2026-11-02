package pt.mataventuras.domain.parent

import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Why the PIN screen stays up. The app maps each value to strings.xml.
 */
enum class PinNotice {
    /** The two PIN fields do not match. */
    MISMATCH,

    /** The PIN was wrong. */
    WRONG,

    /** Too many failures. */
    LOCKED,

    /** The PIN is not four digits. */
    FORMAT,

    /** No PIN is stored. */
    MISSING,
}

/**
 * PIN setup / unlock outcomes for the parental gate UI.
 */
sealed class PinGateResult {
    /** Gate opens. */
    data object Unlocked : PinGateResult()

    /** Stay on the PIN screen. [speak] is pt-PT, or null when nothing is spoken. */
    data class Stay(
        val notice: PinNotice,
        val remaining: Int = 0,
        val speak: String? = null,
    ) : PinGateResult()
}

/**
 * Parental PIN screen rules. Hashing stays in [PinPolicy].
 */
class PinGate(
    private val policy: PinPolicy,
) {
    /**
     * First-time PIN. Both fields must match and be four digits.
     */
    fun setPin(
        pin: String,
        confirm: String,
    ): Pair<PinGateResult, PinState?> {
        if (pin != confirm || !policy.isValidFormat(pin)) {
            return PinGateResult.Stay(PinNotice.MISMATCH) to null
        }
        return PinGateResult.Unlocked to policy.create(pin)
    }

    /**
     * Unlock attempt against stored [state].
     */
    fun unlock(
        state: PinState,
        pin: String,
    ): Pair<PinGateResult, PinState> {
        val (result, next) = policy.attempt(state, pin)
        val ui =
            when (result) {
                PinResult.Correct -> PinGateResult.Unlocked
                is PinResult.Incorrect ->
                    PinGateResult.Stay(PinNotice.WRONG, result.remaining, VoiceScripts.WRONG_PIN)
                is PinResult.Locked -> PinGateResult.Stay(PinNotice.LOCKED, speak = VoiceScripts.PIN_LOCKED)
                PinResult.InvalidFormat -> PinGateResult.Stay(PinNotice.FORMAT)
            }
        return ui to next
    }
}
