package pt.mataventuras.domain.math

/**
 * Spoken cipher legends that name every symbol, not only the first two.
 */
object CipherSpeech {
    /**
     * Reads each `symbol=value` legend line, then [closer].
     */
    fun fromLegend(
        cells: List<String>,
        closer: String,
    ): String {
        val clauses = cells.map { clause(it) }
        return (clauses + closer).joinToString(". ")
    }

    /**
     * Age-3 counting codes. One symbol stays the short star line.
     */
    fun counting(kinds: Int): String =
        if (kinds <= 1) {
            "Cada estrela vale um. Quantas são?"
        } else {
            fromLegend(listOf("⭐=1", "●=2", "■=3").take(kinds.coerceAtMost(3)), "Que número é o código?")
        }

    private fun clause(line: String): String {
        val parts = line.split("=")
        val name = NAMES[parts[0]] ?: "Símbolo"
        val value = parts.getOrElse(1) { "?" }
        return "$name vale $value"
    }

    private val NAMES: Map<String, String> =
        mapOf(
            "▲" to "Triângulo",
            "●" to "Círculo",
            "■" to "Quadrado",
            "⭐" to "Estrela",
            "◆" to "Losango",
            "★" to "Estrela",
            "♥" to "Coração",
        )
}
