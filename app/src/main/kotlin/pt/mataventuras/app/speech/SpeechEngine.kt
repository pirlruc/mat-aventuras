package pt.mataventuras.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * On-device TTS in Portuguese from Portugal. No cloud voice services.
 */
class SpeechEngine(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val pt = Locale("pt", "PT")
        val result = tts.setLanguage(pt)
        ready = result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
        tts.setSpeechRate(0.92f)
    }

    /**
     * Speaks [text] in pt-PT when the engine is ready.
     */
    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mat-aventuras")
    }

    /**
     * Releases the TTS engine.
     */
    fun release() {
        tts.stop()
        tts.shutdown()
    }
}
