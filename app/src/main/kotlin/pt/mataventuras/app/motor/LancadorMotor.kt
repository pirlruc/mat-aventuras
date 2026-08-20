package pt.mataventuras.app.motor

import android.content.Context
import android.content.Intent
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.tipoMotorPara
import pt.mataventuras.dominio.modelo.TipoMotor

/**
 * Ponte nativa → motor. 2D no processo principal; 3D noutro processo (:motor3d).
 */
object LancadorMotor {
    const val EXTRA_MASCOTE: String = "mascote"
    const val EXTRA_NOME: String = "nome"
    const val RESULTADO_CONCLUIDO: String = "concluido"

    fun intentPara(contexto: Context, faixa: FaixaEtaria, mascote: Mascote, nome: String): Intent {
        val destino = when (tipoMotorPara(faixa)) {
            TipoMotor.BIDIMENSIONAL -> AtividadeMotor2D::class.java
            TipoMotor.TRIDIMENSIONAL -> AtividadeMotor3D::class.java
        }
        return Intent(contexto, destino).apply {
            putExtra(EXTRA_MASCOTE, mascote.codigo)
            putExtra(EXTRA_NOME, nome)
        }
    }
}
