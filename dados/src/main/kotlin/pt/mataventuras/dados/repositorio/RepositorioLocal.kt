package pt.mataventuras.dados.repositorio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.mataventuras.dados.local.AvatarEntidade
import pt.mataventuras.dados.local.BaseDadosMatAventuras
import pt.mataventuras.dados.local.DistintivoEntidade
import pt.mataventuras.dados.mapeamento.paraDominio
import pt.mataventuras.dados.mapeamento.paraEntidade
import pt.mataventuras.dominio.modelo.AvatarDesbloqueado
import pt.mataventuras.dominio.modelo.DistintivoDesbloqueado
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.modelo.SessaoAprendizagem
import pt.mataventuras.dominio.progresso.CodigoAvatar

class RepositorioLocal(
    private val base: BaseDadosMatAventuras,
    private val agora: () -> Long = { System.currentTimeMillis() },
) {
    fun observarPerfis(): Flow<List<PerfilCrianca>> =
        base.perfilDao().observarTodos().map { lista -> lista.map { it.paraDominio() } }

    fun observarSessoes(perfilId: Long): Flow<List<SessaoAprendizagem>> =
        base.sessaoDao().observarDe(perfilId).map { lista -> lista.map { it.paraDominio() } }

    fun observarDistintivos(perfilId: Long): Flow<List<DistintivoDesbloqueado>> =
        base.distintivoDao().observarDe(perfilId).map { lista -> lista.map { it.paraDominio() } }

    fun observarAvatares(perfilId: Long): Flow<List<AvatarDesbloqueado>> =
        base.avatarDao().observarDe(perfilId).map { lista -> lista.map { it.paraDominio() } }

    suspend fun criarPerfil(nome: String, faixa: FaixaEtaria, mascote: Mascote): Long {
        val agoraMs = agora()
        val id = base.perfilDao().inserir(
            PerfilCrianca(
                id = 0,
                nome = nome.trim(),
                faixaEtaria = faixa,
                mascoteFavorito = mascote,
                avatarId = CodigoAvatar.INICIAL.name,
                pontos = 0,
                criadoEmEpochMs = agoraMs,
            ).paraEntidade().copy(id = 0),
        )
        base.avatarDao().inserir(
            AvatarEntidade(
                perfilId = id,
                avatarId = CodigoAvatar.INICIAL.name,
                desbloqueadoEmEpochMs = agoraMs,
            ),
        )
        return id
    }

    suspend fun actualizarPerfil(perfil: PerfilCrianca) {
        base.perfilDao().actualizar(perfil.paraEntidade())
    }

    suspend fun obterPerfil(id: Long): PerfilCrianca? = base.perfilDao().obter(id)?.paraDominio()

    suspend fun todasSessoes(): List<SessaoAprendizagem> =
        base.sessaoDao().todas().map { it.paraDominio() }

    suspend fun guardarSessao(sessao: SessaoAprendizagem): Long =
        base.sessaoDao().inserir(sessao.paraEntidade().copy(id = 0))

    suspend fun desbloquearDistintivo(perfilId: Long, codigo: String) {
        base.distintivoDao().inserir(
            DistintivoEntidade(
                perfilId = perfilId,
                codigo = codigo,
                desbloqueadoEmEpochMs = agora(),
            ),
        )
    }

    suspend fun desbloquearAvatar(perfilId: Long, avatarId: String) {
        base.avatarDao().inserir(
            AvatarEntidade(
                perfilId = perfilId,
                avatarId = avatarId,
                desbloqueadoEmEpochMs = agora(),
            ),
        )
    }

    suspend fun codigosDistintivos(perfilId: Long): Set<String> =
        base.distintivoDao().codigosDe(perfilId).toSet()

    suspend fun idsAvatares(perfilId: Long): Set<String> =
        base.avatarDao().idsDe(perfilId).toSet()
}
