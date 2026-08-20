package pt.mataventuras.dados.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PerfilEntidade::class,
        SessaoEntidade::class,
        DistintivoEntidade::class,
        AvatarEntidade::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class BaseDadosMatAventuras : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun sessaoDao(): SessaoDao
    abstract fun distintivoDao(): DistintivoDao
    abstract fun avatarDao(): AvatarDao
}
