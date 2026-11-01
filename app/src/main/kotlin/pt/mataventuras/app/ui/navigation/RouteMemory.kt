package pt.mataventuras.app.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import pt.mataventuras.domain.model.LearningModule

internal sealed interface Destination {
    data object Selection : Destination

    data object Home : Destination

    data class Lesson(
        val module: LearningModule,
    ) : Destination

    data object Leaderboard : Destination

    data object Parents : Destination
}

/**
 * Navigation route and active profile id kept in [SavedStateHandle].
 */
internal class RouteMemory(
    private val saved: SavedStateHandle,
) : ViewModel() {
    /**
     * Last route name, or the age-selection screen.
     */
    fun route(): String = saved[ROUTE] ?: SELECTION

    /**
     * Stored profile id, or zero when none is open.
     */
    fun profileId(): Long = saved[PROFILE] ?: 0L

    /**
     * Lesson module name when [route] is a lesson.
     */
    fun moduleName(): String = saved[MODULE] ?: ""

    /**
     * Writes the current screen so a recreated activity can reopen it.
     */
    fun remember(
        route: String,
        profileId: Long,
        moduleName: String = "",
    ) {
        saved[ROUTE] = route
        saved[PROFILE] = profileId
        saved[MODULE] = moduleName
    }

    /** Names stored in the handle. */
    companion object {
        const val ROUTE: String = "route"
        const val PROFILE: String = "profileId"
        const val MODULE: String = "module"
        const val SELECTION: String = "selection"
        const val HOME: String = "home"
        const val LESSON: String = "lesson"
        const val LEADERBOARD: String = "leaderboard"
        const val PARENTS: String = "parents"

        /**
         * Screen for a stored route. An unknown module opens counting.
         */
        fun destination(
            route: String,
            moduleName: String,
        ): Destination =
            when (route) {
                HOME -> Destination.Home
                LESSON ->
                    Destination.Lesson(
                        runCatching { LearningModule.valueOf(moduleName) }
                            .getOrDefault(LearningModule.COUNTING),
                    )
                LEADERBOARD -> Destination.Leaderboard
                PARENTS -> Destination.Parents
                else -> Destination.Selection
            }

        /**
         * Route name for [destination].
         */
        fun routeOf(destination: Destination): String =
            when (destination) {
                Destination.Selection -> SELECTION
                Destination.Home -> HOME
                is Destination.Lesson -> LESSON
                Destination.Leaderboard -> LEADERBOARD
                Destination.Parents -> PARENTS
            }

        /**
         * Module name when [destination] is a lesson.
         */
        fun moduleOf(destination: Destination): String =
            when (destination) {
                is Destination.Lesson -> destination.module.name
                else -> ""
            }
    }
}
