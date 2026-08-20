package pt.mataventuras.dominio.voz

import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote

/**
 * Guiões de voz e UI em português de Portugal. Comentários de diálogo também em pt-PT.
 */
object GuioesVoz {
    /** Diálogo: «Olá! Escolhe a tua idade.» */
    const val SELECAO_IDADE: String = "Olá! Escolhe a tua idade."

    /** Diálogo: «Três anos. Botões grandes e muita voz.» */
    const val TRES_ANOS: String = "Três anos"

    /** Diálogo: «Sete anos. Mais desafios e texto.» */
    const val SETE_ANOS: String = "Sete anos"

    const val MUITO_BEM: String = "Muito bem!"
    const val TENTA_OUTRA_VEZ: String = "Tenta outra vez."
    const val VAMOS_JOGAR: String = "Vamos jogar um prémio!"
    const val CLASSIFICACAO: String = "Classificação"
    const val RECOMPENSAS: String = "Recompensas"
    const val PAINEL_PAIS: String = "Painel dos pais"
    const val INTRODUZ_PIN: String = "Introduz o PIN dos pais."
    const val DEFINE_PIN: String = "Escolhe um PIN de quatro números."
    const val PIN_ERRADO: String = "PIN incorrecto."
    const val PIN_BLOQUEADO: String = "Demasiadas tentativas. Espera um minuto."
    const val SEM_INTERNET: String = "Tudo fica neste aparelho. Não enviamos dados."

    /**
     * Saudação do mascote anfitrião.
     */
    fun saudacao(
        mascote: Mascote,
        faixa: FaixaEtaria,
    ): String {
        val nome = mascote.nomeVisivel
        return when (faixa) {
            FaixaEtaria.TRES_ANOS -> "Olá! Eu sou o $nome. Vamos brincar com números?"
            FaixaEtaria.SETE_ANOS -> "Olá, eu sou o $nome. Pronto para um desafio de matemática?"
        }
    }

    /**
     * Pedido de confirmação ao sair, só para sete anos.
     */
    fun confirmarSaida(faixa: FaixaEtaria): String? =
        when (faixa) {
            FaixaEtaria.TRES_ANOS -> null
            FaixaEtaria.SETE_ANOS -> "Queres sair deste desafio?"
        }
}
