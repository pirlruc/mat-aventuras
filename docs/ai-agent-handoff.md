# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-08-21
**Last agent focus:** Soup keep-board; tap boost; HUD contrast; course clamps

---

## What this repo is

Native Kotlin/Compose educational math game for ages 3 and 7.
**UI/TTS/dialogue:** Portuguese from Portugal.
**Code, comments, KDoc, documentation:** English.
Privacy: on-device only. Decision log: GitHub Epics via github-issue-adr;
authored backlog is `docs/issues.yml`.

Reward engines: **Godot 4** in `:engine2d` (age 3 platformer) and `:engine3d`
(age 7 2.5D off-road race). Unity is not used. Native Canvas is the
Robolectric fallback. `Kart3dEngine` GLES remains unit-tested.

## Pins

| Companion | How | Value |
| --- | --- | --- |
| methodologies | annotated tag in docs | `1.2.0` |
| guardrails | submodule SHA `docs/guardrails/` | `0354a747` (tag `1.3.0`) |
| github-scaffold | submodule SHA `.github/scaffold/` | `aac408cc` (tag `1.2.0`) |
| Godot Android library | `gradle/libs.versions.toml` | `org.godotengine:godot:4.7.1.stable` |

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | open in GitHub until human sync; tasks done in tree | Compose host, local Room, isolated engines |
| MAT-002 | open | Emulator instrumented tests remain; Robolectric 95% and richer packs are in tree |
| MAT-003 | open in GitHub until human sync; tasks done in tree | Godot 4 plugin Activities + assets; native fallback under Robolectric |
| MAT-004 | open in GitHub until human sync; T1–T3 done in tree | Split CI, SAST, gitleaks, privacy hardening workflow; CodeQL/OSV still open (T4) |

`docs/guardrail-deviations.yml` is empty. Do not re-add KT-TEST-002.
KT-DELIV-001 (500-line PR soft limit) is not a gap for this first PR.

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
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
bash scripts/ci-local.sh
```

## Known pitfalls

- Private companion repos: local submodule clone needs a PAT with Contents: Read
  on `pirlruc/guardrails` and `pirlruc/github-scaffold`. GitHub Actions does not
  clone them; it uses `scripts/` helpers and `config/kotlin.thresholds.yml`.
- `:app` / `:data` are skipped when `ANDROID_HOME` is unset so JDK-only CI
  can still gate `:domain`. With the SDK, coverage is required for all three.
- `MatAventurasApp.shouldOpenContainer` / `resolveProcessName` (API 26–27 uses `/proc/self/cmdline`). Blank process names fail closed (no Room).
- Reward points use `ProfileDao.addPoints`; lesson persist must not stamp an absolute Compose total.
- Do not add `docs/adr/`. Epic MAT-001 / MAT-003 are the decision records.
- Scaffold branch convention is `feature-*`; this cloud run used
  `cursor/mat-aventuras-core-ade3` per the agent environment.
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

## Suggested next work

1. Human: bootstrap labels/milestones and sync `docs/issues.yml`.
2. MAT-002-T1: emulator instrumented tests in CI, including Godot plugin Activities.
3. MAT-004-T4: CodeQL + OSV/SBOM if GitHub Advanced Security and a release SBOM are wanted.

This pass: sopa miss keeps found words; Godot boost is tap-only; runner
stays on the course; HUD contrast; gate posts; consonant soup filler.

*Last updated: 2026-08-21*
