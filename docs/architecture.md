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

A Godot 4 `aar` or Unity-as-a-library Activity may replace `Kart3dActivity`
(or `Platformer2dActivity`) when it keeps this contract:

1. `android:process=":engine3d"` (or `:engine2d`) so the heap dies on `finish()`.
2. Launch extras: `EngineLauncher.EXTRA_MASCOT` (mascot code) and
   `EngineLauncher.EXTRA_NAME` (child display name).
3. Result extra: `EngineLauncher.RESULT_FINISHED` (`true` when the level
   completed). The host uses `StartActivityForResult`.
4. No `INTERNET` permission, no Room, no analytics, no cloud save.

v1 ships the native GLES kart as the fallback. The Compose host must not hold
a GL/Unity view. `EngineLauncher.PROCESS_ENGINE_3D` is `:engine3d`.

### What is still needed for Godot or Unity (MAT-003)

v1 **does not** ship Godot or Unity binaries. The host **can** adopt a stock
Godot 4 `aar` or Unity-as-a-library export without a custom game engine:
the constraint is an **isolated Android process**, not a bespoke renderer.

Already in tree (this pass):

- `EnginePluginContract` / `EnginePluginResolver` — classpath swap
- `IsolatedEngineActivity` — extras + `completeReward`
- `EngineInputMap` — shared kart touch bands
- Optional `libs/engine-plugin.aar` Gradle hook
- Templates in `samples/engine-plugin/`

See [docs/engine-plugin.md](engine-plugin.md).

A later plugin still needs:

1. A Godot 4 Android `aar` **or** a Unity-as-a-library project that provides
   `pt.mataventuras.plugin.KartPluginActivity` and/or
   `pt.mataventuras.plugin.RunnerPluginActivity`.
2. `android:process=":engine3d"` (kart) or `:engine2d` (runner) so the engine
   heap dies on `finish()`.
3. Intent extras `EXTRA_MASCOT` and `EXTRA_NAME`; result extra
   `RESULT_FINISHED` via `setResult` (`RESULT_OK` when the level completed).
4. Asset pipeline (kart, track, rings, mascot tint) matching
   `EngineInputMap` (2D: tap to jump; 3D: left/right thirds steer, centre boost).
5. Networking, analytics, and cloud-save **stripped** from the engine export.
   Confirm the merged manifest still has **no** `INTERNET` permission
   (`EnginePluginContract.manifestAllowed`).
6. The Compose host must **not** hold a GL/Unity view.
7. Emulator instrumented coverage of the swap remains MAT-002-T1.

Until an AAR is dropped in `libs/engine-plugin.aar`, the playable engines are
Kotlin Canvas (age 3) and GLES in `:engine3d` (age 7).

## Modules

```
:domain   Kotlin JVM — models, exercises, PIN, rewards, engine simulation
:data     Android library — Room + DataStore (included only when SDK is present)
:app      Compose UI + Activities (included only when SDK is present)
```

`settings.gradle.kts` skips `:app` / `:data` when `ANDROID_HOME` and
`local.properties` are missing, so a JDK-only runner can still gate `:domain`.
When the Android SDK is present (local or CI), `scripts/verify-coverage.py`
also gates `:data` and `:app` at the org 95% line and branch defaults.
`:app` Kover skips `@Composable` functions and Compose/Activity generated
lambdas (compiler restart-group branches). Screen rules live in `UiLogic`
and stay inside the 95% gate. There is no coverage deviation.

## 3D kart (native GLES)

`Kart3dEngine` simulates an oval asphalt loop in the XZ plane (`OvalTrack`):
auto-drive along the tangent, player steer in `[-1, 1]`, a short boost,
ring pickups, off-track slowdown, and a pull-back toward the centerline so
the kart returns to the asphalt. `KartScene` builds a GLES-friendly draw
list (grass, ribbon, start line, outer cones, inner barriers, kart body,
spoiler, wheels). `Kart3dActivity` in `:engine3d` only issues ES1 calls and a
pt-PT HUD (`Volta`, `Anéis`, `Impulso`). Touch: left third steers left, right
third steers right, centre taps boost.

A later Godot/Unity plugin (MAT-003-T1) may replace `Kart3dActivity` if it
keeps the same Intent extras and isolated process. v1 does **not** embed
Unity or Godot.

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
App icon: gold star inside an orange reward ring, with a small hedgehog,
on a royal-blue adaptive background (`drawable-nodpi/ic_launcher_foreground`
plus `mipmap-anydpi-v26`). Marketing asset: `docs/branding/app-icon.png`.

The entry screen (`AgeSelectionScreen`) shows that icon, the title
**Mat Aventuras**, a short pt-PT description, age-band previews, and an
optional “Continuar como …” shortcut for the last child on the device.

### Curriculum

Home is a module grid (not numbered campaign levels). Each module is an
infinite generator. Reward every 3 consecutive hits (`RewardsEngine`).
Finishing a reward Activity awards 15 bonus points on the last profile.

| Age | Lessons (Compose, mascot-hosted) | Reward mini-game |
| --- | --- | --- |
| **3** | Counting 1–10 (`COUNTING`, Ouriço Veloz); shapes (`SHAPES`, Porquinho Rosa); digits 0–9 (`NUMBERS`, Cão Herói) | 2D ring-collecting side-scroller (`Platformer2dActivity`) |
| **7** | Addition incl. missing addend (`ADDITION`); subtraction (`SUBTRACTION`); multiplication (`MULTIPLICATION`); logic even/largest/smallest (`LOGIC`) | Oval-track 3D kart with steer and boost (`Kart3dActivity` in `:engine3d`) |

Age 7 confirms before leaving a lesson (`VoiceScripts.confirmExit`).
Age 3 leaves immediately. A finished reward returns `RESULT_FINISHED`;
the host speaks a pt-PT line and applies bonus points.

## Game engine wrapper

`EngineLauncher.intentFor(context, ageGroup, mascot, name)` picks
`Platformer2dActivity` or `Kart3dActivity`. `MainActivity` uses
`StartActivityForResult` so Compose is not hosting a GL view.

Simulation is in `:domain` (`Platformer2dEngine`, `Kart3dEngine`) so physics
is unit-tested without an emulator.

The 3D renderer keeps reused `FloatBuffer`s for grass, track, start line, and
box meshes (KT-PERF-001). Scene instances come from `KartScene` in `:domain`.

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
`:domain` kover verify is 95% line + branch. When the Android SDK is present,
`:data` and `:app` use the same numeric gate (Robolectric unit tests).
Remaining emulator instrumented tests are tracked in MAT-002-T1.

Local parity (CI-008):

```bash
python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 .github/scaffold/scripts/lint-doc-links.py --root .
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
bash scripts/ci-local.sh
```

Bootstrap labels/milestones (needs write token; not done by this agent):

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```
