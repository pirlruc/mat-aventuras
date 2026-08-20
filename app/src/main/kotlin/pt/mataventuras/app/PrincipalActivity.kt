package pt.mataventuras.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import pt.mataventuras.app.motor.LancadorMotor
import pt.mataventuras.app.ui.navegacao.GrafoNavegacao
import pt.mataventuras.app.voz.MotorVoz

class PrincipalActivity : ComponentActivity() {
    private lateinit var voz: MotorVoz

    private val motorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        voz.falar("Boa! Vamos continuar.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MatAventurasApp
        voz = MotorVoz(this)
        setContent {
            GrafoNavegacao(
                contentor = app.contentor,
                onFalar = { voz.falar(it) },
                onRecompensa = { faixa, mascote, nome ->
                    motorLauncher.launch(LancadorMotor.intentPara(this, faixa, mascote, nome))
                },
            )
        }
    }

    override fun onDestroy() {
        voz.libertar()
        super.onDestroy()
    }
}
