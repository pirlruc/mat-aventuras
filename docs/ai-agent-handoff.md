# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-11-02
**Last agent focus:** Tag `0.3.1` is annotated on `ef2caa3`. Run 41467120356 published a signed `app-release.apk`. `apksigner` verifies APK Signature Scheme v2. The checksum matches. Tag `0.3.0` stays on `038ff00`. Do not move either tag.

---

## What this repo is

Native Kotlin/Compose educational math game for ages 3–5 and 6–8.
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
| Godot Android library | `gradle/libs.versions.toml` | `org.godotengine:godot:4.7.2.stable` |
| Detekt Gradle plugin | `gradle/libs.versions.toml` | `dev.detekt` `2.0.0-alpha.6` |
| actionlint | `scripts/run-actionlint.sh` | `1.7.12` (commondevops ci-lint pin) |
| commondevops | `.github/workflows/shared-ci.yml` | tag `5.8.0`, peeled `cb34967f` |
| androiddevops | `.github/workflows/ci.yml`, `emulator.yml`, `release.yml` | tag `1.2.0`, peeled `e88c3b8f` |
| pydevops `python-quality` | `.github/workflows/python-quality.yml` | tag `3.3.10`, peeled `7ebee303` |
| ci-lint image | `scripts/check-ci-docker.sh` | `sha256:f9001c4c` |

