package pt.mataventuras.domain.engine

/**
 * On-screen HUD copy for the 3D kart (pt-PT).
 */
object KartHud {
    /**
     * Lap counter, 1-based for display.
     */
    fun lapLabel(state: Kart3dState): String {
        val shown = (state.laps + 1).coerceAtMost(state.lapsTarget)
        return "Volta $shown de ${state.lapsTarget}"
    }

    /**
     * Rings collected.
     */
    fun ringsLabel(state: Kart3dState): String = "Anéis ${state.rings}/${state.ringsTarget}"

    /**
     * Boost call-out when a burst is active.
     */
    fun boostLabel(state: Kart3dState): String? = if (state.boostTimer > 0f) "Impulso!" else null

    /**
     * Off-track warning.
     */
    fun offTrackLabel(state: Kart3dState): String? = if (state.offTrack && !state.finished) "Volta à pista!" else null
}
