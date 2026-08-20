package pt.mataventuras.domain.engine

/**
 * Simulatable 3D race state (age-7 reward).
 */
data class Kart3dState(
    val trackPosition: Float,
    val speed: Float,
    val answerBoosts: Int,
    val laps: Int,
    val lapsTarget: Int,
    val finished: Boolean,
)

/**
 * Simplified kart: correct answers raise speed; [lapsTarget] laps win.
 */
class Kart3dEngine(
    private val acceleration: Float = 12f,
    private val drag: Float = 4f,
    private val lapLength: Float = 40f,
) {
    /**
     * Kart at the start of the track.
     */
    fun initial(lapsTarget: Int = 3): Kart3dState =
        Kart3dState(
            trackPosition = 0f,
            speed = 4f,
            answerBoosts = 0,
            laps = 0,
            lapsTarget = lapsTarget,
            finished = false,
        )

    /**
     * Advances the simulation. [boost] is true after a correct sum.
     */
    fun step(
        state: Kart3dState,
        dt: Float,
        boost: Boolean,
    ): Kart3dState {
        if (state.finished) return state
        val extra = if (boost) acceleration else 0f
        val speed = (state.speed + extra * dt - drag * dt).coerceAtLeast(2f)
        var position = state.trackPosition + speed * dt
        var laps = state.laps
        if (position >= lapLength) {
            position -= lapLength
            laps += 1
        }
        val boosts = if (boost) state.answerBoosts + 1 else state.answerBoosts
        val finished = laps >= state.lapsTarget
        return state.copy(
            trackPosition = position,
            speed = speed,
            answerBoosts = boosts,
            laps = laps,
            finished = finished,
        )
    }
}
