# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-09-21
**Last agent focus:** Playable native Canvas for invaders, chomp, and climb

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
| methodologies | annotated tag in docs | `1.5.0` |
| guardrails | submodule SHA `docs/guardrails/` | `77cf16eb` (tag `1.6.0`) |
| github-scaffold | submodule SHA `.github/scaffold/` | `9e04ed53` (tag `1.5.0`) |
| Godot Android library | `gradle/libs.versions.toml` | `org.godotengine:godot:4.7.1.stable` |
| Detekt Gradle plugin | `gradle/libs.versions.toml` | `dev.detekt` `2.0.0-alpha.6` |

Latest published companion **tags** are already 1.6.0 / 1.5.0. Companion
`main` is ahead only with those repos' own CI/docs issue filings — no kotlin
or supply-chain pack edits. Stay on annotated tags (SC-DEP-004).

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | open in GitHub until human sync; tasks done in tree | Compose host, local Room, isolated engines |
| MAT-002 | open | T4 and T5 done in tree; T1 emulator CI remains |
| MAT-003 | open in GitHub until human sync; tasks done in tree | Godot 4 plugin; T5 arcade lives/HUD done in tree |
| MAT-004 | open | T4–T7 done in tree. MasterKey (T8) remains. Destructive Room wipe is already removed |
| MAT-005 | open | T4 community semgrep, T5 child names, and T6 zizmor done in tree. Shared simulation (T1), release R8 (T2), and narrower Kover excludes (T3) remain |

`docs/guardrail-deviations.yml` is empty. Do not re-add KT-TEST-002.
KT-DOC-001 is public-type KDoc (no numeric `doc_coverage`). KT-CPLX-002 is
detekt `LongMethod` / complexity rules — do not invent a Python-style MI.

GitHub labels/milestones and issue sync need a write token:

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

Do not git-merge `cursor/godot-black-screen-*`; those branches were deleted after
the lives/HUD port landed on `main` via PR #10. PR #11 recorded MAT-003-T5 as
done in `docs/issues.yml`. MAT-004-T7 paints invaders, chomp, and climb on
`ArcadeBoardView` (domain tests cover `ArcadeScene`). `:app` Robolectric proof
is CI, because this VM has no Android SDK. GitHub has no MAT-* issues yet
(only probe #8); sync still needs a write token and `issues-sync.py`.

## Commands

```bash
python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 scripts/lint-doc-links.py --root .
python3 scripts/verify-companion-pins.py
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
bash scripts/check-ci-local.sh
```

## Known pitfalls

- Private companion repos: local submodule clone needs a PAT with Contents: Read
  on `pirlruc/guardrails` and `pirlruc/github-scaffold`. GitHub Actions does not
  clone them; it uses `scripts/` helpers, `config/kotlin.thresholds.yml`, and
  `docs/companion-pins.yml` for the gitlink SHA assert. `verify-coverage.py`
  fails if the overlay is below the submodule org defaults when both exist.
- `:app` / `:data` are skipped when `ANDROID_HOME` is unset so JDK-only CI
  can still gate `:domain`. With the SDK, coverage is required for all three.
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
  pause/resume. `boot.tscn` counts frames in `_process` until
  `DisplayServer.window_get_size()` and the viewport are real, then calls
  `change_scene_to_file`. Do not `call_deferred` a function that `await`s:
  the coroutine stops at the first `await` and the blue boot rect stays up.
  Do not call `RenderingServer.force_draw()` from that wait.
- Detekt is `dev.detekt` `2.0.0-alpha.6`. Config keys use `allowedComplexity` /
  `allowedLines` / `allowedFunctionsPerClass` (not the 1.x `threshold` names).
  Do not revert to `io.gitlab.arturbosch.detekt` 1.23.8: that plugin still calls
  deprecated `ReportingExtension.file` (removed in Gradle 10).
- `android-actions/setup-android` must be v4+: v3.2.2 still runs
  `sdkmanager tools`, and that package no longer exists.
- Grype must not scan `.ci-venv` (semgrep's protobuf/pip). Run it before the
  venv is created and `--exclude` CI/build trees.
- Native invaders/chomp/climb fallback is `ArcadeBoardView`, not a hint `TextView`. Touch steps the domain loop once; do not add `withFrameNanos` on that path. `ArcadeScene` owns the rectangles. `NativeRewardHost.placeholder` remains for kart/runner hint tags only.
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
- Guardrails tag `1.6.0` (`77cf16eb`) is still the newest published tag. `docs/guardrail-deviations.yml` stays empty. KT-SEC-002 is semgrep; KT-SEC-003 is gitleaks.
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
  `boot.tscn`'s window-size wait; do not treat `onGodotForceQuit` as a GLES
  restart (`onGodotRestartRequested` already does that). `Host.finish` lingers
  1.6 s for prize banners via a `SceneTreeTimer` signal (callers do not
  `await` it); boot errors pass `linger=false`.

## Suggested next work

1. Human: bootstrap labels/milestones and sync `docs/issues.yml`.
2. MAT-002-T1: emulator instrumented tests in CI, including Godot plugin Activities.
3. MAT-004-T8: `MasterKey.Builder` (needs a security-crypto bump) and an explicit Room `Migration` on the next schema version.
4. MAT-005-T1/T2/T3: shared Godot/domain simulation, release R8 (device required), narrower Kover excludes.

*Last updated: 2026-09-24*
