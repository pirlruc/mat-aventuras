package pt.mataventuras.dominio.motor

/**
 * Estado simulável da corrida 3D (prémio dos 7 anos).
 */
data class EstadoKart3D(
    val posicaoPista: Float,
    val velocidade: Float,
    val combustivelRespostas: Int,
    val voltas: Int,
    val voltasAlvo: Int,
    val concluido: Boolean,
)

/**
 * Kart simplificado: respostas certas aumentam a velocidade; três voltas ganham.
 */
class MotorKart3D(
    private val aceleracao: Float = 12f,
    private val atrito: Float = 4f,
    private val comprimentoVolta: Float = 40f,
) {
    /**
     * Coloca o kart no início da pista.
     */
    fun inicial(voltasAlvo: Int = 3): EstadoKart3D =
        EstadoKart3D(
            posicaoPista = 0f,
            velocidade = 4f,
            combustivelRespostas = 0,
            voltas = 0,
            voltasAlvo = voltasAlvo,
            concluido = false,
        )

    /**
     * Avança a simulação. [impulso] é verdadeiro após uma conta correcta.
     */
    fun passo(
        estado: EstadoKart3D,
        dt: Float,
        impulso: Boolean,
    ): EstadoKart3D {
        if (estado.concluido) return estado
        val extra = if (impulso) aceleracao else 0f
        val velocidade = (estado.velocidade + extra * dt - atrito * dt).coerceAtLeast(2f)
        var posicao = estado.posicaoPista + velocidade * dt
        var voltas = estado.voltas
        if (posicao >= comprimentoVolta) {
            posicao -= comprimentoVolta
            voltas += 1
        }
        val combustivel = if (impulso) estado.combustivelRespostas + 1 else estado.combustivelRespostas
        val concluido = voltas >= estado.voltasAlvo
        return estado.copy(
            posicaoPista = posicao,
            velocidade = velocidade,
            combustivelRespostas = combustivel,
            voltas = voltas,
            concluido = concluido,
        )
    }
}
