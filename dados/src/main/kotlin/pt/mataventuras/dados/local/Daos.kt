package pt.mataventuras.dados.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {
    @Query("SELECT * FROM perfis ORDER BY pontos DESC, nome COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<PerfilEntidade>>

    @Query("SELECT * FROM perfis WHERE id = :id")
    suspend fun obter(id: Long): PerfilEntidade?

    @Insert
    suspend fun inserir(perfil: PerfilEntidade): Long

    @Update
    suspend fun actualizar(perfil: PerfilEntidade)
}

@Dao
interface SessaoDao {
    @Query("SELECT * FROM sessoes WHERE perfilId = :perfilId ORDER BY iniciadoEmEpochMs DESC")
    fun observarDe(perfilId: Long): Flow<List<SessaoEntidade>>

    @Query("SELECT * FROM sessoes")
    suspend fun todas(): List<SessaoEntidade>

    @Insert
    suspend fun inserir(sessao: SessaoEntidade): Long
}

@Dao
interface DistintivoDao {
    @Query("SELECT * FROM distintivos WHERE perfilId = :perfilId")
    fun observarDe(perfilId: Long): Flow<List<DistintivoEntidade>>

    @Query("SELECT codigo FROM distintivos WHERE perfilId = :perfilId")
    suspend fun codigosDe(perfilId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(distintivo: DistintivoEntidade): Long
}

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatares WHERE perfilId = :perfilId")
    fun observarDe(perfilId: Long): Flow<List<AvatarEntidade>>

    @Query("SELECT avatarId FROM avatares WHERE perfilId = :perfilId")
    suspend fun idsDe(perfilId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(avatar: AvatarEntidade): Long
}
