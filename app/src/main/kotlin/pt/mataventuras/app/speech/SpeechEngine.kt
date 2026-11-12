package pt.mataventuras.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import pt.mataventuras.app.ui.languageSupported
import pt.mataventuras.app.ui.shouldSpeak

/**
 * On-device TTS in Portuguese from Portugal. No cloud voice services.
 */
class SpeechEngine(
    context: Context,
) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var released = false
    private var pending: String? = null

    override fun onInit(status: Int) {
        if (released || status != TextToSpeech.SUCCESS) {
            pending = null
            return
        }
        val pt = Locale.forLanguageTag("pt-PT")
        val result = tts.setLanguage(pt)
        ready = languageSupported(result)
        tts.setSpeechRate(0.92f)
        val queued = pending
        pending = null
        if (queued != null) speak(queued)
    }

    /**
     * Marks the engine ready (unit tests).
     */
    internal fun markReadyForTest() {
        ready = true
    }

    /**
     * Speaks [text] in pt-PT when the engine is ready.
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