Stay on annotated tags (SC-DEP-004). Synced scaffold rules are the scaffold
contract; do not hand-edit them. Repo-owned docs cite methodologies `1.9.2`.

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | done | Compose host, local Room, isolated engines |
| MAT-002 | done | Emulator job, Robolectric gates, lesson packs |
| MAT-003 | done | Godot 4 plugin in `:engine2d` and `:engine3d` |
| MAT-004 | done | T8 MasterKey and `MIGRATION_1_2` are on main. T9–T12 record the MASVS map, the advisories link, hashed pins, and the ruleset |
| MAT-005 | done | T1–T11 done. Release [0.2.0](https://github.com/pirlruc/mat-aventuras/releases/tag/0.2.0) attaches the signed APK, `app-release.apk.sha256`, and `app-release.cdx.json`. Tag commit `2eafb4af`. Do not move `0.1.0` or `0.2.0` |
| MAT-006 | done | T1–T3 done in #113. T4: `mat_api36` log shows `boot-ready` for every prize, and the frames are past the boot overlay. The Godot JNI package stays excluded from Kover |
| MAT-007 | done | `BandDifficulty` replaces `isSevenYears()`. Memory, word board, cell size, and pt-PT parent titles are on this branch |
| MAT-008 | done | Domain memory and word-completion boards. The Compose gaps are MAT-007-T5 and MAT-007-T6 |
| MAT-009 | done | Loading overlay, timeout with Tentar outra vez / Voltar, Ganhaste card, icon HUD, kart boost button, and cancelled embed retries |
| MAT-010 | done | Companion pins and reusable CI are on main |
| MAT-011 | done | T1–T4 are on main. Pull request #151 closed the epic. Do not reopen it |
| MAT-012 | done | Speaker, gentle miss, stars, home lock, exit confirm, saved route, and Room v3 prize limit |
| MAT-013 | done | T2 and T3 are on main. python-quality runs when a Python file or a Python tool config changes. `python-gate` is required |
| MAT-015 | done | 0.3.1 assets verify with cosign and `gh attestation verify`. Do not move `0.3.0` or `0.3.1` |
| MAT-016 | done | Defect fixes and the androiddevops 1.2.0 pin. Release 0.3.2 is the next tag |

`docs/guardrail-deviations.yml` has three entries, none of which lowers a number:
Kover exclusions beyond kotlinx.serialization (KT-TEST-002; floors stay 95),
the Temurin major ignore (SC-DEP-003), and Dokka `doc_coverage` not applicable
(KT-DOC-001). Compose functions are ignored by detekt complexity and length.
`NativeKartHost.attach` and `NativeRunnerHost.attach` do not suppress
complexity. `RewardGodotFragment` keeps `@SuppressLint("MissingSuperCall")` as a
platform-contract suppression, not a deviation. Do not add `key` /
`org_default` / `repo_value`. Do not invent Engineering Lead approval.

GitHub labels/milestones and issue sync need a write token:

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

Do not git-merge `cursor/godot-black-screen-*`; those branches were deleted after
the lives/HUD port landed on `main` via PR #10. MAT-004-T7 paints invaders,
chomp, and climb on `ArcadeBoardView`. This host has the Android SDK at
`~/Android/Sdk` (`ANDROID_HOME`) and Java at `/opt/android-studio/jbr`
(`JAVA_HOME`). `python3 -m venv` fails here; use `uv`. The AVD name is
`mat_api36`. Do not apt-install. MAT-006 is GitHub #19 (tasks #20, #21, #22).

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

- ktlint 14 flags `@Composable` PascalCase names. `.editorconfig` sets
  `ktlint_function_naming_ignore_when_annotated_with = Composable`. ktlint
  joins an expression body that fits its own line length (about 140), and
  detekt's `MaxLineLength` is 120. A body between those two limits stays one
  line with `@Suppress("MaxLineLength")`. A longer body is a block so
  `ktlintFormat` does not rejoin it. Godot 4.7.2 `--import` aborts;
  `scripts/check-godot.sh` uses `--editor` and fails on `SCRIPT ERROR`.
  The devcontainer stays on Temurin 17. Dependabot ignores Temurin
  semver-major under the SC-DEP-003 deviation on MAT-013.
- Lesson and parent copy is in `res/values/strings.xml`. The UI rules are
  top-level functions in `LessonRules`, `ScoringRules`, `SoupGestures`,
  `SudokuRules`, `ParentCopy`, `SpeechRules`, and `ShapeGeometry`. `UiLogic`
  keeps the constants only. `NativeRunnerHost.attach` and
  `NativeKartHost.attach` do not need a complexity suppression.
- `scripts/checkout-guardrails.sh` inits `docs/guardrails` with
  `GUARDRAILS_READ_TOKEN` (full clone; a shallow fetch of the pinned commit
  fails). Push to `main` fails closed without the token. Other events skip
  and keep `config/kotlin.thresholds.yml`. Pass the token with
  `git -c http.extraheader`. A local `git config` extraheader is not visible
  to the submodule clone. The exit trap still clears a leftover local
  header. `verify-coverage.py` and `verify-privacy-manifest.py` parse XML
  with defusedxml. `verify-coverage.py` fails if the overlay is below the
  submodule org defaults when both exist.
- `.github/workflows/shared-ci.yml` calls commondevops `5.8.0` at peeled SHA
  `cb34967f230a0d4841ae1efc2c98e745bd8726a7`. `uses:` cannot take a PAT. When
  the ops repo is private again, this repo still needs Actions access to read
  the workflow file. Scanner jobs pull private `ci-lint` and `ci-supply-chain`
  (this repo has Actions read). Gradle, CodeQL, MobSF, and the APK bill call
  androiddevops `1.2.0` (`e88c3b8f3d8ad530ca3569059fc3c9f7f2ea2ad8`) from
  `ci.yml`, `emulator.yml`, and `release.yml`. `release.yml` still passes
  `environment: release`.
  The CI jobs skip a pull request that does not touch app, data, domain,
  gradle, or scripts. Do not gate them on `github.actor == 'dependabot[bot]'`.
  Scorecard is advisory (`blocking: false`, `publish_results: false`).
- `:app` / `:data` are skipped when `ANDROID_HOME` is unset so a JDK-only
  run can still gate `:domain`. This host has the SDK. CI's `android` job
  runs ktlint, detekt, unit tests, Kover, and `:app:lintDebug` on all three
  modules. CodeQL CLI 2.27.0 and the debug APK bill were proved on this host
  (zero high or critical). With the SDK, coverage is required for all three modules.
- `MatAventurasApp.shouldOpenContainer` / `resolveProcessName` (API 26–27 uses `/proc/self/cmdline`). Blank process names fail closed (no Room).
- Reward points use `ProfileDao.addPoints`; lesson persist must not stamp an absolute Compose total.
- Do not add `docs/adr/`. Epic MAT-001 / MAT-003 are the decision records.
- Scaffold branch convention is `feature-*`.
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
- Age-7 choice lessons fill the viewport; sudoku/soup/cipher/puzzle scroll, and
  Sair/Ficar sit in a footer so the confirm-leave buttons stay on screen.
  Tests click those footer buttons without `performScrollTo` (they are not
  inside the scrollable play column). `LessonPlayColumn` / `LessonExitBar`
  keep Compose screens out of detekt `LongMethod`.
- `:app` Kover uses JaCoCo (`kover { useJacoco() }`, kotlinx-kover #756).
  `LessonScreenKt`, `NavGraphKt`, `NativeKartHost`, and `NativeRunnerHost`
  stay in the report. The latest report is 98.1% line and 95.1% branch.
  New arcade or lesson branches need tests. Do not exclude them to hold 95.
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
- New parent PINs are sealed with a Keystore AES-GCM key (`PinSealer`).
  An old EncryptedSharedPreferences keyset is read with Tink on the next
  unlock. `security-crypto` is not a dependency. Keystore failures fail
  closed. Robolectric uses 1000 PBKDF2 iterations; a device uses 600000
  and rehashes an older hash on unlock.
- `RewardCatalog.fromName` / `packedScenePath` reject cross-engine extras so
  a 2D process cannot load `kart.tscn` (or `boot.tscn`).
- PIN unlock goes through `PinRepository.update` so lockout is not racy.
- PIN digits are ASCII `0-9` only. `PinPolicy.derive` clears the password chars and the `PBEKeySpec`.
- Do not reintroduce `fallbackToDestructiveMigration`. Schema version is 3. `MIGRATION_1_2` rewrites `THREE_YEARS` and `SEVEN_YEARS`. `MIGRATION_2_3` adds `prizeLimitSeconds` (default 120). `security-crypto` is gone. `BandDifficulty.forAge` is the range switch. NUMBERS sudoku stays the small board on both bands. Do not bring back `isSevenYears()`.
- Prize GDScript pointer/HUD/steer helpers live on the `Host` autoload (`read_pointer`, `make_hud`, `axis_from_normalized_x`). Keep them aligned with `EngineInputMap` (dead-zone 0.14, jump flick 56 px).
- Kover 95% verify rules live once in the root `build.gradle.kts` `subprojects` block and read `config/kotlin.thresholds.yml`. Do not copy the bounds back into each module.
- `MissingSuperCall` is `@SuppressLint` on `RewardGodotFragment` only. Manifest merger stubs (`tools:node="remove"`, and the `InitializationProvider` merge node) use `tools:ignore="MissingClass"`. There is no directory-wide `lint.xml`.
- Local secret scan: `git config core.hooksPath .githooks` runs `scripts/gitleaks-pre-commit.sh` (KT-SEC-003). CI still scans; the hook fails closed if `gitleaks` is missing.
- Guardrails tag `1.13.1` (`1945211a`) and scaffold tag `1.10.2` (`c8eddcfb`) are the pins. KT-SEC-002 is semgrep `p/kotlin` on `common-secrets-sast`; KT-SEC-003 is gitleaks there too. KT-DOC-001 Dokka `doc_coverage` does not apply (no published API) and is recorded, not lowered. The one `@SuppressLint("MissingSuperCall")` is `RewardGodotFragment`: those JNI overrides must not call super. Guardrails 1.13.1 treats that as a platform contract, so the two suppression deviations can be dropped once the pin is on `main`. The 14-day number is not lowered. `scripts/verify-licenses.py` reads `license_deny_list` from the guardrails supply-chain thresholds (SC-LIC-001), including BUSL-1.1, Commons-Clause, and CC-BY-NC. When `build/reports/cyclonedx/bom.json` exists it scans that bill instead (SC-LIC-002). The fallback deny list matches the org list and does not add GPL.
- `:app` Kover excludes `BuildConfig`, `*ComposableSingletons*`, and `pt.mataventuras.app.engine.godot.*`. The four screen classes are measured. KT-TEST-002 records the Godot JNI package. Floors stay 95. Do not add `key`, `org_default`, or `repo_value`.
- Do not gate jobs on `github.actor == 'dependabot[bot]'` (zizmor `bot-conditions`). Dependabot updates use `cooldown.default-days: 7`. Workflow lint is zizmor inside `common-infra-lint` (and `scripts/check-ci-docker.sh` locally). There is no in-repo zizmor-action job.
- CodeQL action `v4.38.1` commit is `1c5b675653bb5c22dbe9b12b556ec555138e09fd` (the tag object is not the commit). `upload: never` keeps `contents: read`. The compile step must stay manual (`build-mode: manual`). The APK SBOM is the debug runtime classpath (`scripts/gradle-to-cyclonedx.py` + osv-scanner `2.6.0`). Dex has no Maven coordinates, so scanning the APK file itself yields an empty bill. Community semgrep is `p/kotlin` beside `.semgrep.yml`, still semgrep `1.128.1`, and it runs after grype so `.ci-venv` is not scanned.
- Do not merge `cursor/godot-black-screen-invaders-5d7b` or
  `cursor/godot-black-screen-lives-80ab` as git merges: they fork from PR #6
  and would restore the GLES oval path deleted in PR #9. Port lives/HUD only.
- Invaders ends at 5 lives or an empty 15-ship fleet (not 8 hits / first bomb).
  Chomp and climb use 3 lives with i-frames. Keep GDScript in sync with
  `:domain` engines.
- `GodotEmbed.attach` waits for a >32 px host view (layout listener, then a
  1.2 s sized retry). Only a 4.8 s last resort attaches anyway. The layout
  listener posts the fragment commit. A commit inside that callback leaves
  the Godot view at 0×0, the GL thread never gets a surface, and the scene
  never starts. After commit, the first child is `MATCH_PARENT` and the host
  requests layout again. Both runnables are tracked by `EngineAttachRetries`
  and removed in `onDestroy`. Do not call `GodotEmbed` from that destroy
  path: loading the Godot fragment class pulls in `libgodot_android`. Keep
  `boot.gd`'s window-size wait; do not treat `onGodotForceQuit` as a GLES
  restart (`onGodotRestartRequested` already does that). `Host.finish`
  lingers 1.6 s for prize banners via a `SceneTreeTimer` signal (callers do
  not `await` it); boot errors pass `linger=false`. `Host` resolves the
  `MatAventuras` singleton lazily and calls `prizeReady()` on the object.
  `has_method` does not see those Java plugin methods, so a `has_method`
  guard never hides the boot overlay. Do not raise `PrizeBoot.TIMEOUT_MS`.
- A parent prize limit is `prize_limit` on the reward Intent. `PrizeLimit` arms every host from `SystemClock.elapsedRealtime()`. Zero or a missing extra does not finish the activity. The Godot `Host` reads `prizeLimitSeconds()` and stops on `Time.get_ticks_msec()`. Compose prizes use `Random.Default` unless the test passes a seed. The correct-answer tag is on only when the process is debuggable or Robolectric.
- A Keystore or IO failure while reading the PIN opens the hold-and-sum reset (`6 + 7`). The parent window sets `FLAG_SECURE` while that screen is composed. Lockout stores both a wall-clock deadline and `lockedUntilElapsedMs`. `PinKeyException` is an `IllegalStateException`; catch it first.
- Prize lives, speeds, and hit tests live only in
  `domain/src/main/resources/prize-tuning.json`. The app asset source set
  packages that file as `res://prize-tuning.json`. `Host.tune` reads it.
  Domain engines read the classpath copy. Do not put the numbers back in
  both GDScript and Kotlin. Ages 3–5 life text is `PrizeHud.livesLine`
  (hearts). Kart boost is the Impulso button, not the centre tap. A win sets
  `MainActivity.celebrateWin` and shows the Ganhaste card. Boot timeout is
  12 s (`PrizeBoot`), then Tentar outra vez or Voltar to the Canvas host.

## Suggested next work

1. Current release is `0.3.1` on `ef2caa3` (run 41467120356). `0.3.0` stays on `038ff00`. Do not move `0.1.0`, `0.2.0`, `0.3.0`, or `0.3.1`. The keystore is outside the repo. Do not commit it or copy the password.
2. MAT-015 is done. The 0.3.1 APK and bill verify with cosign v3.0.6 and `gh attestation verify --signer-repo pirlruc/commondevops`.
3. MAT-016 is done, including the androiddevops 1.2.0 pin (`e88c3b8f3d8ad530ca3569059fc3c9f7f2ea2ad8`) and the CycloneDX license bill. The next tag is 0.3.2. `LessonScreen`, `NavGraph`, `NativeKartHost`, and `NativeRunnerHost` stay in the Kover report.
4. The emulator job is `.github/workflows/emulator.yml` (monthly and `workflow_dispatch` only). Run 41358345053 failed because the runner had no KVM permission (`-accel off`), and the multi-line `script:` never executed under `sh -c`. Do not record that as a boot timeout. androiddevops 1.2.0 is the fix.
5. When androiddevops later becomes private, this public repo cannot call its workflows. Vendor the pinned workflow. Do not change visibility from an agent session. mat-aventuras stays public.
6. Companion issues already filed stay as they are: guardrails [#187](https://github.com/pirlruc/guardrails/issues/187) GR-KT-003, [#188](https://github.com/pirlruc/guardrails/issues/188) GR-KT-004, [#189](https://github.com/pirlruc/guardrails/issues/189) GR-PACK-007; pydevops [#172](https://github.com/pirlruc/pydevops/issues/172) PDO-PYPROJECT-001, [#174](https://github.com/pirlruc/pydevops/issues/174) PDO-PYVER-001, [#175](https://github.com/pirlruc/pydevops/issues/175) PDO-COMMENT-001, [#176](https://github.com/pirlruc/pydevops/issues/176) PDO-UV-001; github-scaffold [#150](https://github.com/pirlruc/github-scaffold/issues/150) GS-AND-001. The 0.1.0 review added guardrails [#318](https://github.com/pirlruc/guardrails/issues/318) GR-GD-001, [#320](https://github.com/pirlruc/guardrails/issues/320) GR-KIDS-001, [#322](https://github.com/pirlruc/guardrails/issues/322) GR-A11Y-001, [#324](https://github.com/pirlruc/guardrails/issues/324) GR-ASSET-001, [#326](https://github.com/pirlruc/guardrails/issues/326) GR-AND-EMU-001; methodologies [#166](https://github.com/pirlruc/methodologies/issues/166) MTH-MOB-001 and [#168](https://github.com/pirlruc/methodologies/issues/168) MTH-AGE-001; github-scaffold [#211](https://github.com/pirlruc/github-scaffold/issues/211) GS-AND-002, [#213](https://github.com/pirlruc/github-scaffold/issues/213) GS-AND-003, and [#215](https://github.com/pirlruc/github-scaffold/issues/215) GS-AND-004. Do not file them again. The companion review after 0.3.1 dedupes this list. Do not file them again.
7. python-quality is Medium (CI-022 offsets), not the High/org floors, and `installable_app` is false. The install venv must stay in `RUNNER_TEMP`. `.bandit` skips assert and subprocess findings because the gate counts every result. Caller `python_version` is 3.13. The pyproject cutoff stays a timestamp. Callers pin `3.3.10`, which runs the containers as root, skips mypy when `installable_app` is false, reads pip-audit 2.10's dependencies object, and runs the quality report as a module. This repo has Actions read on `ci-lint` and `ci-python`, so the job runs. It is not behind `PYTHON_QUALITY_ENABLED`. `install_runner_packages` stays false. Do not pin `main`. Do not move pydevops 3.3.1 through 3.3.10. Leave pydevops `feature-cmn-551` alone.
8. The main ruleset requires `changes`, `quality`, `gate`, `dependency-review`, `doc-verify / verify`, `infra-lint / lint`, `scaffold-verify / verify`, `secrets-sast / secrets-sast`, `supply-chain / supply-chain`, `pins / pins`, `python-gate`, and `DCO`. Reviews stay at 0. The owner chose that. Do not set required approvals to 1. Do not require the path-filtered Android jobs or the inner python-quality jobs. Private vulnerability reporting is on. Scorecard Fuzzing alert #5 is dismissed. New commits need a `Signed-off-by` trailer. Do not rewrite old history.
9. OpenSSF baseline project [16162](https://www.bestpractices.dev/projects/16162). The README badge stays `https://www.bestpractices.dev/projects/16162/baseline`. Level 1, 2, and 3 are marked on the form. The public text is on `main`. The README lists the release-certificate SHA-256. Release 0.2.0 also has `app-release.cdx.json`. Do not invent a CPE. Do not install the DCO app or edit the ruleset. OSPS-QA-07.01 stays unmet because reviews stay at 0. Do not mark the form. androiddevops stays public. MAT-011 is done. MAT-006 is done: on `mat_api36`, every prize logs `boot-ready` and the frame is past the boot overlay. The JNI exclude stays. MAT-013 is done. python-quality runs when a Python file or a Python tool config changes.

*Last updated: 2026-11-02 (MAT-016 done: androiddevops 1.2.0 and the license bill).*
