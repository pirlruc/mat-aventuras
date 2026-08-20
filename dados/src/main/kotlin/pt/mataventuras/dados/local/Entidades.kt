package pt.mataventuras.dados.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "perfis")
data class PerfilEntidade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val faixaEtaria: String,
    val mascoteCodigo: String,
    val avatarId: String,
    val pontos: Int,
    val criadoEmEpochMs: Long,
)

@Entity(
    tableName = "sessoes",
    indices = [Index("perfilId")],
)
data class SessaoEntidade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    val modulo: String,
    val acertos: Int,
    val erros: Int,
    val duracaoMs: Long,
    val iniciadoEmEpochMs: Long,
)

@Entity(
    tableName = "distintivos",
    indices = [Index(value = ["perfilId", "codigo"], unique = true)],
)
data class DistintivoEntidade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    val codigo: String,
    val desbloqueadoEmEpochMs: Long,
)

@Entity(
    tableName = "avatares",
    indices = [Index(value = ["perfilId", "avatarId"], unique = true)],
)
data class AvatarEntidade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    val avatarId: String,
    val desbloqueadoEmEpochMs: Long,
)
