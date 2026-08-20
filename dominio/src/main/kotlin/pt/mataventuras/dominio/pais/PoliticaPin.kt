package pt.mataventuras.dominio.pais

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Política do PIN parental: 4 dígitos, PBKDF2, bloqueio após falhas.
 */
data class EstadoPin(
    val hashHex: String,
    val salHex: String,
    val falhasSeguidas: Int,
    val bloqueadoAteEpochMs: Long,
)

/**
 * Resultado de uma tentativa de PIN.
 */
sealed class ResultadoPin {
    /** PIN correcto; o estado de falhas é limpo. */
    data object Correcto : ResultadoPin()

    /** PIN errado; [restantes] tentativas até bloqueio. */
    data class Incorrecto(val restantes: Int) : ResultadoPin()

    /** Demasiadas falhas; [desbloqueiaEmEpochMs] é o instante de desbloqueio. */
    data class Bloqueado(val desbloqueiaEmEpochMs: Long) : ResultadoPin()

    /** O valor não tem quatro dígitos. */
    data object FormatoInvalido : ResultadoPin()
}

/**
 * Regras e criptografia do PIN. Sem persistência — o repositório Android guarda [EstadoPin].
 */
class PoliticaPin(
    private val random: SecureRandom = SecureRandom(),
    private val agora: () -> Long = { System.currentTimeMillis() },
    private val iteracoes: Int = ITERACOES,
) {
    /**
     * Cria o estado inicial a partir de um PIN de 4 dígitos.
     */
    fun criar(pin: String): EstadoPin {
        require(formatoValido(pin)) { "O PIN tem de ter quatro dígitos." }
        val sal = ByteArray(TAMANHO_SAL)
        random.nextBytes(sal)
        val hash = derivar(pin, sal)
        return EstadoPin(
            hashHex = paraHex(hash),
            salHex = paraHex(sal),
            falhasSeguidas = 0,
            bloqueadoAteEpochMs = 0L,
        )
    }

    /**
     * Valida o PIN e devolve o novo estado a persistir.
     */
    fun tentar(
        estado: EstadoPin,
        pin: String,
    ): Pair<ResultadoPin, EstadoPin> {
        val agoraMs = agora()
        if (estado.bloqueadoAteEpochMs > agoraMs) {
            return ResultadoPin.Bloqueado(estado.bloqueadoAteEpochMs) to estado
        }
        if (!formatoValido(pin)) {
            return ResultadoPin.FormatoInvalido to estado
        }
        val esperado = deHex(estado.hashHex)
        val obtido = derivar(pin, deHex(estado.salHex))
        return if (tempoConstanteIguais(esperado, obtido)) {
            ResultadoPin.Correcto to estado.copy(falhasSeguidas = 0, bloqueadoAteEpochMs = 0L)
        } else {
            falhou(estado, agoraMs)
        }
    }

    /**
     * Aceita exactamente quatro dígitos 0-9.
     */
    fun formatoValido(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

    private fun falhou(
        estado: EstadoPin,
        agoraMs: Long,
    ): Pair<ResultadoPin, EstadoPin> {
        val falhas = estado.falhasSeguidas + 1
        val bloqueio = if (falhas >= MAX_FALHAS) agoraMs + BLOQUEIO_MS else 0L
        val novo = estado.copy(falhasSeguidas = falhas, bloqueadoAteEpochMs = bloqueio)
        val resultado =
            if (bloqueio > 0L) {
                ResultadoPin.Bloqueado(bloqueio)
            } else {
                ResultadoPin.Incorrecto(MAX_FALHAS - falhas)
            }
        return resultado to novo
    }

    private fun derivar(
        pin: String,
        sal: ByteArray,
    ): ByteArray {
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), sal, iteracoes, BITS_HASH)
        val factory = SecretKeyFactory.getInstance(ALGORITMO)
        return factory.generateSecret(spec).encoded
    }

    /** Parâmetros PBKDF2 e política de bloqueio. */
    companion object {
        const val MAX_FALHAS: Int = 5
        const val BLOQUEIO_MS: Long = 60_000L
        const val TAMANHO_SAL: Int = 16
        const val BITS_HASH: Int = 256
        const val ITERACOES: Int = 120_000
        const val ALGORITMO: String = "PBKDF2WithHmacSHA256"
    }
}

internal fun paraHex(bytes: ByteArray): String = bytes.joinToString(separator = "") { b -> "%02x".format(b) }

internal fun deHex(hex: String): ByteArray {
    require(hex.length % 2 == 0) { "Hex inválido." }
    return ByteArray(hex.length / 2) { i ->
        hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

internal fun tempoConstanteIguais(
    a: ByteArray,
    b: ByteArray,
): Boolean {
    if (a.size != b.size) return false
    var acc = 0
    for (i in a.indices) {
        acc = acc or (a[i].toInt() xor b[i].toInt())
    }
    return acc == 0
}
