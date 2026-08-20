package pt.mataventuras.app.ui.age

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.app.R
import pt.mataventuras.app.ui.EntryLogic
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.tokensFor
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Branded entry: app story, age band, name, mascot, optional resume shortcut.
 */
@Composable
fun AgeSelectionScreen(
    onSpeak: (String) -> Unit,
    onConfirm: (AgeGroup, String, Mascot) -> Unit,
    lastProfile: ChildProfile? = null,
    onContinueLast: () -> Unit = {},
) {
    var ageGroup by remember { mutableStateOf<AgeGroup?>(null) }
    var name by remember { mutableStateOf("") }
    var mascot by remember { mutableStateOf(Mascot.SPEEDY_HEDGEHOG) }
    LaunchedEffect(Unit) { onSpeak(VoiceScripts.AGE_SELECTION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Ícone de ${VoiceScripts.APP_TITLE}",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
        )
        Text(
            text = VoiceScripts.APP_TITLE,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF0D47A1),
        )
        Text(
            text = VoiceScripts.APP_TAGLINE,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1565C0),
        )
        Text(
            text = VoiceScripts.APP_DESCRIPTION,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF37474F),
        )
        Text(
            text = VoiceScripts.AGE_SELECTION,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF0D47A1),
        )
        if (EntryLogic.showsContinue(lastProfile) && lastProfile != null) {
            Button(
                onClick = {
                    onSpeak(EntryLogic.continueLabel(lastProfile))
                    onContinueLast()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8C00)),
            ) {
                Text(EntryLogic.continueLabel(lastProfile), fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AgeButton(
                label = VoiceScripts.THREE_YEARS,
                selected = ageGroup == AgeGroup.THREE_YEARS,
                color = Color(0xFF42A5F5),
                huge = true,
                onClick = {
                    ageGroup = AgeGroup.THREE_YEARS
                    onSpeak("Três anos. Vamos brincar!")
                },
            )
            AgeButton(
                label = VoiceScripts.SEVEN_YEARS,
                selected = ageGroup == AgeGroup.SEVEN_YEARS,
                color = Color(0xFF66BB6A),
                huge = false,
                onClick = {
                    ageGroup = AgeGroup.SEVEN_YEARS
                    onSpeak("Sete anos. Pronto para desafios?")
                },
            )
        }
        val preview = EntryLogic.previewFor(ageGroup)
        if (preview.isNotEmpty()) {
            Text(
                text = preview,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color(0xFF1B5E20),
            )
        }
        if (ageGroup != null) {
            val tokens = tokensFor(ageGroup!!)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(16) },
                label = { Text(VoiceScripts.YOUR_NAME) },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = tokens.bodySp.sp),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(VoiceScripts.CHOOSE_FRIEND, fontSize = tokens.bodySp.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mascot.entries.forEach { candidate ->
                    Button(
                        onClick = {
                            mascot = candidate
                            onSpeak("Eu sou o ${candidate.displayName}!")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(candidate.primaryArgb),
                        ),
                        modifier = Modifier
                            .size(UiLogic.mascotChipDp(ageGroup!!).dp)
                            .semantics { contentDescription = candidate.displayName },
                        shape = CircleShape,
                    ) {
                        Text(candidate.displayName.take(1), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Button(
                onClick = {
                    val chosen = ageGroup ?: return@Button
                    val finalName = UiLogic.fallbackChildName(name)
                    onConfirm(chosen, finalName, mascot)
                },
                enabled = ageGroup != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.minButtonDp.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(VoiceScripts.START, fontSize = tokens.titleSp.sp)
            }
        }
        Text(
            text = VoiceScripts.STAYS_ON_DEVICE,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF546E7A),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun AgeButton(
    label: String,
    selected: Boolean,
    color: Color,
    huge: Boolean,
    onClick: () -> Unit,
) {
    val side = UiLogic.ageButtonSideDp(huge).dp
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(side)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = UiLogic.ageButtonAlpha(selected)),
        ),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(UiLogic.ageButtonEmoji(huge), fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
