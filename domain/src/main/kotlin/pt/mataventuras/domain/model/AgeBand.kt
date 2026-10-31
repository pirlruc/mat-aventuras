package pt.mataventuras.domain.model

/**
 * Child age band. Drives UI tokens, curriculum, and which reward engine launches.
 * Stored names replace the earlier fixed ages `THREE_YEARS` and `SEVEN_YEARS`.
 */
enum class AgeBand {
    /** 3 to 5 years. */
    THREE_TO_FIVE,

    /** 6 to 8 years. */
    SIX_TO_EIGHT,
}

/**
 * Logical UI tokens applied by Compose. Sizes are dp/sp.
 */
data class UiTokens(
    val minButtonDp: Int,
    val titleSp: Int,
    val bodySp: Int,
    val usesTextNavigation: Boolean,
    val confirmsBeforeExit: Boolean,
)

/**
 * Tokens for the selected age band.
 */
fun tokensFor(age: AgeBand): UiTokens =
    when (age) {
        AgeBand.THREE_TO_FIVE ->
            UiTokens(
                minButtonDp = 88,
                titleSp = 34,
                bodySp = 22,
                usesTextNavigation = false,
                confirmsBeforeExit = false,
            )
        AgeBand.SIX_TO_EIGHT ->
            UiTokens(
                minButtonDp = 56,
                titleSp = 26,
                bodySp = 18,
                usesTextNavigation = true,
                confirmsBeforeExit = true,
            )
    }
