package pt.mataventuras.app.ui.home

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Home grid columns for a window width in dp.
 * Breakpoints match [WindowWidthSizeClass]: compact, medium at 600 dp, expanded at 840 dp.
 */
internal fun columnsForWidth(widthDp: Int): Int =
    when {
        widthDp >= EXPANDED_DP -> 3
        widthDp >= MEDIUM_DP -> 2
        else -> 1
    }

/** Representative width for a window size class, so the column count follows that class. */
internal fun widthDpFor(width: WindowWidthSizeClass): Int =
    when {
        width == WindowWidthSizeClass.Expanded -> EXPANDED_DP
        width == WindowWidthSizeClass.Medium -> MEDIUM_DP
        else -> 0
    }

/**
 * Column count for the module grid. A tablet or landscape width gets more than one column.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun homeColumnCount(): Int {
    val activity = LocalActivity.current
    val widthDp =
        if (activity == null) {
            LocalConfiguration.current.screenWidthDp
        } else {
            widthDpFor(calculateWindowSizeClass(activity).widthSizeClass)
        }
    return columnsForWidth(widthDp)
}

private const val MEDIUM_DP: Int = 600
private const val EXPANDED_DP: Int = 840
