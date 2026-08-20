package pt.mataventuras.dominio.matematica

import pt.mataventuras.dominio.modelo.FormaGeometrica
import pt.mataventuras.dominio.modelo.ModuloAprendizagem

/**
 * Exercício apresentado no ecrã de aprendizagem.
 */
data class Exercicio(
    val modulo: ModuloAprendizagem,
    val pergunta: String,
    val fala: String,
    val opcoes: List<String>,
    val indiceCorreto: Int,
    val quantidadeVisual: Int = 0,
    val formaAlvo: FormaGeometrica? = null,
) {
    init {
        require(opcoes.isNotEmpty()) { "Um exercício precisa de opções." }
        require(indiceCorreto in opcoes.indices) { "Índice correcto fora das opções." }
    }

    /** Verifica se a opção tocada está correcta. */
    fun estaCorrecto(indiceEscolhido: Int): Boolean = indiceEscolhido == indiceCorreto
}

/**
 * Resultado de uma tentativa, para pontuação e voz.
 */
data class ResultadoTentativa(
    val correcto: Boolean,
    val fala: String,
)
