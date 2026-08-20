package pt.mataventuras.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.ui.theme.LocalUiTokens
import pt.mataventuras.app.ui.theme.titleSpSize
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.mascotFor
import pt.mataventuras.domain.model.modulesFor
import pt.mataventuras.domain.voice.VoiceScripts

@Composable
fun HomeScreen(
    profile: ChildProfile,
    onSpeak: (String) -> Unit,
    onModule: (LearningModule) -> Unit,
    onLeaderboard: () -> Unit,
    onParents: () -> Unit,
) {
    val tokens = LocalUiTokens.current
    val modules = modulesFor(profile.ageGroup)
    LaunchedEffect(profile.id) {
        onSpeak(VoiceScripts.greeting(profile.favouriteMascot, profile.ageGroup))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Olá, ${profile.name}!", fontSize = tokens.titleSpSize, fontWeight = FontWeight.ExtraBold)
        Text("${profile.points} pontos", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (profile.ageGroup == AgeGroup.THREE_YEARS) 1 else 2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(modules, key = { it.name }) { module ->
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
                    Text(moduleTitle(module, mascot), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (profile.ageGroup == AgeGroup.SEVEN_YEARS) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLeaderboard, modifier = Modifier.weight(1f)) {
                    Text(VoiceScripts.LEADERBOARD)
                }
                TextButton(onClick = onParents) { Text(VoiceScripts.PARENT_DASHBOARD) }
            }
        } else {
            TextButton(onClick = onLeaderboard) { Text("★") }
            TextButton(onClick = onParents) { Text("· · ·") }
        }
    }
}

private fun moduleTitle(module: LearningModule, mascot: Mascot): String = when (module) {
    LearningModule.COUNTING -> "Contar com o ${mascot.displayName}"
    LearningModule.SHAPES -> "Formas com o ${mascot.displayName}"
    LearningModule.NUMBERS -> "Números com o ${mascot.displayName}"
    LearningModule.ADDITION -> "Somar com o ${mascot.displayName}"
    LearningModule.SUBTRACTION -> "Subtrair com o ${mascot.displayName}"
    LearningModule.MULTIPLICATION -> "Multiplicar com o ${mascot.displayName}"
    LearningModule.LOGIC -> "Lógica com o ${mascot.displayName}"
}
