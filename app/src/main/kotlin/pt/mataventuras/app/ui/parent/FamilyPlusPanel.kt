package pt.mataventuras.app.ui.parent

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.R
import pt.mataventuras.app.entitlement.FamilySettings
import pt.mataventuras.app.entitlement.LanguagePacks
import pt.mataventuras.app.entitlement.applyLanguage
import pt.mataventuras.domain.entitlement.Entitlements
import pt.mataventuras.domain.entitlement.PremiumFeature
import pt.mataventuras.domain.entitlement.Season
import pt.mataventuras.domain.entitlement.WorldTheme

/**
 * Parent-only Family Plus controls. A locked feature is a neutral note, never a purchase.
 */
@Composable
internal fun FamilyPlusPanel(
    entitlements: Entitlements,
    settings: FamilySettings,
    season: Season,
) {
    val worlds = entitlements.isUnlocked(PremiumFeature.THEMED_WORLDS)
    val seasonal = entitlements.isUnlocked(PremiumFeature.SEASONAL_PRIZES)
    val languages = entitlements.isUnlocked(PremiumFeature.LANGUAGE_PACKS)
    var theme by remember { mutableStateOf(settings.theme()) }
    var language by remember { mutableStateOf(settings.language()) }
    val activity = LocalActivity.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!worlds || !seasonal || !languages) {
            Text(stringResource(R.string.family_locked))
        }
        if (worlds) {
            Text(stringResource(R.string.world_theme), fontWeight = FontWeight.Bold)
            WorldTheme.entries.forEach { choice ->
                Button(
                    onClick = {
                        settings.saveTheme(choice)
                        theme = choice
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                ) {
                    Text(themeLabel(choice, theme == choice))
                }
            }
        }
        if (seasonal) {
            Text(stringResource(R.string.season_line, stringResource(seasonLabel(season))))
        }
        if (languages) {
            Text(stringResource(R.string.language_heading), fontWeight = FontWeight.Bold)
            LanguagePacks.TAGS.forEach { tag ->
                Button(
                    onClick = {
                        language = tag
                        applyLanguage(activity, settings, tag)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("lang-$tag"),
                ) {
                    Text(languageLabel(tag, language == tag))
                }
            }
        }
    }
}

@Composable
private fun themeLabel(
    choice: WorldTheme,
    selected: Boolean,
): String {
    val name =
        stringResource(
            when (choice) {
                WorldTheme.MEADOW -> R.string.theme_meadow
                WorldTheme.SUNSET -> R.string.theme_sunset
                WorldTheme.SEA -> R.string.theme_sea
                WorldTheme.NIGHT -> R.string.theme_night
            },
        )
    return if (selected) "● $name" else name
}

@Composable
private fun languageLabel(
    tag: String,
    selected: Boolean,
): String {
    val name =
        stringResource(
            when (tag) {
                "en" -> R.string.lang_en
                "es" -> R.string.lang_es
                "fr" -> R.string.lang_fr
                "pt-BR" -> R.string.lang_br
                else -> R.string.lang_pt
            },
        )
    return if (selected) "● $name" else name
}

private fun seasonLabel(season: Season): Int =
    when (season) {
        Season.WINTER -> R.string.season_winter
        Season.SPRING -> R.string.season_spring
        Season.SUMMER -> R.string.season_summer
        Season.AUTUMN -> R.string.season_autumn
    }
