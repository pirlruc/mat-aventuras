package pt.mataventuras.app.engine

import android.opengl.GLES10
import java.nio.Buffer

/**
 * ES1 calls used by [KartRenderer]. Tests implement this without `GL10`.
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
 * Forwards [KartGles] to [GLES10] statics (current EGL context).
 */
internal object KartGlesAndroid : KartGles {
    override fun clearColor(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    ) {
        GLES10.glClearColor(r, g, b, a)
    }

    override fun enable(cap: Int) {
        GLES10.glEnable(cap)
    }

    override fun shadeModel(mode: Int) {
        GLES10.glShadeModel(mode)
    }

    override fun viewport(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        GLES10.glViewport(x, y, width, height)
    }

    override fun matrixMode(mode: Int) {
        GLES10.glMatrixMode(mode)
    }

    override fun loadIdentity() {
        GLES10.glLoadIdentity()
    }

    override fun loadMatrixf(
        m: FloatArray,
        offset: Int,
    ) {
        GLES10.glLoadMatrixf(m, offset)
    }

    override fun clear(mask: Int) {
        GLES10.glClear(mask)
    }

    override fun pushMatrix() {
        GLES10.glPushMatrix()
    }

    override fun popMatrix() {
        GLES10.glPopMatrix()
    }

    override fun translatef(
        x: Float,
        y: Float,
        z: Float,
    ) {
        GLES10.glTranslatef(x, y, z)
    }

    override fun rotatef(
        angle: Float,
        x: Float,
        y: Float,
        z: Float,
    ) {
        GLES10.glRotatef(angle, x, y, z)
    }

    override fun scalef(
        x: Float,
        y: Float,
        z: Float,
    ) {
        GLES10.glScalef(x, y, z)
    }

    override fun color4f(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    ) {
        GLES10.glColor4f(r, g, b, a)
    }

    override fun enableClientState(cap: Int) {
        GLES10.glEnableClientState(cap)
    }

    override fun disableClientState(cap: Int) {
        GLES10.glDisableClientState(cap)
    }

    override fun vertexPointer(
        size: Int,
        type: Int,
        stride: Int,
        pointer: Buffer,
    ) {
        GLES10.glVertexPointer(size, type, stride, pointer)
    }

    override fun drawArrays(
        mode: Int,
        first: Int,
        count: Int,
    ) {
        GLES10.glDrawArrays(mode, first, count)
    }
}
