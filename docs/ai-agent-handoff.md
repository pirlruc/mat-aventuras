# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-09-19
**Last agent focus:** Bump guardrails 1.6.0 / scaffold 1.5.0; drop dead code; harder age-7 games

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
Robolectric fallback. `Kart3dEngine` GLES remains unit-tested.

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

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | open in GitHub until human sync; tasks done in tree | Compose host, local Room, isolated engines |
| MAT-002 | open | T4 (harder age-7) done in tree; T1 emulator CI and T5 tap-to-fill sudoku remain |
| MAT-003 | open in GitHub until human sync; tasks done in tree | Godot 4 plugin Activities + assets; native fallback under Robolectric |
| MAT-004 | open | T5 grype + companion pins done in tree; CodeQL/OSV still open (T4) |

`docs/guardrail-deviations.yml` is empty. Do not re-add KT-TEST-002.
KT-DOC-001 is public-type KDoc (no numeric `doc_coverage`). KT-CPLX-002 is
detekt `LongMethod` / complexity rules — do not invent a Python-style MI.

GitHub labels/milestones and issue sync need a write token:

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

This agent cannot `gh issue create` (read-only `gh`, and methodology forbids
publishing issues without approval).

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
  `docs/companion-pins.yml` for the gitlink SHA assert.
- `:app` / `:data` are skipped when `ANDROID_HOME` is unset so JDK-only CI
  can still gate `:domain`. With the SDK, coverage is required for all three.
- `MatAventurasApp.shouldOpenContainer` / `resolveProcessName` (API 26–27 uses `/proc/self/cmdline`). Blank process names fail closed (no Room).
- Reward points use `ProfileDao.addPoints`; lesson persist must not stamp an absolute Compose total.
- Do not add `docs/adr/`. Epic MAT-001 / MAT-003 are the decision records.
- Scaffold branch convention is `feature-*`; this cloud run used
  `cursor/code-quality-games-4741` per the agent environment.
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
  engine process.
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
  pause/resume. `boot.tscn` waits for `DisplayServer.window_get_size()` and
  a real viewport before `change_scene_to_file`.
- Detekt is `dev.detekt` `2.0.0-alpha.6`. Config keys use `allowedComplexity` /
  `allowedLines` / `allowedFunctionsPerClass` (not the 1.x `threshold` names).
  Do not revert to `io.gitlab.arturbosch.detekt` 1.23.8: that plugin still calls
  deprecated `ReportingExtension.file` (removed in Gradle 10).
- Age-7 sudoku uses `SudokuHoles.EXTRA_BLANK` (`·`) for extra houses and `""`
  for the question cell. UI glows only the question cell. Punching extra
  blanks must keep the question uniquely determined.

## Suggested next work

1. Human: bootstrap labels/milestones and sync `docs/issues.yml`.
2. MAT-002-T1: emulator instrumented tests in CI, including Godot plugin Activities.
3. MAT-002-T5: tap-to-fill remaining sudoku blanks (not one highlighted house).
4. MAT-004-T4: CodeQL + OSV/SBOM if GitHub Advanced Security and a release SBOM are wanted.

This pass: guardrails 1.6.0 + scaffold 1.5.0; dropped invented Kotlin MI /
numeric KDoc ratio; grype (KT-SEC-004); companion pin assert (SC-DEP-004);
removed unused soup `extraCells` and the broken `:dominio` local runner;
deduplicated number-words / extrema / architecture table; age-7 two-digit
arithmetic, missing factor, skip-counting, extra sudoku blanks.

*Last updated: 2026-09-19*
