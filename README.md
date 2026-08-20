# Mat Aventuras

Jogo educativo de matemática para Android, em **português de Portugal**,
para crianças de **3** e **7** anos. Tudo corre no aparelho: não há contas
na nuvem nem tabela de classificação online.

## O que já existe (MAT-001)

- Ecrã de selecção de idade com UI adaptativa
- Lições (contar, formas, números, contas, lógica)
- Mascotes genéricos (Ouriço Veloz, Cão Herói, Porquinho Rosa,
  Canalizador Valente, Extraterrestre Travesso)
- Prémios 2D (3 anos) e 3D em processo isolado (7 anos)
- Classificação e distintivos locais (Room)
- Painel dos pais com PIN

A decisão de arquitectura vive no Epic **MAT-001** (`docs/issues.yml`),
não num ficheiro ADR. Ver [docs/arquitectura.md](docs/arquitectura.md).

## Metodologia e guardrails

- Processo: [github-issue-adr](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr) @ tag `1.2.0`
- Guardrails: submodule `docs/guardrails/` → [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.3.0` (`0354a747`)
- Scaffold: submodule `.github/scaffold/` → [pirlruc/github-scaffold](https://github.com/pirlruc/github-scaffold) @ `1.2.0` (`aac408cc`)

CI precisa do secret `COMPANION_READ_TOKEN` (Contents: Read em `pirlruc/guardrails` e `pirlruc/github-scaffold`) para materializar os submódulos privados.

## Compilar

JDK 17+. Com o Android SDK:

```bash
echo "sdk.dir=/caminho/para/Android/Sdk" > local.properties
./gradlew :app:assembleDebug
```

Sem SDK, só o módulo de domínio (testes e cobertura):

```bash
./gradlew :dominio:test :dominio:koverVerify :dominio:detekt
python3 scripts/verificar-cobertura.py
```

## Privacidade

A aplicação **não** pede a permissão `INTERNET`. Perfis, sessões, PIN e
classificação ficam em Room/DataStore.

## Licença

Apache License 2.0.
