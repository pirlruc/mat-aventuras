package pt.mataventuras.dominio.matematica

import kotlin.random.Random
import pt.mataventuras.dominio.modelo.FormaGeometrica
import pt.mataventuras.dominio.modelo.ModuloAprendizagem

/**
 * Gera exercícios em português de Portugal, determinísticos quando [random] é injectado.
 */
class GeradorExercicios(
    private val random: Random = Random.Default,
) {
    /**
     * Gera o próximo exercício do módulo pedido.
     */
    fun gerar(modulo: ModuloAprendizagem): Exercicio =
        when (modulo) {
            ModuloAprendizagem.CONTAGEM -> gerarContagem()
            ModuloAprendizagem.FORMAS -> gerarForma()
            ModuloAprendizagem.NUMEROS -> gerarNumero()
            ModuloAprendizagem.ADICAO -> gerarAdicao()
            ModuloAprendizagem.SUBTRACAO -> gerarSubtracao()
            ModuloAprendizagem.MULTIPLICACAO -> gerarMultiplicacao()
            ModuloAprendizagem.LOGICA -> gerarLogica()
        }

    internal fun gerarContagem(): Exercicio {
        val quantidade = random.nextInt(1, 11)
        val opcoes = opcoesNumericas(quantidade, 1, 10)
        return Exercicio(
            modulo = ModuloAprendizagem.CONTAGEM,
            pergunta = "Quantas estrelas vês?",
            fala = "Conta as estrelas. Quantas são?",
            opcoes = opcoes.map { it.toString() },
            indiceCorreto = opcoes.indexOf(quantidade),
            quantidadeVisual = quantidade,
        )
    }

    internal fun gerarForma(): Exercicio {
        val alvo = FormaGeometrica.entries[random.nextInt(FormaGeometrica.entries.size)]
        val outras = FormaGeometrica.entries.filter { it != alvo }.shuffled(random).take(3)
        val opcoes = (outras + alvo).map { it.nomeVisivel }.shuffled(random)
        return Exercicio(
            modulo = ModuloAprendizagem.FORMAS,
            pergunta = "Toca no ${alvo.nomeVisivel}.",
            fala = "Procura o ${alvo.nomeVisivel}. Toca-lhe.",
            opcoes = opcoes,
            indiceCorreto = opcoes.indexOf(alvo.nomeVisivel),
            formaAlvo = alvo,
        )
    }

    internal fun gerarNumero(): Exercicio {
        val numero = random.nextInt(0, 10)
        val opcoes = opcoesNumericas(numero, 0, 9)
        return Exercicio(
            modulo = ModuloAprendizagem.NUMEROS,
            pergunta = "Qual é o número $numero?",
            fala = "Encontra o número $numero.",
            opcoes = opcoes.map { it.toString() },
            indiceCorreto = opcoes.indexOf(numero),
            quantidadeVisual = numero,
        )
    }

    internal fun gerarAdicao(): Exercicio {
        val a = random.nextInt(1, 10)
        val b = random.nextInt(1, 10)
        val soma = a + b
        val opcoes = opcoesNumericas(soma, 2, 18)
        return Exercicio(
            modulo = ModuloAprendizagem.ADICAO,
            pergunta = "$a + $b = ?",
            fala = "Quanto é $a mais $b?",
            opcoes = opcoes.map { it.toString() },
            indiceCorreto = opcoes.indexOf(soma),
        )
    }

    internal fun gerarSubtracao(): Exercicio {
        val a = random.nextInt(2, 13)
        val b = random.nextInt(1, a)
        val diferenca = a - b
        val opcoes = opcoesNumericas(diferenca, 0, 12)
        return Exercicio(
            modulo = ModuloAprendizagem.SUBTRACAO,
            pergunta = "$a − $b = ?",
            fala = "Quanto é $a menos $b?",
            opcoes = opcoes.map { it.toString() },
            indiceCorreto = opcoes.indexOf(diferenca),
        )
    }

    internal fun gerarMultiplicacao(): Exercicio {
        val a = random.nextInt(1, 11)
        val b = random.nextInt(1, 11)
        val produto = a * b
        val opcoes = opcoesNumericas(produto, 1, 100)
        return Exercicio(
            modulo = ModuloAprendizagem.MULTIPLICACAO,
            pergunta = "$a × $b = ?",
            fala = "Quanto é $a vezes $b?",
            opcoes = opcoes.map { it.toString() },
            indiceCorreto = opcoes.indexOf(produto),
        )
    }

    internal fun gerarLogica(): Exercicio {
        return if (random.nextBoolean()) sequenciaPares() else maiorNumero()
    }

    private fun sequenciaPares(): Exercicio {
        val inicio = random.nextInt(1, 6)
        val passo = 2
        val n1 = inicio
        val n2 = inicio + passo
        val n3 = inicio + passo * 2
        val seguinte = inicio + passo * 3
        val opcoes = opcoesNumericas(seguinte, seguinte - 4, seguinte + 4)
        return Exercicio(
            modulo = ModuloAprendizagem.LOGICA,
            pergunta = "Completa: $n1, $n2, $n3, …",
            fala = "Que número vem a seguir na sequência $n1, $n2, $n3?",
            opcoes = opcoes.map { it.toString() },
            indiceCorreto = opcoes.indexOf(seguinte),
        )
    }

    private fun maiorNumero(): Exercicio {
        val valores = mutableSetOf<Int>()
        while (valores.size < 4) {
            valores += random.nextInt(1, 50)
        }
        val lista = valores.toList()
        val maior = lista.max()
        return Exercicio(
            modulo = ModuloAprendizagem.LOGICA,
            pergunta = "Qual é o maior número?",
            fala = "Toca no maior número.",
            opcoes = lista.map { it.toString() },
            indiceCorreto = lista.indexOf(maior),
        )
    }

    internal fun opcoesNumericas(
        correcto: Int,
        minimo: Int,
        maximo: Int,
    ): List<Int> {
        val conjunto = linkedSetOf(correcto)
        var tentativas = 0
        while (conjunto.size < 4 && tentativas < 40) {
            tentativas += 1
            val candidato = random.nextInt(minimo, maximo + 1)
            conjunto += candidato
        }
        var extra = minimo
        while (conjunto.size < 4) {
            if (extra != correcto) conjunto += extra
            extra += 1
        }
        return conjunto.toList().shuffled(random)
    }
}
