package pt.mataventuras.dominio.modelo

/**
 * Módulo curricular. Três anos: contagem, formas, números. Sete anos: operações e lógica.
 */
enum class ModuloAprendizagem {
    CONTAGEM,
    FORMAS,
    NUMEROS,
    ADICAO,
    SUBTRACAO,
    MULTIPLICACAO,
    LOGICA,
}

/**
 * Lista os módulos disponíveis para uma faixa etária.
 */
fun modulosPara(faixa: FaixaEtaria): List<ModuloAprendizagem> =
    when (faixa) {
        FaixaEtaria.TRES_ANOS ->
            listOf(
                ModuloAprendizagem.CONTAGEM,
                ModuloAprendizagem.FORMAS,
                ModuloAprendizagem.NUMEROS,
            )
        FaixaEtaria.SETE_ANOS ->
            listOf(
                ModuloAprendizagem.ADICAO,
                ModuloAprendizagem.SUBTRACAO,
                ModuloAprendizagem.MULTIPLICACAO,
                ModuloAprendizagem.LOGICA,
            )
    }
