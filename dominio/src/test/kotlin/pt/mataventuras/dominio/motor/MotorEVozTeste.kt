package pt.mataventuras.dominio.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.voz.GuioesVoz

class MotorEVozTeste {
    @Test
    fun plataformaSaltaERecolhe() {
        val motor = MotorPlataforma2D()
        var estado = motor.inicial(aneisAlvo = 1)
        estado = motor.passo(estado, 0.1f, aSaltar = true)
        assertFalse(estado.noChao)
        estado = motor.recolher(estado, anelX = estado.x)
        assertTrue(estado.concluido)
        val parado = motor.passo(estado, 0.1f, aSaltar = false)
        assertEquals(estado, parado)
        val longe = motor.recolher(motor.inicial(1), anelX = 50f)
        assertEquals(0, longe.aneis)
        val morto = motor.inicial().copy(vivo = false)
        assertEquals(morto, motor.passo(morto, 0.1f, aSaltar = true))
        assertEquals(morto, motor.recolher(morto, 0f))
    }

    @Test
    fun plataformaCaiNoChao() {
        val motor = MotorPlataforma2D()
        var estado = motor.inicial()
        estado = motor.passo(estado, 0.05f, aSaltar = true)
        repeat(40) {
            estado = motor.passo(estado, 0.05f, aSaltar = false)
        }
        assertTrue(estado.noChao)
        assertTrue(estado.vivo)
    }

    @Test
    fun kartCompletaVoltasComImpulso() {
        val motor = MotorKart3D(aceleracao = 80f, atrito = 0f, comprimentoVolta = 10f)
        var estado = motor.inicial(voltasAlvo = 1)
        repeat(20) {
            estado = motor.passo(estado, 0.2f, impulso = true)
        }
        assertTrue(estado.concluido)
        assertEquals(estado, motor.passo(estado, 0.1f, impulso = false))
    }

    @Test
    fun kartSemImpulsoContinuaLento() {
        val motor = MotorKart3D()
        val depois = motor.passo(motor.inicial(), 0.1f, impulso = false)
        assertFalse(depois.concluido)
        assertEquals(0, depois.combustivelRespostas)
    }

    @Test
    fun guioesPtPt() {
        assertTrue(GuioesVoz.SELECAO_IDADE.contains("Escolhe"))
        assertTrue(GuioesVoz.saudacao(Mascote.OURICO_VELOZ, FaixaEtaria.TRES_ANOS).contains("brincar"))
        assertTrue(GuioesVoz.saudacao(Mascote.CAO_HEROI, FaixaEtaria.SETE_ANOS).contains("desafio"))
        assertEquals(null, GuioesVoz.confirmarSaida(FaixaEtaria.TRES_ANOS))
        assertTrue(GuioesVoz.confirmarSaida(FaixaEtaria.SETE_ANOS)!!.contains("sair"))
    }
}
