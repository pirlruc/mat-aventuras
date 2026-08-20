package pt.mataventuras.dominio.modelo

/**
 * Perfil local de uma criança no mesmo aparelho.
 */
data class PerfilCrianca(
    val id: Long,
    val nome: String,
    val faixaEtaria: FaixaEtaria,
    val mascoteFavorito: Mascote,
    val avatarId: String,
    val pontos: Int,
    val criadoEmEpochMs: Long,
)

/**
 * Sessão de aprendizagem concluída, usada no painel dos pais.
 */
data class SessaoAprendizagem(
    val id: Long,
    val perfilId: Long,
    val modulo: ModuloAprendizagem,
    val acertos: Int,
    val erros: Int,
    val duracaoMs: Long,
    val iniciadoEmEpochMs: Long,
) {
    /** Precisão em 0..1. Zero se não houve tentativas. */
    fun precisao(): Double {
        val total = acertos + erros
        if (total == 0) return 0.0
        return acertos.toDouble() / total.toDouble()
    }
}

/**
 * Distintivo desbloqueado por um perfil.
 */
data class DistintivoDesbloqueado(
    val codigo: String,
    val desbloqueadoEmEpochMs: Long,
)

/**
 * Avatar desbloqueado por um perfil.
 */
data class AvatarDesbloqueado(
    val avatarId: String,
    val desbloqueadoEmEpochMs: Long,
)

/**
 * Linha da classificação local (irmãos/amigos no mesmo aparelho).
 */
data class EntradaClassificacao(
    val posicao: Int,
    val perfilId: Long,
    val nome: String,
    val pontos: Int,
    val precisaoMedia: Double,
    val mascote: Mascote,
)
