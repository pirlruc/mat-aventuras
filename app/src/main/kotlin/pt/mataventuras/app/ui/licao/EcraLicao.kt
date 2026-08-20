package pt.mataventuras.app.ui.licao

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.ContentorAplicacao
import pt.mataventuras.app.ui.tema.LocalTokensUi
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.modelo.SessaoAprendizagem
import pt.mataventuras.dominio.progresso.MotorRecompensas
import pt.mataventuras.dominio.progresso.TotaisProgresso
import pt.mataventuras.dominio.voz.GuioesVoz

@Composable
fun EcraLicao(
    contentor: ContentorAplicacao,
    perfil: PerfilCrianca,
    modulo: ModuloAprendizagem,
    onFalar: (String) -> Unit,
    onRecompensa: (FaixaEtaria) -> Unit,
    onSair: () -> Unit,
) {
    val tokens = LocalTokensUi.current
    val ambito = rememberCoroutineScope()
    var exercicio by remember { mutableStateOf(contentor.gerador.gerar(modulo)) }
    var acertos by remember { mutableIntStateOf(0) }
    var erros by remember { mutableIntStateOf(0) }
    var seguidos by remember { mutableIntStateOf(0) }
    var pontos by remember { mutableIntStateOf(perfil.pontos) }
    val inicio = remember { System.currentTimeMillis() }

    LaunchedEffect(exercicio.pergunta) { onFalar(exercicio.fala) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(exercicio.pergunta, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (exercicio.quantidadeVisual > 0 && modulo != ModuloAprendizagem.NUMEROS) {
            GrelhaEstrelas(exercicio.quantidadeVisual)
        }
        exercicio.opcoes.forEachIndexed { indice, texto ->
            Button(
                onClick = {
                    val correcto = exercicio.estaCorrecto(indice)
                    onFalar(if (correcto) GuioesVoz.MUITO_BEM else GuioesVoz.TENTA_OUTRA_VEZ)
                    val delta = contentor.recompensas.pontosDaTentativa(correcto)
                    pontos = contentor.recompensas.aplicarPontos(pontos, delta)
                    if (correcto) {
                        acertos += 1
                        seguidos += 1
                    } else {
                        erros += 1
                        seguidos = 0
                    }
                    ambito.launch {
                        val actual = contentor.repositorio.obterPerfil(perfil.id) ?: return@launch
                        contentor.repositorio.actualizarPerfil(actual.copy(pontos = pontos))
                    }
                    if (contentor.recompensas.deveAbrirRecompensa(seguidos)) {
                        onFalar(GuioesVoz.VAMOS_JOGAR)
                        onRecompensa(perfil.faixaEtaria)
                    }
                    exercicio = contentor.gerador.gerar(modulo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.tamanhoBotaoMinDp.dp),
            ) { Text(texto, fontWeight = FontWeight.Bold) }
        }
        Text("$acertos certos · $pontos pts")
        Button(onClick = {
            ambito.launch {
                persistirProgresso(contentor, perfil, pontos, modulo, acertos, erros, inicio)
                onSair()
            }
        }) { Text("Sair") }
    }
}

@Composable
private fun GrelhaEstrelas(quantidade: Int) {
    Canvas(modifier = Modifier.size(220.dp)) {
        val colunas = 5
        val passo = size.minDimension / 6f
        repeat(quantidade) { i ->
            val cx = (i % colunas) * passo + passo
            val cy = (i / colunas) * passo + passo
            drawCircle(Color(0xFFFFC107), radius = 16f, center = Offset(cx, cy))
        }
    }
}

private suspend fun persistirProgresso(
    contentor: ContentorAplicacao,
    perfil: PerfilCrianca,
    pontos: Int,
    modulo: ModuloAprendizagem,
    acertos: Int,
    erros: Int,
    inicio: Long,
) {
    val actual = contentor.repositorio.obterPerfil(perfil.id) ?: return
    contentor.repositorio.actualizarPerfil(actual.copy(pontos = pontos))
    contentor.repositorio.guardarSessao(
        SessaoAprendizagem(
            id = 0,
            perfilId = perfil.id,
            modulo = modulo,
            acertos = acertos,
            erros = erros,
            duracaoMs = System.currentTimeMillis() - inicio,
            iniciadoEmEpochMs = inicio,
        ),
    )
    val sessoes = contentor.repositorio.todasSessoes().filter { it.perfilId == perfil.id }
    val jaDist = contentor.repositorio.codigosDistintivos(perfil.id)
    val jaAv = contentor.repositorio.idsAvatares(perfil.id)
    val perfeita = erros == 0 && acertos >= MotorRecompensas.MINIMO_PERFEITO
    contentor.recompensas.distintivosNovos(
        jaDesbloqueados = jaDist,
        totais = TotaisProgresso(
            sessoesCompletas = sessoes.size,
            acertosContagem = sessoes.filter { it.modulo == ModuloAprendizagem.CONTAGEM }.sumOf { it.acertos },
            acertosFormas = sessoes.filter { it.modulo == ModuloAprendizagem.FORMAS }.sumOf { it.acertos },
            acertosContas = sessoes.filter {
                it.modulo == ModuloAprendizagem.ADICAO ||
                    it.modulo == ModuloAprendizagem.SUBTRACAO ||
                    it.modulo == ModuloAprendizagem.MULTIPLICACAO
            }.sumOf { it.acertos },
            sessaoPerfeitaComMinimo = perfeita,
            tempoTotalMs = sessoes.sumOf { it.duracaoMs },
        ),
    ).forEach { contentor.repositorio.desbloquearDistintivo(perfil.id, it.name) }
    contentor.recompensas.avataresNovos(jaAv, pontos).forEach {
        contentor.repositorio.desbloquearAvatar(perfil.id, it.name)
    }
}
