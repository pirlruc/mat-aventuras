package pt.mataventuras.domain.engine

/**
 * On-screen HUD copy for reward races (pt-PT).
 */
object KartHud {
    /**
     * Lap counter, 1-based for display.
     */
    fun lapLabel(state: Kart3dState): String = lapLabel(state.laps, state.lapsTarget)

    /**
     * Lap counter for the 2D off-road racer.
     */
    fun lapLabel(state: OffroadState): String = lapLabel(state.laps, state.lapsTarget)

    /**
     * `Volta X de Y`.
     */
    fun lapLabel(
        laps: Int,
        lapsTarget: Int,
    ): String {
        val shown = (laps + 1).coerceAtMost(lapsTarget)
        return "Volta $shown de $lapsTarget"
    }

    /**
     * Rings collected.
     */
    fun ringsLabel(state: Kart3dState): String = "Anéis ${state.rings}/${state.ringsTarget}"

    /**
     * Gates collected on the dirt circuit.
     */
    fun gatesLabel(state: OffroadState): String = "Portões ${state.gates}/${state.gatesTarget}"

    /**
     * Boost call-out when a burst is active.
     */
    fun boostLabel(state: Kart3dState): String? = if (state.boostTimer > 0f) "Impulso!" else null

    /**
     * Boost call-out for the off-road racer.
     */
    fun boostLabel(state: OffroadState): String? = if (state.boostTimer > 0f) "Impulso!" else null

    /**
     * Off-track warning.
     */
    fun offTrackLabel(state: Kart3dState): String? = if (state.offTrack && !state.finished) "Volta à pista!" else null

    /**
     * Off-track warning for the dirt circuit.
     */
    fun offTrackLabel(state: OffroadState): String? = if (state.offTrack && !state.finished) "Volta à pista!" else null
}
