package pt.mataventuras.domain.engine

/**
 * Playable 2.5D off-road racer state (age-7 reward). Units are metres.
 */
data class OffroadState(
    val distance: Float,
    val lateral: Float,
    val speed: Float,
    val steer: Float,
    val laps: Int,
    val lapsTarget: Int,
    val gates: Int,
    val gatesTarget: Int,
    val collectedMask: Int,
    val boostTimer: Float,
    val answerBoosts: Int,
    val offTrack: Boolean,
    val finished: Boolean,
    val seed: Int,
    val rivals: List<RivalRacer> = emptyList(),
)

/**
 * AI kart the player races against.
 */
data class RivalRacer(
    val distance: Float,
    val lateral: Float,
    val speed: Float,
    val laps: Int,
    val argb: Long,
)

/**
 * Rear-view dirt circuit: analog steer, boost, checkpoints, rival karts.
 */
class OffroadRacerEngine(
    private val circuit: OffroadCircuit = OffroadCircuit(1),
) {
    /**
     * Kart on the start line, centred in the lane.
     */
    fun initial(lapsTarget: Int = 3): OffroadState =
        OffroadState(
            distance = 2f,
            lateral = 0f,
            speed = CRUISE,
            steer = 0f,
            laps = 0,
            lapsTarget = lapsTarget,
            gates = 0,
            gatesTarget = circuit.gateCount,
            collectedMask = 0,
            boostTimer = 0f,
            answerBoosts = 0,
            offTrack = false,
            finished = false,
            seed = circuit.seed,
            rivals = RivalPack.starting(circuit.seed),
        )

    /**
     * Advances the race. [steer] is -1..1; [boost] is a centre-tap burst.
     */
    fun step(
        state: OffroadState,
        dt: Float,
        steer: Float,
        boost: Boolean,
    ): OffroadState {
        if (state.finished) return state
        val clamped = dt.coerceIn(0.001f, 0.05f)
        val wheel = steer.coerceIn(-1f, 1f)
        val timer = if (boost) BOOST_SECONDS else (state.boostTimer - clamped).coerceAtLeast(0f)
        val boosts = if (boost) state.answerBoosts + 1 else state.answerBoosts
        val target =
            if (timer > 0f) {
                BOOST
            } else if (state.offTrack) {
                OFF_ROAD
            } else {
                CRUISE
            }
        val speed = moveToward(state.speed, target, ACCEL * clamped)
        val curve = circuit.curveAt(state.distance)
        val half = circuit.widthAt(state.distance)
        val lateral =
            (state.lateral + wheel * STEER * clamped + curve * speed * DRIFT * clamped)
                .coerceIn(-1.35f, 1.35f)
        val off = kotlin.math.abs(lateral) > half
        val distance = state.distance + speed * clamped
        val lapped = distance >= circuit.length
        val wrapped = if (lapped) distance - circuit.length else distance
        val laps = if (lapped) state.laps + 1 else state.laps
        val mask = if (lapped) 0 else state.collectedMask
        val rivals = RivalPack.step(state.rivals, circuit, clamped)
        val collected =
            collectGates(
                state.copy(
                    distance = wrapped,
                    lateral = lateral,
                    laps = laps,
                    collectedMask = mask,
                    rivals = rivals,
                ),
            )
        return collected.copy(
            speed = speed,
            steer = wheel,
            boostTimer = timer,
            answerBoosts = boosts,
            offTrack = off,
            finished = laps >= state.lapsTarget,
        )
    }

    /**
     * Left / right from a normalised touch X in 0..1. Centre is boost.
     */
    fun steerFromTouch(normalizedX: Float): Float = EngineInputMap.steerFromNormalizedX(normalizedX)

    /**
     * True when the touch is in the centre band (boost).
     */
    fun isBoostTouch(normalizedX: Float): Boolean = EngineInputMap.isBoostBand(normalizedX)

    private fun collectGates(state: OffroadState): OffroadState {
        var mask = state.collectedMask
        var gates = state.gates
        for (i in 0 until circuit.gateCount) {
            val bit = 1 shl i
            if (mask and bit != 0) continue
            val gap = kotlin.math.abs(state.distance - circuit.gateDistance(i))
            if (gap <= GATE_WINDOW && !state.offTrack && kotlin.math.abs(state.lateral) < halfWidth(state)) {
                mask = mask or bit
                gates = (gates + 1).coerceAtMost(state.gatesTarget)
            }
        }
        return state.copy(collectedMask = mask, gates = gates)
    }

    private fun halfWidth(state: OffroadState): Float = circuit.widthAt(state.distance)

    private fun moveToward(
        current: Float,
        target: Float,
        delta: Float,
    ): Float =
        if (current < target) {
            (current + delta).coerceAtMost(target)
        } else {
            (current - delta).coerceAtLeast(target)
        }

    /** Tuning for a short kids' dirt race. */
    companion object {
        const val CRUISE: Float = 28f
        const val BOOST: Float = 46f
        const val OFF_ROAD: Float = 12f
        const val ACCEL: Float = 22f
        const val STEER: Float = 5.6f
        const val DRIFT: Float = 0.006f
        const val BOOST_SECONDS: Float = 1.15f
        const val GATE_WINDOW: Float = 8f
        const val GATE_LATERAL: Float = 0.9f
    }
}
