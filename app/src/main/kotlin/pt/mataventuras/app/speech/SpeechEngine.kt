package pt.mataventuras.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import pt.mataventuras.app.ui.UiLogic
import java.util.Locale

/**
 * On-device TTS in Portuguese from Portugal. No cloud voice services.
 */
class SpeechEngine(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var released = false

    override fun onInit(status: Int) {
        if (released || status != TextToSpeech.SUCCESS) return
        val pt = Locale("pt", "PT")
        val result = tts.setLanguage(pt)
        ready = UiLogic.languageSupported(result)
        tts.setSpeechRate(0.92f)
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
        if (released || !UiLogic.shouldSpeak(ready, text)) return
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
        tts.stop()
        tts.shutdown()
    }
}
