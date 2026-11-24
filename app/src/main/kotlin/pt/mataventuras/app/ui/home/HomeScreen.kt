package pt.mataventuras.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.R
import pt.mataventuras.app.ui.HomeNav
import pt.mataventuras.app.ui.packed
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.app.ui.theme.titleSpSize
import pt.mataventuras.app.ui.usesIconNav
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.mascotFor
import pt.mataventuras.domain.model.modulesFor
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Module grid for the active child. Labels are pt-PT.
 */
@Composable
fun HomeScreen(
    profile: ChildProfile,
    onSpeak: (String) -> Unit,
    onModule: (LearningModule) -> Unit,
    onLeaderboard: () -> Unit,
    onParents: () -> Unit,
    onSwitchProfile: () -> Unit = {},
) {
    val tokens = LocalUiTokens.current
    val modules = modulesFor(profile.ageGroup)
    val greeting =
        stringResource(
            if (profile.ageGroup == AgeBand.THREE_TO_FIVE) {
                R.string.voice_greeting_young
            } else {
                R.string.voice_greeting_older
            },
            profile.favouriteMascot.displayName,
        )
    LaunchedEffect(profile.id) {
        onSpeak(greeting)
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.hello_name, profile.name),
            fontSize = tokens.titleSpSize,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(stringResource(R.string.points_count, profile.points), style = MaterialTheme.typography.titleMedium)
        Text(packed(VoiceScripts.agePreview(profile.ageGroup)), style = MaterialTheme.typography.bodyLarge)
        ModuleGrid(modules, tokens.minButtonDp, onSpeak, onModule)
        val leaderboard = packed(VoiceScripts.LEADERBOARD)
        val parents = packed(VoiceScripts.PARENT_DASHBOARD)
        if (usesIconNav(profile.ageGroup)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        HomeNav.announceAndGo(onSpeak, leaderboard, onLeaderboard)
                    },
                    modifier =
                        Modifier
                            .height(64.dp)
                            .semantics(
                                mergeDescendants = true,
                            ) { contentDescription = leaderboard },
                ) { Text("★") }
                Button(
                    onClick = {
                        HomeNav.announceAndGo(onSpeak, parents, onParents)
                    },
                    modifier =
                        Modifier
                            .height(64.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = parents
                            },
                ) { Text("\uD83D\uDD12") }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLeaderboard, modifier = Modifier.weight(1f)) {
                    Text(packed(VoiceScripts.LEADERBOARD))
                }
                TextButton(
                    onClick = onParents,
                    modifier = Modifier.heightIn(min = 64.dp).testTag("parent-open"),
                ) { Text(packed(VoiceScripts.PARENT_DASHBOARD)) }
            }
        }
        TextButton(onClick = onSwitchProfile) { Text(packed(VoiceScripts.SWITCH_PROFILE)) }
    }
}

@Composable
private fun ModuleGrid(
    modules: List<LearningModule>,
    minButtonDp: Int,
    onSpeak: (String) -> Unit,
    onModule: (LearningModule) -> Unit,
) {
    val columns = homeColumnCount()
    modules.chunked(columns).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { module ->
                ModuleButton(module, minButtonDp, onSpeak, onModule, Modifier.weight(1f))
            }
            repeat(columns - row.size) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModuleButton(
    module: LearningModule,
    minButtonDp: Int,
    onSpeak: (String) -> Unit,
    onModule: (LearningModule) -> Unit,
    modifier: Modifier,
) {
    val mascot = mascotFor(module)
    val letsGo = stringResource(R.string.lets_go_with, mascot.displayName)
    val title =
        stringResource(R.string.module_with_mascot, stringResource(ModuleTitles.nounId(module)), mascot.displayName)
    Button(
        onClick = {
            onSpeak(letsGo)
            onModule(module)
        },
        modifier = modifier.height(minButtonDp.dp + 24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(mascot.primaryArgb)),
    ) {
        Text(title, fontWeight = FontWeight.Bold)
    }
}
