package pt.mataventuras.app.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LeaderboardEntry
import pt.mataventuras.domain.model.UnlockedAvatar
import pt.mataventuras.domain.model.UnlockedBadge
import pt.mataventuras.domain.progress.AvatarCode
import pt.mataventuras.domain.progress.BadgeCode
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * On-device leaderboard and reward collection.
 */
@Composable
fun LeaderboardAndRewardsScreen(
    container: AppContainer,
    activeProfile: ChildProfile?,
    onBack: () -> Unit,
) {
    val table by produceState(emptyList<LeaderboardEntry>()) {
        container.repository.observeProfiles().collect { profiles ->
            value = container.leaderboard.rank(profiles, container.repository.allSessions())
        }
    }
    val badges by produceState(emptyList<UnlockedBadge>(), activeProfile?.id) {
        if (activeProfile == null) {
            value = emptyList()
            return@produceState
        }
        container.repository.observeBadges(activeProfile.id).collect { value = it }
    }
    val avatars by produceState(emptyList<UnlockedAvatar>(), activeProfile?.id) {
        if (activeProfile == null) {
            value = emptyList()
            return@produceState
        }
        container.repository.observeAvatars(activeProfile.id).collect { value = it }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(VoiceScripts.LEADERBOARD, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Só neste aparelho — irmãos e amigos.")
        }
        items(table, key = { it.profileId }) { row ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("${row.rank}º", fontWeight = FontWeight.Black)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(row.mascot.primaryArgb)),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(row.name, fontWeight = FontWeight.Bold)
                        Text("${row.points} pts · ${(row.averageAccuracy * 100).toInt()}% precisos")
                    }
                }
            }
        }
        item {
            Text(VoiceScripts.REWARDS, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Distintivos")
        }
        items(BadgeCode.entries.toList(), key = { it.name }) { code ->
            val owned = badges.any { it.code == code.name }
            Text(if (owned) "★ ${code.title}" else "☆ ${code.title} — ${code.description}")
        }
        item { Text("Avatares") }
        items(AvatarCode.entries.toList(), key = { it.name }) { code ->
            val owned = avatars.any { it.avatarId == code.name }
            Text(if (owned) "★ ${code.title}" else "☆ ${code.title} (${code.minPoints} pts)")
        }
        item {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
        }
    }
}
