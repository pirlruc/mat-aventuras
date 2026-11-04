package pt.mataventuras.domain.engine

/**
 * Goal and controls spoken before a prize starts. Keys are scene or Compose prize names.
 */
object PrizeBrief {
    private val goals: Map<String, String> =
        mapOf(
            "RUNNER" to "Corre e apanha os anéis.",
            "CLIMB" to "Sobe e apanha as letras.",
            "CHOMP" to "Come as bolinhas e foge.",
            "INVADERS" to "Desvia das naves.",
            "KART" to "Passa por baixo da meta.",
            "BALLOONS" to "Rebenta os balões.",
            "FISHING" to "Apanha os peixes.",
            "COLOURING" to "Pinta o desenho.",
            "BUBBLES" to "Rebenta as bolhas no ritmo.",
            "SNAKE" to "Apanha a resposta certa.",
            "BREAKOUT" to "Parte os tijolos.",
            "TOWER" to "Empilha as peças.",
        )

    private val controls: Map<String, String> =
        mapOf(
            "RUNNER" to "Setas para andar. Saltar para o ar.",
            "CLIMB" to "Setas para andar. Saltar para subir.",
            "CHOMP" to "Desliza para virar.",
            "INVADERS" to "Desliza para desviar.",
            "KART" to "Esquerda, direita e Impulso.",
            "BALLOONS" to "Toca num balão.",
            "FISHING" to "Toca no peixe.",
            "COLOURING" to "Toca numa cor e depois no desenho.",
            "BUBBLES" to "Toca nas bolhas.",
            "SNAKE" to "Desliza para virar.",
            "BREAKOUT" to "Desliza para mover a barra.",
            "TOWER" to "Toca para largar a peça.",
        )

    /**
     * One sentence that says what winning looks like.
     */
    fun goal(scene: String): String = goals[scene.uppercase()] ?: "Joga até ao fim."

    /**
     * One sentence that says which buttons to use.
     */
    fun controls(scene: String): String = controls[scene.uppercase()] ?: "Toca em Jogar."
}
