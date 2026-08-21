package pt.mataventuras.domain.engine

/**
 * Three AI karts that cruise the same loop a little ahead of the player.
 */
object RivalPack {
    private val COLOURS: LongArray = longArrayOf(0xFFE53935, 0xFF1E88E5, 0xFF8E24AA)

    /**
     * Staggered start so the player sees someone to chase.
     */
    fun starting(seed: Int): List<RivalRacer> {
        val shift = (kotlin.math.abs(seed) % 7) * 4f
        return listOf(
            RivalRacer(28f + shift, -0.25f, 24f, 0, COLOURS[0]),
            RivalRacer(52f + shift, 0.18f, 26f, 0, COLOURS[1]),
            RivalRacer(76f + shift, -0.08f, 23f, 0, COLOURS[2]),
        )
    }

    /**
     * Advances rivals along [circuit]. They stay in-lane and wrap laps.
     */
    fun step(
        rivals: List<RivalRacer>,
        circuit: OffroadCircuit,
        dt: Float,
    ): List<RivalRacer> =
        rivals.map { rival ->
            var dist = rival.distance + rival.speed * dt
            var laps = rival.laps
            if (dist >= circuit.length) {
                dist -= circuit.length
                laps += 1
            }
            val weave = kotlin.math.sin((dist + rival.lateral) * 0.08f) * 0.22f
            rival.copy(
                distance = dist,
                laps = laps,
                lateral = weave.coerceIn(-0.55f, 0.55f),
            )
        }

    /**
     * 1-based race position including the player.
     */
    fun place(
        state: OffroadState,
        length: Float,
    ): Int {
        val player = state.laps * length + state.distance
        val ahead = state.rivals.count { it.laps * length + it.distance > player }
        return ahead + 1
    }

    /**
     * Field size (player plus rivals).
     */
    fun fieldSize(state: OffroadState): Int = state.rivals.size + 1
}
