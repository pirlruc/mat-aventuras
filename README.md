# Mat Aventuras

Native Android educational math game for children aged **3** and **7**.
User-visible UI, TTS, and dialogue are **Portuguese from Portugal**.
Source identifiers, comments, KDoc, and documentation are **English**.

Everything runs on the device: no cloud accounts and no online leaderboard.

## What exists (MAT-001)

- Age-adaptive selection UI
- Lessons (counting, shapes, numbers, arithmetic, logic)
- Generic mascots (Ouriço Veloz, Cão Herói, Porquinho Rosa,
  Canalizador Valente, Extraterrestre Travesso)
- 2D rewards (age 3) and a process-isolated oval-track 3D kart (age 7)
- Local leaderboard and badges (Room)
- PIN-gated parental dashboard

The architecture decision lives in Epic **MAT-001** (`docs/issues.yml`),
not in an ADR file. See [docs/architecture.md](docs/architecture.md).

## Methodology and guardrails

- Process: [github-issue-adr](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr) @ tag `1.2.0`
- Guardrails: submodule `docs/guardrails/` → [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.3.0` (`0354a747`)
- Scaffold: submodule `.github/scaffold/` → [pirlruc/github-scaffold](https://github.com/pirlruc/github-scaffold) @ `1.2.0` (`aac408cc`)

CI needs the secret `COMPANION_READ_TOKEN` (Contents: Read on `pirlruc/guardrails`
and `pirlruc/github-scaffold`) to materialise the private submodules.

## Build

JDK 17+. With the Android SDK:

```bash
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew :app:assembleDebug
```

Without an SDK, only the domain module (tests and coverage):

```bash
./gradlew :domain:test :domain:koverVerify :domain:detekt
python3 scripts/verify-coverage.py
```

With the SDK, the same 95% gate applies to `:data` and `:app`:

```bash
bash scripts/ci-local.sh
```

## Privacy

The app **does not** request the `INTERNET` permission. Profiles, sessions,
PIN, and the leaderboard stay in Room/DataStore.

## License

Apache License 2.0.
