package pt.mataventuras.app.ui.recompensas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.di.ContentorAplicacao
import pt.mataventuras.dominio.modelo.AvatarDesbloqueado
import pt.mataventuras.dominio.modelo.DistintivoDesbloqueado
import pt.mataventuras.dominio.modelo.EntradaClassificacao
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.progresso.CodigoAvatar
import pt.mataventuras.dominio.progresso.CodigoDistintivo
import pt.mataventuras.dominio.voz.GuioesVoz

/**
 * Classificação local e colecção de recompensas do aparelho.
 */
@Composable
fun ClassificacaoERecompensas(
    contentor: ContentorAplicacao,
    perfilActivo: PerfilCrianca?,
    onVoltar: () -> Unit,
) {
    val tabela by produceState(emptyList<EntradaClassificacao>()) {
        contentor.repositorio.observarPerfis().collect { perfis ->
            value = contentor.classificacao.classificar(perfis, contentor.repositorio.todasSessoes())
        }
    }
    val distintivos by produceState(emptyList<DistintivoDesbloqueado>(), perfilActivo?.id) {
        if (perfilActivo == null) {
            value = emptyList()
            return@produceState
        }
        contentor.repositorio.observarDistintivos(perfilActivo.id).collect { value = it }
    }
    val avatares by produceState(emptyList<AvatarDesbloqueado>(), perfilActivo?.id) {
        if (perfilActivo == null) {
            value = emptyList()
            return@produceState
        }
        contentor.repositorio.observarAvatares(perfilActivo.id).collect { value = it }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(GuioesVoz.CLASSIFICACAO, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Só neste aparelho — irmãos e amigos.")
        }
        items(tabela, key = { it.perfilId }) { linha ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("${linha.posicao}º", fontWeight = FontWeight.Black)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(linha.mascote.corPrincipalArgb)),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(linha.nome, fontWeight = FontWeight.Bold)
                        Text("${linha.pontos} pts · ${(linha.precisaoMedia * 100).toInt()}% precisos")
                    }
                }
            }
        }
        item {
            Text(GuioesVoz.RECOMPENSAS, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Distintivos")
        }
        items(CodigoDistintivo.entries.toList(), key = { it.name }) { codigo ->
            val tem = distintivos.any { it.codigo == codigo.name }
            Text(if (tem) "★ ${codigo.titulo}" else "☆ ${codigo.titulo} — ${codigo.descricao}")
        }
        item { Text("Avatares") }
        items(CodigoAvatar.entries.toList(), key = { it.name }) { codigo ->
            val tem = avatares.any { it.avatarId == codigo.name }
            Text(if (tem) "★ ${codigo.titulo}" else "☆ ${codigo.titulo} (${codigo.pontosMinimos} pts)")
        }
        item {
            Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
        }
    }
}
