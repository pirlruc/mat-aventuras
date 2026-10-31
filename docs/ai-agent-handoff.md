# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-10-30
**Last agent focus:** companion pins at guardrails 1.13.1 and scaffold 1.10.2

---

## What this repo is

Native Kotlin/Compose educational math game for ages 3 and 7.
**UI/TTS/dialogue:** Portuguese from Portugal.
**Code, comments, KDoc, documentation:** English.
Privacy: on-device only. Decision log: GitHub Epics via github-issue-adr;
authored backlog is `docs/issues.yml`.

Reward engines: **Godot 4** in `:engine2d` (age 3 platformer, letter-climb,
maze) and `:engine3d` (age 7 2.5D off-road race with rivals). Age 7 can also
open 2D invaders/maze/climb. Unity is not used. Native Canvas is the
Robolectric fallback (`OffroadRacerEngine` / `Platformer2dEngine`, and
`ArcadeBoardView` for invaders, chomp, and climb).

## Pins

Documented in `docs/companion-pins.yml` (SC-DEP-004). CI asserts gitlink SHA
equals the recorded sha.

| Companion | How | Value |
| --- | --- | --- |
| methodologies | annotated tag in docs | `1.9.2` |
| guardrails | submodule SHA `docs/guardrails/` | `1945211a` (tag `1.13.1`) |
| github-scaffold | submodule SHA `.github/scaffold/` | `c8eddcfb` (tag `1.10.2`) |
| Godot Android library | `gradle/libs.versions.toml` | `org.godotengine:godot:4.7.1.stable` |
| Detekt Gradle plugin | `gradle/libs.versions.toml` | `dev.detekt` `2.0.0-alpha.6` |
| actionlint | `scripts/run-actionlint.sh` | `1.7.12` (commondevops ci-lint pin) |
| commondevops | `.github/workflows/shared-ci.yml` | tag `5.6.2`, peeled `b85f95bb` |
| pydevops `python-quality` | `.github/workflows/python-quality.yml` | tag `3.3.1`, peeled `55f4e52f` |
| ci-lint image | `scripts/check-ci-docker.sh` | `sha256:f9001c4c` |

Stay on annotated tags (SC-DEP-004). Synced scaffold rules are the scaffold
contract; do not hand-edit them. Repo-owned docs cite methodologies `1.9.2`.

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | open in GitHub until human sync; tasks done in tree | Compose host, local Room, isolated engines |
| MAT-002 | open | T4 and T5 done in tree; T1 emulator CI remains |
| MAT-003 | open in GitHub until human sync; tasks done in tree | Godot 4 plugin; T5 arcade lives/HUD done in tree |
| MAT-004 | open | T4–T7 done in tree. MasterKey (T8) is in the working tree; Room migration is still PR4 |
| MAT-005 | open | T4–T6 done. T1 shared simulation remains. Release `isMinifyEnabled` is true; device proof of a minified prize is still T2. T3 still shrinks Kover excludes |
| MAT-006 | open | T1 done in tree (ktlint and detekt on `:app` and `:data`). T2 unit tests and lint pass on this host. T3 CodeQL compile and the APK bill are still open |

`docs/guardrail-deviations.yml` now has two entries, neither of which lowers a number:
Kover exclusions beyond kotlinx.serialization (KT-TEST-002; floors stay 95),
and Dokka `doc_coverage` not applicable (KT-DOC-001). Compose functions are
ignored by detekt complexity and length. `NativeKartHost.attach` and
`NativeRunnerHost.attach` suppress those two rules until the canvas hosts are
split. `RewardGodotFragment` keeps `@SuppressLint("MissingSuperCall")` as a
platform-contract suppression, not a deviation. Do not add `key` /
`org_default` / `repo_value`. Do not invent Engineering Lead approval.

GitHub labels/milestones and issue sync need a write token:

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

