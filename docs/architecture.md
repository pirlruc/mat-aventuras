# Architecture — Mat Aventuras

Technical guide for the native Android educational math game.
Methodology: [github-issue-adr @ 1.9.2](https://github.com/pirlruc/methodologies/tree/1.9.2/github-issue-adr).
Decision record: Epics **MAT-001** and **MAT-003** in `docs/issues.yml` (no ADR markdown files).
Guardrails pin: `docs/guardrails/` → [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.13.1`.
Scaffold pin: `.github/scaffold/` → [pirlruc/github-scaffold](https://github.com/pirlruc/github-scaffold) @ `1.10.2`.

Language split: **code, comments, KDoc, and this documentation are English**.
**User-visible UI copy, TTS, and dialogue are Portuguese from Portugal (pt-PT).**

## Decision (Y-statement)

In the context of a native Android educational math game for ages 3–5 and 6–8,
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
| 2D prizes (ages 3–5, and sometimes 6–8) | Godot 4 (`gl_compatibility`); native Canvas fallback | `:engine2d` |
| 2.5D off-road race (ages 6–8) | Godot 4 Node2D with rivals; native Canvas fallback | `:engine3d` |
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

- ages 3–5 → `pt.mataventuras.plugin.RunnerPluginActivity` in `:engine2d`
- ages 6–8 → `pt.mataventuras.plugin.KartPluginActivity` in `:engine3d`

Contract: extras `mascot` / `name`, result `finished`, no Room, no `INTERNET`.
The Compose host uses `StartActivityForResult` only. Robolectric cannot load
`libgodot_android.so`, so those Activities attach the native Canvas/GLES
hosts instead of `GodotFragment`. Domain `OffroadRacerEngine` /
`Platformer2dEngine` stay the simulation source of truth.

See [docs/engine-plugin.md](engine-plugin.md).

Native Canvas 2D (`Platformer2dActivity`) and GLES 3D (`Kart3dActivity`)
remain in-tree as the fallback and as the unit-test hosts.

## Why Unity is not used

Embedding Unity **and** Godot together is the failure mode the performance
constraint warns about. A single engine is enough; Godot is that engine.
Unity-as-a-library would still have to live in `:engine2d` / `:engine3d`,
but it is a worse fit for APK size, heap, and a local-only privacy policy.

## Modules

```text
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
and a pt-PT HUD (`Volta`, `Lugar`, `Arcos`, `Impulso`, `META`). Touch: tap
the left or right third to steer fully; the centre band boosts. AI karts
share the loop. The yellow META gantry is the lap line you drive under.

The unused oval GLES mesh path (`Kart3dEngine` / `KartRenderer`) was
removed: production Godot and the native fallback both use the 2D
perspective racer.

## Game engine wrapper

`EngineLauncher.intentFor(context, ageGroup, mascot, name)` picks
`RunnerPluginActivity` (ages 3–5, `:engine2d`) or `KartPluginActivity`
(ages 6–8, `:engine3d`). Ages 6–8 can also draw a 2D prize.
`MainActivity` uses `StartActivityForResult` so Compose
is not hosting a Godot view.

On device those Activities attach `GodotFragment` after the host FrameLayout
has a real pixel size. `boot.gd` still waits for a sized window, then
switches to the packed prize from the `MatAventuras` plugin only if that
scene's script compiled. Invaders ends at five lost lives or an empty fleet;
chomp and climb use three lives with i-frames. A first-time GLES restart is
returned to `MainActivity`, which relaunches the plugin Activity in a fresh
isolated process. Under Robolectric they attach `NativeKartHost` /
`NativeRunnerHost` instead.

Simulation is in `:domain` (`Platformer2dEngine`, `OffroadRacerEngine`)
so physics is unit-tested without an emulator.

## State and local storage

All user data stays on the device. The app declares **no `INTERNET`
permission**. No Retrofit, Firebase, or Play Games.

### Room schema (`mat_aventuras.db`, version 3)

| Table | Role |
| --- | --- |
| `profiles` | Child profile: name, `AgeBand`, mascot code, avatar, points, prize time limit |
| `sessions` | Lesson session: module, hits, misses, duration |
| `badges` | Unique `(profileId, code)` badges |
| `avatars` | Unique `(profileId, avatarId)` unlocks |

Leaderboard is a query: profiles ordered by points, then average
accuracy from sessions (`LeaderboardCalculator`).

PIN state is **not** in Room. `PinRepository` stores the PBKDF2 hash,
salt, and lockout, sealed with a Keystore AES-GCM key. Android Keystore
failures fail closed. Robolectric tests opt into a distinct `*_plain`
private prefs file. Plaintext PIN digits are never persisted.
`android:allowBackup` is **false**, `fullBackupContent` is false, and
`dataExtractionRules` exclude databases, shared prefs, and files so ADB
backup and device-to-device transfer cannot copy profiles or the PIN hash.

### Parental PIN

- 4 digits, PBKDF2-HMAC-SHA256, 600000 iterations (a hash written at 120000 is replaced on the next correct unlock), 16-byte salt
- Sealed at rest with a Keystore AES-GCM key
- Constant-time compare
- 5 failures → 60 s lockout (`PinPolicy`)
- Unlock is a single locked read-modify-write so concurrent taps cannot skip lockout
- Godot `change_scene_to_file` paths are allowlisted to packaged prize scenes per engine kind

## UI/UX (pt-PT)

Visible copy, TTS, and spoken prompts are Portuguese from Portugal
(`tu`, *ecrã*, *aplicação*, *classificação*, *rectângulo*). Locale:
`pt-PT` (`res/xml/locales_config.xml`). TTS uses `Locale.forLanguageTag("pt-PT")`.
Identifiers for those strings (`VoiceScripts.WELL_DONE`, resource names)
are English.

### Age-adaptive tokens (`tokensFor`)

| | 3–5 | 6–8 |
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
| `mischievous_alien` | Extraterrestre Travesso | multiply, divide |

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

| Band | Lessons (Compose, mascot-hosted) | Reward mini-game |
| --- | --- | --- |
| **3–5** | Counting, shapes, numbers, addition, logic, memory, letters, odd one out, size, patterns | 2D runner, letter-climb, maze, balloon pop |
| **6–8** | Numbers, addition, subtraction, multiplication, exact division, logic, memory, letters, words, clock, money | Those 2D prizes, invaders, the kart race, and the answer snake |

Both age bands confirm before leaving a lesson (`VoiceScripts.confirmExit`).
Ages 3–5 see "Quadrado mágico" on the number square. A finished reward returns `RESULT_FINISHED`;
the host speaks a pt-PT line and applies bonus points.

`BandDifficulty.forAge` picks the ranges. Ages 3–5 stay on numbers 0–9,
sums up to 10, and logic steps of 1 or 2. Ages 6–8 use numbers up to 100,
addition from 12, and the longer step lists. NUMBERS sudoku stays the
small board on both bands.

## Testing policy (KT-TEST-003)

- Domain tests are deterministic: inject `Random(seed)`, `SecureRandom` with a
  fixed seed, and a controllable `now` clock for PIN lockout.
- A failing test is not marked flaky. A skip names a Task and a reproduction,
  then the skip is removed.
- No `Thread.sleep` in `:domain` tests. Time is injected.
- A major behaviour change adds or updates the automated tests for that behaviour.

## Guardrails

Pinned at `docs/guardrails/` (tag `1.13.1`) and `.github/scaffold/` (tag `1.10.2`),
the same two submodules [commondevops](https://github.com/pirlruc/commondevops)
uses. CI inits `docs/guardrails` with `GUARDRAILS_READ_TOKEN`
(`scripts/checkout-guardrails.sh`). Push to `main` fails closed when that
secret is missing. Other events, including Dependabot, skip and keep
`config/kotlin.thresholds.yml` (CI-022 / CI-024). Submodule SHAs must match
`docs/companion-pins.yml` (SC-DEP-004). Sync scaffold files with
`.github/scaffold/scripts/sync-templates.sh`. Synced rules are the scaffold
contract; do not hand-edit them. This repo's own docs cite methodologies
`1.9.2`.

Departures that are not lowered numbers live in `docs/guardrail-deviations.yml`
and render into Epic MAT-006. Coverage floors stay at 95.

`:domain` kover verify is 95% line + branch. When the Android SDK is present,
`:data` and `:app` use the same numeric gate (Robolectric unit tests).
KT-DOC-001's Dokka `doc_coverage` ratio applies to publishable modules. This
app does not publish API docs, so that ratio is not applicable. Public types
still carry KDoc (`scripts/verify-kdoc.py`).
`:app` and `:data` are omitted from the Gradle build when `ANDROID_HOME` is
unset (`settings.gradle.kts`). A machine with the SDK compiles them. CI
runs ktlint, detekt, unit tests, Kover, and `:app:lintDebug` on all three
modules. CodeQL compile and the APK bill are the androiddevops callers in
`ci.yml` (MAT-006-T3).

### Shared CI jobs

`.github/workflows/shared-ci.yml` calls commondevops at peeled commit
`cb34967f230a0d4841ae1efc2c98e745bd8726a7` (annotated tag `5.8.0`). `uses:`
and `scripts_ref` are that same SHA (CI-018 / CI-034). A `uses:` line cannot
carry a PAT. When an ops repo is private, this public repo still needs
Actions access to read the workflow file. Once androiddevops is private, a
public caller cannot use those workflows and must vendor the pinned file.

`ci.yml` always runs `quality`. A `changes` job skips Gradle, CodeQL, MobSF,
and the APK bill on a pull request that does not touch `app/`, `data/`,
`domain/`, `gradle/`, or `scripts/`. `gate` treats a skipped job as success.
Those four jobs call androiddevops `1.1.0`
(`bca85a8216e4e94fde2f9b54ff8d65a87af07949`) and pass
`secrets.GITHUB_TOKEN` as `checkout_token`. Fork pull requests skip the
reusable jobs that need a secret.

| Ops repo | Reuse here |
| --- | --- |
| commondevops `common-infra-lint` | Called from `shared-ci.yml` (actionlint, shellcheck, zizmor) on `ci-lint`. No Dockerfile under `docker/`, so hadolint no-ops. |
| commondevops `common-doc-verify` | Called with the caller YAML list, link lint, and markdownlint. |
| commondevops `common-scaffold-verify` | Called. In-repo `scripts/validate-issues.py` still runs on the quality job. |
| commondevops `common-secrets-sast` | Called with `semgrep_config: p/kotlin` on `ci-lint`. The workflow also loads `.semgrep.yml` when that file exists. |
| commondevops `common-supply-chain` | Called (Syft directory, Grype, Trivy fs). The APK bill is the androiddevops CycloneDX job, because dex has no Maven coordinates. |
| commondevops `common-pins-verify` | Called. `scripts/check-submodule-pins.sh` asserts the gitlinks. |
| commondevops `common-scorecard` | Called with `blocking: false` and `publish_results: false`. |
| commondevops `common-release` | Called from `release.yml` after `android-release-apk`. The release artifact is `android-release-apk`. Signing secrets are optional. |
| androiddevops | `android-gradle-ci`, `android-codeql`, `android-mobsf`, `android-apk-sbom`, and `android-release-apk`, tag `1.1.0`. |
| containerdevops | No production image or Compose stack. |
| cppdevops | No C++ sources. Godot gameplay is GDScript plus the Kotlin simulation. |
| pydevops `python-quality` | Caller is `python-quality.yml` at peeled commit `7ebee303030c09ce8f52e99660691e4c4b41238f` (annotated tag `3.3.10`). `installable_app` is false. This repo has Actions read on `ci-lint` and `ci-python`, so the job runs. |

Scheduled security jobs run on the 1st and the 15th so the gap stays inside
`security_rescan_interval_days` (CI-029).

Local parity (CI-008):

```bash
bash scripts/check-ci-local.sh
bash scripts/check-ci-docker.sh
bash scripts/check-godot.sh
```

`check-ci-local.sh` runs the host linters it finds, then `scripts/ci-local.sh`
for KDoc, licenses, privacy, and Gradle. Docker covers the linters that are
not on the host.

Issue sync (write token):

```bash
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

## Attack surface

The app has no network attacker: the merged manifest has no `INTERNET` permission. The cases this review treats as in scope are a shared or stolen device, guessing the parent PIN, installing a replaced APK, and extracting data from a backup. Backup and device transfer are disabled.

## Security review (ANDROID-SEC-002)

[OWASP MASVS](https://mas.owasp.org/MASVS/) is the reference for the controls
this app claims. It is not a scanner gate. MASVS-RESILIENCE is not claimed.
There is no remote endpoint, so certificate pinning does not apply.

| Group | Claim |
| --- | --- |
| MASVS-STORAGE | Profiles, sessions, badges, and avatars stay in the app sandbox. `allowBackup` is false and the data-extraction rules exclude databases, shared preferences, and files. |
| MASVS-CRYPTO | The parent PIN is a PBKDF2-HMAC-SHA256 hash with a 16-byte salt. The record is sealed with a Keystore AES-GCM key. A hash stored by the old EncryptedSharedPreferences file is copied across on the next unlock. A Keystore failure fails closed. New hashes use 600000 iterations; an older hash is replaced when the correct PIN unlocks. |
| MASVS-AUTH | The parent area asks for a 4-digit PIN. Five failures lock the check for 60 seconds. Comparison is constant-time. |
| MASVS-NETWORK | The merged manifest removes `INTERNET`. `usesCleartextTraffic` is false. The app has no remote API. |
| MASVS-PLATFORM | Reward activities are not exported. The launcher is exported because it is the MAIN activity. Godot's `FileProvider` and `ProcessPhoenix` are removed at merge. The app does not use a WebView. `MainActivity` stays `singleInstance` so a reward process can deliver a restart extra to the existing launcher instead of stacking a second task. Engine activities stay `singleInstance` so a dying process cannot relaunch itself. |
| MASVS-CODE | `minSdk` is 26 and `targetSdk` is 36. Release builds enable R8. Each keep rule names a Godot class or a plugin method and why. The release keystore is not in the repository (MAT-005-T11). |
| MASVS-PRIVACY | No account, no advert, and no analytics. The only permission the app keeps is `VIBRATE`. Child display names are length-capped before they are stored or spoken. |
