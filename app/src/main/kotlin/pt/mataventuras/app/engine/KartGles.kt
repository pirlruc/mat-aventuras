package pt.mataventuras.app.engine

import java.nio.Buffer
import javax.microedition.khronos.opengles.GL10

/**
 * ES1 calls used by [KartRenderer]. Tests implement this without loading `GL10`.
 */
internal interface KartGles {
    /** `glClearColor`. */
    fun clearColor(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    )

    /** `glEnable`. */
    fun enable(cap: Int)

    /** `glShadeModel`. */
    fun shadeModel(mode: Int)

    /** `glViewport`. */
    fun viewport(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    )

    /** `glMatrixMode`. */
    fun matrixMode(mode: Int)

    /** `glLoadIdentity`. */
    fun loadIdentity()

    /** `glLoadMatrixf`. */
    fun loadMatrixf(
        m: FloatArray,
        offset: Int,
    )

    /** `glClear`. */
    fun clear(mask: Int)

    /** `glPushMatrix`. */
    fun pushMatrix()

    /** `glPopMatrix`. */
    fun popMatrix()

    /** `glTranslatef`. */
    fun translatef(
        x: Float,
        y: Float,
        z: Float,
    )

    /** `glRotatef`. */
    fun rotatef(
        angle: Float,
        x: Float,
        y: Float,
        z: Float,
    )

    /** `glScalef`. */
    fun scalef(
        x: Float,
        y: Float,
        z: Float,
    )

    /** `glColor4f`. */
    fun color4f(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    )

    /** `glEnableClientState`. */
    fun enableClientState(cap: Int)

    /** `glDisableClientState`. */
    fun disableClientState(cap: Int)

    /** `glVertexPointer`. */
    fun vertexPointer(
        size: Int,
        type: Int,
        stride: Int,
        pointer: Buffer,
    )

    /** `glDrawArrays`. */
    fun drawArrays(
        mode: Int,
        first: Int,
        count: Int,
    )
}

/**
 * Forwards [KartGles] to a real ES1 context.
 */
internal class KartGlesEs1(
    private val gl: GL10,
) : KartGles {
    override fun clearColor(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    ) {
        gl.glClearColor(r, g, b, a)
    }

    override fun enable(cap: Int) {
        gl.glEnable(cap)
    }

    override fun shadeModel(mode: Int) {
        gl.glShadeModel(mode)
    }

    override fun viewport(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        gl.glViewport(x, y, width, height)
    }

    override fun matrixMode(mode: Int) {
        gl.glMatrixMode(mode)
    }

    override fun loadIdentity() {
        gl.glLoadIdentity()
    }

    override fun loadMatrixf(
        m: FloatArray,
        offset: Int,
    ) {
        gl.glLoadMatrixf(m, offset)
    }

    override fun clear(mask: Int) {
        gl.glClear(mask)
    }

    override fun pushMatrix() {
        gl.glPushMatrix()
    }

    override fun popMatrix() {
        gl.glPopMatrix()
    }

    override fun translatef(
        x: Float,
        y: Float,
        z: Float,
    ) {
        gl.glTranslatef(x, y, z)
    }

    override fun rotatef(
        angle: Float,
        x: Float,
        y: Float,
        z: Float,
    ) {
        gl.glRotatef(angle, x, y, z)
    }

    override fun scalef(
        x: Float,
        y: Float,
        z: Float,
    ) {
        gl.glScalef(x, y, z)
    }

    override fun color4f(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    ) {
        gl.glColor4f(r, g, b, a)
    }

    override fun enableClientState(cap: Int) {
        gl.glEnableClientState(cap)
    }

    override fun disableClientState(cap: Int) {
        gl.glDisableClientState(cap)
    }

    override fun vertexPointer(
        size: Int,
        type: Int,
        stride: Int,
        pointer: Buffer,
    ) {
        gl.glVertexPointer(size, type, stride, pointer)
    }

    override fun drawArrays(
        mode: Int,
        first: Int,
        count: Int,
    ) {
        gl.glDrawArrays(mode, first, count)
    }
}
