package pt.mataventuras.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.ProfileResume
import pt.mataventuras.app.ui.age.AgeSelectionScreen
import pt.mataventuras.app.ui.home.HomeScreen
import pt.mataventuras.app.ui.lesson.CappedScale
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.engine.PrizeHud
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.Mascot

/**
 * In-memory navigation host for selection, home, lessons, rewards, and parents.
 * [memory] restores the route and profile after process death.
 */
@Composable
internal fun NavGraph(
    container: AppContainer,
    onSpeak: (String) -> Unit,
    onReward: (AgeBand, Mascot, String, Int) -> Unit,
    memory: RouteMemory = viewModel(),
    celebrate: Boolean = false,
    onDismissWin: () -> Unit = {},
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
        Box(Modifier.fillMaxSize()) {
            when (val current = destination) {
                Destination.Selection ->
                    AgeSelectionScreen(
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
                Destination.Home ->
                    profile?.let { p ->
                        HomeScreen(
                            profile = p,
                            onSpeak = onSpeak,
                            onModule = { destination = Destination.Lesson(it) },
                            onLeaderboard = { destination = Destination.Leaderboard },
                            onParents = { destination = Destination.Parents },
                            onSwitchProfile = { destination = Destination.Selection },
                        )
                    }
                is Destination.Lesson ->
                    profile?.let { p ->
                        LessonScreen(
                            container = container,
                            profile = p,
                            module = current.module,
                            onSpeak = onSpeak,
                            onReward = { age ->
                                onReward(age, p.favouriteMascot, p.name, p.prizeLimitSeconds)
                            },
                            onExit = {
                                scope.launch {
                                    profile = container.repository.getProfile(p.id) ?: p
                                    destination = Destination.Home
                                }
                            },
                        )
                    }
                Destination.Leaderboard ->
                    LeaderboardAndRewardsScreen(
                        container = container,
                        activeProfile = profile,
                        onBack = { destination = Destination.Home },
                    )
                Destination.Parents ->
                    ParentDashboard(
                        container = container,
                        profile = profile,
                        onSpeak = onSpeak,
                        onBack = { destination = Destination.Home },
                    )
            }
            if (celebrate) {
                PrizeWinCard(onDismissWin)
            }
        }
    }
}

@Composable
private fun PrizeWinCard(onContinue: () -> Unit) {
    CappedScale {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1565C0))
                    .semantics { contentDescription = PrizeHud.WIN_TITLE },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(PrizeHud.WIN_TITLE, color = Color.White, fontSize = 48.sp)
                Button(
                    onClick = onContinue,
                    modifier = Modifier.padding(top = 24.dp).height(64.dp),
                ) {
                    Text(PrizeHud.WIN_CONTINUE)
                }
            }
        }
    }
}
