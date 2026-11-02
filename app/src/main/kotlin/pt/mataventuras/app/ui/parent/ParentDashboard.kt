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
import pt.mataventuras.domain.parent.AdultChallenge
import pt.mataventuras.domain.parent.ParentSummary
import pt.mataventuras.domain.parent.PinGate
import pt.mataventuras.domain.parent.PinGateResult
import pt.mataventuras.domain.parent.PinNotice
import pt.mataventuras.domain.parent.PinPolicy
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
    challenge: AdultChallenge = AdultChallenge.draw(),
    adultHoldMs: Long = 1_500,
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
    var adultOk by remember { mutableStateOf(false) }
    val unreadable = stringResource(R.string.pin_unreadable)
    val mismatch = stringResource(R.string.pin_mismatch)
    val pinFormat = stringResource(R.string.pin_four_digits)
    val lockedCopy = stringResource(R.string.pin_locked)
    val missingCopy = stringResource(R.string.try_again)
    val wrongLines = (0..PinPolicy.MAX_FAILURES).map { stringResource(R.string.pin_wrong_remaining, it) }
    val puzzle = remember { challenge }
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
            message = unreadable
        } catch (_: IllegalStateException) {
            settingPin = false
            message = VoiceScripts.TRY_AGAIN
        } catch (_: IllegalArgumentException) {
            settingPin = false
            message = VoiceScripts.TRY_AGAIN
        }
    }

    if (keyBroken && !unlocked) {
        ParentKeyReset(container, onBack, adultHoldMs, puzzle) {
            keyBroken = false
            settingPin = true
            pin = ""
            confirmation = ""
            message = pinPrompt(true)
        }
        return
    }

    if (settingPin && !adultOk && !unlocked) {
        AdultHold(
            challenge = puzzle,
            holdMs = adultHoldMs,
            holdLabel = stringResource(R.string.hold_for_adult),
            title = VoiceScripts.PARENT_DASHBOARD,
            buttonLabel = stringResource(R.string.continue_label),
            onBack = onBack,
        ) { adultOk = true }
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
            PinField(stringResource(R.string.pin_label), pin) { pin = it }
            if (settingPin) {
                Spacer(Modifier.height(8.dp))
                PinField(stringResource(R.string.pin_confirm), confirmation) { confirmation = it }
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
                                    message =
                                        pinNotice(
                                            result,
                                            mismatch,
                                            pinFormat,
                                            lockedCopy,
                                            missingCopy,
                                            wrongLines,
                                        )
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
        onValueChange = { onChange(it.filter(Char::isDigit).take(4)) },
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
    holdMs: Long,
    challenge: AdultChallenge,
    onReset: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    AdultHold(
        challenge = challenge,
        holdMs = holdMs,
        holdLabel = stringResource(R.string.hold_to_reset),
        intro = stringResource(R.string.pin_unreadable),
        onBack = onBack,
        onPassed = {
            scope.launch {
                container.pinRepository.clear()
                onReset()
            }
        },
    )
}

@Composable
internal fun AdultHold(
    challenge: AdultChallenge,
    holdMs: Long,
    holdLabel: String,
    title: String? = null,
    intro: String? = null,
    buttonLabel: String? = null,
    onBack: (() -> Unit)? = null,
    onPassed: () -> Unit,
) {
    val action = buttonLabel ?: stringResource(R.string.reset_pin)
    var held by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
        }
        if (intro != null) {
            Text(intro)
            Spacer(Modifier.height(12.dp))
        }
        if (!held) {
            Text(
                holdLabel,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .pointerInput(holdMs) {
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
            Text(challenge.prompt)
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it.filter(Char::isDigit).take(4) },
                label = { Text(stringResource(R.string.answer)) },
                modifier = Modifier.fillMaxWidth().testTag("pin-reset-answer"),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (answer != challenge.product.toString()) return@Button
                    onPassed()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(action) }
        }
        if (onBack != null) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
        }
    }
}

/**
 * pt-PT copy for a PIN [result]. The sentences live in strings.xml.
 */
internal fun pinNotice(
    result: PinGateResult.Stay,
    mismatch: String,
    format: String,
    locked: String,
    missing: String,
    wrongLines: List<String>,
): String =
    when (result.notice) {
        PinNotice.MISMATCH -> mismatch
        PinNotice.FORMAT -> format
        PinNotice.LOCKED -> locked
        PinNotice.MISSING -> missing
        PinNotice.WRONG -> wrongLines[result.remaining.coerceIn(wrongLines.indices)]
    }
