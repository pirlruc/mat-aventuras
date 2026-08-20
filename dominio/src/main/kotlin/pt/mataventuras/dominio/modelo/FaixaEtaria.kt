package pt.mataventuras.dominio.modelo

/**
 * Faixa etária do perfil. Controla tokens de UI, currículo e tipo de motor de recompensa.
 */
enum class FaixaEtaria {
    TRES_ANOS,
    SETE_ANOS,
}

/**
 * Tokens de interface adaptados à idade. Valores em dp/sp lógicos, aplicados no Compose.
 */
data class TokensUi(
    val tamanhoBotaoMinDp: Int,
    val tamanhoTextoTituloSp: Int,
    val tamanhoTextoCorpoSp: Int,
    val usaNavegacaoComTexto: Boolean,
    val usaConfirmacaoAntesDeSair: Boolean,
)

/**
 * Devolve os tokens de UI para a faixa escolhida.
 */
fun tokensPara(faixa: FaixaEtaria): TokensUi =
    when (faixa) {
        FaixaEtaria.TRES_ANOS ->
            TokensUi(
                tamanhoBotaoMinDp = 88,
                tamanhoTextoTituloSp = 34,
                tamanhoTextoCorpoSp = 22,
                usaNavegacaoComTexto = false,
                usaConfirmacaoAntesDeSair = false,
            )
        FaixaEtaria.SETE_ANOS ->
            TokensUi(
                tamanhoBotaoMinDp = 56,
                tamanhoTextoTituloSp = 26,
                tamanhoTextoCorpoSp = 18,
                usaNavegacaoComTexto = true,
                usaConfirmacaoAntesDeSair = true,
            )
    }
