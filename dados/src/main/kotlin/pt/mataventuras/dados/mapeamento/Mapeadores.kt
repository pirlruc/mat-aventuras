package pt.mataventuras.dados.mapeamento

import pt.mataventuras.dados.local.AvatarEntidade
import pt.mataventuras.dados.local.DistintivoEntidade
import pt.mataventuras.dados.local.PerfilEntidade
import pt.mataventuras.dados.local.SessaoEntidade
import pt.mataventuras.dominio.modelo.AvatarDesbloqueado
import pt.mataventuras.dominio.modelo.DistintivoDesbloqueado
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.modelo.ModuloAprendizagem
import pt.mataventuras.dominio.modelo.PerfilCrianca
import pt.mataventuras.dominio.modelo.SessaoAprendizagem

fun PerfilEntidade.paraDominio(): PerfilCrianca = PerfilCrianca(
    id = id,
    nome = nome,
    faixaEtaria = FaixaEtaria.valueOf(faixaEtaria),
    mascoteFavorito = Mascote.deCodigo(mascoteCodigo),
    avatarId = avatarId,
    pontos = pontos,
    criadoEmEpochMs = criadoEmEpochMs,
)

fun PerfilCrianca.paraEntidade(): PerfilEntidade = PerfilEntidade(
    id = id,
    nome = nome,
    faixaEtaria = faixaEtaria.name,
    mascoteCodigo = mascoteFavorito.codigo,
    avatarId = avatarId,
    pontos = pontos,
    criadoEmEpochMs = criadoEmEpochMs,
)

fun SessaoEntidade.paraDominio(): SessaoAprendizagem = SessaoAprendizagem(
    id = id,
    perfilId = perfilId,
    modulo = ModuloAprendizagem.valueOf(modulo),
    acertos = acertos,
    erros = erros,
    duracaoMs = duracaoMs,
    iniciadoEmEpochMs = iniciadoEmEpochMs,
)

fun SessaoAprendizagem.paraEntidade(): SessaoEntidade = SessaoEntidade(
    id = id,
    perfilId = perfilId,
    modulo = modulo.name,
    acertos = acertos,
    erros = erros,
    duracaoMs = duracaoMs,
    iniciadoEmEpochMs = iniciadoEmEpochMs,
)

fun DistintivoEntidade.paraDominio(): DistintivoDesbloqueado =
    DistintivoDesbloqueado(codigo = codigo, desbloqueadoEmEpochMs = desbloqueadoEmEpochMs)

fun AvatarEntidade.paraDominio(): AvatarDesbloqueado =
    AvatarDesbloqueado(avatarId = avatarId, desbloqueadoEmEpochMs = desbloqueadoEmEpochMs)
