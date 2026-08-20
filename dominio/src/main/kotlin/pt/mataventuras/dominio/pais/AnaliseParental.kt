package pt.mataventuras.dominio.pais

import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.SessaoAprendizagem

/**
 * Resumo apresentado no painel dos pais.
 */
data class ResumoParental(
    val perfilId: Long,
    val tempoTotalMs: Long,
    val acertos: Int,
    val erros: Int,
    val precisao: Double,
    val porModulo: List<DesempenhoModulo>,
    val areasAMelhorar: List<ModuloAprendizagem>,
)

/**
 * Desempenho agregado de um módulo.
 */
data class DesempenhoModulo(
    val modulo: ModuloAprendizagem,
    val acertos: Int,
    val erros: Int,
    val precisao: Double,
    val tempoMs: Long,
)

/**
 * Agrega sessões num resumo parental.
 */
class AnaliseParental {
    /**
     * Constrói o resumo de um perfil.
     */
    fun resumir(
        perfilId: Long,
        sessoes: List<SessaoAprendizagem>,
    ): ResumoParental {
        val doPerfil = sessoes.filter { it.perfilId == perfilId }
        val acertos = doPerfil.sumOf { it.acertos }
        val erros = doPerfil.sumOf { it.erros }
        val tempo = doPerfil.sumOf { it.duracaoMs }
        val porModulo =
            doPerfil.groupBy { it.modulo }.map { (modulo, lista) ->
                val a = lista.sumOf { it.acertos }
                val e = lista.sumOf { it.erros }
                DesempenhoModulo(
                    modulo = modulo,
                    acertos = a,
                    erros = e,
                    precisao = precisao(a, e),
                    tempoMs = lista.sumOf { it.duracaoMs },
                )
            }.sortedBy { it.modulo.name }
        val aMelhorar =
            porModulo
                .filter { (it.acertos + it.erros) >= MINIMO_AMOSTRAS && it.precisao < LIMIAR_MELHORIA }
                .sortedBy { it.precisao }
                .map { it.modulo }
        return ResumoParental(
            perfilId = perfilId,
            tempoTotalMs = tempo,
            acertos = acertos,
            erros = erros,
            precisao = precisao(acertos, erros),
            porModulo = porModulo,
            areasAMelhorar = aMelhorar,
        )
    }

    private fun precisao(
        acertos: Int,
        erros: Int,
    ): Double {
        val total = acertos + erros
        if (total == 0) return 0.0
        return acertos.toDouble() / total.toDouble()
    }

    /** Limiares do painel dos pais. */
    companion object {
        const val LIMIAR_MELHORIA: Double = 0.7
        const val MINIMO_AMOSTRAS: Int = 5
    }
}
