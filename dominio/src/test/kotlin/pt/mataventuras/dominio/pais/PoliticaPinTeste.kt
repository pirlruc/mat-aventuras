package pt.mataventuras.dominio.pais

import java.security.SecureRandom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoliticaPinTeste {
    private val agora = mutableListOf(1_000_000L)
    private val politica =
        PoliticaPin(
            random = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(7) },
            agora = { agora.last() },
            iteracoes = 1_000,
        )

    @Test
    fun pinCorrecto() {
        val estado = politica.criar("1234")
        val (resultado, novo) = politica.tentar(estado, "1234")
        assertEquals(ResultadoPin.Correcto, resultado)
        assertEquals(0, novo.falhasSeguidas)
    }

    @Test
    fun pinIncorrectoContaFalhas() {
        val estado = politica.criar("1234")
        val (resultado, novo) = politica.tentar(estado, "0000")
        assertTrue(resultado is ResultadoPin.Incorrecto)
        assertEquals(PoliticaPin.MAX_FALHAS - 1, (resultado as ResultadoPin.Incorrecto).restantes)
        assertEquals(1, novo.falhasSeguidas)
    }

    @Test
    fun bloqueiaAposMaximo() {
        var estado = politica.criar("1234")
        repeat(PoliticaPin.MAX_FALHAS) {
            val par = politica.tentar(estado, "9999")
            estado = par.second
        }
        assertTrue(estado.bloqueadoAteEpochMs > 0L)
        val (resultado, _) = politica.tentar(estado, "1234")
        assertTrue(resultado is ResultadoPin.Bloqueado)
    }

    @Test
    fun formatoInvalido() {
        assertFalse(politica.formatoValido("12"))
        assertFalse(politica.formatoValido("abcd"))
        val estado = politica.criar("1234")
        val (resultado, mesmo) = politica.tentar(estado, "12ab")
        assertEquals(ResultadoPin.FormatoInvalido, resultado)
        assertEquals(estado, mesmo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun criarRejeitaFormato() {
        politica.criar("1")
    }

    @Test
    fun hexRedondo() {
        val bytes = byteArrayOf(0x0f, 0x10, 0xff.toByte())
        assertEquals("0f10ff", paraHex(bytes))
        assertTrue(tempoConstanteIguais(bytes, deHex("0f10ff")))
        assertFalse(tempoConstanteIguais(bytes, byteArrayOf(1)))
        assertFalse(tempoConstanteIguais(bytes, deHex("0f10fe")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun hexImparFalha() {
        deHex("abc")
    }

    @Test
    fun desbloqueiaDepoisDoTempo() {
        var estado = politica.criar("4321")
        repeat(PoliticaPin.MAX_FALHAS) {
            estado = politica.tentar(estado, "0000").second
        }
        agora += estado.bloqueadoAteEpochMs + 1
        val (resultado, _) = politica.tentar(estado, "4321")
        assertEquals(ResultadoPin.Correcto, resultado)
    }
}
