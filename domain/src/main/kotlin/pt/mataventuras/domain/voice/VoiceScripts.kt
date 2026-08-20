package pt.mataventuras.domain.voice

import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot

/**
 * Spoken and on-screen copy in Portuguese from Portugal. Identifiers are English.
 */
object VoiceScripts {
    const val AGE_SELECTION: String = "Olá! Escolhe a tua idade."
    const val THREE_YEARS: String = "Três anos"
    const val SEVEN_YEARS: String = "Sete anos"
    const val WELL_DONE: String = "Muito bem!"
    const val TRY_AGAIN: String = "Tenta outra vez."
    const val LETS_PLAY: String = "Vamos jogar um prémio!"
    const val LEADERBOARD: String = "Classificação"
    const val REWARDS: String = "Recompensas"
    const val PARENT_DASHBOARD: String = "Painel dos pais"
    const val ENTER_PIN: String = "Introduz o PIN dos pais."
    const val SET_PIN: String = "Escolhe um PIN de quatro números."
    const val WRONG_PIN: String = "PIN incorrecto."
    const val PIN_LOCKED: String = "Demasiadas tentativas. Espera um minuto."
    const val STAYS_ON_DEVICE: String = "Tudo fica neste aparelho. Não enviamos dados."
    const val STEER_HINT: String = "Esquerda e direita para guiar. No meio: impulso!"

    /**
     * Host mascot greeting (pt-PT).
     */
    fun greeting(
        mascot: Mascot,
        age: AgeGroup,
    ): String {
        val name = mascot.displayName
        return when (age) {
            AgeGroup.THREE_YEARS -> "Olá! Eu sou o $name. Vamos brincar com números?"
            AgeGroup.SEVEN_YEARS -> "Olá, eu sou o $name. Pronto para um desafio de matemática?"
        }
    }

    /**
     * Exit confirmation, age 7 only.
     */
    fun confirmExit(age: AgeGroup): String? =
        when (age) {
            AgeGroup.THREE_YEARS -> null
            AgeGroup.SEVEN_YEARS -> "Queres sair deste desafio?"
        }
}
