package pt.mataventuras.dominio.motor

/**
 * Estado simulável do side-scroller 2D (prémio dos 3 anos).
 */
data class EstadoPlataforma2D(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val noChao: Boolean,
    val aneis: Int,
    val aneisAlvo: Int,
    val vivo: Boolean,
    val concluido: Boolean,
)

/**
 * Física simples do side-scroller. Testável sem Android.
 */
class MotorPlataforma2D(
    private val chaoY: Float = 0f,
    private val gravidade: Float = -48f,
    private val salto: Float = 22f,
    private val velocidadeX: Float = 8f,
) {
    /**
     * Estado inicial parado no chão.
     */
    fun inicial(aneisAlvo: Int = 5): EstadoPlataforma2D =
        EstadoPlataforma2D(
            x = 0f,
            y = chaoY,
            vx = velocidadeX,
            vy = 0f,
            noChao = true,
            aneis = 0,
            aneisAlvo = aneisAlvo,
            vivo = true,
            concluido = false,
        )

    /**
     * Avança um passo de simulação em segundos.
     */
    fun passo(
        estado: EstadoPlataforma2D,
        dt: Float,
        aSaltar: Boolean,
    ): EstadoPlataforma2D {
        if (!estado.vivo || estado.concluido) return estado
        val vySalto = if (aSaltar && estado.noChao) salto else estado.vy + gravidade * dt
        var y = estado.y + vySalto * dt
        var noChao = false
        var vy = vySalto
        val buraco = estado.x > LIMITE_BURACO
        if (y <= chaoY && !buraco) {
            y = chaoY
            vy = 0f
            noChao = true
        }
        val x = estado.x + estado.vx * dt
        val vivo = y >= -2f
        val concluido = estado.aneis >= estado.aneisAlvo
        return estado.copy(x = x, y = y, vy = vy, noChao = noChao, vivo = vivo, concluido = concluido)
    }

    /**
     * Recolhe um anel se o jogador estiver perto de [anelX].
     */
    fun recolher(
        estado: EstadoPlataforma2D,
        anelX: Float,
        raio: Float = 1.2f,
    ): EstadoPlataforma2D {
        if (!estado.vivo || estado.concluido) return estado
        val distancia = kotlin.math.abs(estado.x - anelX)
        if (distancia > raio) return estado
        val aneis = estado.aneis + 1
        return estado.copy(aneis = aneis, concluido = aneis >= estado.aneisAlvo)
    }

    /** Distância a partir da qual o chão acaba (queda). */
    companion object {
        const val LIMITE_BURACO: Float = 30f
    }
}
