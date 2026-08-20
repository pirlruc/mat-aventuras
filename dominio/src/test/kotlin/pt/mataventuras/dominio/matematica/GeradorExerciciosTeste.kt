package pt.mataventuras.dominio.matematica

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.dominio.modelo.ModuloAprendizagem

class GeradorExerciciosTeste {
    private val gerador = GeradorExercicios(Random(42))

    @Test
    fun gerarCobreTodosOsModulos() {
        ModuloAprendizagem.entries.forEach { modulo ->
            val exercicio = gerador.gerar(modulo)
            assertEquals(modulo, exercicio.modulo)
            assertEquals(4, exercicio.opcoes.size)
            assertTrue(exercicio.estaCorrecto(exercicio.indiceCorreto))
            assertFalse(exercicio.estaCorrecto((exercicio.indiceCorreto + 1) % 4))
        }
    }

    @Test
    fun contagemTemEstrelasVisiveis() {
        val exercicio = gerador.gerarContagem()
        assertTrue(exercicio.quantidadeVisual in 1..10)
        assertEquals(exercicio.quantidadeVisual.toString(), exercicio.opcoes[exercicio.indiceCorreto])
    }

    @Test
    fun adicaoSomaOpcaoCorrecta() {
        val exercicio = gerador.gerarAdicao()
        val partes = exercicio.pergunta.replace(" = ?", "").split(" + ")
        val soma = partes[0].toInt() + partes[1].toInt()
        assertEquals(soma.toString(), exercicio.opcoes[exercicio.indiceCorreto])
    }

    @Test
    fun subtracaoNuncaNegativa() {
        repeat(20) {
            val exercicio = GeradorExercicios(Random(it)).gerarSubtracao()
            val valor = exercicio.opcoes[exercicio.indiceCorreto].toInt()
            assertTrue(valor >= 0)
        }
    }

    @Test
    fun logicaGeraSequenciaOuMaior() {
        val a = GeradorExercicios(Random(1)).gerarLogica()
        val b = GeradorExercicios(Random(2)).gerarLogica()
        assertEquals(ModuloAprendizagem.LOGICA, a.modulo)
        assertEquals(ModuloAprendizagem.LOGICA, b.modulo)
        assertTrue(a.pergunta.contains("Completa") || a.pergunta.contains("maior"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun exercicioSemOpcoesFalha() {
        Exercicio(ModuloAprendizagem.NUMEROS, "?", "?", emptyList(), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun indiceForaFalha() {
        Exercicio(ModuloAprendizagem.NUMEROS, "?", "?", listOf("1"), 4)
    }

    @Test
    fun formaTemAlvo() {
        val exercicio = gerador.gerarForma()
        assertTrue(exercicio.formaAlvo != null)
        assertEquals(exercicio.formaAlvo!!.nomeVisivel, exercicio.opcoes[exercicio.indiceCorreto])
    }

    @Test
    fun numeroZeroANove() {
        val exercicio = gerador.gerarNumero()
        val n = exercicio.opcoes[exercicio.indiceCorreto].toInt()
        assertTrue(n in 0..9)
    }

    @Test
    fun multiplicacaoProdutoCorrecto() {
        val exercicio = gerador.gerarMultiplicacao()
        val partes = exercicio.pergunta.replace(" = ?", "").split(" × ")
        assertEquals((partes[0].toInt() * partes[1].toInt()).toString(), exercicio.opcoes[exercicio.indiceCorreto])
    }
}
