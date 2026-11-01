package pt.mataventuras.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.ProfileResume
import pt.mataventuras.app.ui.age.AgeSelectionScreen
import pt.mataventuras.app.ui.home.HomeScreen
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot

/**
 * In-memory navigation host for selection, home, lessons, rewards, and parents.
 * [memory] restores the route and profile after process death.
 */
@Composable
internal fun NavGraph(
    container: AppContainer,
    onSpeak: (String) -> Unit,
    onReward: (AgeBand, Mascot, String) -> Unit,
    memory: RouteMemory = viewModel(),
) {
    val scope = rememberCoroutineScope()
    var destination by remember {
        mutableStateOf(RouteMemory.destination(memory.route(), memory.moduleName()))
    }
    var profile by remember { mutableStateOf<ChildProfile?>(null) }
    var continueProfile by remember { mutableStateOf<ChildProfile?>(null) }
    var restored by remember { mutableStateOf(false) }
    val ageGroup = profile?.ageGroup ?: AgeBand.THREE_TO_FIVE

    LaunchedEffect(Unit) {
        val id = memory.profileId()
        if (id != 0L) {
            val loaded = container.repository.getProfile(id)
            if (loaded == null) {
                destination = Destination.Selection
            } else {
                profile = loaded
            }
        }
        restored = true
    }
    LaunchedEffect(restored, destination, profile?.id) {
        if (!restored) return@LaunchedEffect
        memory.remember(RouteMemory.routeOf(destination), profile?.id ?: 0L, RouteMemory.moduleOf(destination))
    }
    LaunchedEffect(Unit) {
        continueProfile = ProfileResume.continueCandidate(container.lastProfile, container.repository)
    }
    BackHandler(enabled = destination !is Destination.Lesson && destination !is Destination.Selection) {
        destination = if (destination is Destination.Home) Destination.Selection else Destination.Home
    }
    LaunchedEffect(Unit) {
        container.profileTouches.collect { id ->
            val current = profile ?: return@collect
            if (current.id != id) return@collect
            profile = container.repository.getProfile(id) ?: current
        }
    }

    MatAventurasTheme(ageGroup) {
        when (val current = destination) {
            Destination.Selection -> AgeSelectionScreen(
                onSpeak = onSpeak,
                lastProfile = continueProfile,
                onContinueLast = {
                    val resumed = continueProfile ?: return@AgeSelectionScreen
                    scope.launch {
                        ProfileResume.remember(container.lastProfile, resumed)
                        profile = resumed
                        destination = Destination.Home
                    }
                },
                onConfirm = { chosen, name, mascot ->
                    scope.launch {
                        val created =
                            ProfileResume.openNew(
                                store = container.lastProfile,
                                repository = container.repository,
                                name = name,
                                ageGroup = chosen,
                                mascot = mascot,
                            )
                        profile = created
                        continueProfile = created
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
                    onSwitchProfile = { destination = Destination.Selection },
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
