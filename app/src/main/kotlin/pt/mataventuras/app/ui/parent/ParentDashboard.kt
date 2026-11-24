package pt.mataventuras.app.ui.parent

import android.app.Activity
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import pt.mataventuras.app.R
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.app.ui.formatDuration
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.app.ui.modulePerformanceLine
import pt.mataventuras.app.ui.needsWorkLines
import pt.mataventuras.app.ui.packLine
import pt.mataventuras.app.ui.packed
import pt.mataventuras.app.ui.pinPrompt
import pt.mataventuras.app.ui.pinSubmitLabel
import pt.mataventuras.app.ui.rememberBringIntoViewOnFocus
import pt.mataventuras.data.pin.PinKeyException
import pt.mataventuras.domain.entitlement.PremiumFeature
import pt.mataventuras.domain.entitlement.Season
import pt.mataventuras.domain.math.Worksheet
import pt.mataventuras.domain.math.Worksheets
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.parent.AdultChallenge
import pt.mataventuras.domain.parent.ParentSummary
import pt.mataventuras.domain.parent.PinGate
import pt.mataventuras.domain.parent.PinGateResult
import pt.mataventuras.domain.parent.PinNotice
import pt.mataventuras.domain.parent.PinPolicy
import pt.mataventuras.domain.parent.masteryGrid
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
    var message by remember { mutableStateOf("") }
    var settingPin by remember { mutableStateOf(false) }
    var gateReady by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<ParentSummary?>(null) }
    var pinBusy by remember { mutableStateOf(false) }
    var keyBroken by remember { mutableStateOf(false) }
    var adultOk by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
            onSpeak(context.packLine(message))
        } catch (error: CancellationException) {
            throw error
        } catch (_: PinKeyException) {
            keyBroken = true
            message = unreadable
        } catch (_: IllegalStateException) {
            settingPin = false
            message = missingCopy
        } catch (_: IllegalArgumentException) {
            settingPin = false
            message = missingCopy
        } finally {
            gateReady = true
        }
    }

    if (!unlocked) {
        if (!gateReady) {
            Box(Modifier.fillMaxSize().testTag("parent-loading"), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.parent_loading), modifier = Modifier.padding(top = 12.dp))
                }
            }
        } else if (keyBroken) {
            ParentKeyReset(container, onBack, adultHoldMs, puzzle) {
                keyBroken = false
                settingPin = true
                pin = ""
                confirmation = ""
                message = pinPrompt(true)
            }
        } else if (settingPin && !adultOk) {
            AdultHold(
                challenge = puzzle,
                holdMs = adultHoldMs,
                holdLabel = stringResource(R.string.hold_for_adult),
                title = packed(VoiceScripts.PARENT_DASHBOARD),
                buttonLabel = stringResource(R.string.continue_label),
                onBack = onBack,
            ) { adultOk = true }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                ) { Text(stringResource(R.string.back)) }
                Text(
                    packed(VoiceScripts.PARENT_DASHBOARD),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                Text(packed(message))
                Spacer(Modifier.height(12.dp))
                PinField(stringResource(R.string.pin_label), pin, "parent-pin") { pin = it }
                if (settingPin) {
                    Spacer(Modifier.height(8.dp))
                    PinField(stringResource(R.string.pin_confirm), confirmation, "parent-pin-confirm") {
                        confirmation =
                            it
                    }
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
                                message = missingCopy
                            } catch (_: IllegalArgumentException) {
                                message = missingCopy
                            } finally {
                                pinBusy = false
                            }
                        }
                    },
                    enabled = !pinBusy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("parent-pin-save"),
                ) {
                    Text(
                        pinSubmitLabel(
                            settingPin,
                            stringResource(R.string.pin_save),
                            stringResource(R.string.pin_enter),
                        ),
                    )
                }
            }
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
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        ) { Text(stringResource(R.string.close)) }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                packed(VoiceScripts.PARENT_DASHBOARD),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(packed(VoiceScripts.STAYS_ON_DEVICE), style = MaterialTheme.typography.bodyMedium)
            val current = profile
            val stats = data
            if (current == null || stats == null) {
                Text(stringResource(R.string.choose_child))
            } else {
                Text(current.name, style = MaterialTheme.typography.titleLarge)
                MetricCard(stringResource(R.string.accuracy), "${(stats.accuracy * 100).toInt()} %")
                LinearProgressIndicator(
                    progress = { stats.accuracy.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                MetricCard(stringResource(R.string.hits_misses), "${stats.hits} / ${stats.misses}")
                MetricCard(stringResource(R.string.time), formatDuration(stats.totalTimeMs))
                Text(stringResource(R.string.per_module), fontWeight = FontWeight.Bold)
                for (module in stats.byModule) {
                    Text(
                        modulePerformanceLine(
                            module,
                            stringResource(ModuleTitles.nounId(module.module)),
                        ),
                    )
                }
                Text(stringResource(R.string.needs_work), fontWeight = FontWeight.Bold)
                val clear = stringResource(R.string.needs_work_clear)
                val nounByModule = mutableMapOf<pt.mataventuras.domain.model.LearningModule, String>()
                for (module in stats.needsWork) {
                    nounByModule[module] = stringResource(ModuleTitles.nounId(module))
                }
                needsWorkLines(stats.needsWork, clear) { nounByModule.getValue(it) }.forEach { Text(it) }
                MasteryHeatmap(masteryGrid(stats))
                PrintWorksheetButton(current)
                PrizeLimitRow(container, current)
            }
            FamilyPlusPanel(
                container.entitlements,
                container.family,
                Season.of(LocalDate.now().monthValue),
            )
            ClassroomPanel(
                container,
                container.entitlements.isUnlocked(PremiumFeature.CLASSROOM),
            )
        }
    }
}

