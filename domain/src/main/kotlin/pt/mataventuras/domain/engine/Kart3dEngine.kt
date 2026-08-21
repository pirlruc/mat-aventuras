package pt.mataventuras.domain.engine

import kotlin.math.cos
import kotlin.math.sin

/**
 * Playable 3D kart state for the age-7 reward. World units are metres.
 */
data class Kart3dState(
    val x: Float,
    val y: Float,
    val z: Float,
    val heading: Float,
    val speed: Float,
    val steer: Float,
    val lapProgress: Float,
    val laps: Int,
    val lapsTarget: Int,
    val rings: Int,
    val ringsTarget: Int,
    val collectedMask: Int,
    val answerBoosts: Int,
    val boostTimer: Float,
    val offTrack: Boolean,
    val finished: Boolean,
    val cameraEye: Vec3,
    val cameraTarget: Vec3,
)

/**
 * Oval-track kart with auto-drive, player steering, boost, and ring pickups.
 */
class Kart3dEngine(
    private val track: OvalTrack = OvalTrack(),
    private val ringCount: Int = RING_COUNT,
) {
    /**
     * Kart on the start line, facing along the track.
     */
    fun initial(lapsTarget: Int = 3): Kart3dState {
        val startT = 0.02f
        val p = track.point(startT)
        val heading = headingOf(track.tangent(startT))
        return withCamera(
            Kart3dState(
                x = p.x,
                y = 0.35f,
                z = p.z,
                heading = heading,
                speed = 6f,
                steer = 0f,
                lapProgress = startT,
                laps = 0,
                lapsTarget = lapsTarget,
                rings = 0,
                ringsTarget = ringCount,
                collectedMask = 0,
                answerBoosts = 0,
                boostTimer = 0f,
                offTrack = false,
                finished = false,
                cameraEye = Vec3(0f, 0f, 0f),
                cameraTarget = Vec3(0f, 0f, 0f),
            ),
        )
    }

    /**
     * Advances the race. [steer] is -1..1; [boost] is a burst from a correct answer or a tap.
     */
    fun step(
        state: Kart3dState,
        dt: Float,
        steer: Float,
        boost: Boolean,
    ): Kart3dState {
        if (state.finished) return state
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        val boosted = applyBoost(state, clampedDt, boost)
        val moving = integrate(boosted, clampedDt, steer.coerceIn(-1f, 1f))
        val onTrack = resolveTrack(moving)
        val collected = collectRings(onTrack)
        return withCamera(collected)
    }

    /**
     * Chase-camera view matrix (column-major, OpenGL).
     */
    fun viewMatrix(state: Kart3dState): FloatArray {
        val up = Vec3(0f, 1f, 0f)
        return KartMath.lookAt(state.cameraEye, state.cameraTarget, up)
    }

    /**
     * Perspective projection matrix (column-major, OpenGL).
     */
    fun projectionMatrix(aspect: Float): FloatArray = KartMath.perspective(50f, aspect.coerceAtLeast(0.2f), 0.8f, 80f)

    /**
     * Left / right from a normalised touch X in 0..1. Centre is no steer (boost tap).
     */
    fun steerFromTouch(normalizedX: Float): Float = EngineInputMap.steerFromNormalizedX(normalizedX)

    /**
     * True when the touch is in the centre band (boost).
     */
    fun isBoostTouch(normalizedX: Float): Boolean = EngineInputMap.isBoostBand(normalizedX)

    private fun applyBoost(
        state: Kart3dState,
        dt: Float,
        boost: Boolean,
    ): Kart3dState {
        val timer = if (boost) BOOST_SECONDS else (state.boostTimer - dt).coerceAtLeast(0f)
        val boosts = if (boost) state.answerBoosts + 1 else state.answerBoosts
        return state.copy(boostTimer = timer, answerBoosts = boosts)
    }

    private fun integrate(
        state: Kart3dState,
        dt: Float,
        steer: Float,
    ): Kart3dState {
        val tangentHeading = headingOf(track.tangent(state.lapProgress))
        val assisted = lerpAngle(state.heading, tangentHeading, 0.28f * dt * 8f)
        val heading = assisted + steer * STEER_RATE * dt
        val target = if (state.boostTimer > 0f) BOOST_SPEED else CRUISE_SPEED
        val cap = if (state.offTrack) OFF_TRACK_SPEED else target
        val speed = moveToward(state.speed, cap, ACCEL * dt)
        val x = state.x + sin(heading) * speed * dt
        val z = state.z + cos(heading) * speed * dt
        return state.copy(x = x, z = z, heading = heading, speed = speed, steer = steer)
    }

    private fun resolveTrack(state: Kart3dState): Kart3dState {
        val off = track.isOffTrack(state.x, state.z)
        val pulled =
            if (off) {
                track.pullTowardCenter(state.x, state.z, SNAP_METRES)
            } else {
                Vec3(state.x, 0f, state.z)
            }
        val progress = track.progress(pulled.x, pulled.z)
        val laps = if (track.crossedStart(state.lapProgress, progress)) state.laps + 1 else state.laps
        val finished = laps >= state.lapsTarget
        return state.copy(
            x = pulled.x,
            z = pulled.z,
            lapProgress = progress,
            laps = laps,
            offTrack = off,
            y = if (off) 0.22f else 0.35f,
            finished = finished,
        )
    }

    private fun collectRings(state: Kart3dState): Kart3dState {
        if (state.finished) return state
        var mask = state.collectedMask
        var rings = state.rings
        for (i in 0 until ringCount) {
            val bit = 1 shl i
            if (mask and bit != 0) continue
            val p = track.ringPosition(i, ringCount)
            val dx = state.x - p.x
            val dz = state.z - p.z
            if (dx * dx + dz * dz <= RING_RADIUS * RING_RADIUS) {
                mask = mask or bit
                rings += 1
            }
        }
        return state.copy(collectedMask = mask, rings = rings)
    }

    private fun withCamera(state: Kart3dState): Kart3dState {
        val back = Vec3(-sin(state.heading), 0f, -cos(state.heading))
        val eye = Vec3(state.x, state.y, state.z) + back * 7.5f + Vec3(0f, 3.6f, 0f)
        val target = Vec3(state.x, state.y + 0.6f, state.z) + Vec3(sin(state.heading), 0f, cos(state.heading)) * 5f
        return state.copy(cameraEye = eye, cameraTarget = target)
    }

    private fun headingOf(tangent: Vec3): Float = kotlin.math.atan2(tangent.x, tangent.z)

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

    private fun lerpAngle(
        from: Float,
        to: Float,
        t: Float,
    ): Float {
        var diff = to - from
        while (diff > Math.PI) diff -= OvalTrack.TAU
        while (diff < -Math.PI) diff += OvalTrack.TAU
        return from + diff * t.coerceIn(0f, 1f)
    }

    /** Tuning constants for the kids' auto-drive kart. */
    companion object {
        const val RING_COUNT: Int = 8
        const val RING_RADIUS: Float = 1.7f
        const val CRUISE_SPEED: Float = 7.5f
        const val BOOST_SPEED: Float = 14f
        const val OFF_TRACK_SPEED: Float = 3f
        const val ACCEL: Float = 10f
        const val STEER_RATE: Float = 2.6f
        const val BOOST_SECONDS: Float = 1.15f
        const val SNAP_METRES: Float = 0.45f
    }
}
