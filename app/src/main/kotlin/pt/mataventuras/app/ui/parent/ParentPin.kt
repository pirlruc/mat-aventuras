package pt.mataventuras.app.ui.parent

import pt.mataventuras.data.pin.PinRepository
import pt.mataventuras.domain.parent.PinGate
import pt.mataventuras.domain.parent.PinGateResult
import pt.mataventuras.domain.parent.PinNotice
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * PIN submit rules for the parental gate. Persistence stays in [PinRepository].
 */
internal object ParentPin {
    /**
     * First-time set or unlock, serialized on the PIN store so lockout counts.
     */
    suspend fun submit(
        settingPin: Boolean,
        pin: String,
        confirmation: String,
        gate: PinGate,
        pins: PinRepository,
    ): PinGateResult =
        pins.update { state ->
            when {
                settingPin && state == null -> gate.setPin(pin, confirmation)
                state == null -> PinGateResult.Stay(PinNotice.MISSING, speak = VoiceScripts.TRY_AGAIN) to null
                else -> gate.unlock(state, pin)
            }
        }
}
