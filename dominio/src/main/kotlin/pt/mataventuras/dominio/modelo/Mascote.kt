package pt.mataventuras.dominio.modelo

/**
 * Anfitrião de um módulo. Nomes genéricos, inspirados em ícones populares, sem marcas.
 */
enum class Mascote(
    val codigo: String,
    val nomeVisivel: String,
    val corPrincipalArgb: Long,
) {
    OURICO_VELOZ("ourico_veloz", "Ouriço Veloz", 0xFF1E88E5),
    CAO_HEROI("cao_heroi", "Cão Herói", 0xFFFFB300),
    PORQUINHO_ROSA("porquinho_rosa", "Porquinho Rosa", 0xFFEC407A),
    CANALIZADOR_VALENTE("canalizador_valente", "Canalizador Valente", 0xFF43A047),
    EXTRATERRESTRE_TRAVESSO("extraterrestre_travesso", "Extraterrestre Travesso", 0xFF7E57C2),
    ;

    /** Códigos persistidos e cor de identidade. */
    companion object {
        /**
         * Resolve um código persistido. Devolve [OURICO_VELOZ] se o valor for desconhecido.
         */
        fun deCodigo(codigo: String): Mascote = entries.firstOrNull { it.codigo == codigo } ?: OURICO_VELOZ
    }
}

/**
 * Escolhe o mascote anfitrião de um módulo de aprendizagem.
 */
fun mascoteParaModulo(modulo: ModuloAprendizagem): Mascote =
    when (modulo) {
        ModuloAprendizagem.CONTAGEM -> Mascote.OURICO_VELOZ
        ModuloAprendizagem.FORMAS -> Mascote.PORQUINHO_ROSA
        ModuloAprendizagem.NUMEROS -> Mascote.CAO_HEROI
        ModuloAprendizagem.ADICAO, ModuloAprendizagem.SUBTRACAO -> Mascote.CANALIZADOR_VALENTE
        ModuloAprendizagem.MULTIPLICACAO -> Mascote.EXTRATERRESTRE_TRAVESSO
        ModuloAprendizagem.LOGICA -> Mascote.CAO_HEROI
    }
