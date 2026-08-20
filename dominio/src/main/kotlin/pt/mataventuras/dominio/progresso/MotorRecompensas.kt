package pt.mataventuras.dominio.progresso

/**
 * Catálogo de distintivos digitais. Códigos estáveis persistidos no Room.
 */
enum class CodigoDistintivo(val titulo: String, val descricao: String) {
    PRIMEIROS_PASSOS("Primeiros passos", "Completaste a primeira sessão."),
    CONTADOR_ESTRELAS("Contador de estrelas", "Acertaste 10 contagens."),
    MESTRE_FORMAS("Mestre das formas", "Acertaste 10 formas."),
    CALCULADOR_RAPIDO("Calculador rápido", "Acertaste 20 contas."),
    ESTRELA_DO_DIA("Estrela do dia", "Sessão perfeita com pelo menos 5 perguntas."),
    MARATONISTA("Maratonista", "Jogaste 30 minutos no total."),
}

/**
 * Avatares desbloqueáveis. O código inicial está sempre disponível.
 */
enum class CodigoAvatar(val titulo: String, val pontosMinimos: Int) {
    INICIAL("Explorador", 0),
    CORREDOR("Corredor", 50),
    ASTRONAUTA("Astronauta", 150),
    CAPITAO("Capitão", 300),
    LENDA("Lenda", 500),
}

/**
 * Totais usados para decidir distintivos novos.
 */
data class TotaisProgresso(
    val sessoesCompletas: Int,
    val acertosContagem: Int,
    val acertosFormas: Int,
    val acertosContas: Int,
    val sessaoPerfeitaComMinimo: Boolean,
    val tempoTotalMs: Long,
)

/**
 * Pontuação e desbloqueios locais. Sem rede.
 */
class MotorRecompensas {
    /**
     * Pontos ganhos numa tentativa.
     */
    fun pontosDaTentativa(correcto: Boolean): Int = if (correcto) PONTOS_ACERTO else PONTOS_ERRO

    /**
     * Novo total, nunca negativo.
     */
    fun aplicarPontos(
        actual: Int,
        delta: Int,
    ): Int = (actual + delta).coerceAtLeast(0)

    /**
     * Distintivos novos após uma sessão, dados os totais acumulados.
     */
    fun distintivosNovos(
        jaDesbloqueados: Set<String>,
        totais: TotaisProgresso,
    ): List<CodigoDistintivo> {
        val candidatos = mutableListOf<CodigoDistintivo>()
        if (totais.sessoesCompletas >= 1) candidatos += CodigoDistintivo.PRIMEIROS_PASSOS
        if (totais.acertosContagem >= 10) candidatos += CodigoDistintivo.CONTADOR_ESTRELAS
        if (totais.acertosFormas >= 10) candidatos += CodigoDistintivo.MESTRE_FORMAS
        if (totais.acertosContas >= 20) candidatos += CodigoDistintivo.CALCULADOR_RAPIDO
        if (totais.sessaoPerfeitaComMinimo) candidatos += CodigoDistintivo.ESTRELA_DO_DIA
        if (totais.tempoTotalMs >= TRINTA_MINUTOS_MS) candidatos += CodigoDistintivo.MARATONISTA
        return candidatos.filter { it.name !in jaDesbloqueados }
    }

    /**
     * Avatares que o total de pontos já permite e que ainda não estão na colecção.
     */
    fun avataresNovos(
        jaDesbloqueados: Set<String>,
        pontos: Int,
    ): List<CodigoAvatar> =
        CodigoAvatar.entries.filter { avatar ->
            pontos >= avatar.pontosMinimos && avatar.name !in jaDesbloqueados
        }

    /**
     * A cada [ACERTOS_PARA_RECOMPENSA] acertos seguidos, abre um nível de recompensa.
     */
    fun deveAbrirRecompensa(acertosSeguidos: Int): Boolean {
        if (acertosSeguidos <= 0) return false
        return acertosSeguidos % ACERTOS_PARA_RECOMPENSA == 0
    }

    /** Constantes de pontuação e limiares. */
    companion object {
        const val PONTOS_ACERTO: Int = 10
        const val PONTOS_ERRO: Int = -2
        const val ACERTOS_PARA_RECOMPENSA: Int = 3
        const val TRINTA_MINUTOS_MS: Long = 30L * 60L * 1000L
        const val MINIMO_PERFEITO: Int = 5
    }
}
