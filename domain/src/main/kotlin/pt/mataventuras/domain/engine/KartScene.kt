package pt.mataventuras.domain.engine

import kotlin.math.cos
import kotlin.math.sin
import pt.mataventuras.domain.model.Mascot

/**
 * Which static mesh a [KartDrawItem] instances.
 */
enum class KartMeshId {
    GRASS,
    TRACK,
    START,
    BOX,
}

/**
 * One instanced draw: mesh, transform, and RGB colour.
 */
data class KartDrawItem(
    val mesh: KartMeshId,
    val x: Float,
    val y: Float,
    val z: Float,
    val yawDegrees: Float,
    val scaleX: Float,
    val scaleY: Float,
    val scaleZ: Float,
    val red: Float,
    val green: Float,
    val blue: Float,
)

/**
 * GLES-independent scene graph for the oval-track kart reward.
 */
object KartScene {
    /**
     * Concatenated HUD fingerprint used to skip redundant overlay updates.
     */
    fun hudFingerprint(state: Kart3dState): String =
        KartHud.lapLabel(state) + KartHud.ringsLabel(state) +
            (KartHud.boostLabel(state) ?: "") + (KartHud.offTrackLabel(state) ?: "")

    /**
     * Overlay line under the lap counter (rings plus optional boost/off-track).
     */
    fun ringsLine(state: Kart3dState): String {
        val extra = KartHud.boostLabel(state) ?: KartHud.offTrackLabel(state)
        return listOfNotNull(KartHud.ringsLabel(state), extra).joinToString(" · ")
    }

    /**
     * Linear RGB from [mascot.primaryArgb].
     */
    fun mascotRgb(mascot: Mascot): FloatArray =
        floatArrayOf(
            ((mascot.primaryArgb shr 16) and 0xFF) / 255f,
            ((mascot.primaryArgb shr 8) and 0xFF) / 255f,
            (mascot.primaryArgb and 0xFF) / 255f,
        )

    /**
     * Wheel centres in world space, matching the kart heading.
     */
    fun wheelPositions(state: Kart3dState): List<Vec3> {
        val offsets =
            listOf(
                Vec3(0.45f, 0.12f, 0.45f),
                Vec3(-0.45f, 0.12f, 0.45f),
                Vec3(0.45f, 0.12f, -0.45f),
                Vec3(-0.45f, 0.12f, -0.45f),
            )
        val sinH = sin(state.heading)
        val cosH = cos(state.heading)
        return offsets.map { wheel ->
            Vec3(
                state.x + sinH * wheel.z + cosH * wheel.x,
                wheel.y,
                state.z + cosH * wheel.z - sinH * wheel.x,
            )
        }
    }

    /**
     * Ordered draw list: ground, track, props, kart, wheels.
     */
    fun instances(
        track: OvalTrack,
        state: Kart3dState,
        mascot: Mascot,
    ): List<KartDrawItem> {
        val rgb = mascotRgb(mascot)
        val items = ArrayList<KartDrawItem>(32)
        items += KartDrawItem(KartMeshId.GRASS, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 0.35f, 0.72f, 0.28f)
        items += KartDrawItem(KartMeshId.TRACK, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 0.22f, 0.22f, 0.25f)
        items += KartDrawItem(KartMeshId.START, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 0.95f, 0.85f, 0.2f)
        KartMesh.conePositions(track).forEach { cone ->
            items += KartDrawItem(KartMeshId.BOX, cone.x, cone.y, cone.z, 0f, 0.35f, 0.8f, 0.35f, 1f, 0.45f, 0.12f)
        }
        KartMesh.remainingRings(track, state).forEach { ring ->
            items += KartDrawItem(KartMeshId.BOX, ring.x, ring.y, ring.z, 0f, 0.55f, 0.2f, 0.55f, 1f, 0.84f, 0.2f)
        }
        val yaw = Math.toDegrees(state.heading.toDouble()).toFloat()
        items +=
            KartDrawItem(
                KartMeshId.BOX,
                state.x,
                state.y,
                state.z,
                yaw,
                0.7f,
                0.35f,
                1.1f,
                rgb[0],
                rgb[1],
                rgb[2],
            )
        wheelPositions(state).forEach { wheel ->
            items +=
                KartDrawItem(
                    KartMeshId.BOX,
                    wheel.x,
                    wheel.y,
                    wheel.z,
                    0f,
                    0.18f,
                    0.18f,
                    0.18f,
                    0.08f,
                    0.08f,
                    0.08f,
                )
        }
        return items
    }
}
