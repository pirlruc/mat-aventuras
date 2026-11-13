package pt.mataventuras.app.engine

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import pt.mataventuras.app.R

/**
 * TalkBack exit control drawn above the Godot surface. The canvas Sair is not in the tree.
 */
internal object PrizeExit {
    const val TAG: String = "prize-exit"

    /**
     * Adds the overlay when [embed] is true. A second call leaves the first button in place.
     */
    fun attach(
        activity: Activity,
        embed: Boolean,
        onExit: () -> Unit,
    ) {
        if (!embed) return
        val decor = activity.window.decorView as ViewGroup
        if (decor.findViewWithTag<View>(TAG) != null) return
        if (activity is LifecycleOwner) decor.setViewTreeLifecycleOwner(activity)
        if (activity is SavedStateRegistryOwner) decor.setViewTreeSavedStateRegistryOwner(activity)
        val overlay =
            ComposeView(activity).apply {
                tag = TAG
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    Box(contentAlignment = Alignment.TopEnd) {
                        PrizeExitButton(onExit)
                    }
                }
            }
        decor.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
            ),
        )
        overlay.bringToFront()
    }
}

/**
 * Exit button TalkBack can activate. The label follows the language pack.
 */
@Composable
internal fun PrizeExitButton(onExit: () -> Unit) {
    Button(
        onClick = onExit,
        modifier =
            Modifier
                .padding(8.dp)
                .heightIn(min = 48.dp)
                .widthIn(min = 48.dp)
                .testTag(PrizeExit.TAG),
    ) {
        Text(stringResource(R.string.godot_exit))
    }
}
