package pt.mataventuras.domain.parent

import pt.mataventuras.domain.voice.VoiceScripts

/**
 * PIN setup / unlock outcomes for the parental gate UI.
 */
sealed class PinGateResult {
    /** Gate opens. */
    data object Unlocked : PinGateResult()

    /** Stay on the PIN screen. [message] is pt-PT. */
    data class Stay(val message: String, val speak: String?) : PinGateResult()
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
            return PinGateResult.Stay("Os PIN não coincidem ou não têm quatro números.", null) to null
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
                    PinGateResult.Stay("${VoiceScripts.WRONG_PIN} Restam ${result.remaining}.", VoiceScripts.WRONG_PIN)
                is PinResult.Locked -> PinGateResult.Stay(VoiceScripts.PIN_LOCKED, VoiceScripts.PIN_LOCKED)
                PinResult.InvalidFormat -> PinGateResult.Stay("O PIN tem quatro números.", null)
            }
        return ui to next
    }
}
