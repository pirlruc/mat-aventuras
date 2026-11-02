package pt.mataventuras.app.ui.parent

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import pt.mataventuras.app.R
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.formatDuration
import pt.mataventuras.app.ui.modulePerformanceLine
import pt.mataventuras.app.ui.needsWorkLines
import pt.mataventuras.app.ui.pinPrompt
import pt.mataventuras.app.ui.pinSubmitLabel
import pt.mataventuras.app.ui.waitingForProfile
import pt.mataventuras.data.pin.PinKeyException
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
    var pinBusy by remember { mutableStateOf(false) }
    var keyBroken by remember { mutableStateOf(false) }
    SecureParentWindow()

    LaunchedEffect(Unit) {
        try {
            settingPin = !container.pinRepository.isSet()
            message = pinPrompt(settingPin)
            onSpeak(message)
        } catch (error: CancellationException) {
            throw error
        } catch (_: PinKeyException) {
            keyBroken = true
            message = "Não consigo ler o PIN deste aparelho."
        } catch (_: IllegalStateException) {
            settingPin = false
            message = VoiceScripts.TRY_AGAIN
        } catch (_: IllegalArgumentException) {
            settingPin = false
            message = VoiceScripts.TRY_AGAIN
        }
    }

    if (keyBroken && !unlocked) {
        ParentKeyReset(container, onBack) {
            keyBroken = false
            settingPin = true
            pin = ""
            confirmation = ""
            message = pinPrompt(true)
        }
        return
    }

    if (!unlocked) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                VoiceScripts.PARENT_DASHBOARD,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
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
                    pinBusy = true
                    scope.launch {
                        try {
                            val result =
                                ParentPin.submit(settingPin, pin, confirmation, gate, container.pinRepository)
                            when (result) {
                                PinGateResult.Unlocked -> {
                                    pin = ""
                                    confirmation = ""
                                    unlocked = true
                                }
                                is PinGateResult.Stay -> {
                                    message = result.message
                                    result.speak?.let(onSpeak)
                                }
                            }
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: PinKeyException) {
                            keyBroken = true
                        } catch (_: IllegalStateException) {
                            message = VoiceScripts.TRY_AGAIN
                        } catch (_: IllegalArgumentException) {
                            message = VoiceScripts.TRY_AGAIN
                        } finally {
                            pinBusy = false
                        }
                    }
                },
                enabled = !pinBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    pinSubmitLabel(
                        settingPin,
                        stringResource(R.string.pin_save),
                        stringResource(R.string.pin_enter),
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
        }
        return
    }

    LaunchedEffect(profile?.id, unlocked) {
        val id = profile?.id ?: return@LaunchedEffect
        val sessions = container.repository.sessionsFor(id)
        summary = container.analytics.summarise(id, sessions)
    }

    val data = summary
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            VoiceScripts.PARENT_DASHBOARD,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(VoiceScripts.STAYS_ON_DEVICE, style = MaterialTheme.typography.bodyMedium)
        if (waitingForProfile(profile, data)) {
            Text(stringResource(R.string.choose_child))
        } else {
            Text(profile!!.name, style = MaterialTheme.typography.titleLarge)
            MetricCard(stringResource(R.string.accuracy), "${(data!!.accuracy * 100).toInt()} %")
            LinearProgressIndicator(
                progress = { data.accuracy.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            MetricCard(stringResource(R.string.hits_misses), "${data.hits} / ${data.misses}")
            MetricCard(stringResource(R.string.time), formatDuration(data.totalTimeMs))
            Text(stringResource(R.string.per_module), fontWeight = FontWeight.Bold)
            data.byModule.forEach { module ->
                Text(modulePerformanceLine(module))
            }
            Text(stringResource(R.string.needs_work), fontWeight = FontWeight.Bold)
            needsWorkLines(data.needsWork, stringResource(R.string.needs_work_clear)).forEach { Text(it) }
            PrizeLimitRow(container, profile!!)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.close)) }
    }
}

@Composable
private fun PinField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
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
private fun PrizeLimitRow(
    container: AppContainer,
    profile: ChildProfile,
) {
    val scope = rememberCoroutineScope()
    var seconds by remember(profile.id) { mutableStateOf(profile.prizeLimitSeconds) }
    Text(stringResource(R.string.prize_minutes, seconds / 60), fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(60, 120, 180).forEach { choice ->
            Button(
                onClick = {
                    val stored = container.rewards.prizeLimitSeconds(choice)
                    seconds = stored
                    scope.launch {
                        container.repository.updateProfile(profile.copy(prizeLimitSeconds = stored))
                    }
                },
            ) { Text(stringResource(R.string.minutes_short, choice / 60)) }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
) {
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

@Composable
private fun SecureParentWindow() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

@Composable
internal fun ParentKeyReset(
    container: AppContainer,
    onBack: () -> Unit,
    holdMs: Long = 1_500,
    onReset: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var held by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.pin_unreadable))
        Spacer(Modifier.height(12.dp))
        if (!held) {
            Text(
                stringResource(R.string.hold_to_reset),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val released =
                                        try {
                                            withTimeout(holdMs) {
                                                tryAwaitRelease()
                                                true
                                            }
                                        } catch (_: TimeoutCancellationException) {
                                            false
                                        }
                                    if (!released) held = true
                                },
                            )
                        },
            )
        } else {
            Text(stringResource(R.string.reset_sum))
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it.filter { ch -> ch.isDigit() }.take(2) },
                label = { Text(stringResource(R.string.answer)) },
                modifier = Modifier.fillMaxWidth().testTag("pin-reset-answer"),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (answer != "13") return@Button
                    scope.launch {
                        container.pinRepository.clear()
                        onReset()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.reset_pin)) }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
    }
}