Do not git-merge `cursor/godot-black-screen-*`; those branches were deleted after
the lives/HUD port landed on `main` via PR #10. PR #11 recorded MAT-003-T5 as
done in `docs/issues.yml`. MAT-004-T7 paints invaders, chomp, and climb on
`ArcadeBoardView` (domain tests cover `ArcadeScene`). `:app` Robolectric proof
is CI. This host has the Android SDK at `~/Android/Sdk` and Java at
`/opt/android-studio/jbr`. MAT-006 is GitHub #19 (tasks #20, #21, #22). MAT-002-T1 is #23.
`docs/issues.yml`
already contains those ids; after merge, sync so the manifest owns the bodies.

## Commands

```bash
python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 scripts/lint-doc-links.py --root .
python3 scripts/verify-companion-pins.py
bash scripts/run-actionlint.sh
GUARDRAILS_READ_TOKEN= bash scripts/checkout-guardrails.sh
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
bash scripts/check-ci-local.sh
```

## Known pitfalls

- `scripts/checkout-guardrails.sh` inits `docs/guardrails` with
  `GUARDRAILS_READ_TOKEN` (full clone; a shallow fetch of the pinned commit
  fails). Push to `main` fails closed without the token. Other events skip
  and keep `config/kotlin.thresholds.yml`. Pass the token with
  `git -c http.extraheader`. A local `git config` extraheader is not visible
  to the submodule clone. The exit trap still clears a leftover local
  header. `verify-coverage.py` and `verify-privacy-manifest.py` parse XML
  with defusedxml. `verify-coverage.py` fails if the overlay is below the
  submodule org defaults when both exist.
- `.github/workflows/shared-ci.yml` calls commondevops `5.6.2` at peeled SHA
  `b85f95bbac8ba7d82eb90d1d84d1b8d1e8590a12` with `COMMONDEVOPS_READ_TOKEN`
  as `checkout_token`. `uses:` cannot take a PAT. When the ops repo is
  private again, this repo still needs Actions access to read the workflow
  file. Gitleaks, Semgrep (`p/kotlin` plus `.semgrep.yml`), Grype, actionlint,
  shellcheck, and zizmor live only in those reusable jobs. Keep the token-free
  Gradle, license, CodeQL, MobSF, and APK-bill jobs in `ci.yml` and
  `hardening.yml`. Do not gate them on `github.actor == 'dependabot[bot]'`.
  `scripts/check-ci-docker.sh` runs the host-missing linters in
  `ghcr.io/pirlruc/ci-lint` at `sha256:f9001c4c`. Scorecard is advisory
  (`blocking: false`, `publish_results: false`) so it does not need
  repository settings.
- `:app` / `:data` are skipped when `ANDROID_HOME` is unset so JDK-only CI
  can still gate `:domain`. ktlint and detekt are plugins on `:domain` only,
  so Android sources are not statically analyzed even on the SDK job until
  MAT-006-T1. The CI `test` job does install the SDK and runs `:data`/`:app`
  unit tests, Kover, and `:app:lintDebug`. This VM has no SDK, so those
  tasks were not executed here (MAT-006-T2). CodeQL compile and the APK bill
  have the same limit (MAT-006-T3). With the SDK, coverage is required for
  all three modules.
- `MatAventurasApp.shouldOpenContainer` / `resolveProcessName` (API 26–27 uses `/proc/self/cmdline`). Blank process names fail closed (no Room).
- Reward points use `ProfileDao.addPoints`; lesson persist must not stamp an absolute Compose total.
- Do not add `docs/adr/`. Epic MAT-001 / MAT-003 are the decision records.
- Scaffold branch convention is `feature-*`; this cloud run used
  `cursor/code-quality-guardrails-1948` per the agent environment.
- VM JDK may be 21; target JVM 17 bytecode without `jvmToolchain(17)`.
- Run `:domain:ktlintFormat` before `:domain:ktlintCheck` (parallel format+check races).
- Never construct `GodotFragment` under Robolectric (`GodotRuntime.shouldEmbed`
  is false when `Build.FINGERPRINT` contains `robolectric`).
