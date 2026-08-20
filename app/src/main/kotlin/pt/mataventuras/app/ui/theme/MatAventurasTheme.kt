package pt.mataventuras.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.UiTokens
import pt.mataventuras.domain.model.tokensFor

/** Composition local for age-adaptive spacing and type. */
val LocalUiTokens = staticCompositionLocalOf { tokensFor(AgeGroup.THREE_YEARS) }

/** Composition local for the active [AgeGroup]. */
val LocalAgeGroup = staticCompositionLocalOf { AgeGroup.THREE_YEARS }

private val Blue = Color(0xFF1565C0)
private val Orange = Color(0xFFFB8C00)
private val Cream = Color(0xFFFFF8E1)
private val Sky = Color(0xFFE3F2FD)

/**
 * Material 3 theme with age-adaptive tokens and background.
 */
@Composable
fun MatAventurasTheme(
    ageGroup: AgeGroup,
    content: @Composable () -> Unit,
) {
    val tokens = tokensFor(ageGroup)
    val background = if (ageGroup == AgeGroup.THREE_YEARS) Cream else Sky
    CompositionLocalProvider(
        LocalUiTokens provides tokens,
        LocalAgeGroup provides ageGroup,
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

/** Corner radius for primary buttons. */
fun UiTokens.buttonRadius() = RoundedCornerShape(if (minButtonDp >= 80) 28.dp else 16.dp)

/** Title size as Compose [sp]. */
val UiTokens.titleSpSize get() = titleSp.sp

/** Body size as Compose [sp]. */
val UiTokens.bodySpSize get() = bodySp.sp
