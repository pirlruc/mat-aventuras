package pt.mataventuras.app.ui.navegacao

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import pt.mataventuras.app.di.ContentorAplicacao
import pt.mataventuras.app.ui.casa.EcraCasa
import pt.mataventuras.app.ui.idade.EcraSelecaoIdade
import pt.mataventuras.app.ui.licao.EcraLicao
import pt.mataventuras.app.ui.pais.PainelPais
import pt.mataventuras.app.ui.recompensas.ClassificacaoERecompensas
import pt.mataventuras.app.ui.tema.TemaMatAventuras
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.PerfilCrianca

private sealed interface Destino {
    data object Selecao : Destino
    data object Casa : Destino
    data class Licao(val modulo: ModuloAprendizagem) : Destino
    data object Classificacao : Destino
    data object Pais : Destino
}

@Composable
fun GrafoNavegacao(
    contentor: ContentorAplicacao,
    onFalar: (String) -> Unit,
    onRecompensa: (FaixaEtaria, Mascote, String) -> Unit,
) {
    val ambito = rememberCoroutineScope()
    var destino by remember { mutableStateOf<Destino>(Destino.Selecao) }
    var perfil by remember { mutableStateOf<PerfilCrianca?>(null) }
    val faixa = perfil?.faixaEtaria ?: FaixaEtaria.TRES_ANOS

    TemaMatAventuras(faixa) {
        when (val actual = destino) {
            Destino.Selecao -> EcraSelecaoIdade(
                onFalar = onFalar,
                onConfirmar = { escolhida, nome, mascote ->
                    ambito.launch {
                        val id = contentor.repositorio.criarPerfil(nome, escolhida, mascote)
                        perfil = contentor.repositorio.obterPerfil(id)
                        destino = Destino.Casa
                    }
                },
            )
            Destino.Casa -> perfil?.let { p ->
                EcraCasa(
                    perfil = p,
                    onFalar = onFalar,
                    onModulo = { destino = Destino.Licao(it) },
                    onClassificacao = { destino = Destino.Classificacao },
                    onPais = { destino = Destino.Pais },
                )
            }
            is Destino.Licao -> perfil?.let { p ->
                EcraLicao(
                    contentor = contentor,
                    perfil = p,
                    modulo = actual.modulo,
                    onFalar = onFalar,
                    onRecompensa = { faixa -> onRecompensa(faixa, p.mascoteFavorito, p.nome) },
                    onSair = {
                        ambito.launch {
                            perfil = contentor.repositorio.obterPerfil(p.id) ?: p
                            destino = Destino.Casa
                        }
                    },
                )
            }
            Destino.Classificacao -> ClassificacaoERecompensas(
                contentor = contentor,
                perfilActivo = perfil,
                onVoltar = { destino = Destino.Casa },
            )
            Destino.Pais -> PainelPais(
                contentor = contentor,
                perfil = perfil,
                onFalar = onFalar,
                onVoltar = { destino = Destino.Casa },
            )
        }
    }
}
