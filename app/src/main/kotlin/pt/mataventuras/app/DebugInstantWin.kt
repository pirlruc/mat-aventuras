package pt.mataventuras.app

/**
 * Maestro id for the debug-only prize win. Absent when [debugInstantWin] is false.
 */
internal const val DEBUG_WIN_TAG: String = "debug-win"

/**
 * True on debug builds. Release builds leave the instant-win control out.
 */
internal var debugInstantWin: Boolean = false
