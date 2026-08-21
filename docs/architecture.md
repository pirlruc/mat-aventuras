# Architecture — Mat Aventuras

Technical guide for the native Android educational math game.
Methodology: [github-issue-adr @ 1.2.0](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr).
Decision record: Epics **MAT-001** and **MAT-003** in `docs/issues.yml` (not ADR markdown files).
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

MAT-003 re-evaluated that plugin and **adopted Godot 4** in `:engine2d` /
`:engine3d`. Compose still never holds the engine view. Unity is not used.

## Isolation first, then one engine

Embedding Unity **and** Godot as Android libraries is the failure mode the
performance constraint warns about: two runtimes, two asset pipelines, and a
Compose Activity that cannot reclaim 200–400 MB after `finish()`. MAT-001
therefore kept playable native 2D/3D engines. MAT-003 loads **one** engine
(Godot 4) in isolated processes so the heap dies on `finish()`.

| Layer | Technology | Process |
| --- | --- | --- |
| Menus, lessons, parental PIN, leaderboard | Jetpack Compose | default |
| 2D platformer (age 3) | Godot 4 (`gl_compatibility`); native Canvas fallback | `:engine2d` |
| 2.5D off-road race (age 7) | Godot 4 Node2D; native Canvas fallback | `:engine3d` |
| Persistence | Room + DataStore | default only (engines return extras) |

The 3D Activity does **not** open Room. It returns `RESULT_FINISHED`
via `setResult`. When it finishes, the `:engine3d` process dies and the
engine heap is gone. The host Compose UI is not paused under a Godot view.

### Adopted engine: Godot 4 (MAT-003)

**Godot 4** is the reward engine. Unity-as-a-library is not used.

Godot wins on this product because it is MIT-licensed, ships as
`org.godotengine:godot` on Maven Central, starts a smaller isolated-process
heap than Unity, and has no bundled analytics. Rendering is forced to
`gl_compatibility` plus ETC2 so mid-tier tablets stay reliable. Unity would
add a heavier player, a proprietary export pipeline, and a harder INTERNET
strip.

Plugin Activities:

- age 3 → `pt.mataventuras.plugin.RunnerPluginActivity` in `:engine2d`
- age 7 → `pt.mataventuras.plugin.KartPluginActivity` in `:engine3d`

Contract: extras `mascot` / `name`, result `finished`, no Room, no `INTERNET`.
The Compose host uses `StartActivityForResult` only. Robolectric cannot load
`libgodot_android.so`, so those Activities attach the native Canvas/GLES
hosts instead of `GodotFragment`. Domain `Kart3dEngine` /
`Platformer2dEngine` stay the simulation source of truth.

See [docs/engine-plugin.md](engine-plugin.md).

Native Canvas 2D (`Platformer2dActivity`) and GLES 3D (`Kart3dActivity`)
remain in-tree as the fallback and as the unit-test hosts.

## Why Unity is not used

Embedding Unity **and** Godot together is the failure mode the performance
constraint warns about. A single engine is enough; Godot is that engine.
Unity-as-a-library would still have to live in `:engine2d` / `:engine3d`,
but it is a worse fit for APK size, heap, and a local-only privacy policy.

| Layer | Technology | Process |
| --- | --- | --- |
| Menus, lessons, parental PIN, leaderboard | Jetpack Compose | default |
| 2D platformer (age 3) | Godot 4 (`gl_compatibility`); native Canvas fallback | `:engine2d` |
| 2.5D off-road race (age 7) | Godot 4 Node2D; native Canvas fallback | `:engine3d` |
| Persistence | Room + DataStore | default only (engines return extras) |

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

## 2.5D off-road race (native Canvas)

`OffroadRacerEngine` simulates a Super Off Road-style dirt loop: distance
along a randomised circuit, lateral lane position, steer in `[-1, 1]`, a
short boost, gate pickups, and off-track slowdown when the kart leaves the
dirt. `OffroadScene` draws a rear-view scanline road (grass, rumble, dirt,
gates, kart body). `Kart3dActivity` in `:engine3d` hosts that Canvas view
and a pt-PT HUD (`Volta`, `Portões`, `Impulso`). Touch: left third steers
left, right third steers right, centre taps boost.

The oval GLES kart (`Kart3dEngine` / `KartRenderer`) remains as a
unit-testable mesh path. Production Godot and the native fallback both use
the 2D perspective racer.

## Game engine wrapper

`EngineLauncher.intentFor(context, ageGroup, mascot, name)` picks
`RunnerPluginActivity` (age 3, `:engine2d`) or `KartPluginActivity`
(age 7, `:engine3d`). `MainActivity` uses `StartActivityForResult` so Compose
is not hosting a Godot view.

On device those Activities attach `GodotFragment`. `boot.tscn` switches to
`res://kart.tscn` or `res://runner.tscn` through the `MatAventuras` plugin.
A first-time GLES restart is returned to `MainActivity`, which relaunches
the plugin Activity in a fresh isolated process. Under Robolectric they
attach `NativeKartHost` / `NativeRunnerHost` instead.

Simulation is in `:domain` (`Platformer2dEngine`, `OffroadRacerEngine`,
`Kart3dEngine`) so physics is unit-tested without an emulator.

The oval GLES kart (`Kart3dEngine` / `KartRenderer`) remains as a
unit-testable mesh path. Production Godot and the native fallback both use
the 2D perspective racer.

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
`android:allowBackup` is **false**, `fullBackupContent` is false, and
`dataExtractionRules` exclude databases, shared prefs, and files so ADB
backup and device-to-device transfer cannot copy profiles or the PIN hash.

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
| **3** | Counting 1–10 (`COUNTING`, Ouriço Veloz); shapes (`SHAPES`, Porquinho Rosa); digits 0–9 (`NUMBERS`, Cão Herói) | 2D platformer (`RunnerPluginActivity` in `:engine2d`; native Canvas fallback) |
| **7** | Addition incl. missing addend (`ADDITION`); subtraction (`SUBTRACTION`); multiplication (`MULTIPLICATION`); logic even/largest/smallest (`LOGIC`) | 2.5D off-road race (`KartPluginActivity` in `:engine3d`; native Canvas fallback) |

Age 7 confirms before leaving a lesson (`VoiceScripts.confirmExit`).
Age 3 leaves immediately. A finished reward returns `RESULT_FINISHED`;
the host speaks a pt-PT line and applies bonus points.

## Testing policy (KT-TEST-003)

- Domain tests are deterministic: inject `Random(seed)`, `SecureRandom` with a
  fixed seed, and a controllable `now` clock for PIN lockout.
- Do not mark a failing test as flaky. Quarantine means skip with a linked
  Task id and a deterministic reproduction, then delete the skip.
- No `Thread.sleep` in `:domain` tests. Time is injected.
- Retrying a red test in CI without a root-cause Task is not allowed.

## Guardrails

Pinned at `docs/guardrails/` when that submodule is cloned. GitHub Actions
reads the same numbers from `config/kotlin.thresholds.yml` (CI-022 fail-closed)
because the companion repos are private.
`:domain` kover verify is 95% line + branch. When the Android SDK is present,
`:data` and `:app` use the same numeric gate (Robolectric unit tests).
Remaining emulator instrumented tests are tracked in MAT-002-T1.

Local parity (CI-008):

```bash
python3 scripts/validate-issues.py docs/issues.yml
python3 scripts/lint-doc-links.py --root .
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
bash scripts/ci-local.sh
```

Bootstrap labels/milestones (needs write token; not done by this agent):

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```
