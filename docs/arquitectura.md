# Architecture — Mat Aventuras

Technical guide for the native Android educational math game.
Methodology: [github-issue-adr @ 1.2.0](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr).
Decision record: Epic **MAT-001** in `docs/issues.yml` (not an ADR markdown file).
Guardrails pin: `docs/guardrails/` → [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.3.0`.

## Decision (Y-statement)

In the context of a native Android educational math game for ages 3 and 7,
facing Compose-to-engine RAM spikes on mid-tier tablets and a local-only
privacy constraint, we decided to host all learning and dashboards in
Jetpack Compose and run reward mini-games as dedicated Activities
(Compose Canvas 2D in-process; GLES 3D in `android:process=":motor3d"`),
rejecting Unity/Godot AAR embedding for v1, to achieve killable 3D heaps
and 100% on-device data, accepting lower cinematic fidelity until a later
Epic re-evaluates a single optional engine plugin.

## Why not Unity or Godot in v1

Embedding Unity **and** Godot as Android libraries is the failure mode the
prompt’s performance constraint warns about: two runtimes, two asset
pipelines, and a Compose Activity that cannot reclaim 200–400 MB after
`finish()`. A single exported engine is viable later (MAT-003 candidate)
if it is loaded in an isolated process and unloaded on exit. v1 ships
playable 2D/3D motors in-process Kotlin so the host APK stays lean.

| Layer | Technology | Process |
| --- | --- | --- |
| Menus, lessons, parental PIN, leaderboard | Jetpack Compose | default |
| 2D side-scroller reward (age 3) | Compose Canvas + `MotorPlataforma2D` | default |
| 3D kart reward (age 7) | GLES + `MotorKart3D` | `:motor3d` |
| Persistence | Room + DataStore | default only (3D returns extras) |

The 3D Activity does **not** open Room. It returns `RESULTADO_CONCLUIDO`
via `setResult`. When it finishes, the `:motor3d` process dies and the
GLES heap is gone. The host Compose UI is not paused under a Unity player.

### Later engine plugin (out of scope for MAT-001)

Godot 4 `aar` or Unity as a library can replace `AtividadeMotor3D` if they:

1. Live in `android:process=":motor3d"` (or `:motor2d`).
2. Speak the same `LancadorMotor` Intent contract.
3. Do not initialise networking, analytics, or cloud save.

## Modules

```
:dominio   Kotlin JVM — models, exercises, PIN, rewards, motor simulation
:dados     Android library — Room + DataStore (included only when SDK is present)
:app       Compose UI + Activities (included only when SDK is present)
```

`settings.gradle.kts` skips `:app` / `:dados` when `ANDROID_HOME` and
`local.properties` are missing, so CI can gate `:dominio` on a JDK-only
runner (KT-TEST-002 applied to `:dominio`; see `docs/guardrail-deviations.yml`).

## State and local storage

All user data stays on the device. The app declares **no `INTERNET`
permission**. No Retrofit, Firebase, or Play Games.

### Room schema (`mat_aventuras.db`, version 1)

| Table | Role |
| --- | --- |
| `perfis` | Child profile: name, `FaixaEtaria`, mascot code, avatar, points |
| `sessoes` | Lesson session: module, hits, misses, duration |
| `distintivos` | Unique `(perfilId, codigo)` badges |
| `avatares` | Unique `(perfilId, avatarId)` unlocks |

Leaderboard is a query: profiles ordered by points, then average
precision from sessions (`CalculadoraClassificacao`).

PIN state is **not** in Room. `RepositorioPin` stores PBKDF2 hash + salt
+ lockout in DataStore (`pin_pais`). Plaintext PIN is never persisted.

### Parental PIN

- 4 digits, PBKDF2-HMAC-SHA256, 120k iterations, 16-byte salt
- Constant-time compare
- 5 failures → 60 s lockout (`PoliticaPin`)

## UI/UX (pt-PT)

Visible copy, TTS, and dialogue comments are Portuguese from Portugal
(`tu`, *ecrã*, *aplicação*, *classificação*, *rectângulo*). Locale:
`pt-PT` (`res/xml/locales_config.xml`). TTS uses `Locale("pt","PT")`.

### Age-adaptive tokens (`tokensPara`)

| | 3 years | 7 years |
| --- | --- | --- |
| Min tap target | 88 dp | 56 dp |
| Title | 34 sp | 26 sp |
| Navigation | icons + voice | labelled buttons |
| Exit confirm | no | yes |
| Palette ground | cream `#FFF8E1` | sky `#E3F2FD` |

Colour is never the only signal: shapes have names and silhouettes.

### Mascots (inspired-by, no trademarks)

| Code | Visible name | Hosts |
| --- | --- | --- |
| `ourico_veloz` | Ouriço Veloz | counting |
| `cao_heroi` | Cão Herói | numbers, logic |
| `porquinho_rosa` | Porquinho Rosa | shapes |
| `canalizador_valente` | Canalizador Valente | add/sub |
| `extraterrestre_travesso` | Extraterrestre Travesso | multiply |

### Theme and icon

Primary `#1565C0`, accent `#FB8C00`, mascot colours as identity chips.
App icon: gold star + numeral on blue adaptive background (vector,
`mipmap-anydpi-v26`).

### Curriculum

- **3:** counting 1–10, shapes, digits 0–9. Reward: 2D ring-collecting runner.
- **7:** addition, subtraction, multiplication, sequences / “which is largest”.
  Reward: 3D kart; tap for a boost (stand-in for a correct mid-race sum).

Reward every 3 consecutive correct answers (`MotorRecompensas`).

## Game engine wrapper

`LancadorMotor.intentPara(context, faixa, mascote, nome)` picks
`AtividadeMotor2D` or `AtividadeMotor3D`. `PrincipalActivity` uses
`StartActivityForResult` so Compose is not hosting a GL view.

Simulation is in `:dominio` (`MotorPlataforma2D`, `MotorKart3D`) so physics
is unit-tested without an emulator.

## Guardrails

Pinned at `docs/guardrails/`. CI reads
`docs/guardrails/kotlin/profile.thresholds.yml` (CI-022 fail-closed).
`:dominio` kover verify is 95% line + branch. Android modules wait for
MAT-002 instrumented coverage (deviation on KT-TEST-002).

Local parity (CI-008):

```bash
python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 .github/scaffold/scripts/lint-doc-links.py --root .
./gradlew :dominio:ktlintCheck :dominio:detekt :dominio:test :dominio:koverVerify
python3 scripts/verificar-cobertura.py
```

Bootstrap labels/milestones (needs write token; not done by this agent):

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```
