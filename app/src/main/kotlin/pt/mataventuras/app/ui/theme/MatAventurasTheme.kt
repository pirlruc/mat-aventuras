package pt.mataventuras.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.UiTokens
import pt.mataventuras.domain.model.tokensFor

/** Composition local for age-adaptive spacing and type. */
val LocalUiTokens = staticCompositionLocalOf { tokensFor(AgeBand.THREE_TO_FIVE) }

private val Blue = Color(0xFF1565C0)
private val Orange = Color(0xFFFB8C00)
private val Cream = Color(0xFFFFF8E1)
private val Sky = Color(0xFFE3F2FD)

/**
 * Material 3 theme with age-adaptive tokens and background.
 */
@Composable
fun MatAventurasTheme(
    ageGroup: AgeBand,
    content: @Composable () -> Unit,
) {
    val tokens = tokensFor(ageGroup)
    val background = if (ageGroup == AgeBand.THREE_TO_FIVE) Cream else Sky
    CompositionLocalProvider(
        LocalUiTokens provides tokens,
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Blue,
                secondary = Orange,
                background = background,
                surface = Color.White,
            ),
            content = content,
        )
    }
}

/** Title size as Compose [sp]. */
val UiTokens.titleSpSize get() = titleSp.sp
