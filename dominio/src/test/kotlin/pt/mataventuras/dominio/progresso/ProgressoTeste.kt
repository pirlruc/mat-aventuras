package pt.mataventuras.dominio.progresso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.modelo.SessaoAprendizagem

class ProgressoTeste {
    private val motor = MotorRecompensas()
    private val classificacao = CalculadoraClassificacao()

    @Test
    fun pontosNuncaNegativos() {
        assertEquals(10, motor.pontosDaTentativa(true))
        assertEquals(-2, motor.pontosDaTentativa(false))
        assertEquals(0, motor.aplicarPontos(1, -2))
        assertEquals(11, motor.aplicarPontos(1, 10))
    }

    @Test
    fun recompensaACadaTresAcertos() {
        assertFalse(motor.deveAbrirRecompensa(0))
        assertFalse(motor.deveAbrirRecompensa(2))
        assertTrue(motor.deveAbrirRecompensa(3))
        assertTrue(motor.deveAbrirRecompensa(6))
    }

    @Test
    fun distintivosEAvatares() {
        val novos =
            motor.distintivosNovos(
                jaDesbloqueados = emptySet(),
                totais =
                    TotaisProgresso(
                        sessoesCompletas = 1,
                        acertosContagem = 10,
                        acertosFormas = 10,
                        acertosContas = 20,
                        sessaoPerfeitaComMinimo = true,
                        tempoTotalMs = MotorRecompensas.TRINTA_MINUTOS_MS,
                    ),
            )
        assertEquals(CodigoDistintivo.entries.size, novos.size)
        val ja = setOf(CodigoDistintivo.PRIMEIROS_PASSOS.name)
        assertFalse(
            motor.distintivosNovos(
                ja,
                TotaisProgresso(1, 0, 0, 0, false, 0),
            ).contains(CodigoDistintivo.PRIMEIROS_PASSOS),
        )
        val avatares = motor.avataresNovos(emptySet(), 500)
        assertEquals(CodigoAvatar.entries.size, avatares.size)
        assertTrue(motor.avataresNovos(setOf(CodigoAvatar.INICIAL.name), 0).isEmpty())
    }

    @Test
    fun classificacaoOrdenaPontosDepoisPrecisao() {
        val a = perfil(1, "Ana", 100)
        val b = perfil(2, "Beto", 100)
        val c = perfil(3, "Cata", 50)
        val sessoes =
            listOf(
                sessao(1, 8, 2),
                sessao(2, 9, 1),
                sessao(3, 1, 1),
            )
        val tabela = classificacao.classificar(listOf(c, b, a), sessoes)
        assertEquals(listOf(2L, 1L, 3L), tabela.map { it.perfilId })
        assertEquals(1, tabela.first().posicao)
    }

    @Test
    fun classificacaoSemSessoes() {
        val tabela = classificacao.classificar(listOf(perfil(1, "Ana", 0)), emptyList())
        assertEquals(0.0, tabela.single().precisaoMedia, 0.0)
    }

    private fun perfil(
        id: Long,
        nome: String,
        pontos: Int,
    ) = PerfilCrianca(
        id = id,
        nome = nome,
        faixaEtaria = FaixaEtaria.SETE_ANOS,
        mascoteFavorito = Mascote.OURICO_VELOZ,
        avatarId = CodigoAvatar.INICIAL.name,
        pontos = pontos,
        criadoEmEpochMs = 0,
    )

    private fun sessao(
        perfilId: Long,
        acertos: Int,
        erros: Int,
    ) = SessaoAprendizagem(
        id = perfilId,
        perfilId = perfilId,
        modulo = ModuloAprendizagem.ADICAO,
        acertos = acertos,
        erros = erros,
        duracaoMs = 1_000,
        iniciadoEmEpochMs = 0,
    )
}
