package pt.mataventuras.app.ui.idade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.tokensPara
import pt.mataventuras.dominio.voz.GuioesVoz

/**
 * Ecrã de selecção de idade. Botões enormes e voz para 3 anos; texto extra para 7 anos.
 */
@Composable
fun EcraSelecaoIdade(
    onFalar: (String) -> Unit,
    onConfirmar: (FaixaEtaria, String, Mascote) -> Unit,
) {
    var faixa by remember { mutableStateOf<FaixaEtaria?>(null) }
    var nome by remember { mutableStateOf("") }
    var mascote by remember { mutableStateOf(Mascote.OURICO_VELOZ) }
    LaunchedEffect(Unit) { onFalar(GuioesVoz.SELECAO_IDADE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(
            text = GuioesVoz.SELECAO_IDADE,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF0D47A1),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BotaoIdade(
                texto = GuioesVoz.TRES_ANOS,
                seleccionado = faixa == FaixaEtaria.TRES_ANOS,
                cor = Color(0xFF42A5F5),
                enorme = true,
                onClick = {
                    faixa = FaixaEtaria.TRES_ANOS
                    onFalar("Três anos. Vamos brincar!")
                },
            )
            BotaoIdade(
                texto = GuioesVoz.SETE_ANOS,
                seleccionado = faixa == FaixaEtaria.SETE_ANOS,
                cor = Color(0xFF66BB6A),
                enorme = false,
                onClick = {
                    faixa = FaixaEtaria.SETE_ANOS
                    onFalar("Sete anos. Pronto para desafios?")
                },
            )
        }
        if (faixa != null) {
            val tokens = tokensPara(faixa!!)
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it.take(16) },
                label = { Text("Como te chamas?") },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = tokens.tamanhoTextoCorpoSp.sp),
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Escolhe o teu amigo:", fontSize = tokens.tamanhoTextoCorpoSp.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mascote.entries.forEach { candidato ->
                    Button(
                        onClick = {
                            mascote = candidato
                            onFalar("Eu sou o ${candidato.nomeVisivel}!")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(candidato.corPrincipalArgb),
                        ),
                        modifier = Modifier
                            .size(if (faixa == FaixaEtaria.TRES_ANOS) 64.dp else 48.dp)
                            .semantics { contentDescription = candidato.nomeVisivel },
                        shape = CircleShape,
                    ) {
                        Text(candidato.nomeVisivel.take(1), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Button(
                onClick = {
                    val escolhida = faixa ?: return@Button
                    val nomeFinal = nome.trim().ifBlank { "Amigo" }
                    onConfirmar(escolhida, nomeFinal, mascote)
                },
                enabled = faixa != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.tamanhoBotaoMinDp.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text("Vamos começar!", fontSize = tokens.tamanhoTextoTituloSp.sp)
            }
        }
    }
}

@Composable
private fun BotaoIdade(
    texto: String,
    seleccionado: Boolean,
    cor: Color,
    enorme: Boolean,
    onClick: () -> Unit,
) {
    val lado = if (enorme) 180.dp else 150.dp
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(lado)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) cor else cor.copy(alpha = 0.55f),
        ),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (enorme) "🧸" else "🚀", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(texto, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
