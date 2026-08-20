package pt.mataventuras.dominio.modelo

/**
 * Forma geométrica usada nos exercícios dos 3 anos. Cor e silhueta viajam juntas (daltonismo).
 */
enum class FormaGeometrica(val nomeVisivel: String) {
    CIRCULO("círculo"),
    QUADRADO("quadrado"),
    TRIANGULO("triângulo"),
    RETANGULO("rectângulo"),
    ESTRELA("estrela"),
}

/**
 * Tipo de motor de recompensa. 2D para 3 anos; 3D para 7 anos.
 */
enum class TipoMotor {
    BIDIMENSIONAL,
    TRIDIMENSIONAL,
}

/**
 * Escolhe o motor de recompensa da faixa etária.
 */
fun tipoMotorPara(faixa: FaixaEtaria): TipoMotor =
    when (faixa) {
        FaixaEtaria.TRES_ANOS -> TipoMotor.BIDIMENSIONAL
        FaixaEtaria.SETE_ANOS -> TipoMotor.TRIDIMENSIONAL
    }
