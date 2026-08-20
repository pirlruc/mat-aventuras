package pt.mataventuras.app.ui.age

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.tokensFor
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Age selection. Huge buttons and voice for age 3; extra text for age 7.
 */
@Composable
fun AgeSelectionScreen(
    onSpeak: (String) -> Unit,
    onConfirm: (AgeGroup, String, Mascot) -> Unit,
) {
    var ageGroup by remember { mutableStateOf<AgeGroup?>(null) }
    var name by remember { mutableStateOf("") }
    var mascot by remember { mutableStateOf(Mascot.SPEEDY_HEDGEHOG) }
    LaunchedEffect(Unit) { onSpeak(VoiceScripts.AGE_SELECTION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(
            text = VoiceScripts.AGE_SELECTION,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF0D47A1),
        )
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
        if (ageGroup != null) {
            val tokens = tokensFor(ageGroup!!)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(16) },
                label = { Text("Como te chamas?") },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = tokens.bodySp.sp),
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Escolhe o teu amigo:", fontSize = tokens.bodySp.sp)
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
                Text("Vamos começar!", fontSize = tokens.titleSp.sp)
            }
        }
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
