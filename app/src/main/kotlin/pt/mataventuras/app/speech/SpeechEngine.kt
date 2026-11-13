package pt.mataventuras.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale
import pt.mataventuras.app.entitlement.FamilySettings
import pt.mataventuras.app.ui.languageSupported
import pt.mataventuras.app.ui.shouldSpeak

/**
 * On-device read-aloud for the selected language pack. A network voice is never used.
 */
class SpeechEngine(
    context: Context,
    private val voiceLookup: (() -> Set<Voice>?)? = null,
    private val languageResult: ((TextToSpeech) -> Int)? = null,
) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private val locale = Locale.forLanguageTag(FamilySettings(context.applicationContext).language())
    private var ready = false
    private var released = false
    private var pending: String? = null

    override fun onInit(status: Int) {
        if (released || status != TextToSpeech.SUCCESS) {
            pending = null
            return
        }
        val result = languageResult?.invoke(tts) ?: tts.setLanguage(locale)
        val voices = if (voiceLookup != null) voiceLookup.invoke() else voicesOrNull { tts.voices }
        ready = canSpeakOffline(languageSupported(result), offlineVoice(voices, locale))
        tts.setSpeechRate(0.92f)
        val queued = pending
        pending = null
        if (!ready) return
        if (queued != null) speak(queued)
    }

    /**
     * Marks the engine ready (unit tests).
     */
    internal fun markReadyForTest() {
        ready = true
    }

    /**
     * Speaks [text] when an offline voice for the pack is ready.
     */
    fun speak(text: String) {
        if (released) return
        if (!ready) {
            if (text.isNotBlank()) pending = text
            return
        }
        if (!shouldSpeak(ready, text)) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mat-aventuras")
    }

    /**
     * Stops playback without shutting the engine down.
     */
    fun stop() {
        if (released) return
        tts.stop()
    }

    /**
     * Releases the TTS engine.
     */
    fun release() {
        released = true
        ready = false
        pending = null
        tts.stop()
        tts.shutdown()
    }
}

/**
 * Voices from [read], or null when the engine cannot list them.
 */
internal fun voicesOrNull(read: () -> Set<Voice>?): Set<Voice>? =
    try {
        read()
    } catch (_: Exception) {
        null
    }

/**
 * True when [voices] contains an installed offline voice for [locale]'s language.
 */
internal fun offlineVoice(
    voices: Set<Voice>?,
    locale: Locale,
): Boolean {
    if (voices == null) return false
    return voices.any { voice -> voiceMatches(voice, locale) }
}

/**
 * Speak only when the language is installed and an offline voice exists.
 */
internal fun canSpeakOffline(
    languageOk: Boolean,
    offline: Boolean,
): Boolean = languageOk && offline

private fun voiceMatches(
    voice: Voice,
    locale: Locale,
): Boolean {
    if (voice.locale.language != locale.language) return false
    if (voice.isNetworkConnectionRequired) return false
    return !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
}
