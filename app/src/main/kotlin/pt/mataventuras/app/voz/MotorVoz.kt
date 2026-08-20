package pt.mataventuras.app.voz

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Voz em português de Portugal. Tudo no aparelho — sem serviços de nuvem.
 */
class MotorVoz(contexto: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(contexto.applicationContext, this)
    private var pronto = false

    override fun onInit(estado: Int) {
        if (estado != TextToSpeech.SUCCESS) return
        val pt = Locale("pt", "PT")
        val resultado = tts.setLanguage(pt)
        pronto = resultado != TextToSpeech.LANG_MISSING_DATA &&
            resultado != TextToSpeech.LANG_NOT_SUPPORTED
        tts.setSpeechRate(0.92f)
    }

    fun falar(texto: String) {
        if (!pronto || texto.isBlank()) return
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "mat-aventuras")
    }

    fun libertar() {
        tts.stop()
        tts.shutdown()
    }
}
