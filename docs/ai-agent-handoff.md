# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-09-30
**Last agent focus:** python-quality caller paused on PDO-TOOL-001

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
| methodologies | annotated tag in docs | `1.7.0` |
| guardrails | submodule SHA `docs/guardrails/` | `aa5184ce` (tag `1.8.0`) |
| github-scaffold | submodule SHA `.github/scaffold/` | `e76bb3fd` (tag `1.7.0`) |
| Godot Android library | `gradle/libs.versions.toml` | `org.godotengine:godot:4.7.1.stable` |
| Detekt Gradle plugin | `gradle/libs.versions.toml` | `dev.detekt` `2.0.0-alpha.6` |
| actionlint | `scripts/run-actionlint.sh` | `1.7.12` (commondevops ci-lint pin) |
| pydevops `python-quality` | `.github/workflows/python-quality.yml` | tag `2.1.1`, peeled `19fa370f` |

Stay on annotated tags (SC-DEP-004). Scaffold tag `1.7.0` synced rules still
link guardrails `1.7.0` and methodologies `1.6.0`. That text is the scaffold
contract; do not hand-edit it. Repo-owned docs cite methodologies `1.7.0`.
Scaffold `main` already bumps those links and is not a release.

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | open in GitHub until human sync; tasks done in tree | Compose host, local Room, isolated engines |
| MAT-002 | open | T4 and T5 done in tree; T1 emulator CI remains |
| MAT-003 | open in GitHub until human sync; tasks done in tree | Godot 4 plugin; T5 arcade lives/HUD done in tree |
| MAT-004 | open | T4–T7 done in tree. MasterKey (T8) remains. Destructive Room wipe is already removed |
| MAT-005 | open | T4 community semgrep, T5 child names, and T6 zizmor done in tree. Shared simulation (T1), release R8 (T2), and narrower Kover excludes (T3) remain |
| MAT-006 | open | SDK-host proof for Android analysis, unit tests, CodeQL compile, and the APK bill. Deviations render into this Epic |

`docs/guardrail-deviations.yml` records departures that do not lower a number:
ktlint/detekt only on `:domain` (KT-BUILD-002, KT-CPLX-001), permanent
`MissingSuperCall` on `RewardGodotFragment` (KT-BUILD-002, ANDROID-LINT-001),
Kover exclusions beyond kotlinx.serialization (KT-TEST-002; floors stay 95;
MAT-005-T3 still shrinks the screen/host excludes), and Dokka `doc_coverage`
not applicable (KT-DOC-001). Do not add `key` / `org_default` / `repo_value`
for those. Do not invent Engineering Lead approval. KT-CPLX-002 is detekt
`LongMethod` — do not invent a Python-style MI.

GitHub labels/milestones and issue sync need a write token:

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

Do not git-merge `cursor/godot-black-screen-*`; those branches were deleted after
the lives/HUD port landed on `main` via PR #10. PR #11 recorded MAT-003-T5 as
done in `docs/issues.yml`. MAT-004-T7 paints invaders, chomp, and climb on
`ArcadeBoardView` (domain tests cover `ArcadeScene`). `:app` Robolectric proof
is CI, because this VM has no Android SDK. MAT-006 is GitHub #19 (tasks #20, #21, #22). MAT-002-T1 is #23.
This VM cannot run the Android SDK or an emulator. `docs/issues.yml`
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
- `.github/workflows/shared-ci.yml` calls commondevops `5.1.2` at peeled SHA
  `b3c462bed0de4f6475e6be7875c4ababd831acc6` with `COMMONDEVOPS_READ_TOKEN`
  as `checkout_token`. `uses:` cannot take a PAT. When the ops repo is
  private again, this repo still needs Actions access to read the workflow
  file. Keep the token-free jobs in `ci.yml`. Do not gate them on
  `github.actor == 'dependabot[bot]'`.
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
- Guardrails tag `1.8.0` (`aa5184ce`) and scaffold tag `1.7.0` (`e76bb3fd`) are the newest published tags. KT-SEC-002 is semgrep `p/kotlin`; KT-SEC-003 is gitleaks. KT-DOC-001 Dokka `doc_coverage` does not apply (no published API) and is recorded, not lowered. The one `@SuppressLint("MissingSuperCall")` is `RewardGodotFragment`: those JNI overrides must not call super. That permanent suppression is recorded under KT-BUILD-002 and ANDROID-LINT-001. The 14-day number is not lowered.
- `:app` Kover still excludes Compose facades (`LessonScreenKt`, `NavGraphKt`, `@Composable`, `*Kt$*`), native Canvas hosts, and `engine.godot.*`. MAT-005-T3 wants the first two hosts and the two screen facades measured. This VM has no Android SDK, so those excludes stayed. Room `*_Impl` and `BuildConfig` stay excluded as generated code. KT-TEST-002 names kotlinx.serialization generated members, not Room or the Godot JNI package.
- Do not gate jobs on `github.actor == 'dependabot[bot]'` (zizmor `bot-conditions`). Dependabot updates use `cooldown.default-days: 7`. Workflow lint is zizmor-action `v0.6.4` (`cc914d7f`) with `advanced-security: false` and zizmor `1.30.1`, scoped to `.github/workflows` and `.github/dependabot.yml` so the scaffold submodule is not audited.
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
2. MAT-002-T1: emulator instrumented tests in CI, including Godot plugin Activities.
3. MAT-004-T8: `MasterKey.Builder` (needs a security-crypto bump) and an explicit Room `Migration` on the next schema version.
4. MAT-005-T1/T2/T3: shared Godot/domain simulation, release R8 (device required), narrower Kover excludes (needs the Android SDK).
5. MAT-006-T1/T2/T3: on an SDK host, apply ktlint/detekt to `:app` and `:data`, and prove the existing unit-test, lint, CodeQL, and APK SBOM jobs.
6. When github-scaffold releases the pin bump already on `main`, re-sync rules so they cite guardrails `1.8.0` and methodologies `1.7.0`.
7. Companion issues filed from this review must be copied into each repo's `docs/issues.yml` and synced (contents write was unavailable): guardrails [#187](https://github.com/pirlruc/guardrails/issues/187) GR-KT-003, [#188](https://github.com/pirlruc/guardrails/issues/188) GR-KT-004, [#189](https://github.com/pirlruc/guardrails/issues/189) GR-PACK-007; pydevops [#172](https://github.com/pirlruc/pydevops/issues/172) PDO-PYPROJECT-001, [#174](https://github.com/pirlruc/pydevops/issues/174) PDO-PYVER-001, [#175](https://github.com/pirlruc/pydevops/issues/175) PDO-COMMENT-001, [#176](https://github.com/pirlruc/pydevops/issues/176) PDO-UV-001; github-scaffold [#150](https://github.com/pirlruc/github-scaffold/issues/150) GS-AND-001. The Android launcher Semgrep hit is evidence on commondevops [#165](https://github.com/pirlruc/commondevops/issues/165), not a new epic. Pin-bump epics on the ops repos were already open. Do not file them again.
8. python-quality is Medium (CI-022 offsets), not the High/org floors. The install venv must stay in `RUNNER_TEMP` so radon does not scan it. `.bandit` skips assert and subprocess findings because the gate counts every result. Caller `python_version` is 3.13. The pyproject cutoff is the lock timestamp because uv 0.6.9 cannot parse `exclude-newer = "7 days"`. The caller job is `if: false` until a tag after 2.1.1: `uv tool install pytest-cov==7.1.0` exits 1 (pydevops #170, PDO-TOOL-001). Do not pin `main`.

*Last updated: 2026-09-30 (python-quality paused on PDO-TOOL-001)*
