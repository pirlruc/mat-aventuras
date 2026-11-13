package pt.mataventuras.domain.entitlement

/**
 * Family Plus capability. The shipping build leaves these unlocked.
 * A locked feature adds no child-facing control and no purchase flow.
 */
enum class PremiumFeature {
    THEMED_WORLDS,
    SEASONAL_PRIZES,
    LANGUAGE_PACKS,
}

/**
 * Whether a [PremiumFeature] is available in this build.
 */
fun interface Entitlements {
    /**
     * True when [feature] is available. False shows nothing on the child UI.
     */
    fun isUnlocked(feature: PremiumFeature): Boolean
}
