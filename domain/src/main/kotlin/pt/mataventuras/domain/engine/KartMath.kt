package pt.mataventuras.domain.engine

/**
 * Column-major 4×4 matrices for the GLES host. No Android types.
 */
object KartMath {
    /**
     * Look-at view matrix. [up] is typically (0, 1, 0).
     */
    fun lookAt(
        eye: Vec3,
        center: Vec3,
        up: Vec3,
    ): FloatArray {
        val f = (center - eye).normalized()
        val s = f.cross(up).normalized()
        val u = s.cross(f)
        return floatArrayOf(
            s.x, u.x, -f.x, 0f,
            s.y, u.y, -f.y, 0f,
            s.z, u.z, -f.z, 0f,
            -s.dot(eye), -u.dot(eye), f.dot(eye), 1f,
        )
    }

    /**
     * Perspective projection. [fovYDegrees] is vertical FOV.
     */
    fun perspective(
        fovYDegrees: Float,
        aspect: Float,
        near: Float,
        far: Float,
    ): FloatArray {
        val f = 1f / kotlin.math.tan(Math.toRadians(fovYDegrees.toDouble() / 2.0).toFloat())
        val nf = 1f / (near - far)
        return floatArrayOf(
            f / aspect, 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, (far + near) * nf, -1f,
            0f, 0f, (2f * far * near) * nf, 0f,
        )
    }
}
