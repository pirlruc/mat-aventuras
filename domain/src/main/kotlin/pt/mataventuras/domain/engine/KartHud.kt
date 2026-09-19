package pt.mataventuras.domain.engine

/**
 * On-screen HUD copy for reward races (pt-PT).
 */
object KartHud {
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
     * Side-tap legend so left/right/boost bands stay visible.
     */
    const val CONTROL_HINT: String = "Esquerda · Impulso · Direita"

    /**
     * Overhead lap banner the kart drives under — not an obstacle.
     */
    const val META_HINT: String = "Passa por baixo da META"

    /**
     * Checkpoints the child drove through (auto-collected on the dirt).
     */
    fun gatesLabel(state: OffroadState): String = "Arcos ${state.gates}/${state.gatesTarget}"

    /**
     * Race position against AI karts.
     */
    fun placeLabel(
        place: Int,
        field: Int,
    ): String = "Lugar $place de $field"

    /**
     * Boost call-out for the off-road racer.
     */
    fun boostLabel(state: OffroadState): String? = if (state.boostTimer > 0f) "Impulso!" else null

    /**
     * Off-track warning for the dirt circuit.
     */
    fun offTrackLabel(state: OffroadState): String? = if (state.offTrack && !state.finished) "Volta à pista!" else null

    /**
     * Compact overlay used by native hosts and tests.
     */
    fun raceOverlay(
        state: OffroadState,
        circuitLength: Float,
    ): Pair<String, String> {
        val extra = offTrackLabel(state) ?: boostLabel(state)
        val place = placeLabel(RivalPack.place(state, circuitLength), RivalPack.fieldSize(state))
        val second = listOfNotNull(place, gatesLabel(state), extra).joinToString(" · ")
        return lapLabel(state) to second
    }
}
