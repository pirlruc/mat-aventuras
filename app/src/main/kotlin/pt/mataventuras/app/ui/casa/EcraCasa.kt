package pt.mataventuras.app.ui.casa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.ui.tema.LocalTokensUi
import pt.mataventuras.app.ui.tema.tituloSp
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.modelo.mascoteParaModulo
import pt.mataventuras.dominio.modelo.modulosPara
import pt.mataventuras.dominio.voz.GuioesVoz

@Composable
fun EcraCasa(
    perfil: PerfilCrianca,
    onFalar: (String) -> Unit,
    onModulo: (ModuloAprendizagem) -> Unit,
    onClassificacao: () -> Unit,
    onPais: () -> Unit,
) {
    val tokens = LocalTokensUi.current
    val modulos = modulosPara(perfil.faixaEtaria)
    LaunchedEffect(perfil.id) {
        onFalar(GuioesVoz.saudacao(perfil.mascoteFavorito, perfil.faixaEtaria))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Olá, ${perfil.nome}!", fontSize = tokens.tituloSp, fontWeight = FontWeight.ExtraBold)
        Text("${perfil.pontos} pontos", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (perfil.faixaEtaria == FaixaEtaria.TRES_ANOS) 1 else 2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(modulos, key = { it.name }) { modulo ->
                val mascote = mascoteParaModulo(modulo)
                Button(
                    onClick = {
                        onFalar("Vamos com o ${mascote.nomeVisivel}!")
                        onModulo(modulo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.tamanhoBotaoMinDp.dp + 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(mascote.corPrincipalArgb)),
                ) {
                    Text(tituloModulo(modulo, mascote), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (perfil.faixaEtaria == FaixaEtaria.SETE_ANOS) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onClassificacao, modifier = Modifier.weight(1f)) {
                    Text(GuioesVoz.CLASSIFICACAO)
                }
                TextButton(onClick = onPais) { Text(GuioesVoz.PAINEL_PAIS) }
            }
        } else {
            TextButton(onClick = onClassificacao) { Text("★") }
            TextButton(onClick = onPais) { Text("· · ·") }
        }
    }
}

private fun tituloModulo(modulo: ModuloAprendizagem, mascote: Mascote): String = when (modulo) {
    ModuloAprendizagem.CONTAGEM -> "Contar com o ${mascote.nomeVisivel}"
    ModuloAprendizagem.FORMAS -> "Formas com o ${mascote.nomeVisivel}"
    ModuloAprendizagem.NUMEROS -> "Números com o ${mascote.nomeVisivel}"
    ModuloAprendizagem.ADICAO -> "Somar com o ${mascote.nomeVisivel}"
    ModuloAprendizagem.SUBTRACAO -> "Subtrair com o ${mascote.nomeVisivel}"
    ModuloAprendizagem.MULTIPLICACAO -> "Multiplicar com o ${mascote.nomeVisivel}"
    ModuloAprendizagem.LOGICA -> "Lógica com o ${mascote.nomeVisivel}"
}
