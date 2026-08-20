package pt.mataventuras.app.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.dominio.modelo.FaixaEtaria
import pt.mataventuras.dominio.modelo.TokensUi
import pt.mataventuras.dominio.modelo.tokensPara

val LocalTokensUi = staticCompositionLocalOf { tokensPara(FaixaEtaria.TRES_ANOS) }
val LocalFaixa = staticCompositionLocalOf { FaixaEtaria.TRES_ANOS }

private val Azul = Color(0xFF1565C0)
private val Laranja = Color(0xFFFB8C00)
private val Creme = Color(0xFFFFF8E1)
private val Ceu = Color(0xFFE3F2FD)

@Composable
fun TemaMatAventuras(
    faixa: FaixaEtaria,
    content: @Composable () -> Unit,
) {
    val tokens = tokensPara(faixa)
    val fundo = if (faixa == FaixaEtaria.TRES_ANOS) Creme else Ceu
    CompositionLocalProvider(
        LocalTokensUi provides tokens,
        LocalFaixa provides faixa,
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Azul,
                secondary = Laranja,
                background = fundo,
                surface = Color.White,
            ),
            content = content,
        )
    }
}

fun TokensUi.raioBotao() = RoundedCornerShape(if (tamanhoBotaoMinDp >= 80) 28.dp else 16.dp)

val TokensUi.tituloSp get() = tamanhoTextoTituloSp.sp
val TokensUi.corpoSp get() = tamanhoTextoCorpoSp.sp
