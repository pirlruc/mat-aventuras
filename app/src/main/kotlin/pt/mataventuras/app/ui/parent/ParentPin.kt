package pt.mataventuras.app.ui.parent

import pt.mataventuras.data.pin.PinRepository
import pt.mataventuras.domain.parent.PinGate
import pt.mataventuras.domain.parent.PinGateResult
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
                state == null -> PinGateResult.Stay(VoiceScripts.TRY_AGAIN, null) to null
                else -> gate.unlock(state, pin)
            }
        }
}
