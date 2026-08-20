package pt.mataventuras.domain.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3-space vector used by the kart simulation and camera.
 */
data class Vec3(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    /** Component-wise sum. */
    operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)

    /** Component-wise difference. */
    operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)

    /** Scalar product. */
    operator fun times(scale: Float): Vec3 = Vec3(x * scale, y * scale, z * scale)

    /** Euclidean length. */
    fun length(): Float = sqrt(x * x + y * y + z * z)

    /** Unit vector, or this when the length is zero. */
    fun normalized(): Vec3 {
        val len = length()
        if (len < 1e-5f) return this
        return this * (1f / len)
    }

    /** Right-handed cross product. */
    fun cross(other: Vec3): Vec3 =
        Vec3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x,
        )

    /** Dot product. */
    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
}

/**
 * Closed elliptical racetrack in the XZ plane. [t] is 0..1 around the lap.
 */
class OvalTrack(
    val radiusX: Float = 18f,
    val radiusZ: Float = 11f,
    val halfWidth: Float = 3.4f,
) {
    /**
     * Centerline point for progress [t] wrapped into 0..1.
     */
    fun point(t: Float): Vec3 {
        val a = wrap(t) * TAU
        return Vec3(radiusX * cos(a), 0f, radiusZ * sin(a))
    }

    /**
     * Forward tangent on the centerline.
     */
    fun tangent(t: Float): Vec3 {
        val a = wrap(t) * TAU
        return Vec3(-radiusX * sin(a), 0f, radiusZ * cos(a)).normalized()
    }

    /**
     * Horizontal outward unit normal (away from the oval interior).
     */
    fun outward(t: Float): Vec3 {
        val p = point(t)
        return Vec3(p.x / (radiusX * radiusX), 0f, p.z / (radiusZ * radiusZ)).normalized()
    }

    /**
     * Progress 0..1 of the nearest centerline point to ([x], [z]).
     */
    fun progress(
        x: Float,
        z: Float,
    ): Float {
        val t = kotlin.math.atan2(z / radiusZ, x / radiusX) / TAU
        return wrap(t)
    }

    /**
     * Distance from ([x], [z]) to the centerline.
     */
    fun distanceToCenter(
        x: Float,
        z: Float,
    ): Float {
        val p = point(progress(x, z))
        val dx = x - p.x
        val dz = z - p.z
        return sqrt(dx * dx + dz * dz)
    }

    /**
     * True when the kart is outside the asphalt.
     */
    fun isOffTrack(
        x: Float,
        z: Float,
    ): Boolean = distanceToCenter(x, z) > halfWidth

    /**
     * Moves ([x], [z]) up to [distance] metres toward the centerline (off-track recovery).
     */
    fun pullTowardCenter(
        x: Float,
        z: Float,
        distance: Float,
    ): Vec3 {
        val t = progress(x, z)
        val center = point(t)
        val dx = center.x - x
        val dz = center.z - z
        val len = sqrt(dx * dx + dz * dz)
        if (len < 1e-5f) return Vec3(x, 0f, z)
        val step = distance.coerceAtMost(len)
        return Vec3(x + dx / len * step, 0f, z + dz / len * step)
    }

    /**
     * True when progress wrapped forward across the start line.
     */
    fun crossedStart(
        previous: Float,
        current: Float,
    ): Boolean = previous > 0.82f && current < 0.18f

    /**
     * World position of collectible ring [index] of [count].
     */
    fun ringPosition(
        index: Int,
        count: Int,
        height: Float = 1.15f,
    ): Vec3 {
        val t = (index + 0.5f) / count.toFloat()
        val p = point(t)
        return Vec3(p.x, height, p.z)
    }

    /**
     * Wraps [t] into 0..1.
     */
    fun wrap(t: Float): Float {
        var x = t
        while (x < 0f) x += 1f
        while (x >= 1f) x -= 1f
        return x
    }

    /** Full turn in radians. */
    companion object {
        const val TAU: Float = (Math.PI * 2.0).toFloat()
    }
}