- Godot JNI types live in `pt.mataventuras.app.engine.godot` and are excluded
  from `:app` Kover because `libgodot_android.so` cannot load in unit tests.
- Do not pass `--path` / `--scene` / renderer flags to the Godot library
  command line. First-time GLES restart must return a `restart` extra to
  MainActivity (host relaunch), not `Activity.recreate()`, not ProcessPhoenix,
  and not `startActivity` of the same `singleInstance` plugin from the dying
  engine process. `EngineLauncher.relaunchIntent` allowlists plugin/native
  reward class names only.
- Compose `pointerInput` `size` is `IntSize` (`width: Int`). Use
  `size.width.coerceAtLeast(1).toFloat()`, not `coerceAtLeast(1f)`.
  `:app` is not compiled on this VM (`ANDROID_HOME` unset); CI catches it.
- Age-7 choice lessons fill the viewport; sudoku/soup/cipher/puzzle scroll, and
  Sair/Ficar sit in a footer so the confirm-leave buttons stay on screen.
  Tests click those footer buttons without `performScrollTo` (they are not
  inside the scrollable play column). `LessonPlayColumn` / `LessonExitBar`
  keep Compose screens out of detekt `LongMethod`.
- `:app` kover is 95% line and branch. New arcade/scene branches need tests
  (`EngineCoverageTest`); do not exclude them to make the gate pass.
- `OffroadScene.fill` clears the span list each call. Four gates put the first
  arch at 96 m, so `DRAW_AHEAD` must be greater than that (140 m) or spawn
  paints no posts. Near META, assert `BANNER_ARGB`, not `POST_ARGB`. Kart TTS
  is `virar` / `META`, not `guiar`. `sudokuGapDp(0, box)` is 1 dp; box
  boundaries (`index % box == 0` and `index > 0`) are 4 dp.
- Do not call `Godot` `renderView.onPause()` from `RewardGodotFragment`.
  That disconnects the BufferQueue while the GL thread is swapping and
  yields `EGL_BAD_SURFACE` / a black SurfaceView. Let `GodotFragment` order
  pause/resume. `boot.gd` counts frames in `_process` until
  `DisplayServer.window_get_size()` and the viewport are real, then
  `change_scene_to_packed` only if the prize script compiled. Do not
  `call_deferred` a function that `await`s: the coroutine stops at the
  first `await` and the blue boot rect stays up. Do not call
  `RenderingServer.force_draw()` from that wait. Do not infer a GDScript
  type from a Dictionary value (`var nx := sample.pos.x`). Godot 4.7
  drops the whole script, the scene stays the `#1E88E5` clear color, and
  the maze never starts. Type it: `var pos: Vector2 = sample.pos`.
- Detekt is `dev.detekt` `2.0.0-alpha.6`. Config keys use `allowedComplexity` /
  `allowedLines` / `allowedFunctionsPerClass` (not the 1.x `threshold` names).
  Do not revert to `io.gitlab.arturbosch.detekt` 1.23.8: that plugin still calls
  deprecated `ReportingExtension.file` (removed in Gradle 10).
- `android-actions/setup-android` must be v4+: v3.2.2 still runs
  `sdkmanager tools`, and that package no longer exists.
