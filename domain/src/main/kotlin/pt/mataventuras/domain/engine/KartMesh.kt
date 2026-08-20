package pt.mataventuras.domain.engine

/**
 * Static GLES-friendly triangle meshes (x, y, z per vertex) for the kart reward.
 */
object KartMesh {
    /**
     * Grass quad centred on the origin.
     */
    fun grass(): FloatArray = quad(0f, -0.02f, 0f, 48f, 48f)

    /**
     * Asphalt ribbon around [track].
     */
    fun trackRibbon(
        track: OvalTrack,
        segments: Int = 64,
    ): FloatArray {
        val verts = ArrayList<Float>((segments + 1) * 6 * 3)
        for (i in 0 until segments) {
            val t0 = i / segments.toFloat()
            val t1 = (i + 1) / segments.toFloat()
            val inner0 = track.point(t0) - track.outward(t0) * track.halfWidth
            val outer0 = track.point(t0) + track.outward(t0) * track.halfWidth
            val inner1 = track.point(t1) - track.outward(t1) * track.halfWidth
            val outer1 = track.point(t1) + track.outward(t1) * track.halfWidth
            addTriangle(verts, inner0, outer0, outer1)
            addTriangle(verts, inner0, outer1, inner1)
        }
        return verts.toFloatArray()
    }

    /**
     * Yellow start-line quad across the track at t = 0.
     */
    fun startLine(track: OvalTrack): FloatArray {
        val inner = track.point(0f) - track.outward(0f) * track.halfWidth
        val outer = track.point(0f) + track.outward(0f) * track.halfWidth
        val tangent = track.tangent(0f) * 0.35f
        val a = Vec3(inner.x - tangent.x, 0.03f, inner.z - tangent.z)
        val b = Vec3(outer.x - tangent.x, 0.03f, outer.z - tangent.z)
        val c = Vec3(outer.x + tangent.x, 0.03f, outer.z + tangent.z)
        val d = Vec3(inner.x + tangent.x, 0.03f, inner.z + tangent.z)
        val verts = ArrayList<Float>(18)
        addTriangle(verts, a, b, c)
        addTriangle(verts, a, c, d)
        return verts.toFloatArray()
    }

    /**
     * Axis-aligned box centred at the origin.
     */
    fun box(
        hx: Float,
        hy: Float,
        hz: Float,
    ): FloatArray {
        val v =
            arrayOf(
                Vec3(-hx, -hy, hz),
                Vec3(hx, -hy, hz),
                Vec3(hx, hy, hz),
                Vec3(-hx, hy, hz),
                Vec3(-hx, -hy, -hz),
                Vec3(-hx, hy, -hz),
                Vec3(hx, hy, -hz),
                Vec3(hx, -hy, -hz),
            )
        val verts = ArrayList<Float>(108)
        addQuad(verts, v[0], v[1], v[2], v[3])
        addQuad(verts, v[4], v[5], v[6], v[7])
        addQuad(verts, v[3], v[2], v[6], v[5])
        addQuad(verts, v[0], v[4], v[7], v[1])
        addQuad(verts, v[1], v[7], v[6], v[2])
        addQuad(verts, v[0], v[3], v[5], v[4])
        return verts.toFloatArray()
    }

    /**
     * Cone marker positions along the outer edge.
     */
    fun conePositions(
        track: OvalTrack,
        count: Int = 12,
    ): List<Vec3> =
        (0 until count).map { i ->
            val t = i / count.toFloat()
            val p = track.point(t) + track.outward(t) * (track.halfWidth + 0.6f)
            Vec3(p.x, 0.4f, p.z)
        }

    /**
     * Inner-edge barrier posts along the oval.
     */
    fun innerBarrierPositions(
        track: OvalTrack,
        count: Int = 10,
    ): List<Vec3> =
        (0 until count).map { i ->
            val t = i / count.toFloat()
            val p = track.point(t) - track.outward(t) * (track.halfWidth + 0.45f)
            Vec3(p.x, 0.35f, p.z)
        }

    /**
     * Uncollected ring world positions for [state].
     */
    fun remainingRings(
        track: OvalTrack,
        state: Kart3dState,
    ): List<Vec3> {
        val out = mutableListOf<Vec3>()
        for (i in 0 until state.ringsTarget) {
            if (state.collectedMask and (1 shl i) == 0) {
                out += track.ringPosition(i, state.ringsTarget)
            }
        }
        return out
    }

    private fun quad(
        cx: Float,
        y: Float,
        cz: Float,
        hx: Float,
        hz: Float,
    ): FloatArray {
        val verts = ArrayList<Float>(18)
        val a = Vec3(cx - hx, y, cz - hz)
        val b = Vec3(cx + hx, y, cz - hz)
        val c = Vec3(cx + hx, y, cz + hz)
        val d = Vec3(cx - hx, y, cz + hz)
        addTriangle(verts, a, b, c)
        addTriangle(verts, a, c, d)
        return verts.toFloatArray()
    }

    private fun addQuad(
        out: MutableList<Float>,
        a: Vec3,
        b: Vec3,
        c: Vec3,
        d: Vec3,
    ) {
        addTriangle(out, a, b, c)
        addTriangle(out, a, c, d)
    }

    private fun addTriangle(
        out: MutableList<Float>,
        a: Vec3,
        b: Vec3,
        c: Vec3,
    ) {
        out += a.x
        out += a.y
        out += a.z
        out += b.x
        out += b.y
        out += b.z
        out += c.x
        out += c.y
        out += c.z
    }
}
