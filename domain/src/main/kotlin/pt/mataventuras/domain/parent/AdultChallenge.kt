package pt.mataventuras.domain.parent

import kotlin.random.Random

/**
 * A product a young child is unlikely to answer. The parent gate shows it
 * before the first PIN and again when the Keystore record must be reset.
 */
data class AdultChallenge(
    val left: Int,
    val right: Int,
) {
    /** Two-digit times one-digit. */
    val product: Int
        get() = left * right

    /** On-screen question. */
    val prompt: String
        get() = "Quanto é $left × $right?"

    companion object {
        /**
         * [left] is 10..99 and [right] is 2..9, so the product is not a sum a
         * child in this app already drills.
         */
        fun draw(random: Random = Random.Default): AdultChallenge =
            AdultChallenge(left = random.nextInt(10, 100), right = random.nextInt(2, 10))
    }
}
