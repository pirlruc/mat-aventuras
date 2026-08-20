package pt.mataventuras.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.ui.HomeNav
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.app.ui.theme.titleSpSize
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
    LaunchedEffect(profile.id) {
        onSpeak(VoiceScripts.greeting(profile.favouriteMascot, profile.ageGroup))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Olá, ${profile.name}!", fontSize = tokens.titleSpSize, fontWeight = FontWeight.ExtraBold)
        Text("${profile.points} pontos", style = MaterialTheme.typography.titleMedium)
        Text(VoiceScripts.agePreview(profile.ageGroup), style = MaterialTheme.typography.bodyLarge)
        modules.forEach { module ->
            val mascot = mascotFor(module)
            Button(
                onClick = {
                    onSpeak("Vamos com o ${mascot.displayName}!")
                    onModule(module)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.minButtonDp.dp + 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(mascot.primaryArgb)),
            ) {
                Text(ModuleTitles.of(module, mascot), fontWeight = FontWeight.Bold)
            }
        }
        if (UiLogic.usesIconNav(profile.ageGroup)) {
            TextButton(
                onClick = {
                    HomeNav.announceAndGo(onSpeak, VoiceScripts.LEADERBOARD, onLeaderboard)
                },
                modifier = Modifier.semantics { contentDescription = VoiceScripts.LEADERBOARD },
            ) { Text("★") }
            TextButton(
                onClick = {
                    HomeNav.announceAndGo(onSpeak, VoiceScripts.PARENT_DASHBOARD, onParents)
                },
                modifier = Modifier.semantics { contentDescription = VoiceScripts.PARENT_DASHBOARD },
            ) { Text("· · ·") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLeaderboard, modifier = Modifier.weight(1f)) {
                    Text(VoiceScripts.LEADERBOARD)
                }
                TextButton(onClick = onParents) { Text(VoiceScripts.PARENT_DASHBOARD) }
            }
        }
        TextButton(onClick = onSwitchProfile) { Text(VoiceScripts.SWITCH_PROFILE) }
    }
}
