package pt.mataventuras.dominio.progresso

import pt.mataventuras.dominio.modelo.EntradaClassificacao
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.modelo.SessaoAprendizagem

/**
 * Classificação local: pontos descendentes, depois precisão média.
 */
class CalculadoraClassificacao {
    /**
     * Constrói a tabela de classificação dos perfis do aparelho.
     */
    fun classificar(
        perfis: List<PerfilCrianca>,
        sessoes: List<SessaoAprendizagem>,
    ): List<EntradaClassificacao> {
        val porPerfil = sessoes.groupBy { it.perfilId }
        val ordenados =
            perfis.sortedWith(
                compareByDescending<PerfilCrianca> { it.pontos }
                    .thenByDescending { precisaoMedia(porPerfil[it.id].orEmpty()) }
                    .thenBy { it.nome.lowercase() },
            )
        return ordenados.mapIndexed { indice, perfil ->
            EntradaClassificacao(
                posicao = indice + 1,
                perfilId = perfil.id,
                nome = perfil.nome,
                pontos = perfil.pontos,
                precisaoMedia = precisaoMedia(porPerfil[perfil.id].orEmpty()),
                mascote = perfil.mascoteFavorito,
            )
        }
    }

    private fun precisaoMedia(sessoes: List<SessaoAprendizagem>): Double {
        if (sessoes.isEmpty()) return 0.0
        return sessoes.map { it.precisao() }.average()
    }
}
