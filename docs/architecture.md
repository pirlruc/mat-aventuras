# Architecture — Mat Aventuras

Technical guide for the native Android educational math game.
Methodology: [github-issue-adr @ 1.2.0](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr).
Decision record: Epic **MAT-001** in `docs/issues.yml` (not an ADR markdown file).
Guardrails pin: `docs/guardrails/` → [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.3.0`.

Language split: **code, comments, KDoc, and this documentation are English**.
**User-visible UI copy, TTS, and dialogue are Portuguese from Portugal (pt-PT).**

## Decision (Y-statement)

In the context of a native Android educational math game for ages 3 and 7,
facing Compose-to-engine RAM spikes on mid-tier tablets and a local-only
privacy constraint, we decided to host all learning and dashboards in
Jetpack Compose and run reward mini-games as dedicated Activities
(Compose Canvas 2D in-process; GLES 3D in `android:process=":engine3d"`),
rejecting Unity/Godot AAR embedding for v1, to achieve killable 3D heaps
and 100% on-device data, accepting lower cinematic fidelity until a later
Epic re-evaluates a single optional engine plugin.

## Why not Unity or Godot in v1

Embedding Unity **and** Godot as Android libraries is the failure mode the
prompt’s performance constraint warns about: two runtimes, two asset
pipelines, and a Compose Activity that cannot reclaim 200–400 MB after
`finish()`. A single exported engine is viable later (MAT-003) if it is
loaded in an isolated process and unloaded on exit. v1 ships playable 2D/3D
engines in-process Kotlin so the host APK stays lean.

| Layer | Technology | Process |
| --- | --- | --- |
| Menus, lessons, parental PIN, leaderboard | Jetpack Compose | default |
| 2D side-scroller reward (age 3) | Compose Canvas + `Platformer2dEngine` | default |
| 3D kart reward (age 7) | GLES + `Kart3dEngine` | `:engine3d` |
| Persistence | Room + DataStore | default only (3D returns extras) |

The 3D Activity does **not** open Room. It returns `RESULT_FINISHED`
via `setResult`. When it finishes, the `:engine3d` process dies and the
GLES heap is gone. The host Compose UI is not paused under a Unity player.

### Later engine plugin (MAT-003)

Godot 4 `aar` or Unity as a library can replace `Kart3dActivity` if they:

1. Live in `android:process=":engine3d"` (or `:engine2d`).
2. Speak the same `EngineLauncher` Intent contract.
3. Do not initialise networking, analytics, or cloud save.

## Modules

```
:domain   Kotlin JVM — models, exercises, PIN, rewards, engine simulation
:data     Android library — Room + DataStore (included only when SDK is present)
:app      Compose UI + Activities (included only when SDK is present)
```

`settings.gradle.kts` skips `:app` / `:data` when `ANDROID_HOME` and
`local.properties` are missing, so CI can gate `:domain` on a JDK-only
runner (KT-TEST-002 applied to `:domain`; see `docs/guardrail-deviations.yml`).

## State and local storage

All user data stays on the device. The app declares **no `INTERNET`
permission**. No Retrofit, Firebase, or Play Games.

### Room schema (`mat_aventuras.db`, version 1)

| Table | Role |
| --- | --- |
| `profiles` | Child profile: name, `AgeGroup`, mascot code, avatar, points |
| `sessions` | Lesson session: module, hits, misses, duration |
| `badges` | Unique `(profileId, code)` badges |
| `avatars` | Unique `(profileId, avatarId)` unlocks |

Leaderboard is a query: profiles ordered by points, then average
accuracy from sessions (`LeaderboardCalculator`).

PIN state is **not** in Room. `PinRepository` stores PBKDF2 hash + salt
+ lockout in DataStore (`parent_pin`). Plaintext PIN is never persisted.

### Parental PIN

- 4 digits, PBKDF2-HMAC-SHA256, 120k iterations, 16-byte salt
- Constant-time compare
- 5 failures → 60 s lockout (`PinPolicy`)

## UI/UX (pt-PT)

Visible copy, TTS, and spoken prompts are Portuguese from Portugal
(`tu`, *ecrã*, *aplicação*, *classificação*, *rectângulo*). Locale:
`pt-PT` (`res/xml/locales_config.xml`). TTS uses `Locale("pt","PT")`.
Identifiers for those strings (`VoiceScripts.WELL_DONE`, resource names)
are English.

### Age-adaptive tokens (`tokensFor`)

| | 3 years | 7 years |
| --- | --- | --- |
| Min tap target | 88 dp | 56 dp |
| Title | 34 sp | 26 sp |
| Navigation | icons + voice | labelled buttons |
| Exit confirm | no | yes |
| Palette ground | cream `#FFF8E1` | sky `#E3F2FD` |

Colour is never the only signal: shapes have names and silhouettes.

### Mascots (inspired-by, no trademarks)

| Code | Display name (pt-PT) | Hosts |
| --- | --- | --- |
| `speedy_hedgehog` | Ouriço Veloz | counting |
| `hero_pup` | Cão Herói | numbers, logic |
| `pink_piglet` | Porquinho Rosa | shapes |
| `brave_plumber` | Canalizador Valente | add/sub |
| `mischievous_alien` | Extraterrestre Travesso | multiply |

### Theme and icon

Primary `#1565C0`, accent `#FB8C00`, mascot colours as identity chips.
App icon: gold star + numeral on blue adaptive background (vector,
`mipmap-anydpi-v26`).

### Curriculum

- **3:** counting 1–10, shapes, digits 0–9. Reward: 2D ring-collecting runner.
- **7:** addition, subtraction, multiplication, sequences / “which is largest”.
  Reward: 3D kart; tap for a boost (stand-in for a correct mid-race sum).

Reward every 3 consecutive correct answers (`RewardsEngine`).

## Game engine wrapper

`EngineLauncher.intentFor(context, ageGroup, mascot, name)` picks
`Platformer2dActivity` or `Kart3dActivity`. `MainActivity` uses
`StartActivityForResult` so Compose is not hosting a GL view.

Simulation is in `:domain` (`Platformer2dEngine`, `Kart3dEngine`) so physics
is unit-tested without an emulator.

The 3D renderer keeps a reused `FloatBuffer` for the cube mesh (KT-PERF-001).

## Testing policy (KT-TEST-003)

- Domain tests are deterministic: inject `Random(seed)`, `SecureRandom` with a
  fixed seed, and a controllable `now` clock for PIN lockout.
- Do not mark a failing test as flaky. Quarantine means skip with a linked
  Task id and a deterministic reproduction, then delete the skip.
- No `Thread.sleep` in `:domain` tests. Time is injected.
- Retrying a red test in CI without a root-cause Task is not allowed.

## Guardrails

Pinned at `docs/guardrails/`. CI reads
`docs/guardrails/kotlin/profile.thresholds.yml` (CI-022 fail-closed).
`:domain` kover verify is 95% line + branch. Android modules wait for
MAT-002 instrumented coverage (deviation on KT-TEST-002). Remaining
CI/SAST/docs gates are tracked in MAT-004.

Local parity (CI-008):

```bash
python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 .github/scaffold/scripts/lint-doc-links.py --root .
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
```

Bootstrap labels/milestones (needs write token; not done by this agent):

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```
