# Mat Aventuras

Native Android educational math game for children aged **3** and **7**.
User-visible UI, TTS, and dialogue are **Portuguese from Portugal**.
Source identifiers, comments, KDoc, and documentation are **English**.

Everything runs on the device: no cloud accounts and no online leaderboard.

![Ícone de Mat Aventuras](docs/branding/app-icon.png)

**Mat Aventuras** is a mascot-hosted math adventure. The child picks an age
band on the entry screen, then plays lesson modules that match that age.
Every three correct answers in a row opens a reward mini-game: a 2D
platform run at age 3, or a 2.5D off-road race at age 7.

## What children play

Home is a **module grid**, not a numbered campaign map. Each module is an
infinite exercise generator. The reward engine also changes with age.

| Age | Lesson modules | Reward mini-game |
| --- | --- | --- |
| **3 years** | Counting 1–10; shapes; digits 0–9 | 2D platformer (Godot in `:engine2d`) |
| **7 years** | Addition (incl. missing addend); subtraction; multiplication; logic (even sequence, largest, smallest) | Isolated Godot 2.5D off-road race in `:engine3d` |

## What exists (MAT-001)

- Branded entry / age-selection screen (description, icon, age preview)
- Age-adaptive selection UI
- Lessons (counting, shapes, numbers, arithmetic, logic)
- Generic mascots (Ouriço Veloz, Cão Herói, Porquinho Rosa,
  Canalizador Valente, Extraterrestre Travesso)
- 2D rewards (age 3) and a process-isolated 2.5D off-road race (age 7)
- Local leaderboard and badges (Room)
- PIN-gated parental dashboard
- Last-profile continue shortcut on the entry screen

The architecture decision lives in Epic **MAT-001** (`docs/issues.yml`),
not in an ADR file. See [docs/architecture.md](docs/architecture.md).

Godot 4 is the reward engine (isolated `:engine2d` / `:engine3d` processes).
Unity is not used. Native Canvas/GLES remain the unit-test fallback. See
[docs/engine-plugin.md](docs/engine-plugin.md).

## Methodology and guardrails

- Process: [github-issue-adr](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr) @ tag `1.2.0`
- Guardrails: submodule `docs/guardrails/` → [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.3.0` (`0354a747`)
- Scaffold: submodule `.github/scaffold/` → [pirlruc/github-scaffold](https://github.com/pirlruc/github-scaffold) @ `1.2.0` (`aac408cc`)

CI needs no companion token: helpers live in `scripts/` and thresholds in
`config/kotlin.thresholds.yml`. Cloning `docs/guardrails` / `.github/scaffold`
locally still needs a PAT with Contents: Read on those private repos.

## Build

JDK 17+ (Gradle Daemon Toolchain in `gradle/gradle-daemon-jvm.properties`).
With the Android SDK:

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
PIN, and the leaderboard stay in Room/DataStore. Android backup and
device-to-device transfer are disabled so those stores cannot leave the tablet.

## License

Apache License 2.0.
