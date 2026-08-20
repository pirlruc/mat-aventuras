package pt.mataventuras.dominio.modelo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeloTeste {
    @Test
    fun tokensTresAnosSaoMaiores() {
        val tres = tokensPara(FaixaEtaria.TRES_ANOS)
        val sete = tokensPara(FaixaEtaria.SETE_ANOS)
        assertTrue(tres.tamanhoBotaoMinDp > sete.tamanhoBotaoMinDp)
        assertFalse(tres.usaNavegacaoComTexto)
        assertTrue(sete.usaNavegacaoComTexto)
        assertTrue(sete.usaConfirmacaoAntesDeSair)
    }

    @Test
    fun mascoteDeCodigoDesconhecidoUsaOurico() {
        assertEquals(Mascote.OURICO_VELOZ, Mascote.deCodigo("desconhecido"))
        assertEquals(Mascote.PORQUINHO_ROSA, Mascote.deCodigo("porquinho_rosa"))
    }

    @Test
    fun mascotesPorModulo() {
        assertEquals(Mascote.OURICO_VELOZ, mascoteParaModulo(ModuloAprendizagem.CONTAGEM))
        assertEquals(Mascote.PORQUINHO_ROSA, mascoteParaModulo(ModuloAprendizagem.FORMAS))
        assertEquals(Mascote.CAO_HEROI, mascoteParaModulo(ModuloAprendizagem.NUMEROS))
        assertEquals(Mascote.CANALIZADOR_VALENTE, mascoteParaModulo(ModuloAprendizagem.ADICAO))
        assertEquals(Mascote.CANALIZADOR_VALENTE, mascoteParaModulo(ModuloAprendizagem.SUBTRACAO))
        assertEquals(Mascote.EXTRATERRESTRE_TRAVESSO, mascoteParaModulo(ModuloAprendizagem.MULTIPLICACAO))
        assertEquals(Mascote.CAO_HEROI, mascoteParaModulo(ModuloAprendizagem.LOGICA))
    }

    @Test
    fun modulosPorFaixa() {
        assertEquals(3, modulosPara(FaixaEtaria.TRES_ANOS).size)
        assertEquals(4, modulosPara(FaixaEtaria.SETE_ANOS).size)
        assertTrue(ModuloAprendizagem.CONTAGEM in modulosPara(FaixaEtaria.TRES_ANOS))
        assertTrue(ModuloAprendizagem.MULTIPLICACAO in modulosPara(FaixaEtaria.SETE_ANOS))
    }

    @Test
    fun tipoMotorPorFaixa() {
        assertEquals(TipoMotor.BIDIMENSIONAL, tipoMotorPara(FaixaEtaria.TRES_ANOS))
        assertEquals(TipoMotor.TRIDIMENSIONAL, tipoMotorPara(FaixaEtaria.SETE_ANOS))
    }

    @Test
    fun precisaoSessao() {
        val vazia = SessaoAprendizagem(1, 1, ModuloAprendizagem.ADICAO, 0, 0, 0, 0)
        assertEquals(0.0, vazia.precisao(), 0.0)
        val mista = vazia.copy(acertos = 3, erros = 1)
        assertEquals(0.75, mista.precisao(), 0.0)
    }

    @Test
    fun nomesFormasEmPtPt() {
        assertEquals("rectângulo", FormaGeometrica.RETANGULO.nomeVisivel)
        assertEquals("triângulo", FormaGeometrica.TRIANGULO.nomeVisivel)
    }
}
