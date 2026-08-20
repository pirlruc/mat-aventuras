package pt.mataventuras.app.ui.pais

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.ContentorAplicacao
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.pais.ResumoParental
import pt.mataventuras.dominio.pais.ResultadoPin
import pt.mataventuras.dominio.voz.GuioesVoz
import java.util.concurrent.TimeUnit

/**
 * Painel dos pais protegido por PIN. Dados 100% locais.
 */
@Composable
fun PainelPais(
    contentor: ContentorAplicacao,
    perfil: PerfilCrianca?,
    onFalar: (String) -> Unit,
    onVoltar: () -> Unit,
) {
    val ambito = rememberCoroutineScope()
    var desbloqueado by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf(GuioesVoz.INTRODUZ_PIN) }
    var aDefinir by remember { mutableStateOf(false) }
    var resumo by remember { mutableStateOf<ResumoParental?>(null) }

    LaunchedEffect(Unit) {
        aDefinir = !contentor.repositorioPin.definido()
        mensagem = if (aDefinir) GuioesVoz.DEFINE_PIN else GuioesVoz.INTRODUZ_PIN
        onFalar(mensagem)
    }

    if (!desbloqueado) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(GuioesVoz.PAINEL_PAIS, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(mensagem)
            Spacer(Modifier.height(12.dp))
            CampoPin("PIN", pin) { pin = it.take(4) }
            if (aDefinir) {
                Spacer(Modifier.height(8.dp))
                CampoPin("Confirmar PIN", confirmacao) { confirmacao = it.take(4) }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    ambito.launch {
                        if (aDefinir) {
                            if (pin != confirmacao || !contentor.politicaPin.formatoValido(pin)) {
                                mensagem = "Os PIN não coincidem ou não têm quatro números."
                                return@launch
                            }
                            contentor.repositorioPin.guardar(contentor.politicaPin.criar(pin))
                            desbloqueado = true
                        } else {
                            val estado = contentor.repositorioPin.ler() ?: return@launch
                            val (resultado, novo) = contentor.politicaPin.tentar(estado, pin)
                            contentor.repositorioPin.guardar(novo)
                            when (resultado) {
                                ResultadoPin.Correcto -> desbloqueado = true
                                is ResultadoPin.Incorrecto -> {
                                    mensagem = "${GuioesVoz.PIN_ERRADO} Restam ${resultado.restantes}."
                                    onFalar(GuioesVoz.PIN_ERRADO)
                                }
                                is ResultadoPin.Bloqueado -> {
                                    mensagem = GuioesVoz.PIN_BLOQUEADO
                                    onFalar(GuioesVoz.PIN_BLOQUEADO)
                                }
                                ResultadoPin.FormatoInvalido -> mensagem = "O PIN tem quatro números."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (aDefinir) "Guardar PIN" else "Entrar") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
        }
        return
    }

    LaunchedEffect(perfil?.id, desbloqueado) {
        val id = perfil?.id ?: return@LaunchedEffect
        val sessoes = contentor.repositorio.todasSessoes()
        resumo = contentor.analise.resumir(id, sessoes)
    }

    val dados = resumo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(GuioesVoz.PAINEL_PAIS, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(GuioesVoz.SEM_INTERNET, style = MaterialTheme.typography.bodyMedium)
        if (perfil == null || dados == null) {
            Text("Escolhe um perfil de criança primeiro.")
        } else {
            Text(perfil.nome, style = MaterialTheme.typography.titleLarge)
            CartaoMetrica("Precisão", "${(dados.precisao * 100).toInt()} %")
            LinearProgressIndicator(
                progress = { dados.precisao.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            CartaoMetrica("Acertos / erros", "${dados.acertos} / ${dados.erros}")
            CartaoMetrica("Tempo", formatarTempo(dados.tempoTotalMs))
            Text("Por módulo", fontWeight = FontWeight.Bold)
            dados.porModulo.forEach { modulo ->
                Text(
                    "${modulo.modulo.name.lowercase()} — ${(modulo.precisao * 100).toInt()}% " +
                        "(${modulo.acertos} certos, ${formatarTempo(modulo.tempoMs)})",
                )
            }
            Text("Áreas a melhorar", fontWeight = FontWeight.Bold)
            if (dados.areasAMelhorar.isEmpty()) {
                Text("Nenhum módulo abaixo de 70% com amostra suficiente.")
            } else {
                dados.areasAMelhorar.forEach { Text("• ${it.name.lowercase()}") }
            }
        }
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("Fechar") }
    }
}

@Composable
private fun CampoPin(rotulo: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(rotulo) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun CartaoMetrica(titulo: String, valor: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(titulo)
            Text(valor, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatarTempo(ms: Long): String {
    val minutos = TimeUnit.MILLISECONDS.toMinutes(ms)
    val segundos = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return "${minutos}m ${segundos}s"
}
