package pt.mataventuras.dominio

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.dominio.matematica.Exercicio
import pt.mataventuras.dominio.matematica.GeradorExercicios
import pt.mataventuras.dominio.matematica.ResultadoTentativa
import pt.mataventuras.dominio.modelo.AvatarDesbloqueado
import pt.mataventuras.dominio.modelo.DistintivoDesbloqueado
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.motor.MotorPlataforma2D
import pt.mataventuras.dominio.progresso.CalculadoraClassificacao
import pt.mataventuras.dominio.progresso.CodigoDistintivo
import pt.mataventuras.dominio.progresso.MotorRecompensas
import pt.mataventuras.dominio.progresso.TotaisProgresso

class CoberturaExtraTeste {
    @Test
    fun classificacaoDesempatePorNome() {
        val ana = perfil(1, "Ana", 10)
        val zoe = perfil(2, "Zoe", 10)
        val tabela = CalculadoraClassificacao().classificar(listOf(zoe, ana), emptyList())
        assertEquals(listOf("Ana", "Zoe"), tabela.map { it.nome })
    }

    @Test
    fun distintivosSemPrimeiraSessao() {
        val novos =
            MotorRecompensas().distintivosNovos(
                emptySet(),
                TotaisProgresso(0, 10, 10, 20, true, MotorRecompensas.TRINTA_MINUTOS_MS),
            )
        assertFalse(novos.contains(CodigoDistintivo.PRIMEIROS_PASSOS))
        assertTrue(novos.contains(CodigoDistintivo.CONTADOR_ESTRELAS))
    }

    @Test
    fun opcoesComIntervaloUnico() {
        val opcoes = GeradorExercicios(Random(0)).opcoesNumericas(5, 5, 5)
        assertEquals(4, opcoes.size)
        assertTrue(5 in opcoes)
    }

    @Test
    fun modelosDeRecompensaEResultado() {
        val d = DistintivoDesbloqueado("X", 1)
        val a = AvatarDesbloqueado("Y", 2)
        val r = ResultadoTentativa(true, "Muito bem!")
        assertEquals("X", d.codigo)
        assertEquals("Y", a.avatarId)
        assertTrue(r.correcto)
    }

    @Test(expected = IllegalArgumentException::class)
    fun indiceNegativo() {
        Exercicio(ModuloAprendizagem.NUMEROS, "?", "?", listOf("1", "2"), -1)
    }

    @Test
    fun plataformaBuracoSaltoNoArEConcluido() {
        val motor = MotorPlataforma2D()
        var noAr = motor.passo(motor.inicial(), 0.05f, aSaltar = true)
        noAr = motor.passo(noAr, 0.05f, aSaltar = true)
        assertFalse(noAr.noChao)
        val pronto = motor.inicial(aneisAlvo = 1).copy(aneis = 1, concluido = false)
        val fechado = motor.passo(pronto, 0.01f, aSaltar = false)
        assertTrue(fechado.concluido)
        assertEquals(fechado, motor.recolher(fechado, 0f))
        var queda = motor.inicial().copy(x = MotorPlataforma2D.LIMITE_BURACO + 1f, noChao = true)
        repeat(30) { queda = motor.passo(queda, 0.05f, aSaltar = false) }
        assertFalse(queda.vivo)
        val curto = motor.recolher(motor.inicial(), anelX = 0.5f, raio = 0.1f)
        assertEquals(0, curto.aneis)
    }

    private fun perfil(
        id: Long,
        nome: String,
        pontos: Int,
    ) = PerfilCrianca(
        id = id,
        nome = nome,
        faixaEtaria = FaixaEtaria.TRES_ANOS,
        mascoteFavorito = Mascote.CAO_HEROI,
        avatarId = "INICIAL",
        pontos = pontos,
        criadoEmEpochMs = 0,
    )
}
