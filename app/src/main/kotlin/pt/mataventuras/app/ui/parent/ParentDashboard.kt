package pt.mataventuras.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.parent.ParentSummary
import pt.mataventuras.domain.parent.PinGate
import pt.mataventuras.domain.parent.PinGateResult
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * PIN-gated parental dashboard. Data stays on-device.
 */
@Composable
fun ParentDashboard(
    container: AppContainer,
    profile: ChildProfile?,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val gate = remember { PinGate(container.pinPolicy) }
    var unlocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var message by remember { mutableStateOf(VoiceScripts.ENTER_PIN) }
    var settingPin by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<ParentSummary?>(null) }

    LaunchedEffect(Unit) {
        settingPin = !container.pinRepository.isSet()
        message = if (settingPin) VoiceScripts.SET_PIN else VoiceScripts.ENTER_PIN
        onSpeak(message)
    }

    if (!unlocked) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(VoiceScripts.PARENT_DASHBOARD, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(message)
            Spacer(Modifier.height(12.dp))
            PinField("PIN", pin) { pin = it.take(4) }
            if (settingPin) {
                Spacer(Modifier.height(8.dp))
                PinField("Confirmar PIN", confirmation) { confirmation = it.take(4) }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (settingPin) {
                            val (result, created) = gate.setPin(pin, confirmation)
                            when (result) {
                                PinGateResult.Unlocked -> {
                                    container.pinRepository.save(created!!)
                                    unlocked = true
                                }
                                is PinGateResult.Stay -> message = result.message
                            }
                        } else {
                            val state = container.pinRepository.read() ?: return@launch
                            val (result, next) = gate.unlock(state, pin)
                            container.pinRepository.save(next)
                            when (result) {
                                PinGateResult.Unlocked -> unlocked = true
                                is PinGateResult.Stay -> {
                                    message = result.message
                                    result.speak?.let(onSpeak)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (settingPin) "Guardar PIN" else "Entrar") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
        }
        return
    }

    LaunchedEffect(profile?.id, unlocked) {
        val id = profile?.id ?: return@LaunchedEffect
        val sessions = container.repository.allSessions()
        summary = container.analytics.summarise(id, sessions)
    }

    val data = summary
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(VoiceScripts.PARENT_DASHBOARD, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(VoiceScripts.STAYS_ON_DEVICE, style = MaterialTheme.typography.bodyMedium)
        if (profile == null || data == null) {
            Text("Escolhe um perfil de criança primeiro.")
        } else {
            Text(profile.name, style = MaterialTheme.typography.titleLarge)
            MetricCard("Precisão", "${(data.accuracy * 100).toInt()} %")
            LinearProgressIndicator(
                progress = { data.accuracy.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            MetricCard("Acertos / erros", "${data.hits} / ${data.misses}")
            MetricCard("Tempo", ParentLabels.formatDuration(data.totalTimeMs))
            Text("Por módulo", fontWeight = FontWeight.Bold)
            data.byModule.forEach { module ->
                Text(
                    "${module.module.name.lowercase()} — ${(module.accuracy * 100).toInt()}% " +
                        "(${module.hits} certos, ${ParentLabels.formatDuration(module.timeMs)})",
                )
            }
            Text("Áreas a melhorar", fontWeight = FontWeight.Bold)
            if (data.needsWork.isEmpty()) {
                Text("Nenhum módulo abaixo de 70% com amostra suficiente.")
            } else {
                data.needsWork.forEach { Text("• ${it.name.lowercase()}") }
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Fechar") }
    }
}

@Composable
private fun PinField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun MetricCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

