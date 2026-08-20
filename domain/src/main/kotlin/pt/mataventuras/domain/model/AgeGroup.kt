package pt.mataventuras.domain.model

/**
 * Child age band. Drives UI tokens, curriculum, and which reward engine launches.
 */
enum class AgeGroup {
    THREE_YEARS,
    SEVEN_YEARS,
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
fun tokensFor(age: AgeGroup): UiTokens =
    when (age) {
        AgeGroup.THREE_YEARS ->
            UiTokens(
                minButtonDp = 88,
                titleSp = 34,
                bodySp = 22,
                usesTextNavigation = false,
                confirmsBeforeExit = false,
            )
        AgeGroup.SEVEN_YEARS ->
            UiTokens(
                minButtonDp = 56,
                titleSp = 26,
                bodySp = 18,
                usesTextNavigation = true,
                confirmsBeforeExit = true,
            )
    }
