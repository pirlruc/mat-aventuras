package pt.mataventuras.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.age.AgeSelectionScreen
import pt.mataventuras.app.ui.home.HomeScreen
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot

private sealed interface Destination {
    data object Selection : Destination
    data object Home : Destination
    data class Lesson(val module: LearningModule) : Destination
    data object Leaderboard : Destination
    data object Parents : Destination
}

/**
 * In-memory navigation host for selection, home, lessons, rewards, and parents.
 */
@Composable
fun NavGraph(
    container: AppContainer,
    onSpeak: (String) -> Unit,
    onReward: (AgeGroup, Mascot, String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var destination by remember { mutableStateOf<Destination>(Destination.Selection) }
    var profile by remember { mutableStateOf<ChildProfile?>(null) }
    val ageGroup = profile?.ageGroup ?: AgeGroup.THREE_YEARS

    MatAventurasTheme(ageGroup) {
        when (val current = destination) {
            Destination.Selection -> AgeSelectionScreen(
                onSpeak = onSpeak,
                onConfirm = { chosen, name, mascot ->
                    scope.launch {
                        val id = container.repository.createProfile(name, chosen, mascot)
                        profile = container.repository.getProfile(id)
                        destination = Destination.Home
                    }
                },
            )
            Destination.Home -> profile?.let { p ->
                HomeScreen(
                    profile = p,
                    onSpeak = onSpeak,
                    onModule = { destination = Destination.Lesson(it) },
                    onLeaderboard = { destination = Destination.Leaderboard },
                    onParents = { destination = Destination.Parents },
                )
            }
            is Destination.Lesson -> profile?.let { p ->
                LessonScreen(
                    container = container,
                    profile = p,
                    module = current.module,
                    onSpeak = onSpeak,
                    onReward = { age -> onReward(age, p.favouriteMascot, p.name) },
                    onExit = {
                        scope.launch {
                            profile = container.repository.getProfile(p.id) ?: p
                            destination = Destination.Home
                        }
                    },
                )
            }
            Destination.Leaderboard -> LeaderboardAndRewardsScreen(
                container = container,
                activeProfile = profile,
                onBack = { destination = Destination.Home },
            )
            Destination.Parents -> ParentDashboard(
                container = container,
                profile = profile,
                onSpeak = onSpeak,
                onBack = { destination = Destination.Home },
            )
        }
    }
}
