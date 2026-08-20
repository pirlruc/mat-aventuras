package pt.mataventuras.dominio.pais

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.SessaoAprendizagem

class AnaliseParentalTeste {
    private val analise = AnaliseParental()

    @Test
    fun ignoraOutrosPerfis() {
        val sessoes =
            listOf(
                sessao(1, ModuloAprendizagem.ADICAO, 2, 0, 1_000),
                sessao(2, ModuloAprendizagem.ADICAO, 50, 50, 9_000),
            )
        val resumo = analise.resumir(1, sessoes)
        assertEquals(2, resumo.acertos)
        assertEquals(0, resumo.erros)
        assertEquals(1_000L, resumo.tempoTotalMs)
        assertEquals(1.0, resumo.precisao, 0.0)
        assertTrue(resumo.areasAMelhorar.isEmpty())
    }

    @Test
    fun identificaAreaAMelhorar() {
        val sessoes =
            List(5) {
                sessao(1, ModuloAprendizagem.MULTIPLICACAO, 1, 4, 500)
            } + listOf(sessao(1, ModuloAprendizagem.ADICAO, 10, 0, 200))
        val resumo = analise.resumir(1, sessoes)
        assertEquals(listOf(ModuloAprendizagem.MULTIPLICACAO), resumo.areasAMelhorar)
        assertEquals(2, resumo.porModulo.size)
    }

    @Test
    fun semSessoes() {
        val resumo = analise.resumir(9, emptyList())
        assertEquals(0.0, resumo.precisao, 0.0)
        assertTrue(resumo.porModulo.isEmpty())
    }

    private fun sessao(
        perfilId: Long,
        modulo: ModuloAprendizagem,
        acertos: Int,
        erros: Int,
        duracao: Long,
    ) = SessaoAprendizagem(
        id = perfilId + modulo.ordinal.toLong(),
        perfilId = perfilId,
        modulo = modulo,
        acertos = acertos,
        erros = erros,
        duracaoMs = duracao,
        iniciadoEmEpochMs = 0,
    )
}
