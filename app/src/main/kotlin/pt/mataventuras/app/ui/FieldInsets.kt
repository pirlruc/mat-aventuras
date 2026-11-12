package pt.mataventuras.app.ui

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch

/** True while the Ganhaste card covers the lesson. */
internal val LocalCelebrate = compositionLocalOf { false }

/** Increments when a prize Activity returns, including a loss. */
internal val LocalPrizeClosed = compositionLocalOf { 0 }

/**
 * Scrolls a focused field above the keyboard.
 */
@Composable
internal fun rememberBringIntoViewOnFocus(): Modifier {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    return Modifier
        .bringIntoViewRequester(requester)
        .onFocusEvent { state ->
            if (state.isFocused) {
                scope.launch { requester.bringIntoView() }
            }
        }
}
