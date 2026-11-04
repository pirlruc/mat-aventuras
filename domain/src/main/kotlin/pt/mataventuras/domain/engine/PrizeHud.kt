package pt.mataventuras.domain.engine

/**
 * Prize copy shared by the Godot plugin and the Compose win card.
 * Ages 3–5 see hearts. Ages 6–8 see a vidas count.
 */
object PrizeHud {
    /** One remaining life on the young HUD. */
    const val HEART: String = "♥"

    /**
     * Life line. [young] replaces the word "Vidas" with [HEART]s.
     */
    fun livesLine(
        young: Boolean,
        current: Int,
        maximum: Int,
    ): String {
        if (young && current <= 0) return "·"
        if (young) return HEART.repeat(current.coerceAtLeast(0))
        return "Vidas $current/$maximum"
    }

    /** Full-screen win title. */
    const val WIN_TITLE: String = "Ganhaste!"

    /** Dismisses the win card. */
    const val WIN_CONTINUE: String = "Continuar"

    /** Mascot overlay while the engine starts. */
    const val LOADING_LINE: String = "A preparar o prémio…"

    /** Shown with [BACK_LABEL] after the boot timeout. */
    const val RETRY_LABEL: String = "Tentar outra vez"

    /** Leaves the prize and returns to the lesson. */
    const val BACK_LABEL: String = "Voltar"

    /** Starts the prize after the spoken introduction. */
    const val PLAY_LABEL: String = "Jogar"

    /** Status line once the boot timer has fired. */
    const val TIMEOUT_LINE: String = "O prémio demorou."
}