@Composable
private fun PrintWorksheetButton(profile: ChildProfile) {
    val context = LocalContext.current
    var sheet by remember { mutableStateOf<Worksheet?>(null) }
    val export =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            writeWorksheetIfReady(sheet, uri) { target -> context.contentResolver.openOutputStream(target) }
        }
    Button(
        onClick = {
            sheet = Worksheets.build(profile.ageGroup, profile.id)
            export.launch("ficha.pdf")
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) { Text(stringResource(R.string.print_worksheet)) }
}

@Composable
private fun PinField(
    label: String,
    value: String,
    tag: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth().testTag(tag).then(rememberBringIntoViewOnFocus()),
        singleLine = true,
    )
}

@Composable
private fun PrizeLimitRow(
    container: AppContainer,
    profile: ChildProfile,
) {
    val scope = rememberCoroutineScope()
    var seconds by remember(profile.id) { mutableStateOf(profile.pace.limitSeconds) }
    Text(stringResource(R.string.prize_minutes, seconds / 60), fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(60, 120, 180).forEach { choice ->
            Button(
                onClick = {
                    val stored = container.rewards.prizeLimitSeconds(choice)
                    seconds = stored
                    scope.launch {
                        container.repository.updateProfile(
                            profile.copy(pace = profile.pace.copy(limitSeconds = stored)),
                        )
                        container.publishProfile(profile.id)
                    }
                },
                modifier = Modifier.heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.minutes_short, choice / 60)) }
        }
    }
    var every by remember(profile.id) { mutableIntStateOf(profile.pace.every) }
    Text(stringResource(R.string.prize_every, every), fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(5, 10, 15).forEach { choice ->
            Button(
                onClick = {
                    val stored = container.rewards.prizeEvery(choice)
                    every = stored
                    scope.launch {
                        container.repository.updateProfile(
                            profile.copy(pace = profile.pace.copy(every = stored)),
                            cadenceOnly = true,
                        )
                        container.publishProfile(profile.id)
                    }
                },
                modifier = Modifier.heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.every_short, choice)) }
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
    var progress by remember { mutableFloatStateOf(0f) }
    var answer by remember { mutableStateOf("") }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .semantics { testTagsAsResourceId = true },
        verticalArrangement = Arrangement.Top,
    ) {
        if (onBack != null) {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.back)) }
            Spacer(Modifier.height(8.dp))
        }
        if (title != null) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
        }
        if (intro != null) {
            Text(intro)
            Spacer(Modifier.height(12.dp))
        }
        if (!held) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .testTag("adult-hold")
                        .semantics {
                            role = Role.Button
                            progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                        }.pointerInput(holdMs) {
                            detectTapGestures(
                                onPress = {
                                    val awaitRelease: suspend () -> Unit = { tryAwaitRelease() }
                                    val start = SystemClock.elapsedRealtime()
                                    coroutineScope {
                                        val ticker =
                                            launch {
                                                while (isActive) {
                                                    val elapsed = SystemClock.elapsedRealtime() - start
                                                    progress = (elapsed.toFloat() / holdMs.toFloat()).coerceIn(0f, 1f)
                                                    delay(32)
                                                }
                                            }
                                        val released =
                                            try {
                                                withTimeout(holdMs) {
                                                    awaitRelease()
                                                    true
                                                }
                                            } catch (_: TimeoutCancellationException) {
                                                false
                                            }
                                        ticker.cancel()
                                        if (!released) {
                                            progress = 1f
                                            held = true
                                        } else {
                                            progress = 0f
                                        }
                                    }
                                },
                            )
                        },
            ) {
                Text(holdLabel)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Text(
                stringResource(R.string.adult_prompt, challenge.left, challenge.right),
                modifier = Modifier.testTag("adult-challenge"),
            )
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it.filter(Char::isDigit).take(4) },
                label = { Text(stringResource(R.string.answer)) },
                modifier = Modifier.fillMaxWidth().testTag("pin-reset-answer").then(rememberBringIntoViewOnFocus()),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (answer != challenge.product.toString()) return@Button
                    onPassed()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("adult-continue"),
            ) { Text(action) }
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
