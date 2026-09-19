package pt.mataventuras.domain.math

/**
 * Portuguese number-words shared by letter soup and symbol ciphers.
 */
object PortugueseNumberWords {
    /** Words that fit a soup grid (length 3+). */
    val SOUP: List<String> =
        listOf(
            "dez",
            "dois",
            "três",
            "seis",
            "sete",
            "oito",
            "nove",
            "soma",
            "cinco",
            "vinte",
            "treze",
            "quatro",
            "quinze",
            "trinta",
        )

    /** Cipher answers are at least four letters so the legend is a real word. */
    val CIPHER: List<String> = SOUP.filter { it.length >= 4 }
}
