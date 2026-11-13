package pt.mataventuras.domain.entitlement

/**
 * Parent-selected palette for the runner. The ordinal matches the Godot runner.
 */
enum class WorldTheme {
    MEADOW,
    SUNSET,
    SEA,
    NIGHT,
    ;

    companion object {
        /**
         * Stored name, or [MEADOW] when [raw] is missing or unknown.
         */
        fun fromStored(raw: String?): WorldTheme = entries.firstOrNull { it.name == raw } ?: MEADOW
    }
}
