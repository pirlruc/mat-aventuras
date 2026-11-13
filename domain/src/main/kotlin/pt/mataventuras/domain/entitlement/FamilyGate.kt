package pt.mataventuras.domain.entitlement

/**
 * Reward extras for unlocked Family Plus features. Null fields stay off the child UI.
 */
data class FamilyExtras(
    val theme: Int? = null,
    val season: Int? = null,
    val prizeIndex: Int? = null,
)

/**
 * Turns the entitlement flag into reward extras. A locked feature adds nothing.
 */
object FamilyGate {
    /**
     * Theme, season, and prize slot for this build.
     */
    fun extras(
        entitlements: Entitlements,
        theme: WorldTheme,
        month: Int,
        day: Int,
    ): FamilyExtras {
        val worlds = entitlements.isUnlocked(PremiumFeature.THEMED_WORLDS)
        val seasonal = entitlements.isUnlocked(PremiumFeature.SEASONAL_PRIZES)
        return FamilyExtras(
            theme = if (worlds) theme.ordinal else null,
            season = if (seasonal) Season.of(month).ordinal else null,
            prizeIndex = if (seasonal) SeasonalCalendar.index(month, day) else null,
        )
    }
}
