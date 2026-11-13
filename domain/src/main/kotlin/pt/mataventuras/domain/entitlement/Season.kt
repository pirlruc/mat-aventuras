package pt.mataventuras.domain.entitlement

/**
 * Season derived from the device date. It is not a store listing.
 */
enum class Season {
    WINTER,
    SPRING,
    SUMMER,
    AUTUMN,
    ;

    companion object {
        /**
         * Season for calendar [month] (1–12). Any other month is autumn.
         */
        fun of(month: Int): Season =
            when (month) {
                12, 1, 2 -> WINTER
                3, 4, 5 -> SPRING
                6, 7, 8 -> SUMMER
                9, 10, 11 -> AUTUMN
                else -> AUTUMN
            }
    }
}

/**
 * Deterministic prize slot from a calendar date.
 */
object SeasonalCalendar {
    /**
     * Stable index for [month] and [day]. The same date always returns the same index.
     */
    fun index(
        month: Int,
        day: Int,
    ): Int {
        val safeMonth = if (month in 1..12) month else 1
        val safeDay = if (day in 1..31) day else 1
        return (safeMonth - 1) * 31 + (safeDay - 1)
    }
}