- Grype must not scan `.ci-venv` (semgrep's protobuf/pip). Run it before the
  venv is created and `--exclude` CI/build trees.
- Native invaders/chomp/climb fallback is `ArcadeBoardView`, not a hint `TextView`. Touch steps the domain loop once; do not add `withFrameNanos` on that path. `ArcadeScene` owns the rectangles. `NativeRewardHost.placeholder` remains for kart/runner hint tags only.
- Age-7 division is exact (factors 2–10, no remainder). Subtraction still
  uses the Unicode minus `−` in equation prompts. Picture games are
  `PlayKind.GROUPS` (`●` kept, `✕` removed). The alien hosts both
  multiplication and division.
- Age-7 sudoku publishes `PlayBoard.solution`. The lesson fills every uniquely
  determined blank in turn: the focused house glows, other holes stay pale,
  and digit buttons use the `sudoku-digit` tag. Age 3 keeps an empty solution,
  one glowing question cell, and `correct-answer`. Extra blanks stay uniquely
  determined (`SudokuHoles.EXTRA_BLANK` is `·`; the question cell is `""`).
- PIN prefs use `EncryptedSharedPreferences` on device. Keystore failures
  fail closed. Robolectric sets `allowPlaintextFallback` and uses a distinct
  `*_plain` prefs file — never mix encrypted XML with cleartext.
- `RewardCatalog.fromName` / `packedScenePath` reject cross-engine extras so
  a 2D process cannot load `kart.tscn` (or `boot.tscn`).
- PIN unlock goes through `PinRepository.update` so lockout is not racy.
- PIN digits are ASCII `0-9` only. `PinPolicy.derive` clears the password chars and the `PBEKeySpec`.
- Do not reintroduce `fallbackToDestructiveMigration`. Schema version is still 1; the next bump needs an explicit `Migration` (MAT-004-T8). `security-crypto` 1.0.0 has no `MasterKey.Builder` — that half of T8 needs a library bump.
- Prize GDScript pointer/HUD/steer helpers live on the `Host` autoload (`read_pointer`, `make_hud`, `axis_from_normalized_x`). Keep them aligned with `EngineInputMap` (dead-zone 0.14, jump flick 56 px).
- Kover 95% verify rules live once in the root `build.gradle.kts` `subprojects` block and read `config/kotlin.thresholds.yml`. Do not copy the bounds back into each module.
- `MissingSuperCall` is `@SuppressLint` on `RewardGodotFragment` only. Manifest merger stubs (`tools:node="remove"`, and the `InitializationProvider` merge node) use `tools:ignore="MissingClass"`. There is no directory-wide `lint.xml`.
- Local secret scan: `git config core.hooksPath .githooks` runs `scripts/gitleaks-pre-commit.sh` (KT-SEC-003). CI still scans; the hook fails closed if `gitleaks` is missing.
- Guardrails tag `1.13.1` (`1945211a`) and scaffold tag `1.10.2` (`c8eddcfb`) are the pins. KT-SEC-002 is semgrep `p/kotlin` on `common-secrets-sast`; KT-SEC-003 is gitleaks there too. KT-DOC-001 Dokka `doc_coverage` does not apply (no published API) and is recorded, not lowered. The one `@SuppressLint("MissingSuperCall")` is `RewardGodotFragment`: those JNI overrides must not call super. Guardrails 1.13.1 treats that as a platform contract, so the two suppression deviations can be dropped once the pin is on `main`. The 14-day number is not lowered. `scripts/verify-licenses.py` reads `license_deny_list` from the guardrails supply-chain thresholds (SC-LIC-001), including BUSL-1.1, Commons-Clause, and CC-BY-NC.
- `:app` Kover still excludes Compose facades (`LessonScreenKt`, `NavGraphKt`, `@Composable`, `*Kt$*`), native Canvas hosts, and `engine.godot.*`. MAT-005-T3 wants the first two hosts and the two screen facades measured. This VM has no Android SDK, so those excludes stayed. Room `*_Impl` and `BuildConfig` stay excluded as generated code. KT-TEST-002 names kotlinx.serialization generated members, not Room or the Godot JNI package.
- Do not gate jobs on `github.actor == 'dependabot[bot]'` (zizmor `bot-conditions`). Dependabot updates use `cooldown.default-days: 7`. Workflow lint is zizmor inside `common-infra-lint` (and `scripts/check-ci-docker.sh` locally). There is no in-repo zizmor-action job.
- CodeQL action `v4.38.1` commit is `1c5b675653bb5c22dbe9b12b556ec555138e09fd` (the tag object is not the commit). `upload: never` keeps `contents: read`. The compile step must stay manual (`build-mode: manual`). The APK SBOM is the debug runtime classpath (`scripts/gradle-to-cyclonedx.py` + osv-scanner `2.6.0`). Dex has no Maven coordinates, so scanning the APK file itself yields an empty bill. Community semgrep is `p/kotlin` beside `.semgrep.yml`, still semgrep `1.128.1`, and it runs after grype so `.ci-venv` is not scanned.
- Do not merge `cursor/godot-black-screen-invaders-5d7b` or
  `cursor/godot-black-screen-lives-80ab` as git merges: they fork from PR #6
  and would restore the GLES oval path deleted in PR #9. Port lives/HUD only.
- Invaders ends at 5 lives or an empty 15-ship fleet (not 8 hits / first bomb).
  Chomp and climb use 3 lives with i-frames. Keep GDScript in sync with
  `:domain` engines.
- `GodotEmbed.attach` waits for a >32 px host view (layout listener, then a
  1.2 s sized retry). Only a 4.8 s last resort attaches anyway. Keep
  `boot.gd`'s window-size wait; do not treat `onGodotForceQuit` as a GLES
  restart (`onGodotRestartRequested` already does that). `Host.finish` lingers
  1.6 s for prize banners via a `SceneTreeTimer` signal (callers do not
  `await` it); boot errors pass `linger=false`.

## Suggested next work

1. Human: bootstrap labels/milestones and sync `docs/issues.yml`.
2. Emulator smoke tests passed on `mat_api36`. The CI job is `.github/workflows/emulator.yml` (monthly and `workflow_dispatch` only).
3. MAT-004-T8: Room `Migration` when the age-band schema lands. `MasterKey.Builder` is already in `PinRepository`.
4. MAT-005-T1/T2/T3: shared Godot/domain simulation, a release-variant prize on a device, narrower Kover excludes.
5. MAT-006-T3: CodeQL manual compile and the APK bill on this SDK host. T1 is done. T2 unit tests and `lintDebug` passed locally.
6. Companion issues already filed stay as they are: guardrails [#187](https://github.com/pirlruc/guardrails/issues/187) GR-KT-003, [#188](https://github.com/pirlruc/guardrails/issues/188) GR-KT-004, [#189](https://github.com/pirlruc/guardrails/issues/189) GR-PACK-007; pydevops [#172](https://github.com/pirlruc/pydevops/issues/172) PDO-PYPROJECT-001, [#174](https://github.com/pirlruc/pydevops/issues/174) PDO-PYVER-001, [#175](https://github.com/pirlruc/pydevops/issues/175) PDO-COMMENT-001, [#176](https://github.com/pirlruc/pydevops/issues/176) PDO-UV-001; github-scaffold [#150](https://github.com/pirlruc/github-scaffold/issues/150) GS-AND-001. Do not file them again.
7. python-quality is Medium (CI-022 offsets), not the High/org floors, and `installable_app` is false. The install venv must stay in `RUNNER_TEMP`. `.bandit` skips assert and subprocess findings because the gate counts every result. Caller `python_version` is 3.13. The pyproject cutoff stays a timestamp. PDO-TOOL-001 is fixed in pydevops `3.3.1`, but the reusable workflow pulls `ghcr.io/pirlruc/ci-lint` and `ci-python` and this repo's token gets `manifest unknown`. Leave `PYTHON_QUALITY_ENABLED` unset until those packages are visible. Shared-ci jobs that need a scanner image pass the public `ghcr.io/catthehacker/ubuntu:act-24.04` digest. `install_runner_packages` stays false, and the jobs do not pass `COMMONDEVOPS_READ_TOKEN`. Do not pin `main`.

*Last updated: 2026-10-31 (Godot pointer types and headless import)*
