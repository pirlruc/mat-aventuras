package pt.mataventuras.domain.voice

import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot

/**
 * Spoken and on-screen copy in Portuguese from Portugal. Identifiers are English.
 */
object VoiceScripts {
    const val APP_TITLE: String = "Mat Aventuras"
    const val APP_TAGLINE: String = "Números, formas e jogos."
    const val APP_DESCRIPTION: String =
        "Uma aventura de matemática para os 3 e os 7 anos. Tudo fica neste aparelho."
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
    const val JUMP_HINT: String = "Toca para saltar. Apanha as moedas!"
    const val LEAVE: String = "Sair"
    const val CONFIRM_LEAVE: String = "Sim, sair"
    const val STAY: String = "Ficar"
    const val SWITCH_PROFILE: String = "Outra criança"
    const val START: String = "Vamos começar!"
    const val YOUR_NAME: String = "Como te chamas?"
    const val CHOOSE_FRIEND: String = "Escolhe o teu amigo:"
    const val AGE_THREE_PREVIEW: String =
        "Contar, formas e números. Sopa, puzzle e códigos. Prémio: salta e apanha as moedas!"
    const val AGE_SEVEN_PREVIEW: String =
        "Somar, subtrair, multiplicar e lógica. Sudoku, sopa e códigos. Prémio: plataformas ou kart!"
    const val REWARD_FINISHED: String = "Boa! Ganhaste um prémio. Vamos continuar."
    const val REWARD_RETURN: String = "Boa! Vamos continuar."

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

    /**
     * Short curriculum + reward blurb for the selected age band.
     */
    fun agePreview(age: AgeGroup): String =
        when (age) {
            AgeGroup.THREE_YEARS -> AGE_THREE_PREVIEW
            AgeGroup.SEVEN_YEARS -> AGE_SEVEN_PREVIEW
        }

    /**
     * Resume shortcut when a profile already exists on the device.
     */
    fun continueAs(name: String): String = "Continuar como $name"

    /**
     * Spoken line after a reward Activity returns.
     */
    fun rewardReturn(finished: Boolean): String = if (finished) REWARD_FINISHED else REWARD_RETURN
}
